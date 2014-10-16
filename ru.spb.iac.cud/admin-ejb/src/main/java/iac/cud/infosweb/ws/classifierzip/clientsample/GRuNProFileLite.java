package iac.cud.infosweb.ws.classifierzip.clientsample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;

@Name("gRuNProFileLite")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class GRuNProFileLite implements GRuNProFileLiteLocal {

	@PersistenceContext(unitName = "InfoSCUD-web")
	EntityManager entityManager;

	
	final static Logger LOGGER = LoggerFactory.getLogger(GRuNProFileLite.class);

	public int process() {

		Session session = (Session) entityManager.getDelegate();
		final int[] result = new int[] { -1 };

		session.doWork(new Work() {
			public void execute(Connection conn) throws SQLException {
				LOGGER.debug("IHLogContrList:logContrList:execute:conn:"
						+ conn.isClosed());
				try {
					conn.setAutoCommit(false);
				
					PojoRunProcess pp = new PojoRunProcess();
					pp.startProcess();

					LOGGER.debug("IHLogContrList:logContrList:execute:01");
				
				} catch (Exception e) {
					LOGGER.error("IHLogContrList:logContrList:execute:error:1:"
							+ e);
				} finally {

					// !!!
					// обязательно здесь НЕ закрывать коннект!!!

					/*
					 * if/(conn/!=null/&&!conn./isClosed/()){ /conn.close/() }
					 */

				}
			}
		});
		return result[0];
	}

}
