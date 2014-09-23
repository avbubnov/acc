package ru.spb.iac.cud.sts.core.attrib;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;

class SecurityActions {
	static SecurityContext getSecurityContext() {
		SecurityManager sm = System.getSecurityManager();

		if (sm != null) {
			return AccessController
					.doPrivileged(new PrivilegedAction<SecurityContext>() {
						public SecurityContext run() {
							return SecurityContextAssociation
									.getSecurityContext();
						}
					});
		} else {
			return SecurityContextAssociation.getSecurityContext();
		}
	}

	static SecurityContext createSecurityContext()
			throws PrivilegedActionException {
		SecurityManager sm = System.getSecurityManager();

		if (sm != null) {
			return AccessController
					.doPrivileged(new PrivilegedExceptionAction<SecurityContext>() {
						public SecurityContext run() throws Exception {
							return SecurityContextFactory
									.createSecurityContext("CLIENT");
						}
					});
		} else {
			try {
				return SecurityContextFactory.createSecurityContext("CLIENT");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	static String getSystemProperty(final String key) {
		SecurityManager sm = System.getSecurityManager();

		if (sm != null) {
			return AccessController
					.doPrivileged(new PrivilegedAction<String>() {
						public String run() {
							return System.getProperty(key);
						}
					});
		} else {
			return System.getProperty(key);
		}
	}
}