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
import java.security.cert.X509CRLEntry;
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

public class LaunchCRLRepeatTask {

	private static final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	private static String directory = "/home/jboss/jboss/data/crl/";

	private static String reestr_path = System
			.getProperty("jboss.server.config.dir")
			+ "/"
			+ "crl_reestr.properties";

	final static Logger logger = LoggerFactory
			.getLogger(LaunchCRLRepeatTask.class);

	public static void initTask(Long start) {

		logger.info("initTask:01");

		scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {

				try {

					logger.info("initTask:run:01");

					synchronized (this) {

						LaunchCRLRepeatTask lct = new LaunchCRLRepeatTask();

						String file_name = lct.content();

						logger.info("initTask:02:" + file_name);

						String file_checksum = lct.doChecksumContent(directory
								+ "tmp_" + file_name);

						String reestr_checksum = lct
								.get_reestr("curr_checksum");

						if (!file_checksum.equals(reestr_checksum)) {

							lct.set_reestr(directory + file_name, file_checksum);
							new File(directory + "tmp_" + file_name)
									.renameTo(new File(directory + file_name));

						} else {
							new File(directory + "tmp_" + file_name).delete();
						}
					}

				} catch (Exception e) {
					logger.error("initTask:error:" + e);
				} finally {
					try {

					} catch (Exception e) {
						logger.error("initTask:finally:is:error:" + e);
					}
				}
			}
		}, start, 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
	}

	public String content() {
		InputStream is = null;
		OutputStream output = null;
		BufferedInputStream in = null;

		String path = "http://ca.iac.spb.ru/crl/qual.crl";
		// String path="http://10.128.31.65/crl/qual.crl";

		String file_name = "qual_" + System.currentTimeMillis() + ".crl";

		logger.info("content:file_name:" + file_name);

		try {
			// DateFormat df = new SimpleDateFormat ("dd.MM.yy HH:mm");
			URL url = new URL(path);
			// File f=new File(url.toURI());

			HttpURLConnection uc = (HttpURLConnection) url.openConnection();

			uc.setRequestProperty("Accept-Charset", "UTF-8");
			uc.setRequestProperty("Content-Language", "ru-RU");
			uc.setRequestProperty("Charset", "UTF-8");

			uc.connect();

			// is = uc.getInputStream();

			in = new BufferedInputStream(uc.getInputStream());

			byte[] buffer = new byte[4096];
			int n = -1;

			output = new FileOutputStream(new File(directory + "tmp_"
					+ file_name));

			while ((n = in.read(buffer)) != -1) {
				if (n > 0) {
					output.write(buffer, 0, n);
				}
			}
			output.close();

		} catch (Exception e) {
			logger.error("content:error:" + e);
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
				logger.error("content:finally:is:error:" + e);
			}
		}

		return file_name;
	}

	public void set_reestr(String curr_crl, String curr_checksum) {
		Properties properties = new Properties();
		InputStream is = null;
		OutputStream os = null;

		logger.info("content:set_reestr");

		try {

			File f = new File(reestr_path);

			if (f.exists()) {

				properties.load(is = new FileInputStream(f));

				String prev_file_name = properties.getProperty("prev_crl");

				if (prev_file_name != null) {
					File prev_file = new File(prev_file_name);
					if (prev_file.exists()) {
						try {
							prev_file.delete();
						} catch (Exception e) {
							logger.error("set_reestr:delete:error:" + e);
						}
					}
				}

				properties.setProperty("prev_crl",
						properties.getProperty("curr_crl"));
			}

			properties.setProperty("curr_checksum", curr_checksum);
			properties.setProperty("curr_crl", curr_crl);

			properties.store(os = new FileOutputStream(f), null);

		} catch (Exception e) {
			logger.error("set_reestr:error:" + e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (os != null) {
					os.close();
				}

			} catch (Exception e) {
				logger.error("set_reestr:finally:is:error:" + e);
			}
		}
	}

	public String get_reestr(String prop_name) {
		Properties properties = new Properties();
		InputStream is = null;
		String result = null;

		logger.info("get_reestr");

		try {
			// URL url = new URL(reestr_path);
			// File f=new File(url.toURI());

			File f = new File(reestr_path);

			if (f.exists()) {

				properties.load(is = new FileInputStream(f));

				result = properties.getProperty(prop_name);

				logger.info("get_reestr:result:" + result);
			}

		} catch (Exception e) {
			logger.error("initTask:error:" + e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				logger.error("initTask:finally:is:error:" + e);
			}
		}
		return result;
	}

	private synchronized void loadCrl(String curr_crl) {

		CertificateFactory cf = null;
		InputStream is = null;
		Collection<? extends X509CRL> x509Crls = null;
		File crlFile = new File(curr_crl);

		try {
			logger.info("loadCrl:01:lastModified:" + crlFile.lastModified());

			cf = CertificateFactory.getInstance("X.509");

			x509Crls = (Collection<? extends X509CRL>) cf
					.generateCRLs(is = new FileInputStream(crlFile));

			logger.info("loadCrl:02");

			List<CRL> cpCrls = new ArrayList<CRL>(x509Crls.size());

			for (X509CRL crl : x509Crls) {

				X509CRLEntry xce = crl.getRevokedCertificate(new BigInteger(
						"80281257310973985818053"));

			}

			logger.info("loadCrl:03");

		} catch (Exception e) {
			logger.error("loadCrl:error:" + e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
				logger.error("initTask:finally:is:error:" + e);
			}
		}
	}

	private String doChecksumContent(String fileName) {

		CheckedInputStream cis = null;
		BufferedInputStream in = null;
		String result = null;

		try {

			try {
				// Computer CRC32 checksum
				cis = new CheckedInputStream(new FileInputStream(fileName),
						new CRC32());

				in = new BufferedInputStream(cis);

			} catch (FileNotFoundException e) {
				logger.error("doChecksumContent:error:" + e);
			}

			byte[] buf = new byte[5000];
			while (in.read(buf, 0, 5000) >= 0) {
			}

			long checksum_content = cis.getChecksum().getValue();

			result = Long.toString(checksum_content);

			logger.info("result: " + checksum_content + " " + fileName);

		} catch (IOException e) {
			logger.error("doChecksumContent:error_2:" + e);
		} finally {
			try {
				if (cis != null) {
					cis.close();
				}
				if (in != null) {
					in.close();
				}

			} catch (Exception e) {
				logger.error("set_reestr:finally:is:error:" + e);
			}
		}

		return result;
	}

	private void doChecksum(String fileName) {

		try {

			CheckedInputStream cis = null;
			BufferedInputStream in = null;
			long fileSize = 0;
			try {
				// Computer CRC32 checksum
				cis = new CheckedInputStream(new FileInputStream(fileName),
						new CRC32());

				in = new BufferedInputStream(cis);

				fileSize = new File(fileName).length();

			} catch (FileNotFoundException e) {
				logger.error("doChecksum:error:" + e);
			}

			byte[] buf = new byte[4096];
			while (in.read(buf, 0, 4096) >= 0) {
			}

			long checksum_content = cis.getChecksum().getValue();

			// name
			byte bytes[] = fileName.getBytes();
			Checksum checksum = new CRC32();
			checksum.update(bytes, 0, bytes.length);
			long checksum_name = checksum.getValue();

			System.out.println(checksum_content + " " + checksum_name + " "
					+ fileSize + " " + fileName);

		} catch (IOException e) {
			logger.error("doChecksum:error_2:" + e);
		}
	}

	public void getMD5Checksum(File file) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			InputStream is = new FileInputStream(file);
			try {
				is = new DigestInputStream(is, md);
			} finally {
				is.close();
			}
			byte[] digest = md.digest();

			String result = "";
			for (int i = 0; i < digest.length; i++) {
				result += Integer.toString((digest[i] & 0xff) + 0x100, 16)
						.substring(1);
			}

			logger.info("getMD5Checksum:1:" + result);

			result = "";

			md.update(file.getName().getBytes(), 0, file.getName().length());

			digest = md.digest();

			for (int i = 0; i < digest.length; i++) {
				result += Integer.toString((digest[i] & 0xff) + 0x100, 16)
						.substring(1);
			}
			logger.info("getMD5Checksum:2:" + result);

			logger.info("getMD5Checksum:3:"
					+ new BigInteger(1, md.digest()).toString(16));

			BigInteger i = new BigInteger(1, md.digest());

			logger.info(String.format("%1$032X", i));

		} catch (Exception e) {
			logger.error("getMD5Checksum:error:" + e);
		}
	}

}
