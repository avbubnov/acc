package iac.grn.ramodule.session.audit.zags.search;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import iac.grn.ramodule.entity.zags.VAuditReport;

import javax.ejb.Remote;

public interface InvokeHandlerSearchRemote {

	 public String invoke(String type);
	 public List<VAuditReport> invokeSearchResults(String type, String searchPattern,
			 int firstRow, int numberOfRows)throws Exception;
	 public Long invokeSearchCount(String type, String searchPattern)throws Exception;
}
