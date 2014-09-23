package ru.spb.iac.cud.util;

import java.security.Principal;
import java.security.cert.X509Certificate;

import org.jboss.security.CertificatePrincipal;
import org.jboss.security.SimplePrincipal;

public class CudPrincipal implements Principal {

	private String systemName;

	private String userName;

	public CudPrincipal() {
	}

	public CudPrincipal(String systemName) {
		this.systemName = systemName;
	}

	public CudPrincipal(String systemName, String userName) {
		this.systemName = systemName;
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getName() {
		if (this.userName != null) {
			return this.userName;
		}

		return this.systemName;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

}
