package iac.cud.authmodule.session;

import iac.cud.authmodule.dataitem.AuthItem;
import iac.cud.authmodule.dataitem.PageItem;

import java.util.ArrayList;
import java.util.Map;

import javax.ejb.Remote;

@Remote
public interface CUDAuthManagerRemote {
	public Map<String, ArrayList<String>[]> authComplete(Long appCode,
			String login, String password) throws Exception;

	public Long authenticate(String login, String password) throws Exception;

	public ArrayList<String>[] access(Long appCode, String pageCode, Long idUser)
			throws Exception;

	public AuthItem authCompleteItem(Long appCode, String login, String password)
			throws Exception;

	public PageItem accessItem(Long appCode, String pageCode, Long idUser)
			throws Exception;

}
