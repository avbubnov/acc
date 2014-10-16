package iac.cud.infosweb.dataitems;

import java.io.Serializable;
import java.util.List;

public class AppOrgManItem extends AppItem {

	private String nameOrg;

	private Long idUser;
	private String loginUser;
	private String fioUser;
	private String positionUser;

	private String nameDep;

	private int modeExec;

	private String modeExecValue;

	public AppOrgManItem() {
	}

	public AppOrgManItem(Long idApp, String created, int status,
			String orgName, String usrFio, String rejectReason, String comment,
			String nameOrg, Long idUser, String loginUser, String fioUser,
			String positionUser, String nameDep, int modeExec) {
		super(idApp, created, status, orgName, usrFio, rejectReason, comment);

		this.nameOrg = nameOrg;
		this.idUser = idUser;
		this.loginUser = loginUser;
		this.fioUser = fioUser;
		this.positionUser = positionUser;
		this.nameDep = nameDep;

		this.modeExec = modeExec;
	}

	public String getNameOrg() {
		return nameOrg;
	}

	public void setNameOrg(String nameOrg) {
		this.nameOrg = nameOrg;
	}

	public Long getIdUser() {
		return idUser;
	}

	public void setIdUser(Long idUser) {
		this.idUser = idUser;
	}

	public String getLoginUser() {
		return loginUser;
	}

	public void setLoginUser(String loginUser) {
		this.loginUser = loginUser;
	}

	public String getFioUser() {
		return fioUser;
	}

	public void setFioUser(String fioUser) {
		this.fioUser = fioUser;
	}

	public String getPositionUser() {
		return positionUser;
	}

	public void setPositionUser(String positionUser) {
		this.positionUser = positionUser;
	}

	public String getNameDep() {
		return nameDep;
	}

	public void setNameDep(String nameDep) {
		this.nameDep = nameDep;
	}

	public int getModeExec() {
		return modeExec;
	}

	public void setModeExec(int modeExec) {
		this.modeExec = modeExec;
	}

	public String getModeExecValue() {
		if (this.modeExec == 0) {
			this.modeExecValue = "������";
		} else if (this.modeExec == 1) {
			this.modeExecValue = "����������";
		} else if (this.modeExec == 2) {
			this.modeExecValue = "��������";
		}
		return modeExecValue;
	}

	public void setModeExecValue(String modeExecValue) {
		this.modeExecValue = modeExecValue;
	}

}
