package iac.cud.infosweb.ws.classifierzip.clientsample;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
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


public class PojoGRuNProFileLite {//implements GRuNProFileLiteLocal{

	
//	private Connection conn=null;
	 
	private UserTransaction utx = null;
	
	private EntityManager em = null;
	
	private Long idUser=null;
	
	private String directory_output ;//= "/oracle/jboss/grn_load/output/";
	
	private String tmp_dir_name = "/Development/test/classif/LoadScript/tmp";
	
	private String tablename=null;
	
	private int result = -1;
	
	private File file1=null;
    
	private File file2=null;
	
	private Date date1=null;
    
	private Date date2=null;
     
	private String btype1,btype2;
	
	private String ftype;
	
	private HashMap hm= new HashMap();
 
	private HashMap hm_act= new HashMap();
    
	private HashMap hm_dep= new HashMap();
     
	private File buf_rec_file;
	
	private File buf_rec1_file;

    private File buf_rec2_file;
	     
    private BufferedReader br=null;
  
    private BufferedWriter bw=null;
    
    Long seancact = null;
    Integer clVersion = null;
	
    //public int process2(String fileClassifName, Connection conn){
    public int process22(String fileClassifName, EntityManager em, 
    		UserTransaction utx, Long seancact, Integer clVersion){	
    	
		System.out.println("GRuNProFileLite:process:01");
		Date dstart=new Date();
		
		try{
			
		//	System.out.println("GRuNProFileLite:process:01_conn:"+conn.isClosed());
			
			
			
			//reset_field();
			
			this.result = -1;
			
			//this.conn=conn;
    		this.utx=utx;
    		this.em=em;
    		this.seancact=seancact;
    		this.clVersion=clVersion;
    		
    		//System.out.println("GRuNProFileLite:process:02:"+this.conn.getAutoCommit());
    		
    		//this.conn.setAutoCommit(false);
    		
    	//	System.out.println("GRuNProFileLite:process:03:"+this.conn.getAutoCommit());
    		
    	//	bd_connect();
    		
    		this.file1=new File(fileClassifName);
    		
    		phase_fixed(this.file1);
    		
    		temp_files_init();
 	 
    		clear_prev_load();
    		
    		//this.conn.commit();
    		this.utx.commit();
    		this.utx.begin();
    		
    		
		   
	  	  	configuration_file();
			 
			file_action(this.file1,"1");
			 
			
		    
			 //if(this.tablename.equals("table2")){
			 // adjacencyControl();
			 //}
			
		//	System.out.println("Время выполнения вх контроля: "+
        //            time_period(dstart));
			
		//	phase_fixed();
			
			records_db("1");
			 
			//this.conn.commit();
			this.utx.commit();
			this.utx.begin();
    		
	    	this.result = 1;
	    	
	    	seanc_end(1);
	    	
	    	this.utx.commit();
			//this.utx.begin();
			
		/*}catch(BreakAdjacencyException bae){
			System.out.println("GRuNProFileLite:process:BreakAdjacencyException:"+bae);
			try{
			  //utx.rollback();
				this.result=3;
			}catch(Exception be2){
				System.out.println("GRuNProFileLite:process:rollback:BreakException:"+be2);
		    }
		}catch(BreakException be){
			System.out.println("GRuNProFileLite:process:BreakException:"+be);
			try{
			  //utx.rollback();
				this.result=-10;
			}catch(Exception be2){
				System.out.println("GRuNProFileLite:process:rollback:BreakException:"+be2);
		    }*/
		}catch(Exception e){
			System.out.println("GRuNProFileLite:process:error:"+e);
			e.printStackTrace(System.out);
			try{
			  this.result = -1;
			  
			  this.utx.rollback();
			  
			  this.utx.begin();
			  
			  seanc_end(0);
		    	
		      this.utx.commit();
		 	//  this.utx.begin();
				
			}catch(Exception e2){
				System.out.println("GRuNProFileLite:process:rollback:error:"+e2);
		    }
		}finally{
			try{
			 /*  if(conn!=null){
				   conn.close();
				}*/
			}catch(Exception e){}
			delTempFiles();
			delClassifFiles();
			
			reset_field();
		}
		return this.result;
	}
	
	private void reset_field(){
		
		System.out.println("GRuNProFileLite:reset_field:01");
		
		try{
		 /* if(conn!=null&&!conn.isClosed()){
			 conn.close();
		  }*/
		}catch(Exception e){}
		//conn=null;
	    tablename=null;
		//result = -1;
		file1=null;
	    file2=null;
		date1=null;
	    date2=null;
	    btype1=null;btype2=null;
		ftype=null;
		hm= new HashMap();
	    hm_act= new HashMap();
	    hm_dep= new HashMap();
	    buf_rec_file=null;
		buf_rec1_file=null;
        buf_rec2_file=null;
		br=null;
	    bw=null;
	}
	
	private void temp_files_init(){
		
		System.out.println("GRuNProFileLite:temp_files_init:01");
		
		 File tmp_dir= new File(tmp_dir_name);
		  if(!tmp_dir.exists()){
			  tmp_dir.mkdir();
		  }
		  buf_rec1_file=new File(tmp_dir_name+"/f1_t1");
		  if(buf_rec1_file.exists()){
			  buf_rec1_file.deleteOnExit();
			//  buf_rec1_file=new File(tmp_dir_name+"/f1");
		  }
		
	}
	
	private List <File> toFileArray(List <String> fileList)throws Exception {
		List <File> result = new ArrayList<File>();
		File file=null;
		try{
		  for(String st : fileList){
			file = new File(this.directory_output+st);
			if(file.exists()){
				result.add(file);
			}
		  }
		  return result;
		}catch(Exception e){
		 System.out.println("toFileArray:error:"+e);
		 throw e; 
		}
	}
	
   
	
	private void control_files_failed() throws Exception {
		System.out.println("control_files_failed!!!");
		throw new BreakException();
	}
	
	public void clear_prev_load() throws Exception{
		
		System.out.println("GRuNProFileLite:clear_prev_load:01");
		
		 try{
		//	bd_execute("delete from "+(this.tablename.equals("table1") ? "WAREHOUSE_DATA1" : "WAREHOUSE_DATA2"));
			bd_execute("TRUNCATE TABLE ISP_TEMP_BSS_T ");

		 }catch(Exception e){
			System.out.println("clear_prev_load:error:"+e);
			throw e;
		 }
	 }
	

	
	public void bd_execute(String query) throws Exception {
		try{
			
			/*Statement stmt = conn.createStatement();
	        
		     //	System.out.println("bd_execute:query:"+query);
		     	
				stmt.execute(query);
				stmt.close();
				*/
			
			this.em.createNativeQuery(query)
				.executeUpdate();
				
		}catch (Exception e) {
			System.out.println("bd_execute:error="+e);
	        throw e;
	    }
	}

	public void bd_execute_seancact(String query) throws Exception{
		try {
	  	/*	Statement stmt = conn.createStatement();
	        
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			 seancact = rs.getInt("sgnc");
	        rs.close();
	        stmt.close();
	        */
	      //  String idSess =(String) this.em.createNativeQuery(query).getSingleResult();
	     //   seancact = Integer.valueOf(idSess);
	        
	    } catch (Exception e) {
	    	System.out.println("bd_execute_seancact:error="+e);
	       	throw e;
	    }
	}
	
	private void configuration_file () throws Exception{
		try {
		/*
			 List<Object[]> lo =em.createNativeQuery("select INF.ORDER_, INF.NAME_, INF.ID_FIELD " +
			 		                                 "from INPUT_FIELDS inf " +
			 		                                 "where ID_FILE_TYPE=1")
			                    .getResultList();
		
			  for(Object[] objectArray :lo){
				  hm_act.put(objectArray[0].toString(),
						     new String[]{objectArray[0].toString(),
					                      objectArray[1].toString(),
					                      objectArray[2].toString()
                   }
                 );
   			  }
	
			  if(file2!=null){
				  lo =em.createNativeQuery("select INF.ORDER_, INF.NAME_, INF.ID_FIELD " +
                                           "from INPUT_FIELDS inf " +
                                           "where ID_FILE_TYPE=2")
                       .getResultList();
				
				  for(Object[] objectArray :lo){
					  hm_dep.put(objectArray[0].toString(),
							     new String[]{objectArray[0].toString(),
						                      objectArray[1].toString(),
						                      objectArray[2].toString()
	                   }
	                 );
	   			  }
	   	 }
		*/
/*
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select INF.ORDER_, INF.NAME_, INF.ID_SRV from ISP_INPUT_FIELDS inf order by ORDER_ ");
	
			while(rs.next()){
			  hm_act.put(rs.getString("ORDER_"),
					     new String[]{rs.getString("ORDER_"),
                                      rs.getString("NAME_"),
                                      rs.getString("ID_FIELD")
                 }
              );
			}
			
		   rs.close();
           stmt.close();
   	*/
           
          List<Object[]> lo = this.em.createNativeQuery("select to_char(INF.ORDER_), INF.NAME_, to_char(INF.ID_SRV) from ISP_INPUT_FIELDS inf order by ORDER_ ")
			.getResultList();
           
          
          for(Object[] rec :lo){
        	  hm_act.put((String)rec[0],
					     new String[]{(String)rec[0],
        			                  (String)rec[1],
        			                  (String)rec[2]
              }
           );
          }
          
		} catch (Exception e) {
			System.out.println("configuration_file:error:"+e);
			throw e;
	     }
      	}
	
  private void file_action (File file, String s) throws Exception{
		
		ftype=s;
		
		try{
			
			if(ftype.equals("1")){
				hm=hm_act;
				buf_rec_file=buf_rec1_file;
			}
		//	 id_seancact=(Long)id_seancact_map.get(file.getName());
			
			 file_interaction(file);
			 
	   }catch(Exception e){
		   System.out.println("file_action:error:"+e);
		throw e;
      }
   }
  
  private void file_interaction(File file) throws Exception  {
		
	    String s;
	 	try {
		
	      br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"Cp1251"));
	   
	      bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(buf_rec_file),"Cp1251"));
		   
	      
	      int i=1;
	   
	    /*  s = br.readLine();
	      if(s!=null){*/
	    	 // control_header(s);
	         
	    	  while ((s = br.readLine())!=null){
		    	  file_record_handling(s,i);
		          i++;
		          if((i % 1000)==0){
		    		    bw.flush();
		    		  //  logger.info("file_interaction:flush");
		    		}
		       }
	    	  
	       /*   убрал, так как переместил подчёт строк в inputfiles_save()
	            с помощью LineNumberReader
	            bd_execute(
		                "update LOG_LOADING_SESSIONS LL " +
		                "set LL.REC_CNT="+i+
	                    " where LL.ID_LOG="+id_seancact);*/
	          
		    if(i==1){//файл совсем пуст
		    	System.out.println("file_interaction:файл "+file.getName()+" пуст");
		    	  
		   	     control_files_failed();
		    }
		      
	    /*  }else{  //файл совсем пуст
	    	  System.out.println("file_interaction:файл "+file.getName()+" пуст");
	    	  
	    	 if(ftype.equals("1")){  //не включаем файл убытий - он может быть пуст
	    	  control_files_failed();
	    	  }
	      }*/
	     
	      
	     } catch (Exception e) {
	    	 System.out.println("file_interaction:error="+e);
	      throw e;
	    }finally{
	    	try {
	         br.close();
		     bw.close();
	        } catch (Exception e) { 
	        	System.out.println("file_interaction:finally:error="+e);
	        }
	    }
	}
  /*
  private void control_header(String ps) throws Exception {
		try {
			 int ii=1;
			 String[] sa=ps.split("\t");
			 String[] sconfrec;
			 for(String pss: sa){
				 sconfrec=((String[]) hm.get(Integer.toString(ii)));
				 
				if(!pss.equals(sconfrec[1])){
					System.out.println("control_header:В заголовке файла в "+ii+" столбце находится - "+pss+", тогда как должно быть - "+(sconfrec[1]));
					   control_files_failed();
			
				}
				ii++;
			 }
	     	 } catch (Exception e) {
	     		System.out.println("control_header:error="+e);
	           	throw e;
	        }
	}*/
  
  private void file_record_handling (String ps, int pi)throws Exception  {
		
		try {
			  
		  file_record_write(ps,pi);
			
		 } catch (Exception e) {
			 System.out.println("file_record_handling:error="+e);
	      throw e;
	    }
	}
	/**
	 * Разбор строки и запись в буфер
	 * @param ps строка
	 * @param ppi номер строки
	 * @throws Exception
	 */
	private void file_record_write(String ps,int ppi) throws Exception {
	
	int ivalid=0;
	try{
		
	 // String[] sa=ps.split("\t");
	  String[] sa=ps.split(">", -1);
	  StringBuffer sbn = new StringBuffer(), sbv = new StringBuffer();
	  String query,numeric,id_seance;
	
	  if((sa.length-1)!=hm.size()){
		  System.out.println("file_record_write: в записи № "+ppi+" количество столбцов - "+sa.length+", а должно быть - "+hm.size());
    	  control_files_failed();

	  }
	  
	  /*  int ii=1;
	  String uid="";
	  for(String pss: sa){         //по полям записи
	
	  try{	
		
		String[] sconfrec=((String[]) hm.get(Integer.toString(ii)));
		
		if (pss.length()>255){
			//  bd_execute("insert into LOG_INPUT_CONTROL " +
            //       "(ID_LOG, ID_SESS, ID_ERROR, ID_FILE_TYPE, UID_, ID_FIELD ) " +
  			// 	 "values( " +
  			//	 "SQNC_LOG_INPUT_CONTROL.NEXTVAL, "+id_seancact+", 13, "+ftype+", "+sa[0]+", "+sconfrec[2]+")");
			 
			  if(sconfrec[1].equals("uid")){
					uid="_"+ppi;
				}
			  ivalid=1;
		}else{
			if(sconfrec[1].equals("uid")){
				uid=pss+"_"+ppi;
			}
		}
	 }catch(Exception e){
	 		System.out.println("file_record_write:error on element [pss:"+pss+",ii:"+ii+"] of record:"+e);
	 }
		  
	  	ii++;
    }*/
	bw.append(/*ivalid+"\t"+*/ps+"\n");
	  
   }catch(Exception e){
		 System.out.println("file_record_write:error="+e);
		throw e;
   }
 }
	
 private void records_db(String s) throws Exception{
		
		
		try {
		  ftype=s;
		  if(ftype.equals("1")){
			  buf_rec_file=buf_rec1_file;
			  hm=hm_act;
			 // id_seancact=(Long)id_seancact_map.get(file1.getName());
		  }
		  records_db_ir();
		}catch(Exception e){
			 System.out.println("records_db:error="+e);
	  		throw e;
	  	 }
	}
	/**
	 * Работа с временным буфером на уровне записей
	 * @throws Exception
	 */
	private void records_db_ir() throws Exception{
		PreparedStatement ps=null;
		try {
			int i=1, id_rec;
		    String query, uid, valid_flag, st;// numrec;
		    
		    br = new BufferedReader(new InputStreamReader(new FileInputStream(buf_rec_file),"Cp1251"));
			
		    while ((st = br.readLine())!=null){
		
			  records_db_wh(ps, st);
			  
			   if((i % 1000)==0){
				//   logger.info("records_db_ir:i:"+i);
				  // conn.commit();
				   this.utx.commit();
				   this.utx.begin();
				 //  utx.commit();
				 //  utx.begin();
			   }
			   i++;
		    }
		}catch(Exception e){
			 System.out.println("records_db_ir:error="+e);
	  		throw e;
	  	 }finally{
	  		br.close();
	  	 }
	}
	/**
	 * Работа с временным буфером на уровне столбцов
	 * @param ps заготовка для записи в БД
	 * @param rec запись файла
	 * @throws Exception
	 */
	private void records_db_wh(PreparedStatement ps, String rec) throws Exception{
		try {
		 int i=1;
		 String query;
		 StringBuffer sbn = new StringBuffer(), sbv = new StringBuffer();
		// String[] sa=rec.split("\t");
		 String[] sa=rec.split(">", -1);
		 
		// System.out.println("records_db_wh:01");
		// System.out.println("records_db_wh:01+:"+sa.length);
		 
		 for(String pss: sa){ 
			
			/* if(i==0){
				 sbn.append("ERROR_FLAG");
				 sbv.append("'"+pss+"'");
			
			 }else {*/
			
			 if(i<=hm.size()){
				 
				String[] sconfrec=((String[]) hm.get(Integer.toString(i)));
					
				if(i==1){
				 	sbn.append(sconfrec[1]);
					sbv.append("'"+((pss.toLowerCase()).equals("null")?"":pss.trim().replaceAll("'","''"))+"'");
				
				}else{
		  	  	 	sbn.append(", "+sconfrec[1]);
					sbv.append(", '"+((pss.toLowerCase()).equals("null")?"":pss.trim().replaceAll("'","''"))+"'");
				}  
		  	  	  
		  	  	  
			 }  
		  	   	  
 		  	  //	}
				i++;
			}
		
			query=
				"INSERT /*+ APPEND */ into ISP_TEMP_BSS_T ("+sbn+", UP_ISP_LOAD ) values ( " +
				sbv+", "+this.seancact+" ) ";
				 
		  /*  ps = conn.prepareStatement(query);
			  ps.execute();
			   
			  ps.close();*/
			   
			  this.em.createNativeQuery(query)
				.executeUpdate();
			  
		}catch(Exception e){
			System.out.println("records_db_wh:error="+e);
			e.printStackTrace(System.out);
	  		throw e;
	  	 }
	}
	
	
	
	/*
	public void phase_inp_contr_fixed() throws Exception {
		try{
			HashMap<String, String> paramMap= new HashMap<String, String>();
			paramMap.put("code", "10");
			paramMap.put("unload", Long.toString(date1.getTime()));
			 
			saveCount(this.file1, this.file2, paramMap);
			
			setReportParam(this.tablename, paramMap);
			
	 	  }catch(Exception e){
	 		 System.out.println("phase_inp_contr_fixed:error:"+e);
			// throw e;
		 }
   }*/
	/*
	public void seanc_start(String fileName) throws Exception{
		try{
			
		  bd_execute_seancact("select to_char(JOURN_ISP_LOAD_SEQ.nextval) sgnc from dual");
		
		  bd_execute("INSERT INTO JOURN_ISP_LOAD (ID_SRV, LOAD_START, CREATED, CREATOR, FILE_NAME ) "+
		   		     "VALUES ("+seancact+", sysdate, sysdate, "+this.idUser+", "+fileName+" ) ");
		 }catch(Exception e){
			 System.out.println("seanc_start:error:"+e);
		     throw e;
		}
      }*/
		
	public void phase_fixed(File file) throws Exception {
		
		FileReader fr = null;
		LineNumberReader lineNumberReader = null;
		String fileName; 
		int file_rec_count;
		
		try{
			  fileName=file.getName();
			
			  fr=new FileReader(file);
			  lineNumberReader = new LineNumberReader(fr);
			  lineNumberReader.skip(Long.MAX_VALUE);
			  file_rec_count = lineNumberReader.getLineNumber();
			
			bd_execute(
	                "update JOURN_ISP_LOAD LL " +
	                "set LL.FILE_NAME = '"+fileName+"', "+
	                "LL.FILE_REC_COUNT = "+file_rec_count+", "+
	                "LL.CLASSIF_VERSION =  "+this.clVersion+" "+
                    "where LL.ID_SRV="+this.seancact);
		 }catch(Exception e){
			 System.out.println("phase_fixed:error:"+e);
		    throw e;
         }finally{
        	  if (fr != null) {
				  fr.close();
			  }
			  if (lineNumberReader != null) {
				  lineNumberReader.close();
			  }
         }
   }
	
	public void seanc_end(int success) throws Exception{
		try{
			 //1 - успех
			 //0 или null- не успех
			
		   bd_execute(
	                "update JOURN_ISP_LOAD LL " +
	                "set LL.LOAD_FINISH = sysdate, "+
	                "LL.SUCCESS =  "+success+", "+
	                "LL.LOAD_REC_COUNT = (select count(*) from ISP_TEMP_BSS_T ) "+
                    "where LL.ID_SRV="+this.seancact);
		 }catch(Exception e){
			 System.out.println("seanc_end:error:"+e);
		throw e;
      }
   }
	
	
	
   public void saveCount(File file1, File file2, HashMap<String, String> paramMap) throws Exception {
		Long id_s=null;
		FileReader fr = null;
		LineNumberReader lineNumberReader = null;
		int lines=-1;
		
		System.out.println("saveCount:01");
		
		try{
			
			if(file1==null){
				return;
			}
			
		  try{
			fr=new FileReader(file1);
			lineNumberReader = new LineNumberReader(fr);
			lineNumberReader.skip(Long.MAX_VALUE);
			lines = lineNumberReader.getLineNumber();
			
			if(lines!=0){  //исключаем заголовок
				lines=lines-1;
			}
			System.out.println("saveCount:lines1:"+lines);
			
			paramMap.put("act_file_count", Long.toString(lines));
						 
		  }catch(Exception e){
			System.out.println("inputfiles_save:lines1:error"+e);
		  }
		  
		  if(file2!=null){
		   try{
			 fr=new FileReader(file2);
			 lineNumberReader = new LineNumberReader(fr);
			 lineNumberReader.skip(Long.MAX_VALUE);
			 lines = lineNumberReader.getLineNumber();
			 
			 if(lines!=0){  //исключаем заголовок
				lines=lines-1;
			 }
			 System.out.println("inputfiles_save:lines2:"+lines);
						
			 paramMap.put("dep_file_count",  Long.toString(lines));
			 				 
		   }catch(Exception e){
			  System.out.println("inputfiles_save:lines1:error"+e);
		   }
		  }
		//  setReportParam(this.tablename, paramMap);
		  
	 	}catch(Exception e){
	 		 System.out.println("saveCount:error:"+e);
			// throw e;
		}finally{
		    if(fr != null) {
			  fr.close();
		    }
		    if(lineNumberReader != null) {
			   lineNumberReader.close();
			 }
	   }
	 }
	
   private void setReportParam(String tableName, HashMap<String, String> paramMap){
		
		String path = null;
		Properties properties = new Properties();
		OutputStream os = null;
		
		try{
			
		if(paramMap==null){
			return;
		}	
			
		  path = "/oracle/jboss/data/tt_loading/tt1_loading.info";
		
		
		//URL url = new URL(path);
		//File f=new File(url.toURI()); 
		File f=new File(path);
		
		Set<Map.Entry<String, String>> set = paramMap.entrySet();
        for (Map.Entry<String, String> me : set) {
		       System.out.println("me.getKey+:"+me.getKey());
		       System.out.println("me.getValue:"+me.getValue());
		       
		       properties.setProperty(me.getKey(), me.getValue());
	     }
	    properties.store(os=new FileOutputStream(f), null);
	    
		}catch(Exception e){
			System.out.println("IServCatalogLoading:setReport:error:"+e);
		}finally{
			 try {
				if(os!=null){
					 os.close();
				}
			 } catch (Exception e) {
				System.out.println("IServCatalogLoading:setReport:os:error:"+e);
			 }
		 }
	}
   /*
   private void adjacencyControl() throws Exception{

	   try{
			
		  HashMap<String, String> hm = loadTt1Properies();
   		  
   		  String code1=hm.get("code");
   		  String unload_prev=hm.get("unload");
			
		  List<Object[]> lo = em.createNativeQuery("select * from( "+
                                "select to_date(LLS.UNLOADING_DATE,'YYMMDD') unl_date "+
                                "from LOG_LOADING_SESSIONS lls "+
                                "where LLS.RESULT_IN=1 ) "+
                                "where  unl_date > ? "+
                                "and unl_date < ? ")
             .setParameter(1, new Date(new Long(unload_prev)))
             .setParameter(2, date1)
             .getResultList();
		
			if(lo!=null && !lo.isEmpty()){
				throw new BreakAdjacencyException();
			}
		}catch(Exception e){
			System.out.println("IServCatalogLoading:adjacencyControl:error:"+e);
			throw e;
		}
   	
  }
   
   public HashMap<String, String> loadTt1Properies() throws Exception{
		 System.out.println("IServCatalogLoading:loadTtProperies:01");
	     Long result=null;
	     Properties properties = new Properties();
	     InputStream is = null;
	     String code=null, unload=null;
	     String path = null;
	     
	     HashMap<String, String> paramMap = new HashMap<String, String>();	     
	     try{
	    	
	    	 path = "/oracle/jboss/data/tt_loading/tt1_loading.info";
					
	    	 File f=new File(path);
	    	
	    	 if(f.exists()){
	    	  properties.load(is=new FileInputStream(f));
	    	 
	    	  code=properties.getProperty("code");
	    	  unload=properties.getProperty("unload");
	    	  
	    	  paramMap.put("code", code);
	    	  paramMap.put("unload", unload);
	    	  
	    	 // System.out.println("IServCatalogLoading:loadTt1Properies:code:"+code);
	    	 }
	    	 return paramMap;  
	      }catch(Exception e){
	    	  System.out.println("IServCatalogLoading:loadTt1Properies:error:"+e);
			  throw e;
		  }finally{
			try {
			  if(is!=null){
			    is.close();
			   }
			} catch (Exception e) {
				System.out.println("confLoadDataManager:initConfLDInfoBean:finally:is:error:"+e);
			}
		  }    	  
	}*/
   
   public String time_period(Date d1) {
       
		String s="";
		Date d2=new Date();
		
		long diff= d2.getTime()-d1.getTime();
		
		long diffDays = diff / (24 * 60 * 60 * 1000); 
		diff-=diffDays*(24 * 60 * 60 * 1000);
		
		long diffHours = diff / (60 * 60 * 1000); 
		diff-=diffHours*(60 * 60 * 1000);
		
		long diffMinutes = diff / (60 * 1000); 
		diff-=diffMinutes *(60 * 1000);
		
		long diffSeconds = diff / 1000; 
		diff-=diffSeconds*1000;
		
		if (diffDays!=0) s=diffDays+" дней, ";
		if (diffHours!=0) s+=(diffHours+" часов, ");
		if (diffMinutes!=0) s+=(diffMinutes+" минут, ");
		if (diffSeconds!=0) s+=(diffSeconds+" секунд, ");
		if (diff!=0) s+=(diff+" миллисекунд");
		return s;
	}
   
	private void delTempFiles(){
		try{
		 if(buf_rec1_file!=null){
		 //  buf_rec1_file.deleteOnExit();
		   buf_rec1_file.delete();
		  }
		
		}catch(Exception e){
			System.out.println("delTempFiles:error:"+e);
		}
	}
	
	public void delClassifFiles(){
		
		System.out.println("delClassifFiles:01:"+file1.getAbsolutePath());
		try{
		 if(file1!=null){
		 File parent_dir = file1.getParentFile();
		 
		 System.out.println("delClassifFiles:02:"+parent_dir.getName());
		 
		    if(parent_dir!=null&&
		       parent_dir.isDirectory()){
		    	
		    	System.out.println("delClassifFiles:03");
		    	
		    	for(File file_in_dir : parent_dir.listFiles()){
		    		
		    		System.out.println("delClassifFiles:04:"+file_in_dir.getName());
		    		
		    		try{
		    		  file_in_dir.delete();
		    		}catch(Exception e){
		    			System.out.println("delClassifFiles:error:01:"+file_in_dir.getName());
		    			System.out.println("delClassifFiles:error:02:"+e);
		    		}
		    	}
		    }
		  }
		
		}catch(Exception e){
			System.out.println("delTempFiles:error:"+e);
		}
	}
	
	public static void main(String[] args) {
		
		 System.out.println("main:01");
		 
		 try{
	
			// String st = "1>00100044>>Уполномоченный по правам человека Косарев В.Н.>Специалист>Косарев В. Н.>>>>>>>>>>>>H>01.01.2000>01.01.2000>83>102>{B80A1CE3-AC6D-427D-9FCF-5353FDA5823C}>> ";
			// String st = "1>>>00100044>";
			// System.out.println("main:02:"+st.split(">",-1).length);
			 
			 PojoGRuNProFileLite pg = new  PojoGRuNProFileLite();
			 pg.file1=new File("/Development/test/classif/LoadScript/sources/1bISP20814.txt");
			 
			 pg.delClassifFiles();
			 
			 System.out.println("main:0100");
		 }catch(Exception e){
			 e.printStackTrace(System.out);
			 
			System.out.println("error:"+e);	  
		 }

	}
	
}
