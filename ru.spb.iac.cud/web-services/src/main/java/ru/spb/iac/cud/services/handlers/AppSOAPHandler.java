package ru.spb.iac.cud.services.handlers;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.xml.security.transforms.Transforms;
import org.picketlink.common.util.Base64;
 
import org.picketlink.identity.federation.core.saml.v2.factories.SAMLAssertionFactory;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.items.AuthMode;
import ru.spb.iac.cud.sign.GOSTSignatureUtil;

public class AppSOAPHandler implements SOAPHandler<SOAPMessageContext> {

	final static Logger LOGGER = LoggerFactory.getLogger(AppSOAPHandler.class);

	private static PublicKey publicKey = null;
	
	public Set<QName> getHeaders() {
		return null;
	}

	public void close(MessageContext mc) {
	}

	public boolean handleFault(SOAPMessageContext mc) {
		return true;
	}

	public boolean handleMessage(SOAPMessageContext mc) {

		LOGGER.debug("handleMessage:01:"
				+ mc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));

		String user_id = null;
		String base64TokenId;

		try {

			HttpServletRequest req = (HttpServletRequest) mc
					.get(MessageContext.SERVLET_REQUEST);
			HttpSession http_session = req.getSession();

			SOAPMessage sm = mc.getMessage();
			

			SOAPHeader soapHeader = sm.getSOAPHeader();
			SOAPBody soapBody = sm.getSOAPBody();

			// запрос
			if (Boolean.FALSE.equals(mc
					.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {

				LOGGER.debug("handleMessage:02");

				NodeList usernameTokenList = soapHeader.getElementsByTagNameNS(
						"*", "UsernameToken");

				if (usernameTokenList == null
						|| usernameTokenList.getLength() == 0) {

					// !!!ещё подумать -
					// некототрые операции могут вызываться анонимно
					// теперь для них используется AnonymSOAPHandler
					// здесь анонимно может вызваться один метод
					// user_registration

					
					NodeList userRegistrationMethodList = soapBody
							.getElementsByTagNameNS("*", "user_registration");

					if (userRegistrationMethodList == null
							|| userRegistrationMethodList.getLength() == 0) {
						// все методы кроме user_registration
						throw new GeneralFailure(
								"This service requires UserAuthTokenId, which is missing!!!");
					}

				} else {

					if (usernameTokenList.getLength() > 0/* ==1 */) {
						// логин заявителя
						// берём первый <UsernameToken>
						// ид ApplicantToken_1 должен быть обязательно
						Element el = (Element) usernameTokenList.item(0);
						String el_id = el.getAttribute("wsu:Id");

						 

						if ("UserAuthTokenId".equals(el_id)) {
							// правильно -
							NodeList usernameList = ((Element) usernameTokenList
									.item(0)).getElementsByTagNameNS("*",
									"Username");

							if (usernameList == null
									|| usernameList.getLength() == 0) {
								throw new GeneralFailure(
										"This service requires <Username>, which is missing!!!");
							}

							base64TokenId = usernameList.item(0)
									.getTextContent();

							if (base64TokenId != null) {

								LOGGER.debug("handleMessage:02:" + base64TokenId);

								byte[] byteTokenID = Base64
										.decode(base64TokenId);

								String tokenID = new String(byteTokenID,
										"UTF-8");

								LOGGER.debug("handleMessage:03:" + tokenID);

								String[] arrTokenID = tokenID.split("_");

								if (arrTokenID == null
										|| arrTokenID.length != 4) {
									throw new Exception(
											"UserAuthToken is not valid!!!");
								}

								StringBuilder sb = new StringBuilder();

								sb.append(arrTokenID[0] + "_" + arrTokenID[1] +"_" + arrTokenID[2]);

								byte[] sigValue = Base64.decode(arrTokenID[3]);

							if(publicKey==null){	
								KeyStore ks = KeyStore.getInstance(
										"HDImageStore", "JCP");
								ks.load(null, null);

								publicKey = ks.getCertificate(
										"cudvm_export").getPublicKey();
							}
								boolean tokenIDValidateResult = GOSTSignatureUtil
										.validate(
												sb.toString().getBytes("UTF-8"),
												sigValue, publicKey);

								LOGGER.debug("handleMessage:04:"
										+ tokenIDValidateResult);

								user_id = arrTokenID[0].toString();
								Date expired = new Date(
										Long.parseLong(arrTokenID[1]));

								LOGGER.debug("handleMessage:05:" + user_id);

								LOGGER.debug("handleMessage:06:" + expired);

								if (!tokenIDValidateResult) {
									throw new Exception(
											"UserAuthToken is not valid!!!");
								}

								if (new Date(System.currentTimeMillis())
										.after(expired)) {
									
									throw new TokenExpired(
											"UserAuthToken is expired!!!");
									
									
								}

							} else {
								throw new Exception("UserAuthToken is empty!!!");
							}

							 

							// решили определять пользователей извне ЦУД по их
							// ИД, а не логинам
							// поэтому в usernameList.item(0).getTextContent()
							// передаётся ИД пользователя
							// то есть user_login - это сейчас ИД пользователя
							// и поэтому вызов authenticate_login_obo уже не
							// нужен
							// idU/ser = (new
							// ContextA/ccessSTSManager()).auth/enticate_login_obo(user_login,
							// Auth/Mode.HTTP_REDIR/ECT, getIPAdd/ress(req));

							// это заявитель
							http_session.setAttribute("user_id_principal",
									user_id/* idUser */);

						}/*
						 * els/e{ thr/ow new Ex/ception(
						 * "This service requires UserAut/hTokenId, which is missing!!!"
						 * ); }
						 */
						// !!!ещё подумать -
						// некототрые операции могут вызываться анонимно
					}
				}
			}

		} catch (Exception e) {
			 
			throw new ProtocolException(e);
		}
		return true;
	}

	

}
