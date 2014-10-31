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
import ru.spb.iac.cud.uarm.ejb.context.reg.UserOrgManEJB;
import ru.spb.iac.cud.uarm.ejb.context.reg.UserRegEJB;
import ru.spb.iac.cud.uarm.ejb.entity.AcUsersKnlT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAccessBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppAdminUserSysBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppOrgManagerBssT;
import ru.spb.iac.cud.uarm.ejb.entity.JournAppUserBssT;
import ru.spb.iac.cud.uarm.ejb.entity.RolesAppAccessBssT;
import ru.spb.iac.cud.uarm.util.CUDUserConsoleConstants;
import ru.spb.iac.cud.uarm.web.context.user.UserSessionBean;
import test.ejb.HomeBean;

@ManagedBean(name = "userOrgManBean")
@RequestScoped
public class UserOrgManBean implements Serializable {

	final static Logger LOGGER = LoggerFactory.getLogger(UserOrgManBean.class);
	
	@EJB(beanName = "CUDUserConsole-ejb.jar#UserOrgManEJB")
	private UserOrgManEJB userOrgManEJB;

	@ManagedProperty("#{userSessionBean}")
	private UserSessionBean userSessionBean;
	
	private static final long serialVersionUID = 1L;

	private String reason;

	private Long roleCUD;
	
	public void action() {

		try {

			LOGGER.debug("userOrgManBean:action:01");

			
			
			HttpSession hs = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false); 
			Long authUserID = (Long) hs.getAttribute(CUDUserConsoleConstants.authUserID);
		
			
			JournAppOrgManagerBssT t1 = new JournAppOrgManagerBssT();
			
			t1.setModeExec(1L); //ADD
			t1.setStatus(0L);
			//���������
			t1.setAcUsersKnlT2Long(authUserID);
			//���� ����������� ����
			t1.setAcUsersKnlT1Long(authUserID);
			
			userOrgManEJB.save(t1);

			FacesContext
					.getCurrentInstance()
					.getExternalContext()
					.redirect(
							((HttpServletRequest) FacesContext
									.getCurrentInstance().getExternalContext()
									.getRequest()).getContextPath()
									+ "/context/app/org_man/list.xhtml");

		} catch (Exception e) {
			LOGGER.error("userOrgManBean:action:error:", e);
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
