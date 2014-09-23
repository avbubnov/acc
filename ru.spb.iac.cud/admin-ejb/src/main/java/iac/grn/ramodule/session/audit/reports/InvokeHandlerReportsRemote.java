package iac.grn.ramodule.session.audit.reports;

import iac.cud.infosweb.dataitems.AuditReportsItem;

import java.util.List;


public interface InvokeHandlerReportsRemote {
	public List<AuditReportsItem> getReportItemsList(
			String type,
			int cD,
			int cU,
			int cT,
			String date1,
			String date2)throws Exception;
}
