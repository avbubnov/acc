package ru.spb.iac.cud.idp.web.handlers;

import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.config.federation.IDPType;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.util.XMLEncryptionUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustUtil;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureGenerationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ru.spb.iac.cud.idp.web.util.GOSTXMLEncryptionUtil;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;

import java.security.PublicKey;

//public class GostSAML2EncryptionHandler extends SAML2SignatureGenerationHandler {
public class GOSTSAML2EncryptionHandler extends
		GOSTSAML2SignatureGenerationHandler {

	final static Logger loggerslf4j = LoggerFactory
			.getLogger(GOSTSAML2EncryptionHandler.class);

	@Override
	public void handleRequestType(SAML2HandlerRequest request,
			SAML2HandlerResponse response) throws ProcessingException {

		loggerslf4j.info("handleRequestType:01");

		if (supportsRequest(request) && isEncryptionEnabled()) {
			Document samlResponseDocument = response.getResultingDocument();

			// loggerslf4j.info("handleRequestType:02:"+DocumentUtil.asString(samlResponseDocument));

			if (samlResponseDocument == null) {
				throwResponseDocumentOrAssertionNotFound();
			}

			String samlNSPrefix = getSAMLNSPrefix(samlResponseDocument);

			try {
				QName encryptedAssertionElementQName = new QName(
						JBossSAMLURIConstants.ASSERTION_NSURI.get(),
						JBossSAMLConstants.ENCRYPTED_ASSERTION.get(),
						samlNSPrefix);

				byte[] secret = WSTrustUtil.createRandomSecret(128 / 8);
				// SecretKey secretKey = new SecretKeySpec(secret,
				// getAlgorithm());

				SecretKey secretKey = KeyGenerator.getInstance("GOST28147")
						.generateKey();

				// encrypt the Assertion element and replace it with a
				// EncryptedAssertion element.
				/*
				 * XMLEncryptionUtil.encryptElement(new
				 * QName(JBossSAMLURIConstants.ASSERTION_NSURI.get(),
				 * JBossSAMLConstants.ASSERTION.get(), samlNSPrefix),
				 * samlResponseDocument, getSenderPublicKey(request), secretKey,
				 * getKeySize(), encryptedAssertionElementQName, true);
				 */

				GOSTXMLEncryptionUtil.encryptElement(new QName(
						JBossSAMLURIConstants.ASSERTION_NSURI.get(),
						JBossSAMLConstants.ASSERTION.get(), samlNSPrefix),
						samlResponseDocument, getSenderPublicKey(request),
						secretKey, getKeySize(),
						encryptedAssertionElementQName, true);

				// loggerslf4j.info("handleRequestType:03:"+DocumentUtil.asString(samlResponseDocument));

			} catch (Exception e) {

				loggerslf4j.error("handleRequestType:error:" + e);
				throw logger.processingError(e);
			}
		}

		super.handleRequestType(request, response);
	}

	private String getSAMLNSPrefix(Document samlResponseDocument) {
		Node assertionElement = samlResponseDocument
				.getDocumentElement()
				.getElementsByTagNameNS(
						JBossSAMLURIConstants.ASSERTION_NSURI.get(),
						JBossSAMLConstants.ASSERTION.get()).item(0);

		if (assertionElement == null) {
			throwResponseDocumentOrAssertionNotFound();
		}

		return assertionElement.getPrefix();
	}

	/**
	 * <p>
	 * Indicates if the IDP has encryption enabled.
	 * </p>
	 * 
	 * @return
	 */
	private boolean isEncryptionEnabled() {
		return getType() == HANDLER_TYPE.IDP && getConfiguration().isEncrypt();
	}

	/**
	 * <p>
	 * Indicates if this handler supports the specified
	 * {@link SAML2HandlerRequest}.
	 * </p>
	 * 
	 * @param request
	 * @return
	 */
	private boolean supportsRequest(SAML2HandlerRequest request) {
		return getType() == HANDLER_TYPE.IDP
				&& (request.getSAML2Object() instanceof AuthnRequestType);
	}

	private IDPType getConfiguration() {
		IDPType configuration = (IDPType) handlerChainConfig
				.getParameter(GeneralConstants.CONFIGURATION);

		if (configuration == null) {
			throw logger.nullArgumentError("IDP Configuration");
		}

		return configuration;
	}

	private int getKeySize() {
		String keySize = (String) handlerConfig
				.getParameter(GeneralConstants.SAML_ENC_KEY_SIZE);

		if (keySize == null) {
			keySize = String.valueOf(128);
		}

		return Integer.valueOf(keySize);
	}

	private String getAlgorithm() {
		String algorithm = (String) handlerConfig
				.getParameter(GeneralConstants.SAML_ENC_ALGORITHM);

		if (algorithm == null) {
			algorithm = "AES";
		}

		return algorithm;
	}

	private PublicKey getSenderPublicKey(SAML2HandlerRequest request) {
		PublicKey publicKey = (PublicKey) request.getOptions().get(
				GeneralConstants.SENDER_PUBLIC_KEY);

		// loggerslf4j.info("getSenderPublicKey:01:"+publicKey);

		if (publicKey == null) {
			throw logger.nullArgumentError("Sender Public Key");
		}

		return publicKey;
	}

	private void throwResponseDocumentOrAssertionNotFound() {
		throw new IllegalStateException(
				"No response document/assertions found. Check if this handler is after the SAML2AuthenticationHandler.");
	}

}
