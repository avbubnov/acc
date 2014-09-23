package ru.spb.iac.cud.idp.web.handlers;

import org.picketlink.identity.federation.web.handlers.saml2.AbstractSignatureHandler;
import org.picketlink.common.constants.GeneralConstants;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.spb.iac.cud.idp.web.sig.GOSTSAML2Signature;
import ru.spb.iac.cud.idp.web.util.GOSTRedirectBindingSignatureUtil;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;

import static org.picketlink.common.util.StringUtil.isNotNull;

public class GOSTSAML2SignatureAssertionGenerationHandler extends
		AbstractSignatureHandler {

	final static Logger loggerslf4j = LoggerFactory
			.getLogger(GOSTSAML2SignatureAssertionGenerationHandler.class);

	public void handleRequestType(SAML2HandlerRequest request,
			SAML2HandlerResponse response) throws ProcessingException {

		Document responseDocument = response.getResultingDocument();

		loggerslf4j.info("handleRequestType:01");

		if (responseDocument == null) {
			logger.trace("No response document found");
			return;
		}

		this.signAssertion(responseDocument, request, response);
	}

	private void signAssertion(Document samlDocument,
			SAML2HandlerRequest request, SAML2HandlerResponse response)
			throws ProcessingException {
		if (!isSupportsSignature(request)) {
			return;
		}

		// Get the Key Pair
		KeyPair keypair = (KeyPair) this.handlerChainConfig
				.getParameter(GeneralConstants.KEYPAIR);
		X509Certificate x509Certificate = (X509Certificate) this.handlerChainConfig
				.getParameter(GeneralConstants.X509CERTIFICATE);

		if (keypair == null) {
			logger.samlHandlerKeyPairNotFound();
			throw logger.samlHandlerKeyPairNotFoundError();
		}

		// сами решили подписывать Assertion
		try {

			// loggerslf4j.info("signAssertion:01:"+DocumentUtil.asString(samlDocument));

			NodeList assertionList = samlDocument.getElementsByTagNameNS(
					"urn:oasis:names:tc:SAML:2.0:assertion", "Assertion");

			// loggerslf4j.info(sign:02:"+(assertionList==null));

			// loggerslf4j.info("sign:03:"+assertionList.getLength());

			if (assertionList != null && assertionList.getLength() > 0) {

				Node oldAssertion = assertionList.item(0);

				Node parentOldAssertion = oldAssertion.getParentNode();

				Document assertionDocument = DocumentUtil.createDocument();
				Node n = assertionDocument.importNode(oldAssertion, true);
				assertionDocument.appendChild(n);

				// loggerslf4j.info("sign:04:"+DocumentUtil.asString(assertionDocument));

				SAML2Signature samlSignature = new GOSTSAML2Signature();
				samlSignature
						.setSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411");
				samlSignature
						.setDigestMethod("http://www.w3.org/2001/04/xmldsig-more#gostr3411");

				Node nextSibling = samlSignature
						.getNextSiblingOfIssuer(assertionDocument);
				samlSignature.setNextSibling(nextSibling);
				// if(x509Certificate != null){
				// samlSignature.setX509Certificate(x509Certificate);
				// }
				samlSignature.signSAMLDocument(assertionDocument, keypair);

				// loggerslf4j.info("sign:05:"+DocumentUtil.asString(assertionDocument));

				Node newAssertion = samlDocument.importNode(
						assertionDocument.getFirstChild(), true);

				parentOldAssertion.replaceChild(newAssertion, oldAssertion);

				// loggerslf4j.info("signAssertion:06:"+DocumentUtil.asString(samlDocument));

			}

		} catch (Exception e) {
			loggerslf4j.error("signAssertion:Error:" + e);
		}

	}

}
