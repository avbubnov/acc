package _ru.spb.iac.cud.items;

import java.util.List;
import java.util.ArrayList;


public class UserAttributes {
    private List<Attribute> attributes;
  //  private List<String> roles;

    public UserAttributes() {
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    /*   public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }*/

    @Override
    public String toString() {
        return "{user " + attributes /*+ " " + roles + "}"*/;
    }
}
