package iac.cud.infosweb.ws.classifierzip.clientsample;

import java.math.BigInteger;
import java.security.MessageDigest;

public class CUDWSSMEVConstants {

    private static String login="bubnov";
	
	private static String password="9753560";
	
	private static String passwordMD5;
	
	private static String classifFullName = "Классификатор Исполнительные органы государственной власти Санкт-Петербурга, их подразделения и должностные лица";
	
	private static int classifRegNum = 66;
	
	public static String fileZipPath = "D:\\Development\\test\\classif\\LoadScript\\sources\\";
	
	public static String fileLoadScriptPath = "D:\\Development\\test\\classif\\LoadScript\\";
	
	public static String fileLoadScriptName = "insert.bat";
	
    static {
		
      	byte[] bytesOfMessage;
		try {
			
		   bytesOfMessage = password.getBytes("UTF-8");
		
	       MessageDigest md = MessageDigest.getInstance("MD5");
	    	
		   byte[] thedigest = md.digest(bytesOfMessage);

		   passwordMD5=new BigInteger(1, thedigest).toString(16);
		   
		   System.out.println("passwordMD5:"+passwordMD5);
		   
		  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }

	public static String getLogin() {
		return login;
	}

	public static void setLogin(String login) {
		CUDWSSMEVConstants.login = login;
	}

	public static String getPasswordMD5() {
		return passwordMD5;
	}

	public static void setPasswordMD5(String passwordMD5) {
		CUDWSSMEVConstants.passwordMD5 = passwordMD5;
	}

	public static String getClassifFullName() {
		return classifFullName;
	}

	public static void setClassifFullName(String classifFullName) {
		CUDWSSMEVConstants.classifFullName = classifFullName;
	}

	public static int getClassifRegNum() {
		return classifRegNum;
	}

	public static void setClassifRegNum(int classifRegNum) {
		CUDWSSMEVConstants.classifRegNum = classifRegNum;
	}
	
}
