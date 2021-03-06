package iac.grn.ramodule.session.audit;
import iac.grn.ramodule.entity.VAuditReport;
import java.util.*;

public interface InvokeHandlerRemote {

	 public String invoke(String type);
	 public VAuditReport invokeAuditBean(String type, Long sessionId, Long entryid)throws Exception;
	 public List<VAuditReport> invokeAuditList(String type)throws Exception;
	 public List<VAuditReport> invokeAuditListJPA(String type, int firstRow,
			 int numberOfRows,HashMap<String,String> fFHM)throws Exception;
	 public Long invokeAuditCountJPA(String type,
			 HashMap<String,String> fFHM)throws Exception;
	 public void auditStatusChange(String type) throws Exception;
	 public int auditStatus() throws Exception;
	 public void auditClear(Date d1, Date d2)throws Exception;
}
