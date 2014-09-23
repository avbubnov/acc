package mypackage;

import java.util.Date;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.jboss.seam.annotations.Logger;
//import org.apache.log4j.Logger;

/**
 * MyPhaseListener. The phase listener which prints the current phase to the system console.
 *
 * @author BalusC
 * @link http://balusc.blogspot.com/2006/06/using-datatables.html
 */

public class MyPhaseListener implements PhaseListener {

	//@Logger private Log log;
	
	//private Logger log = Logger.getLogger(MyPhaseListener.class);
	
	final static Logger logger = LoggerFactory.getLogger(MyPhaseListener.class);
	
    private Long phaseTime;
    private Long requestTime;
    
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    /**
     * This is invoked at the beginning of any JSF lifecycle phase.
     * @param event The Phase Event.
     */
    public void beforePhase(PhaseEvent event) {
    	if(event.getPhaseId().equals(PhaseId.RESTORE_VIEW)){
    		requestTime=System.currentTimeMillis();
    	}
    	phaseTime=System.currentTimeMillis();
    	
        logger.debug("Phase Start: " + event.getPhaseId());
      //  log.info("Phase Start: " + event.getPhaseId());
     }

    /**
     * This is invoked at the end of any JSF lifecycle phase.
     * @param event The Phase Event.
     */
    public void afterPhase(PhaseEvent event) {
    	logger.debug("Phase Finish: " + event.getPhaseId());
    	logger.debug("Phase Time: " + (System.currentTimeMillis()-phaseTime));
      if(event.getPhaseId().equals(PhaseId.RENDER_RESPONSE)){
    	  logger.debug("Request Time: " + (System.currentTimeMillis()-requestTime));
  	 }
   }
}
