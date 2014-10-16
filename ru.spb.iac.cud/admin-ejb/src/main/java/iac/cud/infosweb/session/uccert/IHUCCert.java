package iac.cud.infosweb.session.uccert;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.transaction.UserTransaction;

import mypackage.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.entity.UcCertReestr;
import iac.cud.infosweb.local.service.IHLocal;
import iac.cud.infosweb.local.service.ServiceReestr;
import iac.cud.infosweb.local.service.ServiceReestrAction;
import iac.grn.infosweb.context.proc.TaskProcessor;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class IHUCCert extends IHUCCertBase implements IHLocal {

	@PersistenceContext(unitName = "InfoSCUD-web")
	EntityManager em;

	@Resource
	UserTransaction utx;

	//private Logger log = Logger.getLogger(IHUCCert.class);
	final static Logger LOGGER = LoggerFactory
			.getLogger(IHUCCert.class);
	
    private static final String proc_uccert_info_file = System
			.getProperty("jboss.server.config.dir")
			+ "/"
			+ "proc_uccert_info.properties";

	private static String directory = Configuration.getUccert();

	private static String file_name = "qual_dump.txt";

	private HashMap hm = null;

	private static final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	private int persist_record = 0;

	// "http://ca.iac.spb.ru/crl/qual_dump.txt";
	private static String path = Configuration.getUcCertReestr();

	public BaseParamItem process_start(BaseParamItem paramMap) throws Exception {

		BaseParamItem jpi = new BaseParamItem();

		LOGGER.debug("IHUCCert:process_start:01");

		// startSpan - задержка от текущего времени
		// до момента запуска
		// устанавливается через calendar()

		Long startSpan = (Long) paramMap.get("startSpan");
		Long period = (Long) paramMap.get("period");

		LOGGER.debug("IHUCCert:process_start:02:" + startSpan);

		if (startSpan == null || period == null) {
			LOGGER.debug("IHUCCert:process_start:return");
			return jpi;
		}
		
		ScheduledFuture shf = scheduler.scheduleAtFixedRate(new Runnable() {

			public void run() {

				try {

					LOGGER.info("IHUCCert:process_start:run");

					Calendar cln = Calendar.getInstance();

					process_start_content();
	
				} catch (Exception e) {
					LOGGER.error("IHUCCert:process_start:run:error:", e);
				} finally {
					try {

					} catch (Exception e) {
						LOGGER.error("IHUCCert:process_start:run:finally:error:"
								+ e);
					}
				}
			}
		}, startSpan, period * 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);

	
		if (TaskProcessor.getControls().containsKey("UCCertScheduled")) {
			try {
				TaskProcessor.getControls().get("UCCertScheduled")
						.cancel(false);
			} catch (Exception e) {
				LOGGER.debug("IHUCCert:process_start:error:", e);
			}
		}
		TaskProcessor.getControls().put("UCCertScheduled", shf);

		return jpi;
	}

	public BaseParamItem process_stop(BaseParamItem paramMap) throws Exception {

		BaseParamItem jpi = new BaseParamItem();

		LOGGER.debug("IHUCCert:process_stop:01");

		try {
	
			ScheduledFuture shf = TaskProcessor.getControls().get(
					"UCCertScheduled");

			LOGGER.debug("IHUCCert:process_stop:02");
			if (shf != null) { // может быть = null, когда приостановили, а
								// потом отключаем
				shf.cancel(false);
			}
		} catch (Exception e) {
			LOGGER.error("IHUCCert:process_stop:error:", e);
			throw e;
		}

		return jpi;
	}

	public BaseParamItem task_run(BaseParamItem paramMap) throws Exception {

		BaseParamItem jpi = new BaseParamItem();

		LOGGER.debug("IHUCCert:task_run");

		try {
	
			process_start_content();

		} catch (Exception e) {
			LOGGER.error("IHUCCert:task_run:error:", e);
			throw e;
		}
		return jpi;
	}

	private synchronized void process_start_content() throws Exception {

		boolean hit = true;
		OutputStream os = null;
		File file_reestr = null;

		LOGGER.info("UCCERT:task_run:start");
		
		LOGGER.debug("IHUCCert:process_start_content:01");

		try {
			if (UCCertProcessor.getControls().containsKey("uccert")) {
				LOGGER.debug("IHUCCert:process_start_content:return");
				return;
			}

			UCCertProcessor.getControls().put("uccert", "");

			// закоммент 25.04.14 в связи с ситуацией, когда транзакция
			// получается очень долгой
			// и оракл закрывает соединение
			// utx . begin

		
			file_reestr = content();
			// file_reestr = n e w
			// F i l e  ("/Проекты/ЦУД разработка 5.0/Сертификаты/"+file_name);

			LOGGER.debug("IHUCCert:process_start_content:02:" + file_name);

			if (hm == null) {
				hm = new HashMap();
				configuration_file();
			}

		
			if (file_reestr != null && file_reestr.exists()) {
				// file_interaction(new File(directory+file_name));
				file_interaction(file_reestr);
			}

			// закоммент 25.04.14 в связи с ситуацией, когда транзакция
			// получается очень долгой
			// и оракл закрывает соединение
			// utx . commit 

			LOGGER.debug("IHUCCert:process_start_content:persist_record:" + persist_record);

			LOGGER.info("UCCERT:task_run:end");
			
		} catch (Exception e) {
			LOGGER.error("IHUCCert:process_start_content:error", e);

			utx.rollback();

			hit = false;
			throw e;

		} finally {

			try {

				UCCertProcessor.getControls().remove("uccert");

				LOGGER.debug("IHUCCert:process_start_content:finally:hit:" + hit);

				DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
			
				File f = new File(proc_uccert_info_file);

				Properties properties = new Properties();

				properties.setProperty("exec_date",
						df.format(System.currentTimeMillis()));
				properties.setProperty("exec_hit", hit ? "true" : "false");

				properties.store(os = new FileOutputStream(f), null);

			} catch (Exception e) {
				LOGGER.error("IHUCCert:process_start_content:error:2:", e);
			} finally {
				try {
					if (os != null) {
						os.close();
					}
				} catch (Exception e) {
				}
				try {
					if (file_reestr != null && file_reestr.exists()) {
						file_reestr.delete();
					}
				} catch (Exception e) {
				}
			}
		}
	}

	public File content() {
		InputStream is = null;
		OutputStream output = null;
		BufferedInputStream in = null;

	
		File resultFile = null;

		LOGGER.debug("IHUCCert:content:path:" + path);

		try {
			URL url = new URL(path);
		
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();

			uc.setRequestProperty("Accept-Charset", "UTF-8");
			uc.setRequestProperty("Content-Language", "ru-RU");
			uc.setRequestProperty("Charset", "UTF-8");

			uc.connect();

		
			in = new BufferedInputStream(uc.getInputStream());

			byte[] buffer = new byte[4096];
			int n = -1;

			resultFile = new File(directory + /* "tmp_"+ */file_name);
			output = new FileOutputStream(resultFile);

			while ((n = in.read(buffer)) != -1) {
				if (n > 0) {
					output.write(buffer, 0, n);
				}
			}
			output.close();

		} catch (Exception e) {
			LOGGER.error("IHUCCert:content:error:", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (is != null) {
					is.close();
				}
				if (output != null) {
					output.close();
				}
			} catch (Exception e) {
				LOGGER.error("LaunchCRLTask:content:finally:is:error:", e);
			}
		}

		return resultFile;
	}

	public void file_interaction(File file) throws Exception {

		LOGGER.debug("file_interaction:01");

		String s;
		PreparedStatement ps = null;
		BufferedReader br = null;

		StringBuffer localRow = new StringBuffer();
		try {

			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "Cp1251"));

			
			int i_logich = 1;

			persist_record = 0;

			s = br.readLine();
			if (s != null) {
			
				localRow.append(s);

				while ((s = br.readLine()) != null) {

					// новая логическая запись
					if (s.startsWith("|")) {
			
						records_db_wh(ps, localRow.toString());
						localRow = new StringBuffer();

						// просто для информации
						if ((i_logich % 100) == 0) {
							LOGGER.debug("file_interaction:03:" + i_logich);

						}
						if (persist_record != 0 && (persist_record % 100) == 0) {
							LOGGER.debug("file_interaction:04:commit+:"
									+ persist_record);

							// закоммент 25.04.14 в связи с ситуацией, когда
							// транзакция получается очень долгой
							// и оракл закрывает соединение
							// utx . commit

							// utx . begin
						}
						i_logich++;
					}

					localRow.append(s);

					

				}
				// utx . commit

			}

		} catch (Exception e) {
			LOGGER.error("file_interaction:error=", e);
			throw e;
		} finally {
			try {
				br.close();
			} catch (Exception e) {
				LOGGER.error("file_interaction:finally:error=", e);
			}
		}
	}

	public void records_db_wh(PreparedStatement ps, String rec)
			throws Exception {

		try {
			int i = 1;
			String[] sa = rec.split("\t");
			String certNum = null;

		
			certNum = sa[3];

			// проверяем есть ли уже такой сертификат в реестре
			List certExist = em
					.createNativeQuery(
							"select 1 " + "from UC_CERT_REESTR uc "
									+ "where UC.CERT_NUM = ? ")
					.setParameter(1, certNum).getResultList();

			// у нас в реестре уже есть этот сертификат
			if (certExist != null && !certExist.isEmpty()) {
				return;
			}

			UcCertReestr ucr = new UcCertReestr();

			for (String pss : sa) {

			
				switch (i) {
				case 1: {
					if (pss.trim().startsWith("|")) {
						ucr.setUserFio(pss.trim().substring(1));
					} else {
						ucr.setUserFio(pss.trim());
					}
					break;
				}
				case 2: {
					ucr.setOrgName(pss.trim());
					break;
				}
				case 3: {
					ucr.setUserPosition(pss.trim());
					break;
				}
				case 4: {
					ucr.setCertNum(pss.trim());
					break;
				}
				case 5: {
					ucr.setXType(pss.trim());
					break;
				}
				case 6: {
					ucr.setCertDate(pss.trim());
					break;
				}
				case 7: {
					ucr.setUserEmail(pss.trim());
					break;
				}
				case 8: {
					ucr.setCertValue(pss.trim().getBytes());
					break;
				}
				default:

				}

			
				i++;
			}

			ucr.setCreator(new Long(1));
			ucr.setCreated(new Date());

		 if(!"".equals(ucr.getUserFio())) { //принудительно не даём загружать без ФИО
			// добавлено 25.04.14 в связи с ситуацией, когда транзакция
			// получается очень долгой
			// и оракл закрывает соединение
			utx.begin();
			em.persist(ucr);

			// добавлено 25.04.14 в связи с ситуацией, когда транзакция
			// получается очень долгой
			// и оракл закрывает соединение
			utx.commit();

			persist_record++;
			
		 }	
			
			LOGGER.debug("records_db_wh:persist:" + persist_record);

			
		} catch (Exception e) {
			LOGGER.error("records_db_wh:error:", e);

			// закомментили, т.к. допускаем незагрузку отдельных ошибочных
			// записей
			// например, нет ФИО
			// throw e;
		}
	}

	public void configuration_file() throws Exception {

		LOGGER.debug("configuration_file:01");

		try {
			hm.put(1, "USER_FIO");
			hm.put(2, "ORG_NAME");
			hm.put(3, "USER_POSITION");
			hm.put(4, "CERT_NUM");
			hm.put(5, "X_TYPE");
			hm.put(6, "CERT_DATE");
			hm.put(7, "USER_EMAIL");
			hm.put(8, "CERT_VALUE");

		} catch (Exception e) {
			LOGGER.error("configuration_file:error=", e);
			throw e;
		}
	}

}
