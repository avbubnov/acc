package iac.grn.infosweb.session.auth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

//@Remote
public interface AuthManagerRemote {
	public Map<String, List<String>[]> authComplete(Long appCode,  String login, String password) throws Exception;
	public Long authenticate(String login, String password)throws Exception;
	public List<String>[] access(Long appCode,String pageCode, Long idUser) throws Exception;
}
