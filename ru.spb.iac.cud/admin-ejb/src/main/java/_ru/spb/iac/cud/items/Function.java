package _ru.spb.iac.cud.items;

import java.util.Date;

public class Function {

	private String idFunction;
	
	private String name;
	private String description;
	 
	public void Function() {
	}
	
	public String getIdFunction() {
        return idFunction;
    }
    public void setIdFunction(String idFunction) {
        this.idFunction = idFunction;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
