package iac.cud.infosweb.dataitems;

import java.io.Serializable;
import java.util.List;

public class AppUserCertModifyItem extends AppItem {

	// ����������
	private String certNum;
	private String certDate;
	private String certOrgName;
	private String certDepName;
	private String certUserFio;

	// ��������
	private String nameOrg;
	private Long idUser;
	private String loginUser;
	private String fioUser;
	private String positionUser;
	private String nameDep;
	private String certUser;
	private String emailUser;
	private String phoneUser;
	private String iogvCodeUser;

	private int modeExec;

	private String modeExecValue;

	public AppUserCertModifyItem() {
	}

	public AppUserCertModifyItem(Long idApp, String created, int status,
			String orgName, String usrFio, String rejectReason, String comment,

			String certNum, String certDate, String certOrgName,
			String certDepName, String certUserFio,

			String nameOrg, Long idUser, String loginUser, String fioUser,
			String positionUser, String nameDep, String certUser,
			String iogvCodeUser, String emailUser, String phoneUser,
			int modeExec) {
		super(idApp, created, status, orgName, usrFio, rejectReason, comment);
		this.certNum = certNum;
		this.certDate = certDate;
		this.certOrgName = certOrgName;
		this.certDepName = certDepName;
		this.certUserFio = certUserFio;

		this.nameOrg = nameOrg;
		this.idUser = idUser;
		this.loginUser = loginUser;
		this.fioUser = fioUser;
		this.positionUser = positionUser;
		this.nameDep = nameDep;
		this.certUser = certUser;
		this.iogvCodeUser = iogvCodeUser;
		this.emailUser = emailUser;
		this.phoneUser = phoneUser;
		this.modeExec = modeExec;
	}

	public String getCertNum() {
		return certNum;
	}

	public void setCertNum(String certNum) {
		this.certNum = certNum;
	}

	public String getCertDate() {
		return certDate;
	}

	public void setCertDate(String certDate) {
		this.certDate = certDate;
	}

	public String getCertOrgName() {
		return certOrgName;
	}

	public void setCertOrgName(String certOrgName) {
		this.certOrgName = certOrgName;
	}

	public String getCertDepName() {
		return certDepName;
	}

	public void setCertDepName(String certDepName) {
		this.certDepName = certDepName;
	}

	public String getCertUserFio() {
		return certUserFio;
	}

	public void setCertUserFio(String certUserFio) {
		this.certUserFio = certUserFio;
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

	public String getNameOrg() {
		return nameOrg;
	}

	public void setNameOrg(String nameOrg) {
		this.nameOrg = nameOrg;
	}

	public String getCertUser() {
		return certUser;
	}

	public void setCertUser(String certUser) {
		this.certUser = certUser;
	}

	public String getEmailUser() {
		return emailUser;
	}

	public void setEmailUser(String emailUser) {
		this.emailUser = emailUser;
	}

	public String getPhoneUser() {
		return phoneUser;
	}

	public void setPhoneUser(String phoneUser) {
		this.phoneUser = phoneUser;
	}

	public String getIogvCodeUser() {
		return iogvCodeUser;
	}

	public void setIogvCodeUser(String iogvCodeUser) {
		this.iogvCodeUser = iogvCodeUser;
	}

	public int getModeExec() {
		return modeExec;
	}

	public void setModeExec(int modeExec) {
		this.modeExec = modeExec;
	}

	public String getModeExecValue() {
		if (this.modeExec == 0) {
			this.modeExecValue = "��������";
		} else if (this.modeExec == 1) {
			this.modeExecValue = "����������";
		}
		return modeExecValue;
	}

	public void setModeExecValue(String modeExecValue) {
		this.modeExecValue = modeExecValue;
	}

}
