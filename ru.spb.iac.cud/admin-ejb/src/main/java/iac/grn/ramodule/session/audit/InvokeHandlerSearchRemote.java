package iac.grn.ramodule.session.audit;

import java.util.List;
import iac.grn.ramodule.entity.VAuditReport;
import javax.ejb.Remote;

//@Remote
public interface InvokeHandlerSearchRemote {

	 public String invoke(String type)throws Exception;
	 public List<VAuditReport> invokeSearchResults(String type, String searchPattern,
			 int firstRow, int numberOfRows)throws Exception;
	 public Long invokeSearchCount(String type, String searchPattern)throws Exception;
}
