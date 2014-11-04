package ru.spb.iac.cud.sts.core.handlers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URI;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.XMLValidateContext;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Dispatch;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;




 
 
import org.apache.xml.security.transforms.Transforms;
 
 
import org.jboss.xb.binding.SimpleTypeBindings;
import org.picketlink.common.util.Base64;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.bindings.jboss.subject.PicketLinkPrincipal;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.BaseIDAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.ws.trust.UseKeyType;
import org.picketlink.trust.jbossws.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.spb.iac.cud.context.ContextAccessSTSManager;
import ru.spb.iac.cud.context.ContextIDPAccessManager;
import ru.spb.iac.cud.context.ContextIDPUtilManager;
import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.idp.web.sig.GOSTSAML2Signature;
import ru.spb.iac.cud.idp.web.util.GOSTSignatureUtil;
import ru.spb.iac.cud.items.AuthMode;
import ru.spb.iac.cud.items.Token;
import ru.spb.iac.cud.services.web.init.Configuration;
import ru.spb.iac.pl.sp.key.KeyStoreKeyManager;

public class ServerSOAPHandler implements SOAPHandler<SOAPMessageContext> {

	private static final String auth_type_password = "urn:oasis:names:tc:SAML:2.0:ac:classes:password";
	private static final String auth_type_x509 = "urn:oasis:names:tc:SAML:2.0:ac:classes:X509";
	
	private static PublicKey publicKey = null;
	
	final static Logger LOGGER = LoggerFactory
			.getLogger(ServerSOAPHandler.class);

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

		// ���� soap � �������� usernametoken � header
		// � �������������� transform

		PublicKey publicKey2 = null;
		String loginUser = null;

		try {
			HttpServletRequest req = (HttpServletRequest) mc
					.get(MessageContext.SERVLET_REQUEST);
			HttpSession http_session = req.getSession();

			String system_principal = null;

			String user_principal = null;

			String user_obo_principal = null;

			String user_obo_auth_type = null;
			
			String user_password = null;

			X509Certificate userCertX = null;

			SOAPMessage soapMessage = mc.getMessage();
			SOAPHeader soapHeader = soapMessage.getSOAPHeader();

			SOAPBody soapBody = soapMessage.getSOAPBody();

			SOAPPart soapPart = soapMessage.getSOAPPart();

			Document soapDoc = soapMessage.getSOAPPart().getEnvelope()
					.getOwnerDocument();

			// ������
			if (Boolean.FALSE.equals(mc
					.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {

				 

				Provider xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();

				XMLSignatureFactory fac = XMLSignatureFactory.getInstance(
						"DOM", xmlDSigProvider);

				// signature
				NodeList signatureList = soapHeader.getElementsByTagNameNS("*",
						"Signature");

				if (signatureList == null || signatureList.getLength() == 0) {
					LOGGER.debug("handleMessage:02_1");
					
					
					if(Configuration.isSignRequired()){
					   throw new GeneralFailure(
							"This service requires <dsig:Signature>, which is missing!!!");
					}
				}

				// ����������� ������������ ������ apache cxf
				// ReadHeadersInterceptor � ������������� id
				NodeList securityList = soapHeader.getElementsByTagNameNS("*",
						"Security");

				if (securityList == null || securityList.getLength() == 0) {
					LOGGER.debug("handleMessage:02_2");
					throw new GeneralFailure(
							"This service requires <Security>, which is missing!!!");
				}

				NamedNodeMap attrs = securityList.item(0).getAttributes();
				for (int i = 0; i < attrs.getLength(); i++) {
					Attr attribute = (Attr) attrs.item(i);
					LOGGER.debug("attrib:" + attribute.getName() + " = "
							+ attribute.getValue());
				}

				LOGGER.debug("handleMessage:02_3");

				// ������ ����� �� ��������� ����������

				// ��������� Body-RequestSecurityToken �� UseKey
				NodeList onUseKeyList = soapBody.getElementsByTagNameNS("*",
						"UseKey");

				// ��������� Body-RequestSecurityToken �� OnBehalfOf
				NodeList onBehalfOfList = soapBody.getElementsByTagNameNS("*",
						"OnBehalfOf");

				if (onBehalfOfList != null && onBehalfOfList.getLength() > 0) {
					LOGGER.debug("handleMessage:02_2_1");

					if (onUseKeyList != null && onUseKeyList.getLength() > 0) {
						// ������ ������������ UseKey � OnBehalfOf
						throw new GeneralFailure(
								"This <UseKey> and <OnBehalfOf> �re not compatible!!!");
					}

					NodeList usernameTokenOnBehalfOfList = ((Element) onBehalfOfList
							.item(0)).getElementsByTagNameNS("*",
							"UsernameToken");

					if (usernameTokenOnBehalfOfList == null
							|| usernameTokenOnBehalfOfList.getLength() == 0) {
						LOGGER.debug("handleMessage:02_2_2");
						throw new GeneralFailure(
								"This <OnBehalfOf> requires <UsernameToken>, which is missing!!!");
					}

					NodeList usernameOnBehalfOfList = ((Element) usernameTokenOnBehalfOfList
							.item(0)).getElementsByTagNameNS("*", "Username");

					if (usernameOnBehalfOfList == null
							|| usernameOnBehalfOfList.getLength() == 0) {
						LOGGER.debug("handleMessage:02_2_3");
						throw new GeneralFailure(
								"This <UsernameToken> of OnBehalfOf requires <Username>, which is missing!!!");
					}

					String base64TokenId = usernameOnBehalfOfList.item(0)
							.getTextContent();

					// !!!
					// ���������� �� �������� �� ������� ������, � ������
					// token_id

					if (base64TokenId != null) {

						LOGGER.debug("handleMessage:02:" + base64TokenId);

						byte[] byteTokenID = Base64.decode(base64TokenId);

						String tokenID = new String(byteTokenID, "UTF-8");

						LOGGER.debug("handleMessage:03:" + tokenID);

						String[] arrTokenID = tokenID.split("_");

						if (arrTokenID == null || arrTokenID.length != 4) {
							throw new Exception("UserAuthToken is not valid!!!");
						}

						StringBuilder sb = new StringBuilder();

						sb.append(arrTokenID[0] + "_" + arrTokenID[1] + "_" + arrTokenID[2]);

						byte[] sigValue = Base64.decode(arrTokenID[3]);

					if(this.publicKey ==null){
						
						KeyStore ks = KeyStore.getInstance("HDImageStore",
								"JCP");
						ks.load(null, null);

						this.publicKey = ks.getCertificate("cudvm_export")
								.getPublicKey();

					}
					
						boolean tokenIDValidateResult = GOSTSignatureUtil
								.validate(sb.toString().getBytes("UTF-8"),
										sigValue, this.publicKey);

						LOGGER.debug("handleMessage:" + tokenIDValidateResult);

						user_obo_principal = arrTokenID[0].toString();
						Date expired = new Date(Long.parseLong(arrTokenID[1]));

						LOGGER.debug("handleMessage:" + user_obo_principal);

						LOGGER.debug("handleMessage:" + expired);

						if (!tokenIDValidateResult) {
							throw new Exception(
									"TokenId of OnBehalfOf is not valid!!!");
						}

						//�� ����� ���� ������� ������
						//���� ����� ���������, �� ����� �� �� ��� ������ �������� 
						//������ �����?
						//���� ����������� ������� ������ �� ����� � ������
						// � ���� ��� �������� ����������, ��� ����� ��������� -
						//�� ��� � ������?
						//��� ��� ������ ���� ������� �������� �� ���� ��� 
						//��������� ������ �� 1 �����
						if (new Date(System.currentTimeMillis()).after(expired)) {
							//th/row new Token/Expired("TokenId of OnBehalfOf is expired!!!");
							//th/row new Exce/ption(
							//		"TokenId of OnBehalfOf is expired!!!");
						}

						 user_obo_auth_type = arrTokenID[2].toString();
						 
					} else {
						throw new Exception("TokenId of OnBehalfOf is empty!!!");
					}

					/*
					 * //!!! //���������� �� �������� �� ������� ������, �
					 * ������ token_id
					 */

					LOGGER.debug("handleMessage:02_2_4+:" + user_obo_principal);

				}

				NodeList usernameTokenList = soapHeader.getElementsByTagNameNS(
						"*", "UsernameToken");

				if (usernameTokenList == null
						|| usernameTokenList.getLength() == 0) {
					LOGGER.debug("handleMessage:02_3");
					throw new GeneralFailure(
							"This service requires UsernameToken, which is missing!!!");
				}

				if (user_obo_principal == null) {

					if (usernameTokenList.getLength() == 1) {
						// ����� ����
						// 1) �������������� �������
						// 2) �������������� ������������ �� �����������

						// �� ������� ������ ���� �����������
						Element el = (Element) usernameTokenList.item(0);
						String el_id = el.getAttribute("wsu:Id");

						LOGGER.debug("handleMessage:02_3_1:" + el_id);

						if ("SystemToken_1".equals(el_id)) {
							// ��������� - ������� ������ ��������������
							NodeList usernameList = ((Element) usernameTokenList
									.item(0)).getElementsByTagNameNS("*",
									"Username");

							if (usernameList == null
									|| usernameList.getLength() == 0) {
								LOGGER.debug("handleMessage:02_3_1+");
								throw new GeneralFailure(
										"This service requires <Username>, which is missing!!!");
							}

							system_principal = usernameList.item(0)
									.getTextContent();

							LOGGER.debug("handleMessage:02_3_1_2:"
									+ system_principal);

						} else {
							throw new Exception(
									"This service requires SystemToken_1, which is missing!!!");
						}

						// ������� - ���� �� ���������� ������������
						NodeList usernameCertList = soapHeader
								.getElementsByTagNameNS("*", "X509Certificate");

						if (usernameCertList != null
								&& usernameCertList.getLength() != 0) {
							// ����
							LOGGER.debug("handleMessage:02_3_2");

							if (onUseKeyList != null
									&& onUseKeyList.getLength() > 0) {
								// ������ ������������ UseKey � ����������
								// ������������
								throw new GeneralFailure(
										"This <UseKey> and users X509Certificate �re not compatible!!!");
							}

							String base64X509Certificate = usernameCertList
									.item(0).getTextContent();

							 
							byte[] byteX509Certificate = Base64
									.decode(base64X509Certificate);

							CertificateFactory cf = CertificateFactory
									.getInstance("X.509");
							ByteArrayInputStream bais = new ByteArrayInputStream(
									byteX509Certificate);

							while (bais.available() > 0)
								userCertX = (X509Certificate) cf
										.generateCertificate(bais);

							 

						}

					} else if (usernameTokenList.getLength() == 2) {
						// �������������� ������������ �� �����/������

						// �� ������� ������ ���� �����������
						Element el_1 = (Element) usernameTokenList.item(0);
						String el_id_1 = el_1.getAttribute("wsu:Id");

						LOGGER.debug("handleMessage:02_3_4:" + el_id_1);

						Element el_2 = (Element) usernameTokenList.item(1);
						String el_id_2 = el_2.getAttribute("wsu:Id");

						LOGGER.debug("handleMessage:02_3_5:" + el_id_2);

						if ("SystemToken_1".equals(el_id_1)
								&& "UsernameToken_1".equals(el_id_2)) {

							if (onUseKeyList != null
									&& onUseKeyList.getLength() > 0) {
								// ������ ������������ UseKey � �����/������
								// ������������
								throw new GeneralFailure(
										"This <UseKey> and UsernameToken_1 �re not compatible!!!");
							}

							// �������
							NodeList usernameList = ((Element) usernameTokenList
									.item(0)).getElementsByTagNameNS("*",
									"Username");

							if (usernameList == null
									|| usernameList.getLength() == 0) {
								LOGGER.debug("handleMessage:02_3_5+");
								throw new GeneralFailure(
										"This service requires <Username>, which is missing!!!");
							}

							system_principal = usernameList.item(0)
									.getTextContent();

							// ������������

							// 1)�����
							usernameList = ((Element) usernameTokenList.item(1))
									.getElementsByTagNameNS("*", "Username");

							if (usernameList == null
									|| usernameList.getLength() == 0) {
								LOGGER.debug("handleMessage:02_3_5+");
								throw new GeneralFailure(
										"This service requires <Username>, which is missing!!!");
							}

							user_principal = usernameList.item(0)
									.getTextContent();

							// 2)������
							NodeList passwordList = ((Element) usernameTokenList
									.item(1)).getElementsByTagNameNS("*",
									"Password");

							if (passwordList == null
									|| passwordList.getLength() == 0) {
								LOGGER.debug("handleMessage:02_3_5+");
								throw new GeneralFailure(
										"This service requires <Password>, which is missing!!!");
							}

							user_password = passwordList.item(0)
									.getTextContent();

						} else if ("SystemToken_1".equals(el_id_2)
								&& "UsernameToken_1".equals(el_id_1)) {

							if (onUseKeyList != null
									&& onUseKeyList.getLength() > 0) {
								// ������ ������������ UseKey � �����/������
								// ������������
								throw new GeneralFailure(
										"This <UseKey> and UsernameToken_1 �re not compatible!!!");
							}

							// �������
							NodeList usernameList = ((Element) usernameTokenList
									.item(1)).getElementsByTagNameNS("*",
									"Username");

							if (usernameList == null
									|| usernameList.getLength() == 0) {
								LOGGER.debug("handleMessage:02_3_5+");
								throw new GeneralFailure(
										"This service requires <Username>, which is missing!!!");
							}

							system_principal = usernameList.item(0)
									.getTextContent();

							// ������������

							// 1)�����
							usernameList = ((Element) usernameTokenList.item(0))
									.getElementsByTagNameNS("*", "Username");

							if (usernameList == null
									|| usernameList.getLength() == 0) {
								LOGGER.debug("handleMessage:02_3_5+");
								throw new GeneralFailure(
										"This service requires <Username>, which is missing!!!");
							}

							user_principal = usernameList.item(0)
									.getTextContent();

							// 2)������
							NodeList passwordList = ((Element) usernameTokenList
									.item(0)).getElementsByTagNameNS("*",
									"Password");

							if (passwordList == null
									|| passwordList.getLength() == 0) {
								LOGGER.debug("handleMessage:02_3_5+");
								throw new GeneralFailure(
										"This service requires <Password>, which is missing!!!");
							}

							user_password = passwordList.item(0)
									.getTextContent();

						} else {
							throw new Exception(
									"���������� ���������� SystemToken_1 � UsernameToken_1!");
						}
					}

				} else { // onBehalfOf

					LOGGER.debug("handleMessage:onBehalfOf:01");

					if (usernameTokenList.getLength() == 1) {
						// ������ �������������� - �������������� �������
						// �������������� ������������ �� ����������� -
						// ���� � ���� - ����������

						Element el = (Element) usernameTokenList.item(0);
						String el_id = el.getAttribute("wsu:Id");

						LOGGER.debug("handleMessage:onBehalfOf:02:" + el_id);

						if ("SystemToken_1".equals(el_id)) {
							// ��������� - ������� ������ ��������������
							NodeList usernameList = ((Element) usernameTokenList
									.item(0)).getElementsByTagNameNS("*",
									"Username");

							if (usernameList == null
									|| usernameList.getLength() == 0) {
								LOGGER.debug("handleMessage:onBehalfOf:03");
								throw new GeneralFailure(
										"This service requires <Username>, which is missing!!!");
							}

							system_principal = usernameList.item(0)
									.getTextContent();

							LOGGER.debug("handleMessage:onBehalfOf:04:"
									+ system_principal);

						} else {
							throw new Exception(
									"This service requires SystemToken_1, which is missing!!!");
						}

					} else if (usernameTokenList.getLength() == 2) {
						// �������� ������������ �������������� ������������ ��
						// �����/������
						// ����� �� ��, ������� ����� �� �������

						// �� ������� ������ ���� �����������
						Element el_1 = (Element) usernameTokenList.item(0);
						String el_id_1 = el_1.getAttribute("wsu:Id");

						LOGGER.debug("handleMessage:onBehalfOf:05:" + el_id_1);

						Element el_2 = (Element) usernameTokenList.item(1);
						String el_id_2 = el_2.getAttribute("wsu:Id");

						LOGGER.debug("handleMessage:02_3_5:" + el_id_2);

						if ("SystemToken_1".equals(el_id_1)) {

							// �������
							NodeList usernameList = ((Element) usernameTokenList
									.item(0)).getElementsByTagNameNS("*",
									"Username");

							if (usernameList == null
									|| usernameList.getLength() == 0) {
								LOGGER.debug("handleMessage:02_3_5+");
								throw new GeneralFailure(
										"This service requires <Username>, which is missing!!!");
							}

							system_principal = usernameList.item(0)
									.getTextContent();

						} else if ("SystemToken_1".equals(el_id_2)) {

							// �������
							NodeList usernameList = ((Element) usernameTokenList
									.item(1)).getElementsByTagNameNS("*",
									"Username");

							if (usernameList == null
									|| usernameList.getLength() == 0) {
								LOGGER.debug("handleMessage:02_3_5+");
								throw new GeneralFailure(
										"This service requires <Username>, which is missing!!!");
							}

							system_principal = usernameList.item(0)
									.getTextContent();

						} else {
							throw new Exception(
									"���������� ���������� SystemToken_1 !");
						}
					}

				}
				LOGGER.debug("handleMessage:02_5_1:" + system_principal);
				LOGGER.debug("handleMessage:02_5_2:" + user_principal);
				LOGGER.debug("handleMessage:02_5_3:" + user_password);
				
				LOGGER.debug("handleMessage:obo:01:" + user_obo_principal);

				if (user_obo_principal != null) {
					// �������������� ������������ �� onBehalfOf
					// idUser = (new
					// ContextAccessSTSManager()).authenticate_login_obo(user_obo_principal,
					// AuthMode.HTTP_REDIRECT, getIPAddress(req));

					// !!!
					// ���������� �� �������� ������ �������
					// ������� token_id
					// � � �� ������ uid ������������
					// ������� � ��� user_obo_principal - ��� id ������������
					// � ����� �������� authenticate_uid_obo
					// � ����� ���������� ����� �������, �� ��� �����
					// ������������
					// � �������� authenticate_login_obo
					loginUser = (new ContextAccessSTSManager())
							.authenticate_uid_obo(user_obo_principal,
									AuthMode.WEB_SERVICES, getIPAddress(req),
									system_principal);

					LOGGER.debug("handleMessage:obo:02:" + loginUser);

					http_session.setAttribute("user_principal", loginUser);

					http_session.setAttribute("cud_auth_type", user_obo_auth_type);
					 
				} else if (user_principal != null) {
					// �������������� ������������ �� �����/������

					loginUser = (new ContextAccessSTSManager())
							.authenticate_login(user_principal, user_password,
									AuthMode.WEB_SERVICES, getIPAddress(req),
									system_principal);

					// user_principal � ��� �����
					http_session.setAttribute("user_principal", user_principal);

					http_session.setAttribute("cud_auth_type", auth_type_password);
					 
				} else if (userCertX != null) {
					// �������������� ������������ �� �����������

					String certSN = dec_to_hex(userCertX.getSerialNumber());

					LOGGER.debug("handleMessage:02_5_5:" + certSN);

					loginUser = (new ContextAccessSTSManager())
							.authenticate_cert_sn(certSN, getIPAddress(req),
									system_principal);

					LOGGER.debug("handleMessage:02_5_6:" + loginUser);

					user_principal = loginUser;

					http_session.setAttribute("user_principal", user_principal);

					http_session.setAttribute("cud_auth_type", auth_type_x509);
					 
				} else {
					// ������ �������������� �������

					// ������ �����
					(new ContextAccessSTSManager()).sys_audit_public(83L,
							"inp_param", "true", getIPAddress(req), null,
							system_principal);

				}

				LOGGER.debug("handleMessage:02_5_7");

				http_session.setAttribute("system_principal", system_principal);

			 if(Configuration.isSignRequired()){
				
				X509Certificate cert_user = (new ContextIDPUtilManager())
						.system_cert(system_principal);

				 

				
				if (cert_user != null) {

					publicKey2 = cert_user.getPublicKey();
				}

				 

				if (publicKey2 == null) {

					throw new GeneralFailure("Public key is null!!!");
				}

				Node securityToken2 = soapDoc.getDocumentElement()
						.getElementsByTagNameNS("*", "UsernameToken").item(0);

				LOGGER.debug("handleMessage:09:" + securityToken2.getNodeName());

				// ������
				Document newDoc = DocumentUtil.createDocument();
				Node signingNode = newDoc.importNode(securityToken2, true);
				newDoc.appendChild(signingNode);

				 

				Node signatureNode1 = signatureList.item(0);

				LOGGER.debug("handleMessage:09_2:"
						+ signatureNode1.getNodeName());

				DOMValidateContext valContext1 = new DOMValidateContext(
						publicKey2, signatureNode1);

				valContext1.putNamespacePrefix(XMLSignature.XMLNS, "dsig");

				LOGGER.debug("handleMessage:09_3");

				valContext1
						.setIdAttributeNS(
								soapHeader,
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
								"Id");
				valContext1
						.setIdAttributeNS(
								soapBody,
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
								"Id");

				LOGGER.debug("handleMessage:09_4");

				javax.xml.crypto.dsig.XMLSignature signature1 = fac
						.unmarshalXMLSignature(valContext1);

				boolean result1 = signature1.validate(valContext1);

				LOGGER.debug("dispatch:011_5:" + result1);

				if (result1 == false) {
					throw new GeneralFailure("Signature is not valid!!!");
				}

				
			}
				
				// �����
			} else {

			

			
				soapPart.getEnvelope()
						.addNamespaceDeclaration(
								"wsse",
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
				soapPart.getEnvelope().addNamespaceDeclaration("ds",
						"http://www.w3.org/2000/09/xmldsig#");

				soapPart.getEnvelope()
						.addNamespaceDeclaration(
								"wsu",
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

				soapBody.addNamespaceDeclaration(
						"wsu",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
				soapBody.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Id", "Body");

				soapHeader
						.addNamespaceDeclaration(
								"wsu",
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
				soapHeader
						.setAttributeNS(
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
								"wsu:Id", "Header");

				LOGGER.debug("dispatch:03");

				// ��� ������������ Pre-digested input ���������� ������
				// <wsse:Username/>,
				// �� ���� ������������� ������ <wsse:Username/>
				// � ����� �� ����� ��������� �������� parentNode.replaceChild,
				// �� ������� ����������
				// <wsse:Username
				// xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"/><wsse:Password
				// xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"/>
				// ����� �� � ������������.
				// � �� ������� ������� ��� �������� ������� Pre-digested input
				// ������ ����������
				// <wsse:Username xmlns:wsse="http://..."/>, �� ���� �����������
				// ������ <wsse:Username xmlns:wsse="http://..."/>
				// � ����� ����� �������� ����� �� SOAPElement ������������
				// Usern/ame/SOAP.addNamespa/ceDeclaration.

				QName securityQN = new QName(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"Security", "wsse");
				QName timestampQN = new QName(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"Timestamp", "wsu");
				QName createdQN = new QName(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"Created", "wsu");

				SOAPElement securitySOAP = soapHeader
						.addChildElement(securityQN);

				securitySOAP
						.addNamespaceDeclaration(
								"wsu",
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
				securitySOAP
						.setAttributeNS(
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
								"wsu:Id", "_id_sec");

				SOAPElement timestampSOAP = securitySOAP
						.addChildElement(timestampQN);

				SOAPElement createdSOAP = timestampSOAP
						.addChildElement(createdQN);

				createdSOAP.addTextNode(SimpleTypeBindings
						.marshalDateTime(new GregorianCalendar()));

				 

				
				if(Configuration.isSignRequired()){
					
					
					KeyStoreKeyManager kskm = new KeyStoreKeyManager();
					// � KeyStoreKeyManager KeyStore ks - static
					// ������� ks ��� ��������������� ������� �����������
					// � ����� �����, ��� st/atic:
					// pri/vate stat/ic char/[] signing/KeyPass;
					// priv/ate st/atic Stri/ng signin/gAlias;

					KeyPair keyPair = kskm.getSigningKeyPair();

					PublicKey publicKey = keyPair.getPublic();

					PrivateKey privateKey = keyPair.getPrivate();	
					
				
				org.apache.xml.security.Init.init();

				Provider xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();

				XMLSignatureFactory fac = XMLSignatureFactory.getInstance(
						"DOM", xmlDSigProvider);

				// �������������� ��� ������ SignedInfo
				List<Transform> transformList = new ArrayList<Transform>();
				Transform transform = fac.newTransform(Transform.ENVELOPED,
						(XMLStructure) null);
				Transform transformC14N = fac.newTransform(
						Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS,
						(XMLStructure) null);
				transformList.add(transform);
				transformList.add(transformC14N);

				LOGGER.debug("dispatch:07");

				// ��� �����������

				Reference ref1 = fac
						.newReference(
								"#Header",
								fac.newDigestMethod(
										"http://www.w3.org/2001/04/xmldsig-more#gostr3411",
										null), transformList, null, null);
				Reference ref2 = fac.newReference("#Body", fac.newDigestMethod(
						"http://www.w3.org/2001/04/xmldsig-more#gostr3411",
						null), transformList, null, null);
				List<Reference> referenceList = new ArrayList<Reference>();

				referenceList.add(ref1);
				referenceList.add(ref2);

				LOGGER.debug("dispatch:06");

				SignedInfo si = fac
						.newSignedInfo(
								fac.newCanonicalizationMethod(
										CanonicalizationMethod.EXCLUSIVE,
										(C14NMethodParameterSpec) null),
								fac.newSignatureMethod(
										"http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411",
										null), referenceList);

				KeyInfoFactory kif = fac.getKeyInfoFactory();

				KeyValue kv = kif.newKeyValue(publicKey);
				KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));

				javax.xml.crypto.dsig.XMLSignature sig = fac.newXMLSignature(
						si, ki);

				// ���� ��������� �������
				DOMSignContext signContext = new DOMSignContext(privateKey,
						securitySOAP);

				signContext.putNamespacePrefix(XMLSignature.XMLNS, "dsig");

				// �������� ��������� id � ������������� ��������
				// ����� ������������� �� ���� ��������� Pre-digested input �
				// ����

				signContext
						.setIdAttributeNS(
								soapBody,
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
								"Id");
				signContext
						.setIdAttributeNS(
								soapHeader,
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
								"Id");

				LOGGER.debug("dispatch:011");

				sig.sign(signContext);

				}
			}
			LOGGER.debug("handleMessage:0100");

		} catch (Exception e) {
			LOGGER.error("handleMessage:error:", e);
			throw new ProtocolException(e);

		}
		return true;
	}

	private String getIPAddress(HttpServletRequest request) {

		String ipAddress = request.getRemoteAddr();

		return ipAddress;
	}

	private static String dec_to_hex(BigInteger bi) {

		String result = null;

		try {
			result = bi.toString(16);
		} catch (NumberFormatException e) {
			LOGGER.error("Error! tried to parse an invalid number format");
		}
		return result;
	}

	

	

}
