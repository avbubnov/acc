package iac.grn.infosweb.context.mc.arm;

import iac.cud.infosweb.dataitems.BaseItem;
import iac.grn.infosweb.context.mc.usr.UsrDataModel;
import iac.grn.infosweb.context.mc.usr.UsrDataProvider;
import iac.grn.infosweb.context.mc.usr.UsrStateHolder;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import java.util.Map;

import javax.el.ELException;
import javax.el.Expression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
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
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.richfaces.model.ExtendedFilterField;
import org.richfaces.model.FilterField;
import org.richfaces.model.Modifiable;
import org.richfaces.model.SortField2;
 

@Name("armUsrDataModel")
public class ArmUsrDataModel  extends SerializableDataModel implements Modifiable  {
	
@Logger private Log log;
	
	@In(create=true)
	private ArmUsrDataProvider armUsrDataProvider;
	

	
	private Long currentPk;
	private Map<Long,BaseItem> wrappedData = new HashMap<Long, BaseItem>();
	private List<Long> wrappedKeys = null;

	
	private List<BaseItem> cachedItems=new ArrayList<BaseItem>();
	private Integer rowCount;
	
	private HashMap<String, String> filterColumnValues = new HashMap<String, String>();
	 
	private int flagAction=0;
	/**
	 * 
	 */
	private static final long serialVersionUID = -1956179896877538628L;

	/**
	 * This method never called from framework.
	 * (non-Javadoc)
	 * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
	 */
	@Override
	public Object getRowKey() {
		return currentPk;
	}

	/**
	 * This method normally called by Visitor before request Data Row.
	 */
	@Override
	public void setRowKey(Object key) {
		this.currentPk = (Long) key;
		
	}
	/**
	 * This is main part of Visitor pattern. Method called by framework many times during request processing. 
	 */
	@Override
	public void walk(FacesContext context, DataVisitor visitor, Range range, Object argument) throws IOException {
		
		log.info("ugroupUsrDataModel:walk:01:start");
	
		
		int firstRow = ((SequenceRange)range).getFirstRow();
		int numberOfRows = ((SequenceRange)range).getRows();
		
		log.info("ugroupUsrDataModel:walk:firstRow:"+firstRow);
		log.info("ugroupUsrDataModel:walk:numberOfRows:"+numberOfRows);
		log.info("ugroupUsrDataModel:walk:cachedItems:01:"+(this.cachedItems==null));
		
		wrappedKeys = new ArrayList<Long>();
		//!!!����� ������������ 24.12.13
		//��� ������ � - � filterAction(): setAuditList(null);
	
		
			log.info("ugroupUsrDataModel:walk:cachedItems:02");
			 this.cachedItems=getDataProvider().getItemsByrange(firstRow, numberOfRows, null, true);
		
		log.info("ugroupUsrDataModel:walk:cachedItems:03");
		if(this.cachedItems!=null){
		  for (BaseItem item:cachedItems) {
			wrappedKeys.add(item.getBaseId());
			wrappedData.put(item.getBaseId(), item);  
				 
		    visitor.process(context, item.getBaseId(), argument);
			 
		 }
		}
		log.info("usrDataModel:walk:end");
	}
	public int getRowCount2() {
		log.info("usrDataModel:getRowCount2!!!");
		return getRowCount();
	}
	/**
	 * This method must return actual data rows count from the Data Provider. It is used by pagination control
	 * to determine total number of data items.
	 */
	 // better to buffer row count locally
	@Override
	public int getRowCount() {
		
	log.info("usrDataModel:getRowCount:01");
		
		String remoteAudit = FacesContext.getCurrentInstance().getExternalContext()
		         .getRequestParameterMap()
		         .get("remoteAudit");
		String  auditListCount = FacesContext.getCurrentInstance().getExternalContext()
			        .getRequestParameterMap()
			        .get("auditListCount");
		log.info("usrDataModel:getRowCount:remoteAudit:"+remoteAudit);
		log.info("usrDataModel:getRowCount:auditListCount:"+auditListCount);
		log.info("usrDataModel:getRowCount:flagAction:"+this.flagAction);
		
		if(this.flagAction==0){
			if (rowCount==null) {
				if(auditListCount!=null){
				   rowCount = new Integer(auditListCount);
				 }else{
				   rowCount = 0;
				 }
				log.info("usrDataModel:getRowCount:02:rowCount:"+rowCount);
			}
		}else{
			if (rowCount==null) {
				// ��� selRecAllFact, clRecAllFact, clSelOneFact ��������
				// dataScroller �� ����������, � � ����������
				// rowCount � ��� ������������ ����� param['auditListCount']
				if(remoteAudit==null){
					log.info("usrDataModel:getRowCount:03_+");
					return 0;
				}
				if(remoteAudit!=null &&
					("rowSelectFact".equals(remoteAudit)/*||
				    "selRecAllFact".equals(remoteAudit)||
					"clRecAllFact".equals(remoteAudit)||
					"clSelOneFact".equals(remoteAudit)*/)&&
					auditListCount!=null){
					rowCount = new Integer(auditListCount);
				}else{
					rowCount = new Integer(
							getDataProvider().getRowCount());
				}
				log.info("usrDataModel:getRowCount:03:rowCount:"+rowCount);
			}
		}
		return rowCount.intValue();
	}
	/**
	 * This is main way to obtain data row. It is intensively used by framework. 
	 * We strongly recommend use of local cache in that method. 
	 */
	@Override
	public BaseItem getRowData() {
	//	log.info("getRowData:currentPk:"+currentPk);
		if (currentPk==null) {
			return null;
		} else {
			BaseItem ret = wrappedData.get(currentPk);
			if (ret==null) {
				
				return ret;
			} else {
				return ret;
			}
		}
	}
	
	 public void modify(List<FilterField> filterFields, List<SortField2> sortFields) {
		 log.info("usrDataModel:!!!modify!!!");
	
		if(this.flagAction==1){
		
		}
	 }

	 public void filterAction() {
		 log.info("usrDataModel:filterAction");
		 this.cachedItems = null;
		 this.rowCount=null;
		 this.flagAction=1;
		 
		 ArmUsrStateHolder armUsrStateHolder = (ArmUsrStateHolder)Component.getInstance("armUsrStateHolder", ScopeType.SESSION);
		 armUsrStateHolder.clearFilters();
		 
		 ArmUsrManager ugroupUsrManager = (ArmUsrManager)Component.getInstance("armUsrManager", ScopeType.EVENT);
		 ugroupUsrManager.setAuditList(null);
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
		 
		if (currentPk==null) {
			return false;
		} else {
			 
			if(wrappedKeys!=null){
				 
			}
			
			return true;
		
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
	
	
	
	private String auctionDataModelExpressionString;

	private String auctionDataProviderExpressionString;

	
	/**
	 * This is helper method that is called by framework after model update. 
	 * In must delegate actual database update to Data Provider.
	 */
	@Override
	public void update() {
	
	}
	
	protected void resetDataProvider() {
		this.armUsrDataProvider = null;
	}

	public ArmUsrDataProvider getDataProvider() {
		  log.info("getDataProvider:01");
		if (armUsrDataProvider == null) {
			log.info("getDataProvider:02");
			//dataProvider = lookupInContext(auctionDataProviderExpressionString, AuctionDataProvider.class);
		}
		return armUsrDataProvider;
	}
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
	    
	   public Map<String, String> getFilterColumnValues() {
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
	
}
