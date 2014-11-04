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
 * EJB ��� �������������� ����������� ������� �� ���� �������
 * 
 * @author bubnov
 * 
 */
@Stateless
public class IDPUtilManager implements IDPUtilManagerLocal,
		IDPUtilManagerRemote {

	@PersistenceContext(unitName = "AuthServices")
	EntityManager em;

	final static Logger LOGGER = LoggerFactory.getLogger(IDPUtilManager.class);

	/**
	 * �������������� ����������� ������� �� ���� �������
	 */
	public X509Certificate system_cert(String domain_name) throws Exception {

		X509Certificate userCertX = null;
		String certDataX = null;
		try {

			// domain_name - ��� ��� ������� ��� ��� ����������
			// ������ �������� �� issuer

			LOGGER.debug("system_cert:01:" + domain_name);

			// ���� ��� � ��������
			if (domain_name.startsWith(CUDConstants.armPrefix)) {

				certDataX = (String) em
						.createNativeQuery(
								"select to_char(T1.CERT_DATE) "
										+ "from AC_IS_BSS_T t1 "
										+ "where T1.SIGN_OBJECT=? ")
						.setParameter(1, domain_name).getSingleResult();

			} else if (domain_name.startsWith(CUDConstants.subArmPrefix)) {
				// ����������
				certDataX = (String) em
						.createNativeQuery(
								"select to_char(T1.CERT_DATE) "
										+ "from AC_SUBSYSTEM_CERT_BSS_T t1 "
										+ "where T1.SUBSYSTEM_CODE=? ")
						.setParameter(1, domain_name).getSingleResult();

			} else if (domain_name.startsWith(CUDConstants.groupArmPrefix)) {
				// ������ ������

				certDataX = (String) em
						.createNativeQuery(
								"select to_char(T1.CERT_DATA) "
										+ "from GROUP_SYSTEMS_KNL_T t1 "
										+ "where T1.GROUP_CODE=? ")
						.setParameter(1, domain_name).getSingleResult();

			}

			 

			if (certDataX != null) {

				byte[] certByteX = Base64.decode(certDataX);

				CertificateFactory userCf = CertificateFactory
						.getInstance("X.509");
				userCertX = (X509Certificate) userCf
						.generateCertificate(new ByteArrayInputStream(certByteX));

			}

			 

		} catch (Exception e) {
			LOGGER.error("system_cert:error:", e);
		}

		// ���� result = null, �� ����� ��������� ����������
		// � KeySto/reKeyManager.getVali/datingKey() -
		// throw log/ger.keyS/toreMissingDomainAlias(domain);

		return  userCertX  ;
	}

}
