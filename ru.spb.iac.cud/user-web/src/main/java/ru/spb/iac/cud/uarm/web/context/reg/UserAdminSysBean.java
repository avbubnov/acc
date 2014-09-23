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
import ru.spb.iac.cud.uarm.ejb.context.reg.UserAdminSysEJB;
import ru.spb.iac.cud.uarm.ejb.context.reg.UserRegEJB;
import ru.spb.iac.cud.uarm.ejb.entity.AcUsersKnlT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAccessBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAdminUserSysBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppUserBssT;
import ru.spb.iac.cud.uarm.ejb.entity.RolesAppAccessBssT;
import ru.spb.iac.cud.uarm.util.CUDUserConsoleConstants;
import ru.spb.iac.cud.uarm.web.context.user.UserSessionBean;
import test.ejb.HomeBean;

@ManagedBean(name = "userAdminSysBean")
@RequestScoped
public class UserAdminSysBean implements Serializable {

	final static Logger logger = LoggerFactory.getLogger(UserAdminSysBean.class);
	
	@EJB(beanName = "CUDUserConsole-ejb.jar#UserAdminSysEJB")
	private UserAdminSysEJB userAdminSysEJB;

	@ManagedProperty("#{userSessionBean}")
	private UserSessionBean userSessionBean;
	
	private static final long serialVersionUID = 1L;

	private String reason;

	private Long roleCUD;
	
	public void action() {

		try {

			logger.info("userAdminSysBean:action:01");

			//String version = FacesContext.class.getPackage()
			//		.getImplementationVersion();

			String pidArm = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				     .get("idArm");
	
			logger.info("userAdminSysBean:action:03:" + pidArm);
	
			
			HttpSession hs = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false); 
			Long authUserID = (Long) hs.getAttribute(CUDUserConsoleConstants.authUserID);
		
			
			JournAppAdminUserSysBssT t1 = new JournAppAdminUserSysBssT();
			
			t1.setAcIsBssTLong(new Long(pidArm));
			t1.setModeExec(1L); //ADD
			t1.setStatus(0L);
			//заявитель
			t1.setAcUsersKnlT2Long(authUserID);
			//кому назначаются роли
			t1.setAcUsersKnlT1Long(authUserID);
			
			userAdminSysEJB.save(t1);

			// return "home?faces-redirect=true";
			FacesContext
					.getCurrentInstance()
					.getExternalContext()
					.redirect(
							((HttpServletRequest) FacesContext
									.getCurrentInstance().getExternalContext()
									.getRequest()).getContextPath()
									+ "/context/app/admin/list.xhtml");

		} catch (Exception e) {
			logger.error("userAdminSysBean:action:error:" + e);
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

	public Long getRoleCUD() {
		return roleCUD;
	}

	public void setRoleCUD(Long roleCUD) {
		this.roleCUD = roleCUD;
	}

}
