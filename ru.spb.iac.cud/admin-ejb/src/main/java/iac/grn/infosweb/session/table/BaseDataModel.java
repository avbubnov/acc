package iac.grn.infosweb.session.table;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.ajax4jsf.model.SerializableDataModel;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.richfaces.model.FilterField;
import org.richfaces.model.SortField2;

@Name("baseDataModel")
public class BaseDataModel<T, U> extends SerializableDataModel {

	@Logger protected Log log;
	
	private U currentPk;
	private Map<U, T> wrappedData = new HashMap<U, T>();
	private List<U> wrappedKeys = null;

	
	private List<T> cachedItems=new ArrayList<T>();
	private Integer rowCount;
	
	private HashMap<String, String> filterColumnValues = new HashMap<String, String>();
	 
	private int flagAction=0;
 
	private static final long serialVersionUID = -1956179896877538628L;

	private String modelType=null;
	
	public void init(String modelType){
		log.info("tableDataModel:init:modelType:"+modelType);
		this.modelType=modelType;
	}
	
	public String getModelType(){
		return this.modelType;
	}
	/**
	 * This method never called from framework.
	 * (non-Javadoc)
	 * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
	 */
	@Override
	public Object getRowKey() {
	//	log.info("getRowKey:currentPk:"+currentPk);
		return currentPk;
	}

	/**
	 * This method normally called by Visitor before request Data Row.
	 */
	@Override
	public void setRowKey(Object key) {
	//	log.info("setRowKey:key:"+key);
		this.currentPk = (U) key;
		
	}
	/**
	 * This is main part of Visitor pattern. Method called by framework many times during request processing. 
	 */
	@Override
	public void walk(FacesContext context, DataVisitor visitor, Range range, Object argument) throws IOException {
		
		log.info("walk:01:start");
	/*	if(componentState!=null){
			log.info("walk:01:2");
			Range range2=componentState.getRange();
			int firstRow = ((SequenceRange)range2).getFirstRow();
			int numberOfRows = ((SequenceRange)range2).getRows();
			
			log.info("walkt:firstRow:"+firstRow);
			log.info("walk:numberOfRows:"+numberOfRows);
		}else{
			log.info("walk:01:3");
		}*/
		
	/*	try{
			throw new NullPointerException();
		}catch(Exception e){
			e.printStackTrace(System.out);
		}*/
		
		int firstRow = ((SequenceRange)range).getFirstRow();
		int numberOfRows = ((SequenceRange)range).getRows();
		
		log.info("walk:firstRow:"+firstRow);
		log.info("walk:numberOfRows:"+numberOfRows);
		log.info("walk:cachedItems:01"+(this.cachedItems==null));
		
		wrappedKeys = new ArrayList<U>();
		if(this.cachedItems==null){
			log.info("walk:cachedItems:02");
			 this.cachedItems=findObjects(firstRow, numberOfRows, null, true, 
					 modelType);
			/* this.cachedItems=getDataProvider().getItemsByrange(firstRow, numberOfRows, null, true, 
					 ServiceReestr.JournInpContr, "journDataModel",
					 ServiceReestrPro.JournInpContr.name());*/
		}
		log.info("walk:cachedItems:03");
		if(this.cachedItems!=null){
		  for (T item:cachedItems) {
			  
		//	log.info("walk:cachedItems:04:getId:"+getId(item));
			  
			wrappedKeys.add(getId(item));
			wrappedData.put(getId(item), item);  
		//	wrappedKeys.add(item.getSessionId());
		//	wrappedData.put(item.getSessionId(), item);
			//log.info("visitor:start:"+item.getId().toString());
		    visitor.process(context, getId(item), argument);
		  //  log.info("walk:cachedItems:05");
		 }
		}
		log.info("walk:end");
	}
	public int getNumRecords(String modelType){
		return 0;
	}
	
	public U getId(T object){
		return null;
	}
	
	public List<T> findObjects(int firstRow, int numberOfRows, String sortField, 
            boolean ascending, String modelType){
		return null;
	}
	
	public int getRowCount2() {
		log.info("auditDataModel:getRowCount2!!!");
		return getRowCount();
	}
	/**
	 * This method must return actual data rows count from the Data Provider. It is used by pagination control
	 * to determine total number of data items.
	 */
	 // better to buffer row count locally
	@Override
	public int getRowCount() {
		
	
		log.info("auditDataModel:getRowCount:01");
		/*if(componentState!=null){
			log.info("getRowCount:01:2");
			Range range=componentState.getRange();
			int firstRow = ((SequenceRange)range).getFirstRow();
			int numberOfRows = ((SequenceRange)range).getRows();
			
			log.info("getRowCount:firstRow:"+firstRow);
			log.info("getRowCount:numberOfRows:"+numberOfRows);
		}else{
			log.info("getRowCount:01:3");
		}*/
		
		/*if (rowCount==null) {
			rowCount = new Integer(getDataProvider().getRowCount(filterColumnValues));
			log.info("getRowCount:02:rowCount:"+rowCount);
			return rowCount.intValue();
		} else {
			log.info("getRowCount:03:rowCount:"+rowCount);
			return rowCount.intValue();
		}*/
		String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
		         .getRequestParameterMap()
		         .get("remoteAudit");
		String  auditListCount = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("auditListCount");
		log.info("auditDataModel:getRowCount:remoteAudit:"+remoteAudit);
		log.info("auditDataModel:getRowCount:auditListCount:"+auditListCount);
	
		if(this.flagAction==0){
			if (rowCount==null) {
				if(auditListCount!=null){
				   rowCount = new Integer(auditListCount);
				 }else{
				   rowCount = 0;
				 }
				log.info("getRowCount:02:rowCount:"+rowCount);
			}
		}else{
			if (rowCount==null) {
				// При selRecAllFact, clRecAllFact, clSelOneFact запросах
				// dataScroller не рендерится, а в параметрах
				// rowCount и так определяется через param['auditListCount']
				if((remoteAudit.equals("rowSelectFact")/*||
				    remoteAudit.equals("selRecAllFact")||
					remoteAudit.equals("clRecAllFact")||
					remoteAudit.equals("clSelOneFact")*/)&&
					auditListCount!=null){
					rowCount = new Integer(auditListCount);
				}else{
				/*	rowCount = new Integer(
						getDataProvider().getRowCount(ServiceReestr.JournInpContr, "journDataModel",
								 ServiceReestrPro.JournInpContr.name()));*/
					rowCount = getNumRecords(modelType);
				}
				log.info("getRowCount:03:rowCount:"+rowCount);
			}
		}
		return rowCount.intValue();
	}
	/**
	 * This is main way to obtain data row. It is intensively used by framework. 
	 * We strongly recommend use of local cache in that method. 
	 */
	@Override
	public T getRowData() {
	//	log.info("getRowData:currentPk:"+currentPk);
		if (currentPk==null) {
			return null;
		} else {
			T ret = wrappedData.get(currentPk);
			if (ret==null) {
				//ret = getDataProvider().getAuctionItemByPk(currentPk);
				//wrappedData.put(currentPk, ret);
			//	log.info("getRowData:currentPk:ret==null!!");
				return ret;
			} else {
				return ret;
			}
		}
	}
	//@Override
	 public void modify(List<FilterField> filterFields, List<SortField2> sortFields) {
		 log.info("!!!modify!!!");
		// this.filterFields = filterFields;
		if(this.flagAction==1){
		// appendFilters(filterFields,FacesContext.getCurrentInstance());
		}// this.cachedItems = null;
	 }
/*
	 private void appendFilters(List<FilterField> filterFields, FacesContext context) {
        
		 if (filterFields != null && !filterFields.isEmpty()) {
			 
		//	 filterFieldsHM.clear();
			 
        	      for (FilterField filterField : filterFields) {
        	    	  
                       //для rich:columns 
        	    	    String propertyName = getPropertyName(context, filterField.getExpression());
                       
        	    	    log.info("appendFilters:1:"+(filterField.getExpression().getExpressionString()));
        	    	     
        	    	     //  для rich:column 
        	    	     //  String propertyName = (filterField.getExpression().getExpressionString()).replaceAll("[#|$]{1}\\{.*?\\.", "").replaceAll("\\}", "");
        	    	    
        	    	     String filterValue=null;
        	    	     try{ 
        	    	    	 log.info("appendFilters:filterField.getClass:"+filterField.getClass());
        	    	         filterValue = ((ExtendedFilterField) filterField).getFilterValue();
                        }catch(Exception e){
                        	log.info("appendFilters:error:"+e);
                        }
        	    	     
        	    	   //log.info("appendFilters:filterColumnValue:"+filterColumnValues.get(propertyName));
        	    	     
                         log.info("appendFilters:propertyName:"+propertyName);
                         log.info("appendFilters:filterValue:"+filterValue);
                         
                      //   if (propertyName!=null && filterColumnValues.get(propertyName)!=null && filterColumnValues.get(propertyName).length() != 0 ) {
                         if (propertyName!=null && filterValue!=null && filterValue.length() != 0 ) {
                                 	 
                        	 filterColumnValues.put(propertyName, filterValue);   
                        	 //filterColumnValues.put(propertyName, filterColumnValues.get(propertyName));   
                                  
                         }
                 }
         }
    }
	 
	 
	 private String getPropertyName(FacesContext facesContext, Expression expression) {
         try {
                 return (String) ((ValueExpression) expression).getValue(facesContext.getELContext());
         } catch (ELException e) {
                 throw new FacesException(e.getMessage(), e);
         }
    }
	 */
	 public void filterAction() {
		 log.info("filterAction");
		 this.cachedItems = null;
		 this.rowCount=null;
		 this.flagAction=1;
		 
		/* JournStateHolder journStateHolder = (JournStateHolder)Component.getInstance("journStateHolder",ScopeType.SESSION);
		 journStateHolder.clearFilters("journDataModel");*/
	}
	/**
	 * Unused rudiment from old JSF staff.
	 */
	@Override
	public int getRowIndex() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Unused rudiment from old JSF staff.
	 */
	@Override
	public Object getWrappedData() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Never called by framework.
	 */
	@Override
	public boolean isRowAvailable() {
		//log.info("isRowAvailable1");
		if (currentPk==null) {
			return false;
		} else {
			//log.info("isRowAvailable2");
			if(wrappedKeys!=null){
				//log.info("isRowAvailable3:"+(wrappedKeys.contains(currentPk)));
			}
			
			return true;
		//	return getDataProvider().hasAuctionItemByPk(currentPk);
		}
	}

	/**
	 * Unused rudiment from old JSF staff.
	 */
	@Override
	public void setRowIndex(int rowIndex) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Unused rudiment from old JSF staff.
	 */
	@Override
	public void setWrappedData(Object data) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method suppose to produce SerializableDataModel that will be serialized into View State and used on a post-back.
	 * In current implementation we just mark current model as serialized. In more complicated cases we may need to 
	 * transform data to actually serialized form.
	 */
	public  SerializableDataModel getSerializableDataModel(Range range) {
		
		log.info("getSerializableModel");
		
		if (wrappedKeys!=null) {
			return this; 
		} else {
			return null;
		}
	}
	
	private <V> V lookupInContext(String expression, Class<? extends V> c) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Application application = facesContext.getApplication();
		return c.cast(application.evaluateExpressionGet(facesContext, MessageFormat.format("#'{'{0}'}'", expression), c));
	}
	
	private String auctionDataModelExpressionString;

	private String auctionDataProviderExpressionString;

	
	/**
	 * This is helper method that is called by framework after model update. 
	 * In must delegate actual database update to Data Provider.
	 */
	@Override
	public void update() {
	/*	AuditDataModel auctionDataModel = lookupInContext(auctionDataModelExpressionString, AuditDataModel.class);
		Object savedKey = getRowKey();
		for (Integer key : wrappedKeys) {
			auctionDataModel.setRowKey(key);
			auctionDataModel.getRowData().setBid(wrappedData.get(key).getBid());
		}
		setRowKey(savedKey);
		//getDataProvider().update();
		
		this.wrappedData.clear();
		this.wrappedKeys.clear();
		resetDataProvider();*/
	}
	
	/*
	protected void resetDataProvider() {
		this.journDataProvider = null;
	}

	public JournDataProvider getDataProvider() {
		  log.info("getDataProvider:01");
		if (journDataProvider == null) {
			log.info("getDataProvider:02");
			}
		return journDataProvider;
	}*/
	
	public String getAuctionDataModelExpressionString() {
		return auctionDataModelExpressionString;
	}
	public void setAuctionDataModelExpressionString(
			String auctionDataModelExpressionString) {
		this.auctionDataModelExpressionString = auctionDataModelExpressionString;
	}
	public String getAuctionDataProviderExpressionString() {
		return auctionDataProviderExpressionString;
	}
	public void setAuctionDataProviderExpressionString(
			String auctionDataProviderExpressionString) {
		this.auctionDataProviderExpressionString = auctionDataProviderExpressionString;
	}
	    
	   public HashMap<String, String> getFilterColumnValues() {
		   if(filterColumnValues!=null){
		       log.info("getFilterColumnValues:01:"+filterColumnValues.size());
		   }else{
			   log.info("getFilterColumnValues:02:");
		   }
	        return filterColumnValues;
	    }
	   public void setFilterColumnValues(HashMap<String, String> filterColumnValues){
		   if(filterColumnValues!=null){
		       log.info("setFilterColumnValues:01:"+filterColumnValues.size());
		   }else{
			   log.info("setFilterColumnValues:02:");
		   };
		   this.filterColumnValues=filterColumnValues;
	   }
	/*   public DataComponentState getComponentState(){
		   return this.componentState;
	   }
	   public void setComponentState(DataComponentState componentState){
		   log.info("setComponentState!");
		   this.componentState=componentState;
	   }*/
	
}

