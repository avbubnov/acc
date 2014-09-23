package iac.cud.infosweb.ws.classifierzip.clientsample;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;



/**
 * 
 */
public class PojoRunProcess implements Runnable {

	private String fileClassifName;
	private Connection conn;
	
	public PojoRunProcess() {
    }
	
	public PojoRunProcess(Connection conn) {
		try{
		 System.out.println("PojoRunProcess:PojoRunProcess:conn:"+conn.isClosed());
		}catch(Exception e){}
		
		this.conn=conn;
    }
	
	 public PojoRunProcess(String fileClassifName) {
    	this.fileClassifName=fileClassifName;
    }
    
    public void startProcess() {
        Thread t = new Thread(this);
        t.start();
    }
    
    public void run(){
    	
    	try{
     	  System.out.println("PojoRunProcess:conn:"+conn.isClosed());
     	 
    	}catch(Exception e){}
    	
     	  ClientSample.run2(null, null, null, null);
     	 
     	  /*
		     Process pr=null;
		    //  InputStream is=null;
		    //  PrintStream o1=null;
		   //   ByteArrayOutputStream baos = new ByteArrayOutputStream();
		      
		      try{
		    	  Runtime rt = Runtime.getRuntime();
		    	 // pr = rt.exec("/oracle/jboss/mopl/grunpro.sh");
		    	//  pr = rt.exec("cmd.exe /c start "+
		    	//		       CUDWSSMEVConstants.fileLoadScriptPath+
		    	//		       CUDWSSMEVConstants.fileLoadScriptName+
		    	//		       " 12345");
		    	 
		    	  pr = rt.exec(
		    		   CUDWSSMEVConstants.fileLoadScriptPath+
	   			       CUDWSSMEVConstants.fileLoadScriptName+
	   			       " ./sources/"+fileClassifName);
		    	  
		    	  System.out.println("exec:02");
		    	  pr.waitFor();
		    	  System.out.println("exec:03");
		    	  
		 	
		    	//  is = pr.getErrorStream();
		    	//  System.out.println("exec:04");
		       //   int c;
		      //    while ((c = is.read()) != -1){
		       //        baos.write(c);
		      //    }
		     //     System.out.println("exec:loader:"+baos.toString("Cp1251"));
		
		    //      o1 = new PrintStream(
		    //    		           new BufferedOutputStream(
		    //    				   new FileOutputStream(
		    //    						   CUDWSSMEVConstants.fileLoadScriptPath+"sqlloaderOutput.log",
		      //  						   false)));
		      //    o1.println(baos.toString("Cp1251"));
		    //      o1.flush();
		 
		    	  
		           System.out.println("exec:05");
		      }catch(Exception e){
		   		 System.out.println("IHConfLoadDataGen:exec:error:"+e);
		   	  }finally {
		   		  if(pr!=null){pr.destroy();}
		   		// if(o1!=null){o1.close();}
		      }
		  */	
	}
    
}
