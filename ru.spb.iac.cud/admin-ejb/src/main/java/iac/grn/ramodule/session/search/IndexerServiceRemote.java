package iac.grn.ramodule.session.search;

import iac.grn.ramodule.entity.VAuditReport;

import java.util.Date;
import java.util.List;

import javax.ejb.Remote;

public interface IndexerServiceRemote {

	public void purgeAll();
	public List<VAuditReport> getSearchResults(String searchPattern,
			 int firstRow, int numberOfRows);
	public Long getSearchCount(String searchPattern);
	public void index_pro();
	public void purgeByDate(Date d1, Date d2);
}
