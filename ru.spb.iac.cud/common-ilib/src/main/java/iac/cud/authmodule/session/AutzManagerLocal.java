package iac.cud.authmodule.session;

import iac.cud.authmodule.dataitem.AuthItem;
import iac.cud.authmodule.dataitem.PageItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

/**
 * �������������� ���������� � ������� ���� �������, jndi name:
 * authModule.AuthManager.local
 * 
 * @author bubnov
 */
@Local
public interface AutzManagerLocal {

	public AuthItem getAccessComplete(Long appCode, List<String> roles)
			throws Exception;

	/**
	 * �������������� � ����������� ������������
	 * 
	 * @param appCode
	 *            �� ����������
	 * @param login
	 *            �����
	 * @param password
	 *            ������
	 * @return AuthItem, ���� ������, ����� null
	 * @throws Exception
	 */
	public PageItem getAccessPage(Long appCode, List<String> roles,
			String pageCode) throws Exception;

	public boolean getAccessPage(Long appCode, List<String> roles,
			String pageCode, String permCode) throws Exception;
}
