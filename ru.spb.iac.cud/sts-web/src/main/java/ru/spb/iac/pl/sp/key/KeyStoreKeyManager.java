package ru.spb.iac.pl.sp.key;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.TrustKeyConfigurationException;
import org.picketlink.common.exceptions.TrustKeyProcessingException;
import org.picketlink.config.federation.AuthPropertyType;
import org.picketlink.config.federation.KeyValueType;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.context.ContextIDPUtilManager;

public class KeyStoreKeyManager implements TrustKeyManager {
	private static final PicketLinkLogger logger = PicketLinkLoggerFactory
			.getLogger();

	final static Logger loggerslf4j = LoggerFactory
			.getLogger(KeyStoreKeyManager.class);

	private final Map<String, SecretKey> keys = new HashMap();

	private final Map<String, Object> options = new HashMap();

	private final HashMap<String, String> domainAliasMap = new HashMap();

	private final HashMap<String, String> authPropsMap = new HashMap();

	private static KeyStore ks = null;
	private PublicKey publicKey = null;

	private String keyStoreURL;
	private static char[] signingKeyPass;
	private static String signingAlias;
	private String keyStorePass;
	public static final String KEYSTORE_URL = "KeyStoreURL";
	public static final String KEYSTORE_PASS = "KeyStorePass";
	public static final String SIGNING_KEY_PASS = "SigningKeyPass";
	public static final String SIGNING_KEY_ALIAS = "SigningKeyAlias";

	public PrivateKey getSigningKey() throws TrustKeyConfigurationException,
			TrustKeyProcessingException {
		try {
			loggerslf4j.info("getSigningKey:01");

			initKeyStore();

			// loggerslf4j.info("getSigningKey:02");

			return (PrivateKey) this.ks.getKey(this.signingAlias,
					this.signingKeyPass);

		} catch (KeyStoreException e) {
			throw logger.keyStoreConfigurationError(e);
		} catch (NoSuchAlgorithmException e) {
			throw logger.keyStoreProcessingError(e);
		} catch (UnrecoverableKeyException e) {
			throw logger.keyStoreProcessingError(e);
		} catch (GeneralSecurityException e) {
			throw logger.keyStoreProcessingError(e);
		} catch (IOException e) {
			throw logger.keyStoreProcessingError(e);
		}

	}

	// ������������ ������� ���
	public KeyPair getSigningKeyPair() throws TrustKeyConfigurationException,
			TrustKeyProcessingException {
		try {
			PrivateKey privateKey = null;
			loggerslf4j.info("getSigningKeyPair:01");

			initKeyStore();

			// loggerslf4j.info("getSigningKeyPair:02");

			privateKey = getSigningKey();

			if (this.publicKey == null) {
				// loggerslf4j.info("getSigningKeyPair:02+");
				Certificate cert = this.ks.getCertificate(this.signingAlias);

				this.publicKey = cert.getPublicKey();

			}

			// loggerslf4j.info("getSigningKeyPair:03");

			return new KeyPair(publicKey, privateKey);
		} catch (KeyStoreException e) {
			throw logger.keyStoreConfigurationError(e);
		} catch (GeneralSecurityException e) {
			throw logger.keyStoreProcessingError(e);
		} catch (IOException e) {
			throw logger.keyStoreProcessingError(e);
		}

	}

	// ���� ������, �� ������ �� ��������������
	// ������� ����� ��� ������ �� ��������������
	// ����� ����� ���������� null
	public Certificate getCertificate(String alias)
			throws TrustKeyConfigurationException, TrustKeyProcessingException {

		loggerslf4j.info("getCertificate:01");
		// throw new TrustKeyConfigurationException("getCertificate not used!");
		return null;

	}

	// �� ������������
	public PublicKey getPublicKey(String alias)
			throws TrustKeyConfigurationException, TrustKeyProcessingException {
		loggerslf4j.info("getPublicKey:01");
		throw new TrustKeyConfigurationException("getPublicKey not used!");

	}

	// ��������� ������� ��
	public PublicKey getValidatingKey(String domain)
			throws TrustKeyConfigurationException, TrustKeyProcessingException {
		PublicKey publicKey = null;
		try {
			loggerslf4j.info("getValidatingKey:01:" + domain);

			// String domainAlias = (String)this.domainAliasMap.get(domain);

			// String domainAlias = null;
			X509Certificate cert_user = null;
			try {

				// domainAlias = (new
				// ContextIDPUtilManager()).domain_alias(domain);

				cert_user = (new ContextIDPUtilManager()).system_cert(domain);

				// loggerslf4j.info("getValidatingKey:03:"+cert_user);

				if (cert_user != null) {
					publicKey = cert_user.getPublicKey();
				}

			} catch (Exception e) {
				loggerslf4j.error("getValidatingKey:04:error" + e);
			}

			// loggerslf4j.info("getValidatingKey:03");

		} catch (Exception e) {
			throw logger.keyStoreProcessingError(e);
		}
		return publicKey;
	}

	private void initKeyStore() throws GeneralSecurityException, IOException {

		loggerslf4j.info("initKeyStore:01");

		if (this.ks == null) {
			// loggerslf4j.info("initKeyStore:02");
			logger.keyStoreSetup();
			setUpKeyStore();
		}
		// loggerslf4j.info("initKeyStore:03");

		if (this.ks == null)
			throw logger.keyStoreNullStore();
	}

	public void setAuthProperties(List<AuthPropertyType> authList)
			throws TrustKeyConfigurationException, TrustKeyProcessingException {
		for (AuthPropertyType auth : authList) {
			this.authPropsMap.put(auth.getKey(), auth.getValue());
		}

		this.keyStoreURL = ((String) this.authPropsMap.get("KeyStoreURL"));
		this.keyStorePass = ((String) this.authPropsMap.get("KeyStorePass"));

		this.signingAlias = ((String) this.authPropsMap.get("SigningKeyAlias"));

		String keypass = (String) this.authPropsMap.get("SigningKeyPass");
		if ((keypass == null) || (keypass.length() == 0))
			throw logger.keyStoreNullSigningKeyPass();
		this.signingKeyPass = keypass.toCharArray();
	}

	public void setValidatingAlias(List<KeyValueType> aliases)
			throws TrustKeyConfigurationException, TrustKeyProcessingException {
		for (KeyValueType alias : aliases)
			this.domainAliasMap.put(alias.getKey(), alias.getValue());
	}

	// �� ������������
	public SecretKey getEncryptionKey(String domain,
			String encryptionAlgorithm, int keyLength)
			throws TrustKeyConfigurationException, TrustKeyProcessingException {
		loggerslf4j.info("getEncryptionKey:01");
		throw new TrustKeyConfigurationException("getEncryptionKey not used!");

	}

	public void addAdditionalOption(String key, Object value) {
		this.options.put(key, value);
	}

	public Object getAdditionalOption(String key) {
		return this.options.get(key);
	}

	private void setUpKeyStore() throws GeneralSecurityException, IOException {
		loggerslf4j.info("setUpKeyStore:01");

		this.ks = loadKeyStore();
	}

	private InputStream getKeyStoreInputStream(String keyStore) {
		InputStream is = null;
		try {
			File file = new File(keyStore);
			is = new FileInputStream(file);
		} catch (Exception e) {
			URL url = null;
			try {
				url = new URL(keyStore);
				is = url.openStream();
			} catch (Exception ex) {
				// url = SecurityActions.loadResource(getClass(), keyStore);
				if (url != null)
					try {
						is = url.openStream();
					} catch (IOException e1) {
					}
			}
		}

		if (is == null)
			throw logger.keyStoreNotLocated(keyStore);
		return is;
	}

	public KeyStore loadKeyStore() throws IOException {

		String keystoreType = "HDImageStore";
		String keystoreProvider = "JCP";

		FileInputStream inputStream = null;

		try {
			KeyStore keyStore = KeyStore.getInstance(keystoreType,
					keystoreProvider);

			inputStream = new FileInputStream(new File(this.keyStoreURL));

			keyStore.load(inputStream, this.keyStorePass.toCharArray());

			return keyStore;

		} catch (Exception ex) {

			ex.printStackTrace(System.out);

			throw new IOException(ex);

		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ioe) {
				}
			}
		}

	}

}