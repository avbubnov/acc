package iac.cud.infosweb.dataitems;

import java.io.Serializable;
import java.util.Date;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;


@Name("testComponent")
public class TestComponent implements Serializable {
	
	   private static final long serialVersionUID = 1L;
	
	   	private String tableName;

	   	@Create
		public void create() {
			 System.out.println("TestComponent:create");
		/*	 try{
				   throw new NullPointerException();
			   }catch(Exception e){
				  e.printStackTrace(System.out);
			   }*/
		}
	    public TestComponent() {
	    	 System.out.println("TestComponent:TestComponent");
	    }

		public String getTableName() {
			System.out.println("TestComponent:getTableName:tableName:"+tableName);
			return this.tableName;
		}
    	public void setTableName(String tableName) {
			this.tableName = tableName;
		}
}
