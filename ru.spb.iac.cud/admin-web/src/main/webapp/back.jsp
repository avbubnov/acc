<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.jboss.seam.ScopeType,org.jboss.seam.Component,
                iac.grn.infosweb.session.Authenticator,org.jboss.seam.security._Identity,org.jboss.seam.contexts.Lifecycle,org.jboss.seam.contexts.Contexts"%>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Welcome!</title>
</head>
<body>
I by Back!<br/>
success:<%=request.getParameter("success")%>
<br/>
tokenID:<%=request.getParameter("tokenID")%>

<%
	/*
  Authenticator atr = (Authenticator)Component.getInstance("authenticator",ScopeType.EVENT);
  atr.authenticate();*/

  try{

	 System.out.println("back.jsp:01");
	 
     Lifecycle.beginCall();

     System.out.println("back.jsp:02");
     
     _Identity idt = (_Identity)Component.getInstance("org.jboss.seam.security.identity");
    
     System.out.println("back.jsp:03:idt:"+(idt==null));
     
     idt.login();
     
     System.out.println("back.jsp:04+");
     
  }catch(Exception e){
	 System.out.println("back.jsp:error:"+e);
  }finally{
	 Lifecycle.endCall();
  }
%>
</html>