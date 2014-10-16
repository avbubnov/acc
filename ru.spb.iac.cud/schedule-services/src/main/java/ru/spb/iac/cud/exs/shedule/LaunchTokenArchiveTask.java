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

public class LaunchTokenArchiveTask {

	private static final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	private static String jndiBinding = "java:global/InfoS-ear/InfoS-ejb/IRemoteFrontage!iac.cud.infosweb.remote.frontage.IRemoteFrontageLocal";

	private static final String proc_atoken_exec_file = System
			.getProperty("jboss.server.config.dir")
			+ "/"
			+ "proc_atoken_exec.properties";

	final static Logger LOGGER = LoggerFactory
			.getLogger(LaunchTokenArchiveTask.class);

	public static void initTask(int delaySeconds) {

		LOGGER.debug("initTask");

		scheduler.schedule(new Runnable() {

			public void run() {

				String  status = null;
				Properties properties = new Properties();
				String path = proc_atoken_exec_file;
			

				try {

					LOGGER.debug("initTask:run:01");

					
					File f = new File(path);

					if (f.exists()) {

						LOGGER.debug("initTask:run:02");

						properties.load(new FileInputStream(f));

						status = properties.getProperty("status");

						LOGGER.debug("initTask:run:status!:" + status);

						if (status != null && status.equals("active")) {

							LOGGER.debug("initTask:run:03");

							Context ctx = new InitialContext();

							BaseParamItem bpi = new BaseParamItem(
									ServiceReestrPro.ArchiveToken.name());
							bpi.put("gactiontype",
									ServiceReestrAction.PROCESS_START.name());

							((IRemoteFrontageLocal) ctx.lookup(jndiBinding))
									.run(bpi);
						}
					}

				} catch (Exception e) {
					LOGGER.error("initTask:run:error:", e);
				} finally {
					try {
						scheduler.shutdown();
					} catch (Exception e) {
						LOGGER.error("initTask:run:finally:is:error:", e);
					}
				}
			}
		}, delaySeconds, TimeUnit.SECONDS);
	}

}
