package _ru.spb.iac.cud.items;

import java.util.List;


public class Role {
	
    private String idRole;
    private String name;
    private String description;
    
    public Role() {
    }

    public String getIdRole() {
        return idRole;
    }
    public void setIdRole(String idRole) {
        this.idRole = idRole;
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
