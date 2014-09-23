package iac.cud.infosweb.dataitems;

import java.io.Serializable;
import java.util.List;

public class AppUserModifyItem extends AppItem {

	// полученные
	private String surname;
	private String name;
	private String patronymic;
	private String iogvCode;
	private String position;
	private String email;
	private String phone;
	private String certificate;
	private String departament;

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

	public AppUserModifyItem() {
	}

	public AppUserModifyItem(Long idApp, String created, int status,
			String orgName, String usrFio, String rejectReason, String comment,

			String surname, String name, String patronymic, String iogvCode,
			String position, String email, String phone, String certificate,
			String departament,

			String nameOrg, Long idUser, String loginUser, String fioUser,
			String positionUser, String nameDep, String certUser,
			String iogvCodeUser, String emailUser, String phoneUser) {
		super(idApp, created, status, orgName, usrFio, rejectReason, comment);
		this.surname = surname;
		this.name = name;
		this.patronymic = patronymic;
		this.iogvCode = iogvCode;
		this.position = position;
		this.email = email;
		this.phone = phone;
		this.certificate = certificate;
		this.departament = departament;

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

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPatronymic() {
		return patronymic;
	}

	public void setPatronymic(String patronymic) {
		this.patronymic = patronymic;
	}

	public String getIogvCode() {
		return iogvCode;
	}

	public void setIogvCode(String iogvCode) {
		this.iogvCode = iogvCode;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public String getDepartament() {
		return departament;
	}

	public void setDepartament(String departament) {
		this.departament = departament;
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
