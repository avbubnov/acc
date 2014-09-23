package ru.spb.iac.cud.exs.shedule;

import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.local.service.ServiceReestrAction;
import iac.cud.infosweb.local.service.ServiceReestrPro;
import iac.cud.infosweb.remote.frontage.IRemoteFrontageLocal;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.cert.CRL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LaunchUCCertTask {

	private static final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	private static String jndiBinding = "java:global/InfoS-ear/InfoS-ejb/IRemoteFrontage!iac.cud.infosweb.remote.frontage.IRemoteFrontageLocal";

	private static final String proc_uccert_exec_file = System
			.getProperty("jboss.server.config.dir")
			+ "/"
			+ "proc_uccert_exec.properties";

	final static Logger logger = LoggerFactory
			.getLogger(LaunchUCCertTask.class);

	public static void initTask(int delaySeconds) {

		logger.info("initTask");

		scheduler.schedule(new Runnable() {

			public void run() {

				String start_date = null, period = null, scan_interval = null, scan_period = null, waitNextFile, status = null;
				Properties properties = new Properties();
				String path = proc_uccert_exec_file;
				InputStream is = null;
				Long startSpan = null;

				try {

					logger.info("initTask:run:01+");

					DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");

					File f = new File(path);

					// запускаем на автомате
					// даже если админ не запустил процесс из консоли
					// а соответственно нет файла

					logger.info("initTask:run:02+");

					// временное решение
					// запуск на автомате без админа

					logger.info("initTask:run:03+!+");

					logger.info("initTask:run:03++:" + ServiceReestrPro.class);

					logger.info("initTask:run:04+:"
							+ ServiceReestrPro.UCCert.name());

					BaseParamItem bpi = new BaseParamItem(
							ServiceReestrPro.UCCert.name());

					logger.info("initTask:run:05+");

					bpi.put("gactiontype",
							ServiceReestrAction.PROCESS_START.name());

					logger.info("initTask:run:06+");

					period = "1";
					startSpan = calendar();

					logger.info("initTask:run:07+");

					bpi.put("startSpan", startSpan);
					bpi.put("period", new Long(period));

					Context ctx = new InitialContext();
					logger.info("initTask:run:02");
					((IRemoteFrontageLocal) ctx.lookup(jndiBinding)).run(bpi);
					logger.info("initTask:run:03");

				} catch (Exception e) {
					logger.error("initTask:run:error:" + e);
				} finally {
					try {
						scheduler.shutdown();
					} catch (Exception e) {
						logger.error("initTask:run:finally:is:error:" + e);
					}
				}
			}
		}, delaySeconds, TimeUnit.SECONDS);
	}

	public static Long calendar() {

		Long currentTime = System.currentTimeMillis();

		Calendar cln = Calendar.getInstance();
		cln.set(Calendar.HOUR_OF_DAY, 3);
		cln.set(Calendar.MINUTE, 5);
		cln.set(Calendar.SECOND, 0);
		cln.set(Calendar.MILLISECOND, 0);

		Long trans = cln.getTimeInMillis();

		Long start = trans - currentTime;

		if (start <= 0) {
			start = start + 24 * 60 * 60 * 1000;
		}

		logger.info("calendar:03:start:" + start);

		return start;
		// return 5000L;
	}
}
