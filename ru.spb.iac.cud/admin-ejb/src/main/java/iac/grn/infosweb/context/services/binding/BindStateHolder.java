package iac.grn.infosweb.context.services.binding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.log.Log;
import org.richfaces.model.SortOrder;


@Name("bindStateHolder")
@Scope(ScopeType.SESSION)
@AutoCreate
public class BindStateHolder {
	
	   @Logger private Log log;
	
       private HashMap<String, String> sortOrders = new HashMap<String, String>();
       private HashMap<String, String> columnFilterValues = new HashMap<String, String>();
      
     /*  private String testSelect="";
       public String getTestSelect(){
    	   log.info("datatableStateHolder:getTestSelect:testSelect:"+testSelect);
    	   return testSelect;
       }
       public void setTestSelect(String testSelect){
    	   log.info("datatableStateHolder:setTestSelect:testSelect:"+testSelect);
    	   this.testSelect= testSelect;
       }*/
       
       private List <String> auditFieldListSelect = new ArrayList<String>();
       
       @Create
       public void create() {
    	   log.info("datatableStateHolder:create");
    	
    	 /*  auditFieldListSelect.add("extendedTimestamp");
   		   auditFieldListSelect.add("objectName");
		   auditFieldListSelect.add("osUser");
		   auditFieldListSelect.add("osHost");
		   auditFieldListSelect.add("sessionId");*/
   		   
        }
       
       public List <String> getAuditFieldListSelect() {
    	   return this.auditFieldListSelect;
       }
       public void setAuditFieldListSelect(List <String> auditFieldListSelect) {
    	   this.auditFieldListSelect=auditFieldListSelect;
       }
       public HashMap<String, String> getColumnFilterValues() {
    	//   log.info("getColumnFilterValues:01");
           return columnFilterValues;
       }
       public void setColumnFilterValues(HashMap<String, String> columnFilterValues) {
    	//   log.info("setColumnFilterValues:02"); 
    	   this.columnFilterValues = columnFilterValues;
       }
       public HashMap<String, String> getSortOrders() {
               return sortOrders;
       }
       public void setSortOrders(HashMap<String, String> sortOrders) {
                this.sortOrders = sortOrders;
       }
       public void clearFilters(){
    	   log.info("clearFilters:01");
    	   if(columnFilterValues!=null){
          	//	log.info("clearFilters:02");
          		for(Iterator<Map.Entry<String, String>> it = columnFilterValues.entrySet().iterator(); it.hasNext();)
    			{
    			      Map.Entry<String, String> me = it.next();
    			      log.info("BindStateHolder:clearFilters:me.getKey:"+me.getKey());
    	     		  log.info("BindStateHolder:clearFilters: me.getValue:"+me.getValue());
    	     		  if(me.getValue()==null||me.getValue().isEmpty()||me.getValue().equals("#-1#")){
    	     			  log.info("Ahtung!!!");
    	     			  it.remove();
    	     		   }
    			}
          }
       }
       public void sort(String itemField){
    	   log.info("sort:itemField:"+itemField);
    	   
    	   String value=null;
    	   
    	   if(this.sortOrders.containsKey(itemField)){
    		   value=this.sortOrders.get(itemField);
    		   if(value!=null && value.equals("asc")){
    			   this.sortOrders.put(itemField, "desc");
    		   }else{
    			   this.sortOrders.put(itemField, "asc");
    		   }
    	   }else{
    		   this.sortOrders.clear();
    		   this.sortOrders.put(itemField, "asc");  
    	   }
    	   log.info("sort:02");
       }
       
       public void sort(){
    	   String itemField = FacesContext.getCurrentInstance().getExternalContext()
  	             .getRequestParameterMap()
  	             .get("itemField");
  	 
    	   log.info("sort:itemField:"+itemField);
    	   
    	   String value=null;
    	   
    	   if(this.sortOrders.containsKey(itemField)){
    		   value=this.sortOrders.get(itemField);
    		   if(value!=null && value.equals("asc")){
    			   this.sortOrders.put(itemField, "desc");
    		   }else{
    			   this.sortOrders.put(itemField, "asc");
    		   }
    	   }else{
    		   this.sortOrders.clear();
    		   this.sortOrders.put(itemField, "asc");  
    	   }
    	   log.info("sort:02");
       }
    }