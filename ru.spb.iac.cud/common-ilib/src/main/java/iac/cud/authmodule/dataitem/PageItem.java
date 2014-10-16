package iac.cud.authmodule.dataitem;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * ������ �� ������� ���� ������� � ������� ����������
 * 
 * @author bubnov
 */
public class PageItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private ArrayList<String> permList = new ArrayList<String>();

	public PageItem() {
	}

	public PageItem(ArrayList<String> permList) {
		this.permList = permList;
	}

	/**
	 * ���������� �� ������
	 * 
	 * @return ������ ��������� ����������, ���� ����, ����� - empty
	 */
	public ArrayList<String> getPermList() {
		return this.permList;
	}

	public void setPermList(ArrayList<String> permList) {
		this.permList = permList;
	}

}
