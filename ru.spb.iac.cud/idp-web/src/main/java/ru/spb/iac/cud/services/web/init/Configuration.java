package ru.spb.iac.cud.services.web.init;


public class Configuration{
 
  private static boolean signRequired;

 // private static boolean encryptRequired;
  
  public static boolean isSignRequired() {
	return signRequired;
  }

  public static void setSignRequired(boolean signRequired) {
	Configuration.signRequired = signRequired;
  }
/*
  public static boolean isEncryptRequired() {
	return encryptRequired;
  }

  public static void setEncryptRequired(boolean encryptRequired) {
	Configuration.encryptRequired = encryptRequired;
  }*/   

  
} 