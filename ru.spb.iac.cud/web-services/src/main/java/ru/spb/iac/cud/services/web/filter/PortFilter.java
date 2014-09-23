package ru.spb.iac.cud.services.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortFilter implements Filter {

	Logger logger = LoggerFactory.getLogger(PortFilter.class);

	private List<String> available_ports;

	public PortFilter() {
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		boolean success = true;

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		logger.info("doFilter:01:" + available_ports);
		logger.info("doFilter:02:" + httpRequest.getServerPort());
		logger.info("doFilter:03:" + httpRequest.getServerName());
		/*
		 * System.out.println("PortFilter:doFilter:04:" +
		 * httpRequest.getLocalAddr());
		 * System.out.println("PortFilter:doFilter:05:" +
		 * httpRequest.getLocalName());
		 * System.out.println("PortFilter:doFilter:06:" +
		 * httpRequest.getLocalPort());
		 * System.out.println("PortFilter:doFilter:07:" +
		 * httpRequest.getRemoteAddr());
		 * System.out.println("PortFilter:doFilter:08:" +
		 * httpRequest.getRemoteHost());
		 * System.out.println("PortFilter:doFilter:09:" +
		 * httpRequest.getRemotePort());
		 * System.out.println("PortFilter:doFilter:010:" +
		 * httpRequest.getProtocol());
		 * System.out.println("PortFilter:doFilter:011:" +
		 * httpRequest.getScheme());
		 */
		logger.info("doFilter:012:" + httpRequest.getContextPath());

		if (available_ports != null) {

			if (!available_ports.contains(String.valueOf(httpRequest
					.getServerPort()))) {
				success = false;
			}
		}

		logger.info("doFilter:013:" + success);

		if (success) {
			chain.doFilter(request, response);
		} else {
			// httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND,
			// "Ресурс на порту "+httpRequest.getServerPort()+" не доступен");
			httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/**
	 * 
	 */
	public void init(FilterConfig fConfig) throws ServletException {

		String param = fConfig.getInitParameter("available_ports");
		logger.info("doFilter:init:01:" + param);

		// если параметр не задан, или без значения - то доступно на всех портах

		if (param != null && !param.trim().isEmpty()) {
			available_ports = new ArrayList<String>(Arrays.asList(param
					.split(",")));
			// fixed size
			// available_ports = Arrays.asList(param.split(","));
		}
	}

}
