package ru.spb.iac.cud.idp.core.util;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.core.util.CUDConstants;

/**
 * EJB для предоставления сертификата системы по коду системы
 * 
 * @author bubnov
 * 
 */
@Stateless
public class IDPUtilManager implements IDPUtilManagerLocal,
		IDPUtilManagerRemote {

	@PersistenceContext(unitName = "AuthServices")
	EntityManager em;

	Logger logger = LoggerFactory.getLogger(IDPUtilManager.class);

	/**
	 * предоставление сертификата системы по коду системы
	 */
	public X509Certificate system_cert(String domain_name) throws Exception {

		X509Certificate user_cert = null;
		String cert_data = null;
		try {

			// domain_name - это код системы или код подсистемы
			// берётся напрямую из issuer

			logger.info("system_cert:01:" + domain_name);

			// ищем код в системах
			if (domain_name.startsWith(CUDConstants.armPrefix)) {

				cert_data = (String) em
						.createNativeQuery(
								"select to_char(T1.CERT_DATE) "
										+ "from AC_IS_BSS_T t1 "
										+ "where T1.SIGN_OBJECT=? ")
						.setParameter(1, domain_name).getSingleResult();

			} else if (domain_name.startsWith(CUDConstants.subArmPrefix)) {
				// подсистемы
				cert_data = (String) em
						.createNativeQuery(
								"select to_char(T1.CERT_DATE) "
										+ "from AC_SUBSYSTEM_CERT_BSS_T t1 "
										+ "where T1.SUBSYSTEM_CODE=? ")
						.setParameter(1, domain_name).getSingleResult();

			} else if (domain_name.startsWith(CUDConstants.groupArmPrefix)) {
				// группы систем

				cert_data = (String) em
						.createNativeQuery(
								"select to_char(T1.CERT_DATA) "
										+ "from GROUP_SYSTEMS_KNL_T t1 "
										+ "where T1.GROUP_CODE=? ")
						.setParameter(1, domain_name).getSingleResult();

			}

			// logger.info("system_cert:01:"+cert_data);

			if (cert_data != null) {

				byte[] cert_byte = Base64.decode(cert_data);

				CertificateFactory user_cf = CertificateFactory
						.getInstance("X.509");
				user_cert = (X509Certificate) user_cf
						.generateCertificate(new ByteArrayInputStream(cert_byte));

			}

			// logger.info("system_cert:01:result:"+user_cert);

		} catch (Exception e) {
			logger.error("system_cert:error:" + e);
		}

		// если result = null, то будет выброшено исключение
		// в KeyStoreKeyManager.getValidatingKey() -
		// throw logger.keyStoreMissingDomainAlias(domain);

		return user_cert;
	}

}
