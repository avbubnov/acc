package iac.cud.infosweb.dataitems;

import java.io.Serializable;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;


@Name("testAction")
public class TestAction implements Serializable {
	
	   private static final long serialVersionUID = 1L;
	
	   @Out
       private TestComponent testComponent;
	   
	   public void test() {
	   	 System.out.println("TestAction:test:01");
	     TestComponent tc= new TestComponent();
	     tc.setTableName("001");
	     testComponent=tc;
	   }

}
