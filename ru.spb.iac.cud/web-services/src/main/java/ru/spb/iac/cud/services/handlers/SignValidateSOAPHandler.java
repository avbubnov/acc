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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.spb.iac.cud.exceptions.GeneralFailure;

public class SignValidateSOAPHandler implements SOAPHandler<SOAPMessageContext> {

	Logger logger = LoggerFactory.getLogger(SignValidateSOAPHandler.class);

	public Set<QName> getHeaders() {
		return null;
	}

	public void close(MessageContext mc) {
	}

	public boolean handleFault(SOAPMessageContext mc) {
		return true;
	}

	public boolean handleMessage(SOAPMessageContext mc) {

		logger.info("handleMessage:01+:"
				+ mc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));

		try {

			HttpServletRequest req = (HttpServletRequest) mc
					.get(MessageContext.SERVLET_REQUEST);
			HttpSession http_session = req.getSession();

			String cud_sts_principal = null;

			PublicKey publicKey2 = null;

			X509Certificate cert_user = null;

			SOAPMessage sm = mc.getMessage();
			// sm.writeTo(System.out);

			SOAPHeader soapHeader = sm.getSOAPHeader();
			SOAPBody soapBody = sm.getSOAPBody();

			// ������
			if (Boolean.FALSE.equals(mc
					.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {

				// logger.info("handleMessage:02");

				// ������ ����� �� ��������� ����������

				NodeList assertionList = soapHeader.getElementsByTagNameNS("*",
						"Assertion");

				if (assertionList == null || assertionList.getLength() == 0) {
					// logger.info("handleMessage:03_1");
					throw new GeneralFailure(
							"This service requires <saml:Assertion>, which is missing!!!");
				}

				NodeList subjectList = ((Element) assertionList.item(0))
						.getElementsByTagNameNS("*", "Subject");

				if (subjectList == null || subjectList.getLength() == 0) {
					// logger.info("handleMessage:03_2");
					throw new GeneralFailure(
							"This service requires <Subject>, which is missing!!!");
				}

				NodeList nameIDList = ((Element) subjectList.item(0))
						.getElementsByTagNameNS("*", "NameID");

				if (nameIDList == null || nameIDList.getLength() == 0) {
					// logger.info("handleMessage:03_3");
					throw new GeneralFailure(
							"This service requires <NameID>, which is missing!!!");
				}

				// SOAPElementImpl sei_codeSystem =
				// (SOAPElementImpl)nameIDList.item(0);
				// String codeSystem = sei_codeSystem.getValue();

				String codeSystem = nameIDList.item(0).getTextContent();

				// logger.info("handleMessage:04:"+codeSystem);

				cud_sts_principal = codeSystem;
				http_session.setAttribute("cud_sts_principal",
						cud_sts_principal);

				/*
				 * //1-� �������: ����� ��� ������� �� Assertion/subject/NameID
				 * //�� ����� ���� ����� � ���� ����������
				 * 
				 * cert_user = (new
				 * ContextIDPUtilManager()).system_cert(codeSystem);
				 */

				// 2-� �������: ����� ����� ���������� ��
				// Assertion/subject/SubjectConfirmationData/X509Certificate

				NodeList x509CertificateList = ((Element) subjectList.item(0))
						.getElementsByTagNameNS("*", "X509Certificate");

				if (x509CertificateList == null
						|| x509CertificateList.getLength() == 0) {
					// logger.info("handleMessage:05");
					throw new GeneralFailure(
							"This service requires SubjectConfirmationData/X509Certificate, which is missing!!!");
				}

				String x509CertSystem = x509CertificateList.item(0)
						.getTextContent();

				// logger.info("handleMessage:06:"+x509CertSystem);

				byte[] byteX509Certificate = Base64.decode(x509CertSystem);

				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				ByteArrayInputStream bais = new ByteArrayInputStream(
						byteX509Certificate);

				while (bais.available() > 0)
					cert_user = (X509Certificate) cf.generateCertificate(bais);

				if (cert_user != null) {

					publicKey2 = cert_user.getPublicKey();

					/*
					 * cert_verify_sign =
					 * HandlerUtils.getStore().getCertificate(domainAlias);
					 * 
					 * logger.info("handleMessage:06:"+cert_verify_sign);
					 * 
					 * if(cert_verify_sign!=null){
					 * publicKey2=cert_verify_sign.getPublicKey(); }
					 */
				} else {
					throw new GeneralFailure(
							"Cannot to define a client public key!!!");
				}

				// logger.info("handleMessage:07+:"+publicKey2);

				Provider xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();

				XMLSignatureFactory fac = XMLSignatureFactory.getInstance(
						"DOM", xmlDSigProvider);

				Node securityNode = soapHeader.getFirstChild();

				if (securityNode == null) {
					// logger.info("handleMessage:07_1");
					throw new GeneralFailure(
							"This service requires <wsse:Security>, which is missing!!!");
				}

				NodeList securityNodeChilds = securityNode.getChildNodes();

				Node signatureNode1 = null;

				for (int i = 0; i < securityNodeChilds.getLength(); i++) {

					// logger.info("handleMessage:07_2:"+securityNodeChilds.item(i).getLocalName());

					if (securityNodeChilds.item(i).getLocalName() != null
							&& securityNodeChilds.item(i).getLocalName()
									.equals("Signature")) {
						signatureNode1 = securityNodeChilds.item(i);
					}
				}

				// logger.info("handleMessage:07_3");

				// signature

				if (signatureNode1 == null) {
					// logger.info("handleMessage:08");
					throw new GeneralFailure(
							"This service requires <dsig:Signature>, which is missing!!!");
				}

				// logger.info("handleMessage:09:"+signatureNode1.getNodeName());

				DOMValidateContext valContext1 = new DOMValidateContext(
						publicKey2, signatureNode1);

				valContext1.putNamespacePrefix(XMLSignature.XMLNS, "dsig");

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

				javax.xml.crypto.dsig.XMLSignature signature1 = fac
						.unmarshalXMLSignature(valContext1);

				boolean result1 = signature1.validate(valContext1);

				logger.info("010:" + result1);

				if (result1 == false) {
					throw new GeneralFailure("Signature is not valid!!!");
				}

			} else {
				// �����

				char[] signingKeyPass = "Access_Control".toCharArray();
				String signingAlias = "cudvm_export";

				KeyStore ks = KeyStore.getInstance("HDImageStore", "JCP");
				ks.load(null, null);

				PrivateKey privateKey = (PrivateKey) ks.getKey(signingAlias,
						signingKeyPass);

				Certificate cert = ks.getCertificate(signingAlias);
				PublicKey publicKey = cert.getPublicKey();

				org.apache.xml.security.Init.init();

				Provider xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();

				XMLSignatureFactory fac = XMLSignatureFactory.getInstance(
						"DOM", xmlDSigProvider);

				Node SecuritySOAP = soapHeader.getFirstChild();

				// System.out.println("TestClientCryptoSOAPHandler:handleMessage:02:"+SecuritySOAP.getLocalName());

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

				List<Transform> transformList = new ArrayList<Transform>();
				Transform transform = fac.newTransform(Transform.ENVELOPED,
						(XMLStructure) null);
				Transform transformC14N = fac.newTransform(
						Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS,
						(XMLStructure) null);
				transformList.add(transform);
				transformList.add(transformC14N);

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

				// X509Data x509d =
				// kif.newX509Data(Collections.singletonList((X509Certificate)
				// cert));
				// KeyInfo ki =
				// kif.newKeyInfo(Collections.singletonList(x509d));

				javax.xml.crypto.dsig.XMLSignature sig = fac.newXMLSignature(
						si, ki);

				// ���� ��������� �������
				// DOMSignContext signContext = new DOMSignContext(privateKey,
				// newDoc.getDocumentElement());
				DOMSignContext signContext = new DOMSignContext(privateKey,
						SecuritySOAP);

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

				// System.out.println("dispatch:011");

				sig.sign(signContext);

				// !!!
				// ��������
				// �������� ��� ��������

				/*
				 * Node securityNode = soapHeader.getFirstChild();
				 * 
				 * if(securityNode==null){ logger.info("handleMessage:07_1");
				 * throw new Exception(
				 * "This service requires <wsse:Security>, which is missing!!!"
				 * ); }
				 * 
				 * NodeList securityNodeChilds = securityNode.getChildNodes();
				 * 
				 * Node signatureNode1 = null;
				 * 
				 * for (int i = 0; i<securityNodeChilds.getLength(); i++) {
				 * 
				 * logger.info("handleMessage:07_2:"+securityNodeChilds.item(i).
				 * getLocalName());
				 * 
				 * if(securityNodeChilds.item(i).getLocalName()!=null&&
				 * securityNodeChilds
				 * .item(i).getLocalName().equals("Signature")){ signatureNode1
				 * = securityNodeChilds.item(i); } }
				 * 
				 * logger.info("handleMessage:07_3");
				 * 
				 * //signature
				 * 
				 * if(signatureNode1==null){ logger.info("handleMessage:08");
				 * throw new Exception(
				 * "This service requires <dsig:Signature>, which is missing!!!"
				 * ); }
				 * 
				 * 
				 * 
				 * logger.info("handleMessage:09:"+signatureNode1.getNodeName());
				 * 
				 * 
				 * DOMValidateContext valContext1 = new
				 * DOMValidateContext(publicKey, signatureNode1);
				 * 
				 * valContext1.putNamespacePrefix(XMLSignature.XMLNS, "dsig");
				 * 
				 * valContext1.setIdAttributeNS(soapHeader,
				 * "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
				 * , "Id"); valContext1.setIdAttributeNS(soapBody,
				 * "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
				 * , "Id");
				 * 
				 * 
				 * javax.xml.crypto.dsig.XMLSignature signature1 =
				 * fac.unmarshalXMLSignature(valContext1);
				 * 
				 * boolean result1 = signature1.validate(valContext1);
				 * 
				 * logger.info("010+:"+result1);
				 */
			}

		} catch (Exception e) {
			logger.error("handleMessage:error:" + e);
			throw new ProtocolException(e);
			// throw new GeneralFailure("");
		}
		return true;
	}

}
