package probation;

import iac.cud.infosweb.entity.UcCertReestr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

@Name("proba")
public class Probation {
	
	
	 @In 
	 EntityManager entityManager;
	
	    private String name1="/img/msg!info.png";

	    private String name2;
	    
	    private List<String> nameList;
	    
	    private byte[] fileData;
	    
	    public String getName1() {
	    	System.out.println("Probation:getName1:name1:"+name1);
	        return this.name1;
	    }
	    public void setName1(String name1){
	    	System.out.println("Probation:setName1:name1:"+name1);
	    	this.name1=name1;
	    }
	    public String getName2() {
	    	System.out.println("Probation:getName2:name2:"+name2);
	        return this.name2;
	    }
	    public void setName2(String name2){
	    	System.out.println("Probation:setName2:name2:"+name2);
	    	this.name2=name2;
	    }
	    public int evalute() {
	    	int result=0;
	    	System.out.println("Probation:evalute:01");
	    	String remote = FacesContext.getCurrentInstance().getExternalContext()
	    		         .getRequestParameterMap()
	    		         .get("remote");
	    	System.out.println("Probation:remote:"+remote);
	    	
	    	if(remote!=null){
	    		result=Integer.parseInt(remote);
	    	}
	        return result;
	    }
	    public List<String> getNameList() {
	    	System.out.println("Probation:getNameList:01");
	    	if(this.nameList==null){
	    		System.out.println("Probation:getNameList:02");
	    		this.nameList=new ArrayList<String>();
	    		this.nameList.add("text1");
	    		this.nameList.add("text2");
	    		this.nameList.add("text3");
	    	}
	        return this.nameList;
	    }
	    public void setNameList(List<String> nameList){
	    	System.out.println("Probation:setNameList");
	    	this.nameList=nameList;
	    }
		public byte[] getFileData() {
			return fileData;
		}
		public void setFileData(byte[] fileData) {
			
			System.out.println("Probation:setFileData:01:"+(fileData==null));
			
			this.fileData = fileData;
		}
		
		 private  List<UploadItem> list = new ArrayList<UploadItem>();
			
	     public void listener(UploadEvent event) {
	       	 System.out.println("===saveSost-listener-1="+(event!=null));
	       	try{
	       	// UploadItem item = event.getUploadItem();
	         list =event.getUploadItems();
	        
	       	  System.out.println("===saveSost-listener-2-");
	        	

	       	}catch(Exception e){
	             System.out.println("===saveSost-listener-error="+e);
	         } 
	     
	        
	        System.out.println("===saveSost-listener-3-");
	         }
		
       public void action() {
    	   
    		System.out.println("Probation:listener:01");
    		
       /*
    	   for(UploadItem item:list){
        	   System.out.println("===saveSost-1-name="+item.getFileName());
    	   }*/
			
    		try{
    			
    		 HttpServletResponse hresp = (HttpServletResponse) 
    				 FacesContext.getCurrentInstance().getExternalContext().getResponse();
    		   
    		 hresp.sendRedirect("http://localhost:8080/infoscud/test2.seam");
    		 
    		 System.out.println("Probation:listener:02");
    		 
    		}catch(Exception e){
    			System.out.println("Probation:listener:error:"+e);
    		}
		}
      
       public void clob_persist() {
    	   
   		System.out.println("Probation:action:01");
   		
      /*
   	   for(UploadItem item:list){
       	   System.out.println("===saveSost-1-name="+item.getFileName());
   	   }*/
   	   
   	   try{
   		 String st ="hQMDgQMBARIMMDA3ODE1MDAwODcwMRgwFgYFKoUDZAESDTEwMzc4NDMwNDI5MDcx" + 
   		 		"HDAaBgkqhkiG9w0BCQEWDWNhQGlhYy5zcGIucnUxMDAuBgNVBAsMJ9Cj0LTQvtGB" + 
   		 		"0YLQvtCy0LXRgNGP0Y7RidC40Lkg0YbQtdC90YLRgDEoMCYGA1UECgwf0KHQn9Cx" + 
   		 		"INCT0KPQnyDCq9Ch0J/QsSDQmNCQ0KbCuzFKMEgGA1UECQxB0KLRgNCw0L3RgdC/" + 
   		 		"0L7RgNGC0L3Ri9C5INC/0LXRgC4g0LQuIDYg0LvQuNGCLiDQkCDQv9C+0LwuIDfQ" + 
   		 		"nSA40J0xJjAkBgNVBAcMHdCh0LDQvdC60YIt0J/QtdGC0LXRgNCx0YPRgNCzMSkw" + 
   		 		"JwYDVQQIDCA3OCDQodCw0L3QutGCLdCf0LXRgtC10YDQsdGD0YDQszELMAkGA1UE" + 
   		 		"BhMCUlUxLTArBgNVBAMMJNCj0KYg0KHQn9CxINCT0KPQnyDCq9Ch0J/QsSDQmNCQ" + 
   		 		"0KbCuzAeFw0xMjA5MjUwNjI3MDBaFw0xMzA5MjUwNjM3MDBaMIIBMjEWMBQGBSqF" + 
   		 		"A2QDEgsxMzQwNzI0ODAzMTEaMBgGCCqFAwOBAwEBEgw3ODAyNDE4NzkxMDkxIDAe" + 
   		 		"BgkqhkiG9w0BCQEWEXNhbGVrc2VldkBtYWlsLnJ1MQswCQYDVQQGEwJSVTEtMCsG" + 
   		 		"A1UECB4kADcAOAAgBCEEMAQ9BDoEQgAtBB8ENQRCBDUEQAQxBEMEQAQzMScwJQYD" + 
   		 		"VQQHHh4EIQQwBD0EOgRCAC0EHwQ1BEIENQRABDEEQwRABDMxQzBBBgNVBAMeOgQQ" + 
   		 		"BDsENQQ6BEEENQQ1BDIAIAQhBDUEQAQzBDUEOQAgBBAEOwQ1BDoEQQQwBD0ENARA" + 
   		 		"BD4EMgQ4BEcxFTATBgNVBCoeDAQhBDUEQAQzBDUEOTEZMBcGA1UEBB4QBBAEOwQ1" + 
   		 		"BDoEQQQ1BDUEMjBjMBwGBiqFAwICEzASBgcqhQMCAiQABgcqhQMCAh4BA0MABECq" + 
   		 		"x9EplWmoza8b9a1CZ6u5ryhOJbN5FQKQsOoeGktfbwRP5yMxm6tBdAKhY+WnJw6H" + 
   		 		"ez2vlNp02ZtJ2Lqr5Yv7o4IJaTCCCWUwDgYDVR0PAQH/BAQDAgTwMB0GA1UdJQQW" + 
   		 		"MBQGCCsGAQUFBwMEBggrBgEFBQcDAjAdBgNVHQ4EFgQUh+Ra+EytjOOCZtkvKfa7" + 
   		 		"lXHb2oMwggYTBgNVHSMEggYKMIIGBjCCBbOgAwIBAgIQTraklbCiCopCJUfFvDS3" + 
   		 		"sTAKBgYqhQMCAgMFADCCAYsxGjAYBggqhQMDgQMBARIMMDA3ODE1MDAwODcwMRgw" + 
   		 		"FgYFKoUDZAESDTEwMzc4NDMwNDI5MDcxHDAaBgkqhkiG9w0BCQEWDWNhQGlhYy5z" + 
   		 		"cGIucnUxMDAuBgNVBAsMJ9Cj0LTQvtGB0YLQvtCy0LXRgNGP0Y7RidC40Lkg0YbQ" + 
   		 		"tdC90YLRgDEoMCYGA1UECgwf0KHQn9CxINCT0KPQnyDCq9Ch0J/QsSDQmNCQ0KbC" + 
   		 		"uzFKMEgGA1UECQxB0KLRgNCw0L3RgdC/0L7RgNGC0L3Ri9C5INC/0LXRgC4g0LQu" + 
   		 		"IDYg0LvQuNGCLiDQkCDQv9C+0LwuIDfQnSA40J0xJjAkBgNVBAcMHdCh0LDQvdC6" + 
   		 		"0YIt0J/QtdGC0LXRgNCx0YPRgNCzMSkwJwYDVQQIDCA3OCDQodCw0L3QutGCLdCf" + 
   		 		"0LXRgtC10YDQsdGD0YDQszELMAkGA1UEBhMCUlUxLTArBgNVBAMMJNCj0KYg0KHQ" + 
   		 		"n9CxINCT0KPQnyDCq9Ch0J/QsSDQmNCQ0KbCuzAeFw0xMjA5MTIxMzU2NThaFw0x" + 
   		 		"NzA5MTIxNDA1MzdaMIIBizEaMBgGCCqFAwOBAwEBEgwwMDc4MTUwMDA4NzAxGDAW" + 
   		 		"BgUqhQNkARINMTAzNzg0MzA0MjkwNzEcMBoGCSqGSIb3DQEJARYNY2FAaWFjLnNw" + 
   		 		"Yi5ydTEwMC4GA1UECwwn0KPQtNC+0YHRgtC+0LLQtdGA0Y/RjtGJ0LjQuSDRhtC1" + 
   		 		"0L3RgtGAMSgwJgYDVQQKDB/QodCf0LEg0JPQo9CfIMKr0KHQn9CxINCY0JDQpsK7" + 
   		 		"MUowSAYDVQQJDEHQotGA0LDQvdGB0L/QvtGA0YLQvdGL0Lkg0L/QtdGALiDQtC4g" + 
   		 		"NiDQu9C40YIuINCQINC/0L7QvC4gN9CdIDjQnTEmMCQGA1UEBwwd0KHQsNC90LrR" + 
   		 		"gi3Qn9C10YLQtdGA0LHRg9GA0LMxKTAnBgNVBAgMIDc4INCh0LDQvdC60YIt0J/Q" + 
   		 		"tdGC0LXRgNCx0YPRgNCzMQswCQYDVQQGEwJSVTEtMCsGA1UEAwwk0KPQpiDQodCf" + 
   		 		"0LEg0JPQo9CfIMKr0KHQn9CxINCY0JDQpsK7MGMwHAYGKoUDAgITMBIGByqFAwIC" + 
   		 		"IwEGByqFAwICHgEDQwAEQAv3JIVufy/T94PfXZ3hA3PqPo3eQtdbUm/U2ApfDcvn" + 
   		 		"sV9qEfKwu4+OcT9ABFW57jSVFF/4ZqQwFb/nmJcdIoWjggHpMIIB5TA2BgUqhQNk" + 
   		 		"bwQtDCsi0JrRgNC40L/RgtC+0J/RgNC+IENTUCIgKNCy0LXRgNGB0LjRjyAzLjYp" + 
   		 		"MIIBMwYFKoUDZHAEggEoMIIBJAwrItCa0YDQuNC/0YLQvtCf0YDQviBDU1AiICjQ" + 
   		 		"stC10YDRgdC40Y8gMy42KQxTItCj0LTQvtGB0YLQvtCy0LXRgNGP0Y7RidC40Lkg" + 
   		 		"0YbQtdC90YLRgCAi0JrRgNC40L/RgtC+0J/RgNC+INCj0KYiINCy0LXRgNGB0LjQ" + 
   		 		"uCAxLjUMT9Ch0LXRgNGC0LjRhNC40LrQsNGCINGB0L7QvtGC0LLQtdGC0YHRgtCy" + 
   		 		"0LjRjyDihJYg0KHQpC8xMjEtMTg1OSDQvtGCIDE3LjA2LjIwMTIMT9Ch0LXRgNGC" + 
   		 		"0LjRhNC40LrQsNGCINGB0L7QvtGC0LLQtdGC0YHRgtCy0LjRjyDihJYg0KHQpC8x" + 
   		 		"MjgtMTgyMiDQvtGCIDAxLjA2LjIwMTIwCwYDVR0PBAQDAgGGMA8GA1UdEwEB/wQF" + 
   		 		"MAMBAf8wHQYDVR0OBBYEFLL+jQCtOFKHUAfB5xIQSBFkpw0KMBAGCSsGAQQBgjcV" + 
   		 		"AQQDAgEAMCUGA1UdIAQeMBwwCAYGKoUDZHEBMAgGBiqFA2RxAjAGBgRVHSAAMAoG" + 
   		 		"BiqFAwICAwUAA0EA6Rn8QB6Eger086Z6CCSqvZRQIvqto749UM/385T+gSm07ikN" + 
   		 		"y5K0lHH5mp+MtKIn73YD0jP+Bsyhu514vzdxZjCB3QYDVR0fBIHVMIHSMCegJaAj" + 
   		 		"hiFodHRwOi8vY2EuaWFjLnNwYi5ydS9jcmwvcXVhbC5jcmwwIqAgoB6GHGh0dHA6" + 
   		 		"Ly9jYWlhYy5ydS9jcmwvcXVhbC5jcmwwJqAkoCKGIGh0dHA6Ly8xMC4xMjguMzEu" + 
   		 		"NjUvY3JsL3F1YWwuY3JsMDGgL6AthitodHRwOi8vc2VydHNlcnZlci5zbW9sbnku" + 
   		 		"dXRzL2NhaWFjL3F1YWwuY3JsMCigJqAkhiJmaWxlOi8vY2VydHNlcnZlci9jZXJ0" + 
   		 		"c3J2L3F1YWwuY3JsMG0GCCsGAQUFBwEBBGEwXzAuBggrBgEFBQcwAoYiaHR0cDov" + 
   		 		"L2NhLmlhYy5zcGIucnUvY2VydC9xdWFsLmNlcjAtBggrBgEFBQcwAoYhaHR0cDov" + 
   		 		"LzEwLjEyOC4zMS42NS9jZXJ0L3F1YWwuY2VyMCsGA1UdEAQkMCKADzIwMTIwOTI1" + 
   		 		"MDYyNzAwWoEPMjAxMzA5MjUwNjI3MDBaMBMGA1UdIAQMMAowCAYGKoUDZHEBMDYG" + 
   		 		"BSqFA2RvBC0MKyLQmtGA0LjQv9GC0L7Qn9GA0L4gQ1NQIiAo0LLQtdGA0YHQuNGP" + 
   		 		"IDMuNikwggEzBgUqhQNkcASCASgwggEkDCsi0JrRgNC40L/RgtC+0J/RgNC+IENT" + 
   		 		"UCIgKNCy0LXRgNGB0LjRjyAzLjYpDFMi0KPQtNC+0YHRgtC+0LLQtdGA0Y/RjtGJ" + 
   		 		"0LjQuSDRhtC10L3RgtGAICLQmtGA0LjQv9GC0L7Qn9GA0L4g0KPQpiIg0LLQtdGA" + 
   		 		"0YHQuNC4IDEuNQxP0KHQtdGA0YLQuNGE0LjQutCw0YIg0YHQvtC+0YLQstC10YLR" + 
   		 		"gdGC0LLQuNGPIOKEliDQodCkLzEyMS0xODU5INC+0YIgMTcuMDYuMjAxMgxP0KHQ" + 
   		 		"tdGA0YLQuNGE0LjQutCw0YIg0YHQvtC+0YLQstC10YLRgdGC0LLQuNGPIOKEliDQ" + 
   		 		"odCkLzEyOC0xODIyINC+0YIgMDEuMDYuMjAxMjAKBgYqhQMCAgMFAANBAFdEGfZz" + 
   		 		"idpk2X3XDpKkRJCtyU72ynPjdw7g1u5Q4Gkoea3XOGucvsDqFWzfn3pZeKwKJBaF" + 
   		 		"iB4stKXxX38tPuU=";
   		   
   		   
   		 UcCertReestr ucr = new UcCertReestr();
 			 
 				 
 			 //	System.out.println("records_db_wh:02:"+pss);
 					 
 	       ucr.setUserFio("123");
           ucr.setOrgName("123");
     	   ucr.setUserPosition("123");
 	       ucr.setCertNum("123");
           ucr.setXType("1");
           ucr.setCertDate("123");
           ucr.setUserEmail("123") ;
           ucr.setCertValue(st.getBytes());

 		   ucr.setCreator(new Long(1));
 		   ucr.setCreated(new Date());
 			 
 		   entityManager.persist(ucr);
 		
 		  System.out.println("Probation:action:02");
 		  
   	   }catch(Exception e){
   		   System.out.println("Probation:action:error:"+e);
   	   }
			
		}
}
