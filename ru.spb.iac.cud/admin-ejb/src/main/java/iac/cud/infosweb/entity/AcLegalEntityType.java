package iac.cud.infosweb.entity;

import java.io.Serializable;
import javax.persistence.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * �������� ��� �����������
 * @author bubnov
 *
 */
@Entity
@Table(name="TOR_BSS_T")
public class AcLegalEntityType implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="ID_SRV")
	private Long id;

	@Column(name="FULL_")
	private String name;

	@Column(name="SIGN_OBJECT")
	private String signObject;
	
//	@Column(name="SORT_ORDER")
//	private Long sortOrder;

	//@OneToMany(mappedBy="acLegalEntityType2", fetch=FetchType.EAGER)
	@OneToMany(mappedBy="acLegalEntityType2")
	private List<AcOrganization> acOrganizations;

    public AcLegalEntityType() {
    }

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getSignObject() {
		return this.signObject;
	}

	public void setSignObject(String signObject) {
		this.signObject = signObject;
	}
/*
	public Long getSortOrder() {
		return this.sortOrder;
	}
	public void setSortOrder(Long sortOrder) {
		this.sortOrder = sortOrder;
	}*/

	public List<AcOrganization> getAcOrganizations() {
		return this.acOrganizations;
	}

	public void setAcOrganizations(List<AcOrganization> acOrganizations) {
		this.acOrganizations = acOrganizations;
	}
	
}