package iac.cud.authmodule.dataitem;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.Serializable;

/**
 * ������ �� ������� ���� ������� � �������� ����������
 * 
 * @author bubnov
 */
public class AuthItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long idUser;
	private HashMap<String, PageItem> pageList = new HashMap<String, PageItem>();

	/**
	 * �� ������������
	 * 
	 * @return �� ������������
	 */
	public Long getIdUser() {
		return this.idUser;
	}

	public void setIdUser(Long idUser) {
		this.idUser = idUser;
	}

	/**
	 * ����� � ������������ ����������� �� ������� ���������� �� PageItem
	 * 
	 * @return ����� ��������� ������������ ��������, ���� ����, ����� - empty
	 */
	public HashMap<String, PageItem> getPageList() {
		return this.pageList;
	}

	public void setPageList(HashMap<String, PageItem> pageList) {
		this.pageList = pageList;
	}
}
