package probation;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Token{
	
  private String id;

  private String dateIssue;

  private String dateExpire;
  
  private String authType;
  
  public String getId() {
     return id;
  }
  public void setId(String id) {
    this.id= id;
  }

  public String getDateIssue() {
    return dateIssue;
  }
  public void setDateIssue(String dateIssue) {
    this.dateIssue = dateIssue;
  }

  public String getDateExpire() {
	    return dateExpire;
  }
  public void setDateExpire(String dateExpire) {
	    this.dateExpire = dateExpire;
  }
  
  public String getAuthType() {
	    return authType;
  }
  public void setAuthType(String authType) {
	    this.authType = authType;
  }
} 