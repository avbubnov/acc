package iac.grn.infosweb.session.audit.export;

public enum ActionsMap {

	CREATE("create"),
	UPDATE("update"),
	UPDATE_USER("update_user"),
	UPDATE_ROLE("update_role"),
	UPDATE_GROUP("update_group"),
	UPDATE_ADMIN_SYS("update_admin_sys"),
	DELETE("delete"),
	START("start"),
	STOP("stop"),
	PAUSE("pause"),
	SET_PARAM("set_param");
	
    private String code;
	
    ActionsMap(String code){
		this.code=code;
    }
      
    public String getCode(){
    	return this.code;
    }  
    public void setCode(String code){
    	this.code=code;
    }
}
