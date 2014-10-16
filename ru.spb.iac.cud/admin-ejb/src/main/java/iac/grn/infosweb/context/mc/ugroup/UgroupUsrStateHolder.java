package iac.grn.infosweb.context.mc.ugroup;

import java.util.ArrayList;
import java.util.HashMap; import java.util.Map;
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


@Name("ugroupUsrStateHolder")
@Scope(ScopeType.SESSION)
@AutoCreate
public class UgroupUsrStateHolder {
	
	   @Logger private Log log;
	
       private Map<String, String> sortOrders = new HashMap<String, String>();
       private Map<String, String> columnFilterValues = new HashMap<String, String>();
      
       private List <String> auditFieldListSelect = new ArrayList<String>();
       
       private int pageNumber;
       
       @Create
       public void create() {
    	   log.info("datatableStateHolder:create");
    	
    			   
        }
       
       public List <String> getAuditFieldListSelect() {
    	   return this.auditFieldListSelect;
       }
       public void setAuditFieldListSelect(List <String> auditFieldListSelect) {
    	   this.auditFieldListSelect=auditFieldListSelect;
       }
       public Map<String, String> getColumnFilterValues() {
    	 
           return columnFilterValues;
       }
       public void setColumnFilterValues(HashMap<String, String> columnFilterValues) {
    	  
    	   this.columnFilterValues = columnFilterValues;
       }
       public Map<String, String> getSortOrders() {
               return sortOrders;
       }
       public void setSortOrders(HashMap<String, String> sortOrders) {
                this.sortOrders = sortOrders;
       }
       public void clearFilters(){
    	   log.info("clearFilters:01");
    	   if(columnFilterValues!=null){
           		for(Iterator<Map.Entry<String, String>> it = columnFilterValues.entrySet().iterator(); it.hasNext();)
    			{
    			      Map.Entry<String, String> me = it.next();
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
       
       public int getPageNumber() {  
     		return this.pageNumber; 
        }       
        public void setPageNumber(int pageNumber) { 
     		this.pageNumber= pageNumber; 
        } 

        public void resetPageNumber() { 
     		this.pageNumber=1; 
        } 
        
       
    }