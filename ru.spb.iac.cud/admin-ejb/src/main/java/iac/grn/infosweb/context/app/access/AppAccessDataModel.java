package iac.grn.infosweb.context.app.access;

import iac.cud.infosweb.dataitems.BaseItem;
import iac.grn.infosweb.session.table.BaseDataModel;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
//import org.ajax4jsf.model.DataComponentState;

@Name("appAccessDataModel")
public class AppAccessDataModel extends BaseDataModel<BaseItem, Long>  {
	
	@In(create=true)
	private AppAccessManager appAccessManager;

	@Override
	public int getNumRecords(String modelType) {
		
		int ri=0;
		Long rL=appAccessManager.getAuditCount();
		if(rL!=null){
			ri = Integer.parseInt(rL.toString());
		}
		return ri;
	}

	@Override
	public Long getId(BaseItem object) {
		// TODO Auto-generated method stub
		//log.info("AppAccessDataModel:getId:"+object.getBaseId());
		return object.getBaseId();
	}

	@Override
	public List<BaseItem> findObjects(int firstRow, int numberOfRows,
			String sortField, boolean ascending, String modelType) {
		
    	return appAccessManager.getAuditList(firstRow, numberOfRows);  
	}
	
	public void filterAction() {
		 log.info("appAccessDataModel:filterAction");
	     super.filterAction();
		 
	     AppAccessStateHolder appAccessStateHolder = (AppAccessStateHolder)Component.getInstance("appAccessStateHolder", ScopeType.SESSION);
		 appAccessStateHolder.clearFilters();
	}

}

