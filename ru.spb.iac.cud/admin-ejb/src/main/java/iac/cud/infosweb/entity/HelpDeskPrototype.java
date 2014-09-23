package iac.cud.infosweb.entity;

import java.io.Serializable;
import javax.persistence.*;

import java.util.Date;


/**
 * The persistent class for the HELP_DESK_PROTOTYPE database table.
 * 
 */
//@Entity
//@Table(name="HELP_DESK_PROTOTYPE")
public class HelpDeskPrototype implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="ID_HELP")
	private Long idHelp;

	private String author;

    @Temporal(TemporalType.TIMESTAMP)
	private Date created;

    @ManyToOne
   	@JoinColumn(name="CREATOR", insertable=false, updatable=false)
   	private AcUser crtUser;
 
	private Long creator;

	@Column(name="ID_APP")
	private Long idApp;
	
    @Temporal(TemporalType.TIMESTAMP)
	@Column(name="DONE_DATE")
	private Date doneDate;

	private String email;

	@Column(name="IS_DONE")
	private Long isDone;

	@Column(name="\"MESSAGE\"")
	private String message;

	private Long modificator;

    @Temporal(TemporalType.DATE)
	private Date modified;

	private String phone;

	@Column(name="\"POSITION\"")
	private String position;

	@Transient
	private Boolean isDoneValue;
	
    public HelpDeskPrototype() {
    }

	public Long getIdHelp() {
		return this.idHelp;
	}

	public void setIdHelp(Long idHelp) {
		this.idHelp = idHelp;
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getCreated() {
		return this.created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Long getCreator() {
		return this.creator;
	}

	public void setCreator(Long creator) {
		this.creator = creator;
	}

	public Date getDoneDate() {
		return this.doneDate;
	}

	public void setDoneDate(Date doneDate) {
		this.doneDate = doneDate;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getIsDone() {
		return this.isDone;
	}

	public void setIsDone(Long isDone) {
		this.isDone = isDone;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getModificator() {
		return this.modificator;
	}

	public void setModificator(Long modificator) {
		this.modificator = modificator;
	}

	public Date getModified() {
		return this.modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPosition() {
		return this.position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public Boolean getIsDoneValue() {
		if(this.isDone!=null && this.isDone.equals(1L)){
		  return true;
		}else{
		  return false;
		}
	}

	public void setIsDoneValue(Boolean isDoneValue) {
		this.isDoneValue = isDoneValue;
	}
	
	public AcUser getCrtUser() {
		return this.crtUser;
	}
	public void setCrtUser(AcUser crtUser) {
		this.crtUser = crtUser;
	}
	
	public Long getIdApp() {
		return this.idApp;
	}

	public void setIdApp(Long idApp) {
		this.idApp = idApp;
	}

}