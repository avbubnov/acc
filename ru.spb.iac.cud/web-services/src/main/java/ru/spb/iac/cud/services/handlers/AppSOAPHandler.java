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
//import org.jboss.ws.core.soap.SOAPElementImpl;
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
import ru.spb.iac.cud.items.AuthMode;
import ru.spb.iac.cud.sign.GOSTSignatureUtil;

public class AppSOAPHandler implements SOAPHandler<SOAPMessageContext> {

	Logger logger = LoggerFactory.getLogger(AppSOAPHandler.class);

	public Set<QName> getHeaders() {
		return null;
	}

	public void close(MessageContext mc) {
	}

	public boolean handleFault(SOAPMessageContext mc) {
		return true;
	}

	public boolean handleMessage(SOAPMessageContext mc) {

		logger.info("handleMessage:01:"
				+ mc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));

		String user_id = null;
		String base64TokenId;

		try {

			HttpServletRequest req = (HttpServletRequest) mc
					.get(MessageContext.SERVLET_REQUEST);
			HttpSession http_session = req.getSession();

			SOAPMessage sm = mc.getMessage();
			// sm.writeTo(System.out);

			SOAPHeader soapHeader = sm.getSOAPHeader();
			SOAPBody soapBody = sm.getSOAPBody();

			// ������
			if (Boolean.FALSE.equals(mc
					.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {

				logger.info("handleMessage:02");

				NodeList usernameTokenList = soapHeader.getElementsByTagNameNS(
						"*", "UsernameToken");

				if (usernameTokenList == null
						|| usernameTokenList.getLength() == 0) {

					// !!!��� �������� -
					// ���������� �������� ����� ���������� ��������
					// ������ ��� ��� ������������ AnonymSOAPHandler
					// ����� �������� ����� ��������� ���� �����
					// user_registration

					// NodeList userRegistrationMethodList =
					// soapBody.getElementsByTagNameNS("http://application.services.cud.iac.spb.ru",
					// "user_registration");

					NodeList userRegistrationMethodList = soapBody
							.getElementsByTagNameNS("*", "user_registration");

					if (userRegistrationMethodList == null
							|| userRegistrationMethodList.getLength() == 0) {
						// ��� ������ ����� user_registration
						throw new GeneralFailure(
								"This service requires UserAuthTokenId, which is missing!!!");
					}

				} else {

					if (usernameTokenList.getLength() > 0/* ==1 */) {
						// ����� ���������
						// ���� ������ <UsernameToken>
						// �� ApplicantToken_1 ������ ���� �����������
						Element el = (Element) usernameTokenList.item(0);
						String el_id = el.getAttribute("wsu:Id");

						// logger.info("handleMessage:04:"+el_id);

						if ("UserAuthTokenId".equals(el_id)) {
							// ��������� -
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

								logger.info("handleMessage:02:" + base64TokenId);

								byte[] byteTokenID = Base64
										.decode(base64TokenId);

								String tokenID = new String(byteTokenID,
										"UTF-8");

								logger.info("handleMessage:03:" + tokenID);

								String[] arrTokenID = tokenID.split("_");

								if (arrTokenID == null
										|| arrTokenID.length != 3) {
									throw new Exception(
											"UserAuthToken is not valid!!!");
								}

								StringBuilder sb = new StringBuilder();

								sb.append(arrTokenID[0] + "_" + arrTokenID[1]);

								byte[] sigValue = Base64.decode(arrTokenID[2]);

								KeyStore ks = KeyStore.getInstance(
										"HDImageStore", "JCP");
								ks.load(null, null);

								PublicKey publicKey = ks.getCertificate(
										"cudvm_export").getPublicKey();

								boolean tokenIDValidateResult = GOSTSignatureUtil
										.validate(
												sb.toString().getBytes("UTF-8"),
												sigValue, publicKey);

								logger.info("handleMessage:04:"
										+ tokenIDValidateResult);

								user_id = arrTokenID[0].toString();
								Date expired = new Date(
										Long.parseLong(arrTokenID[1]));

								logger.info("handleMessage:05:" + user_id);

								logger.info("handleMessage:06:" + expired);

								if (!tokenIDValidateResult) {
									throw new Exception(
											"UserAuthToken is not valid!!!");
								}

								if (new Date(System.currentTimeMillis())
										.after(expired)) {
									throw new Exception(
											"UserAuthToken is expired!!!");
								}

							} else {
								throw new Exception("UserAuthToken is empty!!!");
							}

							// logger.info("handleMessage:06:"+user_login);

							// ������ ���������� ������������� ����� ��� �� ��
							// ��, � �� �������
							// ������� � usernameList.item(0).getTextContent()
							// ��������� �� ������������
							// �� ���� user_login - ��� ������ �� ������������
							// � ������� ����� authenticate_login_obo ��� ��
							// �����
							// idUser = (new
							// ContextAccessSTSManager()).authenticate_login_obo(user_login,
							// AuthMode.HTTP_REDIRECT, getIPAddress(req));

							// ��� ���������
							http_session.setAttribute("user_id_principal",
									user_id/* idUser */);

						}/*
						 * else{ throw new Exception(
						 * "This service requires UserAuthTokenId, which is missing!!!"
						 * ); }
						 */
						// !!!��� �������� -
						// ���������� �������� ����� ���������� ��������
					}
				}
			}

		} catch (Exception e) {
			// logger.error("handleMessage:error:"+e);
			throw new ProtocolException(e);
		}
		return true;
	}

	private String getIPAddress(HttpServletRequest request) {

		String ipAddress = request.getRemoteAddr();

		return ipAddress;
	}

}
