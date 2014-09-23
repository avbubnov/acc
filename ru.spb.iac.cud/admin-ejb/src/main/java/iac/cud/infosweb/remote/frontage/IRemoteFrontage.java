package iac.cud.infosweb.remote.frontage;

import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.Context;
import javax.naming.InitialContext;

import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.local.service.IHLocal;
import iac.cud.infosweb.local.service.ReestrItem;



@Stateless
//@LocalBinding(jndiBinding="infoscud.IRemoteFrontage.local")
//@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class IRemoteFrontage implements IRemoteFrontageLocal{
	
    private Context ctx=null;
	
	@PostConstruct
    public void init() {
   	 try{
   		 if(ctx==null){
             ctx = new InitialContext(); 
            }
        }catch(Exception e){
        }
     }
    @PreDestroy
    public void remove() {
   	 try{
   		 if(ctx!=null){
             ctx.close(); 
            }
        }catch(Exception e){
        }
     }
	
	public BaseParamItem run(BaseParamItem paramMap)throws Exception{
		
		BaseParamItem jpi = new BaseParamItem();
		String service=null;
		
		String gtype = (String)paramMap.get("gtype");
				
		System.out.println("IRemoteFrontage:run:gtype:"+gtype);
		
		String url = ReestrItem.getUrl(gtype);
		
		/*
		 if(gtype.equals(ConstType.JOURN)){
			if(gsubtype.equals(ConstSubType.CHANGEDATA)){
				service=ServiceReestr.JournChangeData;
			}else if(gsubtype.equals(ConstSubType.CORRECT)){
				service=ServiceReestr.JournCorrect;
			}else if(gsubtype.equals(ConstSubType.ERRDATAFLCONTR)){
				service=ServiceReestr.JournErrDataFLContr;
			}
		}else if(gtype.equals(ConstType.OPSTAT)){
			
		}else if(gtype.equals(ConstType.PERSTAT)){
			
		}*/
		
		if(url==null){
			return null;
		}
		
		return ((IHLocal)ctx.lookup(url)).run(paramMap);
	}
	
}
