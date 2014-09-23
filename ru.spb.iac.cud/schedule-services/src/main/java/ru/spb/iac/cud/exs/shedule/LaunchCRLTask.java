package ru.spb.iac.cud.exs.shedule;

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

public class LaunchCRLTask {

	private static final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	final static Logger logger = LoggerFactory.getLogger(LaunchCRLTask.class);

	public static void initTask(int delaySeconds) {
		logger.info("initTask:01");

		scheduler.schedule(new Runnable() {

			public void run() {

				try {

					logger.info("initTask:run");

					LaunchCRLRepeatTask.initTask(calendar());

				} catch (Exception e) {
					logger.error("initTask:error:" + e);
				} finally {
					try {
						scheduler.shutdown();
					} catch (Exception e) {
						logger.error("initTask:finally:is:error:" + e);
					}
				}
			}
		}, delaySeconds, TimeUnit.SECONDS);
	}

	public static Long calendar() {

		Long currentTime = System.currentTimeMillis();

		Calendar cln = Calendar.getInstance();
		cln.set(Calendar.HOUR_OF_DAY, 3);
		cln.set(Calendar.MINUTE, 0);
		cln.set(Calendar.SECOND, 0);
		cln.set(Calendar.MILLISECOND, 0);

		Long trans = cln.getTimeInMillis();

		Long start = trans - currentTime;

		if (start <= 0) {
			start = start + 24 * 60 * 60 * 1000;
		}

		logger.info("calendar:03:start:" + start);

		return start /* 10L */;
	}
}
