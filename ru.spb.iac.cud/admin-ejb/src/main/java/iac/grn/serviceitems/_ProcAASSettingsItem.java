package iac.grn.serviceitems;


public class _ProcAASSettingsItem {

	private String directory;
	 
	private String url;
	
	private String interrupt;
	
	private String directory_output;
	
	private String yesterday_only;
	
	private String current_today;
    
	private String run_eas;
	
	private String run_meta;
	
	public String getDirectory(){
		return this.directory;
	}
	public void setDirectory(String directory){
		this.directory=directory;
	}
	
	public String getUrl(){
		return this.url;
	}
	public void setUrl(String url){
		this.url=url;
	}
	
	public String getInterrupt(){
		return this.interrupt;
	}
	public void setInterrupt(String interrupt){
		this.interrupt=interrupt;
	}
	
	public String getDirectory_output(){
		return this.directory_output;
	}
	public void setDirectory_output(String directory_output){
		this.directory_output=directory_output;
	}
	
	public String getYesterday_only(){
		return this.yesterday_only;
	}
	public void setYesterday_only(String yesterday_only){
		this.yesterday_only=yesterday_only;
	}
	
	public String getCurrent_today(){
		return this.current_today;
	}
	public void setCurrent_today(String current_today){
		this.current_today=current_today;
	}
	
	public String getRun_eas(){
		return this.run_eas;
	}
	public void setRun_eas(String run_eas){
		this.run_eas=run_eas;
	}
	
	public String getRun_meta(){
		return this.run_meta;
	}
	public void setRun_meta(String run_meta){
		this.run_meta=run_meta;
	}
}
