package iac.cud.infosweb.ws.classifierzip.clientsample;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;

@Name("gRuNProFileLite")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)  
public class GRuNProFileLite implements GRuNProFileLiteLocal{

	@PersistenceContext(unitName="InfoSCUD-web")
    EntityManager entityManager;
	
	//@In 
	//EntityManager entityManager;

    
    public int process(/*final String tableName, final List <String> fileList, final String dirName*/){
		   
		   Session session = (Session) entityManager.getDelegate();
	    	 final int[] result=new int[]{-1};
	  
			 session.doWork(new Work(){
			     public void execute(Connection conn)throws SQLException{
				    System.out.println("IHLogContrList:logContrList:execute:conn:"+conn.isClosed());
				    try{
				    	conn.setAutoCommit(false);
				    	//PojoGRuNProFileLite pojo = new PojoGRuNProFileLite();
				    	//result[0]=pojo.process2(tableName, fileList, dirName, conn);
				    	
				    	 PojoRunProcess pp = new PojoRunProcess(conn);
				    	 pp.startProcess();
				    	  
				    	 System.out.println("IHLogContrList:logContrList:execute:01");
				    	// Thread.sleep(600000);
				    	 System.out.println("IHLogContrList:logContrList:execute:02");
				    	 
				    }catch(Exception e){
				     System.out.println("IHLogContrList:logContrList:execute:error:1:"+e);
					// throw e;
				    }finally{
				    	try{
				    	//!!!
				    	//обязательно здесь НЕ закрывать коннект!!!
				    		
				  		/*  if(conn!=null&&!conn.isClosed()){
				  			 conn.close();
				  		  }*/
				  		}catch(Exception e){}
				    }
			   }
			  });
		 return result[0];
    }
    
	
}
