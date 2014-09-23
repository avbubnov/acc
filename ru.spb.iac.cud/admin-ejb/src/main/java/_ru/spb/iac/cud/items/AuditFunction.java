package _ru.spb.iac.cud.items;

import java.util.Date;

public class AuditFunction {

	private String idFunction;
	
	private Date dateFunction;
	
	public void Function() {
	}
	
	public String getIdFunction() {
        return idFunction;
    }
    public void setIdFunction(String idFunction) {
        this.idFunction = idFunction;
    }
    
    public Date getDateFunction() {
        return dateFunction;
    }

    public void setDateFunction(Date dateFunction) {
        this.dateFunction = dateFunction;
    }
}
