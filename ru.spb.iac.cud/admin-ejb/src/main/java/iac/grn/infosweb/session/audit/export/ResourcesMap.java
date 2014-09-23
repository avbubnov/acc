package iac.grn.infosweb.session.audit.export;

public enum ResourcesMap {

	

	USER("user"),
	ROLE("role"),
	IS("is"),
	UGROUP("ugroup"),
	RES("res"),
	AUDIT_SYS("audit_sys"),
	AUDIT_USER("audit_user"),
	//AUDIT_TOKEN("audit_token"),
	PROC_ARCH_AUDIT_SYS("proc_arch_audit_sys"),
	PROC_ARCH_AUDIT_USER("proc_arch_audit_user"),
	PROC_ARCH_TOKEN("proc_arch_token"),
	PROC_BIND_UNBIND("proc_bind_unbind"),
	PROC_BIND_NOACT("proc_bind_noact"),
	NAVIG("navig_tree"),
	CONF_PARAM("conf_param"),
	BINDING_IOGV("binding_iogv");
	
    private String code;
	
    ResourcesMap(String code){
		this.code=code;
    }
      
    public String getCode(){
    	return this.code;
    }  
    public void setCode(String code){
    	this.code=code;
    }
}
