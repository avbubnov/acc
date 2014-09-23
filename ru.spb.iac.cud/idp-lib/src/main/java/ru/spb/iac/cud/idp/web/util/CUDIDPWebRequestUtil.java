package ru.spb.iac.cud.idp.web.util;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.exceptions.fed.IssuerNotTrustedException;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.TrustType;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.factories.JBossSAMLAuthnResponseFactory;
import org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IDPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.SPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.util.HTTPRedirectUtil;
import org.picketlink.identity.federation.web.util.IDPWebRequestUtil;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingSignatureUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.w3c.dom.Document;

import ru.spb.iac.cud.idp.web.sig.GOSTSAML2Signature;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.StringTokenizer;

import static org.picketlink.common.util.StringUtil.*;

/**
 * Request Util <b> Not thread safe</b>
 * 
 * @author Anil.Saldhana@redhat.com
 * @since May 18, 2009
 */
public class CUDIDPWebRequestUtil extends IDPWebRequestUtil {

	// используется для isPassive при failed
	// для подписи ГОСТ в сообщении об ошибке
	// переопределяем getErrorResponse()

	private static final PicketLinkLogger logger = PicketLinkLoggerFactory
			.getLogger();

	private final TrustKeyManager keyManager;

	public CUDIDPWebRequestUtil(HttpServletRequest request, IDPType idp,
			TrustKeyManager keym) {

		super(request, idp, keym);
		this.keyManager = keym;
	}

	/**
	 * Create an Error Response
	 * 
	 * @param responseURL
	 * @param status
	 * @param identityURL
	 * @param supportSignature
	 * @return
	 * @throws ConfigurationException
	 */
	public Document getErrorResponse(String responseURL, String status,
			String identityURL, boolean supportSignature) {

		// вызывается из CUDAbstractIDPValve.handleIsPassiveFailedResponse()

		Document samlResponse = null;
		ResponseType responseType = null;

		SAML2Response saml2Response = new SAML2Response();

		// Create a response type
		String id = IDGenerator.create("ID_");

		IssuerInfoHolder issuerHolder = new IssuerInfoHolder(identityURL);
		issuerHolder.setStatusCode(status);

		IDPInfoHolder idp = new IDPInfoHolder();
		idp.setNameIDFormatValue(null);
		idp.setNameIDFormat(JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT
				.get());

		SPInfoHolder sp = new SPInfoHolder();
		sp.setResponseDestinationURI(responseURL);

		responseType = saml2Response.createResponseType(id);
		responseType.setStatus(JBossSAMLAuthnResponseFactory
				.createStatusType(status));

		// Lets see how the response looks like
		if (logger.isTraceEnabled()) {
			StringWriter sw = new StringWriter();
			try {
				saml2Response.marshall(responseType, sw);
			} catch (ProcessingException e) {
				logger.trace(e);
			}
			logger.trace("SAML Response Document: " + sw.toString());
		}

		if (supportSignature) {
			try {
				// SAML2Signature ss = new SAML2Signature();
				SAML2Signature ss = new GOSTSAML2Signature();
				ss.setSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411");
				ss.setDigestMethod("http://www.w3.org/2001/04/xmldsig-more#gostr3411");

				samlResponse = ss.sign(responseType,
						keyManager.getSigningKeyPair());
			} catch (Exception e) {
				logger.trace(e);
				throw new RuntimeException(logger.signatureError(e));
			}
		} else
			try {
				samlResponse = saml2Response.convert(responseType);
			} catch (Exception e) {
				logger.trace(e);
			}

		return samlResponse;
	}

}