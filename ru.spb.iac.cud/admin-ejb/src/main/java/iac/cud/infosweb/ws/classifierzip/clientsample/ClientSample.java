package iac.cud.infosweb.ws.classifierzip.clientsample;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import iac.cud.infosweb.ws.classifierzip.*;
import iac.cud.infosweb.ws.classif.common.Response1;
import iac.cud.infosweb.ws.classif.common.Response4;

public class ClientSample {

	private static String fileZipName = "classif.zip";
	
    public static String getVersion() {
		
		System.out.println("ClientSample:getVersion:01");
	    System.out.println("Create Web Service Client...");
	    ClassifierZip_Service service1 = new ClassifierZip_Service();
	    System.out.println("Create Web Service...");
	    ClassifierZip port1 = service1.getClassifierZipPort();
	    System.out.println("Call Web Service Operation...");
	      //  System.out.println("Server said: " + port1.getClassifierByClassifierName(null,null,null,Integer.parseInt(args[0])));
	    
	     int actualDoc = 0;
	     FileOutputStream fos = null;
	     String result =null;	     
	     try {
	       
	        ResponseElement53 re53 =  port1.getClassifierZipListByClassifierNumber(
	        		 CUDWSSMEVConstants.getLogin(),
	    		     CUDWSSMEVConstants.getPasswordMD5(),
	    		     CUDWSSMEVConstants.getClassifRegNum());
	        
	        if(re53.getItem()==null){
	        	System.out.println("Server said: return");
	        	return null;
	        }
	        
	        System.out.println("Server said: " + re53.getItem().size());
	        
	        for(Response4 r1 : re53.getItem()){
	        	
	        	if(r1.getActualDoc() > actualDoc) {
	        	  actualDoc = r1.getActualDoc();
	        	}  
	        	System.out.println("actualDoc: " +  actualDoc);
	        	System.out.println();
	        	
	        }
	        return String.valueOf(actualDoc);
	        
	    
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
				try {
					if(fos!=null){
				    	fos.close();
				    }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	     
	     return result;
	}
    
	//public static void run(Connection conn) {
		
    public static void run2(EntityManager em, UserTransaction utx, Long seancact, Integer clVersion) {
		
		System.out.println("ClientSample:run:01");
	    System.out.println("Create Web Service Client...");
	    ClassifierZip_Service service1 = new ClassifierZip_Service();
	    System.out.println("Create Web Service...");
	    ClassifierZip port1 = service1.getClassifierZipPort();
	    System.out.println("Call Web Service Operation...");
	      //  System.out.println("Server said: " + port1.getClassifierByClassifierName(null,null,null,Integer.parseInt(args[0])));
	    
	     int actualDoc = 0;
	     FileOutputStream fos = null;
	    
	     try {
	    	// System.out.println("ClientSample:run:conn:"+conn.isClosed());
	    
	     if(clVersion!=null){
	    	 
	    	 actualDoc=clVersion.intValue(); 
	    	 
	     }else{ 
	    	 
	        ResponseElement53 re53 =  port1.getClassifierZipListByClassifierNumber(
	        		 CUDWSSMEVConstants.getLogin(),
	    		     CUDWSSMEVConstants.getPasswordMD5(),
	    		     CUDWSSMEVConstants.getClassifRegNum());
	        
	        if(re53.getItem()==null){
	        	System.out.println("Server said: return");
	        	return;
	        }
	        
	        System.out.println("Server said: " + re53.getItem().size());
	        
	        for(Response4 r1 : re53.getItem()){
	        	
	        	if(r1.getActualDoc() > actualDoc) {
	        	  actualDoc = r1.getActualDoc();
	        	}  
	        	System.out.println("actualDoc: " +  actualDoc);
	        	System.out.println();
	        	
	        }
	     }
	         ResponseElement51 re51 =  port1.getClassifierByClassifierNumber(
	        		 CUDWSSMEVConstants.getLogin(),
	    		     CUDWSSMEVConstants.getPasswordMD5(),
	    		     CUDWSSMEVConstants.getClassifRegNum(),
	    		     actualDoc);
	        
	        if(re51.getItem()==null){
	        	System.out.println("Server said: return");
	        	return;
	        }
	        
	        System.out.println("Server said: " + re51.getItem().size());
	        
	        for(Item51 r1 : re51.getItem()){
	        	
	        	System.out.println("Fullname: " +  r1.getFileName());
	        	System.out.println();
	        	
	        	
				fos = new FileOutputStream(CUDWSSMEVConstants.fileZipPath+fileZipName);
				
	        	fos.write(r1.getFileContent());
	        	
	        	fos.flush();
	        }
	        
	        try {
	        	if(fos!=null){
	        		fos.close();
	        	}
			} catch (IOException e) {e.printStackTrace();}
	        
	        String fileClassifName = unZip(CUDWSSMEVConstants.fileZipPath+fileZipName);
	        
	        runLoader2(fileClassifName, em, utx, seancact, actualDoc);
	       
	        
	        System.out.println("***********************");
	        System.out.println("Call Over!");
	       
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
				try {
					if(fos!=null){
				    	fos.close();
				    }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static String unZip(String file) {

		String result = null;
		final int BUFFER = 2048;
		try {
			BufferedOutputStream dest = null;
			FileInputStream fis = new FileInputStream(file);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				System.out.println("Extracting: " + entry);
				
				if(entry.getName().startsWith("1b")){
					result=entry.getName();
				}
				
				int count;
				byte data[] = new byte[BUFFER];
				// write the files to the disk
				FileOutputStream fos = new FileOutputStream(CUDWSSMEVConstants.fileZipPath+entry.getName());
				dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = zis.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
			}
			zis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	//public static void runLoader(String fileClassifName, Connection conn) {
	public static void runLoader2(String fileClassifName, EntityManager em, 
			UserTransaction utx, Long seancact, Integer clVersion) {
		
		final int[] result=new int[]{-1};
		try {
			
			PojoGRuNProFileLite pojo = new PojoGRuNProFileLite();
	    	
	    	result[0]=pojo.process22(CUDWSSMEVConstants.fileZipPath+fileClassifName, em, utx, seancact, clVersion);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	public static void runLoader(String fileClassifName) throws Exception{
	    	
	      System.out.println("exec:01:"+fileClassifName);
	    	 
	      if(fileClassifName==null){
	    	  return ;
	      }
	      
	      
	    //  PojoRunProcess pp = new PojoRunProcess(fileClassifName);
		//  pp.startProcess();
	      
	   
	      Process pr=null;
	  //    InputStream is=null;
	    //  PrintStream o1=null;
	     // ByteArrayOutputStream baos = new ByteArrayOutputStream();
	      
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
	      //    int c;
	      //    while ((c = is.read()) != -1){
	     //          baos.write(c);
	     //     }
	     //     System.out.println("exec:loader:"+baos.toString("Cp1251"));
	
	       //   o1 = new PrintStream(
	       // 		           new BufferedOutputStream(
	       // 				   new FileOutputStream(
	       // 						   CUDWSSMEVConstants.fileLoadScriptPath+"sqlloaderOutput.log",
	       // 						   false)));
	        //  o1.println(baos.toString("Cp1251"));
	        //  o1.flush();
	    	  
	    	  
	           System.out.println("exec:05");
	      }catch(Exception e){
	   		 System.out.println("IHConfLoadDataGen:exec:error:"+e);
	   	  }finally {
	   		  if(pr!=null){pr.destroy();}
	   		// if(o1!=null){o1.close();}
	      }
	    }
	    */
}
