package iac.cud.infosweb.entity;

import iac.cud.infosweb.dataitems.BaseItem;

import java.io.Serializable;
import javax.persistence.*;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Role;
import org.jboss.seam.annotations.Roles;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;


/**
 * The persistent class for the ISP_BSS_T database table.
 * 
 */
@Entity
@Table(name="ISP_TEMP_BSS_T")
@Name("clTempBean")

public class IspTempBssT extends BaseItem implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="ID_SRV")
	private Long idSrv;

	private String country;

    @Temporal( TemporalType.DATE)
	private Date created;

	private String email;

	private String fax;

	private String fio;

	private String flat;

	@Column(name="FULL_")
	private String full;

	private String house;

	private String housing;

	@Column(name="IS_EXTERNAL")
	private Long isExternal;

	private String phone;

	private String place;

	@Column(name="\"POSITION\"")
	private String position;

	@Column(name="POSTAL_CODE")
	private String postalCode;

	@Column(name="\"PREFIX\"")
	private String prefix;

	private String region;

	@Column(name="SIGN_OBJECT")
	private String signObject;

	@Column(name="SIGN_OKOGY")
	private String signOkogy;

	private String status;

	@Column(name="UNI_ID")
	private String uniId;

	@Transient
	private String department;
	
    public IspTempBssT() {
    }

    public Long getBaseId() {
  	   return this.idSrv;
  	} 
    
	public Long getIdSrv() {
		return this.idSrv;
	}

	public void setIdSrv(Long idSrv) {
		this.idSrv = idSrv;
	}

	public String getCountry() {
		return this.country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Date getCreated() {
		return this.created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFax() {
		return this.fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getFio() {
		return this.fio;
	}

	public void setFio(String fio) {
		this.fio = fio;
	}

	public String getFlat() {
		return this.flat;
	}

	public void setFlat(String flat) {
		this.flat = flat;
	}

	public String getFull() {
		return this.full;
	}

	public void setFull(String full) {
		this.full = full;
	}

	public String getHouse() {
		return this.house;
	}

	public void setHouse(String house) {
		this.house = house;
	}

	public String getHousing() {
		return this.housing;
	}

	public void setHousing(String housing) {
		this.housing = housing;
	}

	public Long getIsExternal() {
		return this.isExternal;
	}

	public void setIsExternal(Long isExternal) {
		this.isExternal = isExternal;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPlace() {
		return this.place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getPosition() {
		return this.position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getPostalCode() {
		return this.postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getPrefix() {
		return this.prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getRegion() {
		return this.region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getSignObject() {
		return this.signObject;
	}

	public void setSignObject(String signObject) {
		this.signObject = signObject;
	}

	public String getSignOkogy() {
		return this.signOkogy;
	}

	public void setSignOkogy(String signOkogy) {
		this.signOkogy = signOkogy;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUniId() {
		return this.uniId;
	}

	public void setUniId(String uniId) {
		this.uniId = uniId;
	}

	public String getDepartment() {
		return this.department;
	}
    public void setDepartment(String department) {
		this.department = department;
	}

}