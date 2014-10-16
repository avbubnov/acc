package ru.spb.iac.cud.uarm.web.context.reg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.spb.iac.cud.uarm.ejb.context.reg.UserAccessEJB;
import ru.spb.iac.cud.uarm.ejb.context.reg.UserRegEJB;
import ru.spb.iac.cud.uarm.ejb.entity.AcUsersKnlT;
import ru.spb.iac.cud.uarm.ejb.entity.GroupsAppAccessGrBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAccessBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAccessGroupsBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppUserBssT;
import ru.spb.iac.cud.uarm.ejb.entity.RolesAppAccessBssT;
import ru.spb.iac.cud.uarm.util.CUDUserConsoleConstants;
import ru.spb.iac.cud.uarm.web.context.user.UserSessionBean;
import test.ejb.HomeBean;

@ManagedBean(name = "userAccessBean")
@RequestScoped
public class UserAccessBean implements Serializable {

	final static Logger logger = LoggerFactory.getLogger(UserAccessBean.class);
	
	@EJB(beanName = "CUDUserConsole-ejb.jar#UserAccessEJB")
	private UserAccessEJB userAccessEJB;

	@ManagedProperty("#{userSessionBean}")
	private UserSessionBean userSessionBean;
	
	private static final long serialVersionUID = 1L;

	private String reason;

	public void action() {

		try {

			logger.info("userAccessRegBean:action:01");

			//String version = FacesContext.class.getPackage()
			//		.getImplementationVersion();

			String pidArm = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				     .get("idArm");
	
			logger.info("userAccessRegBean:action:03:" + pidArm);
			//logger.info("userAccessRegBean:action:04:" + userSessionBean.getSumRoles().size());

			
			HttpSession hs = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false); 
			Long authUserID = (Long) hs.getAttribute(CUDUserConsoleConstants.authUserID);
		
			
			JournAppAccessBssT t1 = new JournAppAccessBssT();
			
			t1.setAcIsBssTLong(new Long(pidArm));
			t1.setModeExec(1L); //ADD
			t1.setStatus(0L);
			//���������
			t1.setAcUsersKnlT2Long(authUserID);
			//���� ����������� ����
			t1.setAcUsersKnlT3Long(authUserID);
			
			t1.setCodeSystem("**2");
			t1.setLoginUser("**3");
			
			List<RolesAppAccessBssT> raa_list = new ArrayList<RolesAppAccessBssT>();
			
			for (Long idRole : userSessionBean.getSumRoles().keySet()){
			  
				RolesAppAccessBssT raa = new RolesAppAccessBssT();
			    raa.setAcRolesBssTLong(idRole);
			    
			    raa.setJournAppAccessBssT(t1);
			    //???
			    raa.setCodeRole("**1");	    
			    
			    raa_list.add(raa);
			}
			t1.setRolesAppAccessBssTs(raa_list);
			
			userAccessEJB.save(t1);

			// return "home?faces-redirect=true";
			FacesContext
					.getCurrentInstance()
					.getExternalContext()
					.redirect(
							((HttpServletRequest) FacesContext
									.getCurrentInstance().getExternalContext()
									.getRequest()).getContextPath()
									+ "/context/app/roles/list.xhtml");

		} catch (Exception e) {
			logger.error("userAccessRegBean:action:error:" + e);
		}
	}

	public void actionGroup() {

		try {

			logger.info("userAccessRegBean:actionGroup:01");

			//String version = FacesContext.class.getPackage()
			//		.getImplementationVersion();

			String pidArm = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				     .get("idArm");
	
			logger.info("userAccessRegBean:actionGroup:03:" + pidArm);
			//logger.info("userAccessRegBean:action:04:" + userSessionBean.getSumRoles().size());

			
			HttpSession hs = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false); 
			Long authUserID = (Long) hs.getAttribute(CUDUserConsoleConstants.authUserID);
		
			
			JournAppAccessGroupsBssT t1 = new JournAppAccessGroupsBssT();
			
			t1.setAcIsBssTLong(new Long(pidArm));
			t1.setModeExec(1L); //ADD
			t1.setStatus(0L);
			//���������
			t1.setAcUsersKnlT2Long(authUserID);
			//���� ����������� ������
			t1.setAcUsersKnlT3Long(authUserID);
			
			List<GroupsAppAccessGrBssT> raa_list = new ArrayList<GroupsAppAccessGrBssT>();
			
			for (Long idGroup : userSessionBean.getSumGroups().keySet()){
			  
				GroupsAppAccessGrBssT raa = new GroupsAppAccessGrBssT();
			    raa.setGroupUsersKnlTLong(idGroup);
			    
			    raa.setJournAppAccessGroupsBssT(t1);
			     
			    raa_list.add(raa);
			}
			t1.setGroupsAppAccessGrBssTs(raa_list);
			
			userAccessEJB.saveGroup(t1);

			// return "home?faces-redirect=true";
			FacesContext
					.getCurrentInstance()
					.getExternalContext()
					.redirect(
							((HttpServletRequest) FacesContext
									.getCurrentInstance().getExternalContext()
									.getRequest()).getContextPath()
									+ "/context/app/groups/list.xhtml");

		} catch (Exception e) {
			logger.error("userAccessRegBean:actionGroup:error:" + e);
			e.printStackTrace(System.out);
		}
	}
	
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public UserSessionBean getUserSessionBean() {
		return userSessionBean;
	}

	public void setUserSessionBean(UserSessionBean userSessionBean) {
		this.userSessionBean = userSessionBean;
	}

}
