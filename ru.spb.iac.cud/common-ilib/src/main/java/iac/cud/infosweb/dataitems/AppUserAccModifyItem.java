package iac.cud.infosweb.dataitems;

import java.io.Serializable;
import java.util.List;

public class AppUserAccModifyItem extends AppItem {

	// полученные
	private String login;
	private String password;
	private String certificate;

	// исходные
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

	public AppUserAccModifyItem() {
	}

	public AppUserAccModifyItem(Long idApp, String created, int status,
			String orgName, String usrFio, String rejectReason, String comment,

			String login, String password, String certificate,

			String nameOrg, Long idUser, String loginUser, String fioUser,
			String positionUser, String nameDep, String certUser,
			String iogvCodeUser, String emailUser, String phoneUser) {
		super(idApp, created, status, orgName, usrFio, rejectReason, comment);
		this.login = login;
		this.password = password;
		this.certificate = certificate;

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

	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
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

}
