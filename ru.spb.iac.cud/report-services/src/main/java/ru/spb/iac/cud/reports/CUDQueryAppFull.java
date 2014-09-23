package ru.spb.iac.cud.reports;

import iac.cud.infosweb.dataitems.ReportDownloadItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.AbstractSampleApp;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOdsReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

/**
 * @author bubnov
 */
public class CUDQueryAppFull {

	private static String REPORT_JRXML = "/home/jboss/jboss/data/reports/templates/";

	private static String REPORT_JASPER = "/home/jboss/jboss/data/reports/build/";

	private static String REPORT_JRNPRINT = "/home/jboss/jboss/data/reports/build/";

	private static String REPORT_DOWNLOAD = "/home/jboss/jboss/data/reports/build/";

	private String reportCode;

	private Map<String, Object> parameters;

	final static Logger logger = LoggerFactory.getLogger(CUDQueryAppFull.class);

	CUDQueryAppFull() {

	}

	CUDQueryAppFull(String reportCode) {
		this.reportCode = reportCode;
	}

	CUDQueryAppFull(String reportCode, Map<String, Object> parameters) {
		this.reportCode = reportCode;
		this.parameters = parameters;
	}

	public static void main(String[] args) {
		try {

			logger.info("main:01");

			CUDQueryAppFull caf = new CUDQueryAppFull();

			caf.create_report();

			logger.info("main:0100");

		} catch (Exception e) {
			logger.error("main:error:" + e);
		}
	}

	public void create_report() throws Exception {
		try {

			logger.info("create_report:01");

			// Thread.sleep(20000);

			logger.info("create_report:02");

			if (!new File(REPORT_JASPER + this.reportCode + ".jasper").exists()) {
				compile();
			}

			fill();

			xls();

			logger.info("create_report:03");

		} catch (Exception e) {
			logger.error("create_report:error:" + e);
			throw e;
		}
	}

	public ReportDownloadItem download_report(String reportType)
			throws Exception {

		ReportDownloadItem result = new ReportDownloadItem();
		byte[] content = null;
		File file = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;

		try {

			logger.info("download_report:01:" + REPORT_DOWNLOAD
					+ this.reportCode + "." + reportType);

			// Thread.sleep(20000);

			if ((file = new File(REPORT_DOWNLOAD + this.reportCode + "."
					+ reportType)).exists()) {

				logger.info("download_report:02");

				is = new FileInputStream(file);

				byte[] buffer = new byte[4096];

				int bytesRead;
				while ((bytesRead = is.read(buffer)) >= 0) {
					baos.write(buffer, 0, bytesRead);
				}

				content = baos.toByteArray();

			}

			if (content != null) {
				result.setContent(content);
				result.setFlagExec(1);
			} else {
				// нет ресурса
				result.setFlagExec(0);
			}

			logger.info("download_report:03");

		} catch (Exception e) {
			logger.error("download_report:error:" + e);
			throw e;
		} finally {
			try {
				if (baos != null) {
					baos.close();
				}
				if (is != null) {
					is.close();
				}

			} catch (Exception e) {

			}
		}

		return result;
	}

	protected Connection getDemoHsqldbConnection() throws JRException {
		Connection conn;

		try {

			String url = "jdbc:oracle:thin:CUD/CUD@192.168.2.28:1521:cudvm";

			String driver = "oracle.jdbc.driver.OracleDriver";

			Class.forName(driver);
			conn = DriverManager.getConnection(url);
		} catch (ClassNotFoundException e) {
			throw new JRException(e);
		} catch (SQLException e) {
			throw new JRException(e);

		}

		return conn;
	}

	/**
	 *
	 */
	public void test() throws JRException {
		fillIgnorePagination();
		fill();
		pdf();
		xmlEmbed();
		xml();
		html();
		rtf();
		xls();
		jxl();
		csv();
		odt();
		ods();
		docx();
		xlsx();
		pptx();
		xhtml();
	}

	/**
	 *
	 */
	public void compile() throws JRException {
		long start = System.currentTimeMillis();
		JasperCompileManager.compileReportToFile(REPORT_JRXML + this.reportCode
				+ ".jrxml", REPORT_JASPER + this.reportCode + ".jasper");
		System.err.println("Compile time : "
				+ (System.currentTimeMillis() - start));
	}

	public void fill() throws JRException {
		fill(false);
	}

	/**
	 *
	 */
	public void fillIgnorePagination() throws JRException {
		fill(true);
	}

	/**
	 *
	 */
	private void fill(boolean ignorePagination) throws JRException {
		long start = System.currentTimeMillis();
		// Preparing parameters
		/*
		 * Map<String, Object> parameters = new HashMap<String, Object>();
		 * parameters.put("ReportTitle", "Address Report");
		 * 
		 * List<String> excludedCities = new ArrayList<String>();
		 * excludedCities.add("Boston"); excludedCities.add("Chicago");
		 * excludedCities.add("Oslo"); parameters.put("ExcludedCities",
		 * excludedCities);
		 * 
		 * parameters.put("OrderClause", "City");
		 * 
		 * if (ignorePagination) {
		 * parameters.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE); }
		 */

		JasperFillManager.fillReportToFile(REPORT_JASPER + this.reportCode
				+ ".jasper", REPORT_JRNPRINT + this.reportCode + ".jrprint",
				this.parameters, getDemoHsqldbConnection());
		System.err.println("Filling time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	public void print() throws JRException {
		long start = System.currentTimeMillis();
		JasperPrintManager.printReport(REPORT_JRNPRINT + this.reportCode
				+ ".jrprint", true);
		System.err.println("Printing time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	public void pdf() throws JRException {
		long start = System.currentTimeMillis();
		JasperExportManager.exportReportToPdfFile(REPORT_JRNPRINT
				+ this.reportCode + ".jrprint");
		System.err.println("PDF creation time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	public void rtf() throws JRException {
		long start = System.currentTimeMillis();
		File sourceFile = new File(REPORT_JRNPRINT + this.reportCode
				+ ".jrprint");

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(sourceFile);

		File destFile = new File(sourceFile.getParent(), jasperPrint.getName()
				+ ".rtf");

		JRRtfExporter exporter = new JRRtfExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleWriterExporterOutput(destFile));

		exporter.exportReport();

		System.err.println("RTF creation time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	public void xml() throws JRException {
		long start = System.currentTimeMillis();
		JasperExportManager.exportReportToXmlFile(REPORT_JRNPRINT
				+ this.reportCode + ".jrprint", false);
		System.err.println("XML creation time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	public void xmlEmbed() throws JRException {
		long start = System.currentTimeMillis();
		JasperExportManager.exportReportToXmlFile(REPORT_JRNPRINT
				+ this.reportCode + ".jrprint", true);
		System.err.println("XML creation time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	public void html() throws JRException {
		long start = System.currentTimeMillis();
		JasperExportManager.exportReportToHtmlFile(REPORT_JRNPRINT
				+ this.reportCode + ".jrprint");
		System.err.println("HTML creation time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	public void xls() throws JRException {
		long start = System.currentTimeMillis();
		File sourceFile = new File(REPORT_JRNPRINT + this.reportCode
				+ ".jrprint");

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(sourceFile);

		// File destFile = new File(sourceFile.getParent(),
		// jasperPrint.getName() + ".xls");
		File destFile = new File(sourceFile.getParent(), this.reportCode
				+ ".xls");

		JRXlsExporter exporter = new JRXlsExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
				destFile));
		SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
		configuration.setOnePagePerSheet(false);

		configuration.setRemoveEmptySpaceBetweenRows(true);
		configuration.setRemoveEmptySpaceBetweenColumns(true);
		configuration.setCollapseRowSpan(true);

		exporter.setConfiguration(configuration);

		exporter.exportReport();

		System.err.println("XLS creation time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	@SuppressWarnings("deprecation")
	public void jxl() throws JRException {
		long start = System.currentTimeMillis();
		File sourceFile = new File(REPORT_JRNPRINT + this.reportCode
				+ ".jrprint");

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(sourceFile);

		File destFile = new File(sourceFile.getParent(), jasperPrint.getName()
				+ ".jxl.xls");

		net.sf.jasperreports.engine.export.JExcelApiExporter exporter = new net.sf.jasperreports.engine.export.JExcelApiExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
				destFile));
		net.sf.jasperreports.export.SimpleJxlReportConfiguration configuration = new net.sf.jasperreports.export.SimpleJxlReportConfiguration();
		configuration.setOnePagePerSheet(true);
		exporter.setConfiguration(configuration);

		exporter.exportReport();

		System.err.println("XLS creation time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	public void csv() throws JRException {
		long start = System.currentTimeMillis();
		File sourceFile = new File(REPORT_JRNPRINT + this.reportCode
				+ ".jrprint");

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(sourceFile);

		File destFile = new File(sourceFile.getParent(), jasperPrint.getName()
				+ ".csv");

		JRCsvExporter exporter = new JRCsvExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleWriterExporterOutput(destFile));

		exporter.exportReport();

		System.err.println("CSV creation time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	public void odt() throws JRException {
		long start = System.currentTimeMillis();
		File sourceFile = new File(REPORT_JRNPRINT + this.reportCode
				+ ".jrprint");

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(sourceFile);

		File destFile = new File(sourceFile.getParent(), jasperPrint.getName()
				+ ".odt");

		JROdtExporter exporter = new JROdtExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
				destFile));

		exporter.exportReport();

		System.err.println("ODT creation time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	public void ods() throws JRException {
		long start = System.currentTimeMillis();
		File sourceFile = new File(REPORT_JRNPRINT + this.reportCode
				+ ".jrprint");

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(sourceFile);

		File destFile = new File(sourceFile.getParent(), jasperPrint.getName()
				+ ".ods");

		JROdsExporter exporter = new JROdsExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
				destFile));
		SimpleOdsReportConfiguration configuration = new SimpleOdsReportConfiguration();
		configuration.setOnePagePerSheet(true);
		exporter.setConfiguration(configuration);

		exporter.exportReport();

		System.err.println("ODS creation time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	public void docx() throws JRException {
		long start = System.currentTimeMillis();
		File sourceFile = new File(REPORT_JRNPRINT + this.reportCode
				+ ".jrprint");

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(sourceFile);

		File destFile = new File(sourceFile.getParent(), jasperPrint.getName()
				+ ".docx");

		JRDocxExporter exporter = new JRDocxExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
				destFile));

		exporter.exportReport();

		System.err.println("DOCX creation time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	public void xlsx() throws JRException {
		long start = System.currentTimeMillis();
		File sourceFile = new File(REPORT_JRNPRINT + this.reportCode
				+ ".jrprint");

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(sourceFile);

		File destFile = new File(sourceFile.getParent(), jasperPrint.getName()
				+ ".xlsx");

		JRXlsxExporter exporter = new JRXlsxExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
				destFile));
		SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
		configuration.setOnePagePerSheet(false);
		exporter.setConfiguration(configuration);

		exporter.exportReport();

		System.err.println("XLSX creation time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	public void pptx() throws JRException {
		long start = System.currentTimeMillis();
		File sourceFile = new File(REPORT_JRNPRINT + this.reportCode
				+ ".jrprint");

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(sourceFile);

		File destFile = new File(sourceFile.getParent(), jasperPrint.getName()
				+ ".pptx");

		JRPptxExporter exporter = new JRPptxExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
				destFile));

		exporter.exportReport();

		System.err.println("PPTX creation time : "
				+ (System.currentTimeMillis() - start));
	}

	/**
	 *
	 */
	@SuppressWarnings("deprecation")
	public void xhtml() throws JRException {
		long start = System.currentTimeMillis();
		File sourceFile = new File(REPORT_JRNPRINT + this.reportCode
				+ ".jrprint");

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(sourceFile);

		File destFile = new File(sourceFile.getParent(), jasperPrint.getName()
				+ ".x.html");

		net.sf.jasperreports.engine.export.JRXhtmlExporter exporter = new net.sf.jasperreports.engine.export.JRXhtmlExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleHtmlExporterOutput(destFile));

		exporter.exportReport();

		System.err.println("XHTML creation time : "
				+ (System.currentTimeMillis() - start));
	}

}
