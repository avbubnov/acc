package ru.spb.iac.cud.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.xml.security.utils.Base64;

/**
 * ����� ��� ��������� ��������� �������� � ������� �� � Base64 ������
 * @author bubnov
 *
 */
public class TIDEncode {

	/**
	 * ��������� ��������� �������� � ������� �� � Base64 ������
	 */
	public static String getSecret() {

		Random random = new Random();

		byte[] keyRandom = new byte[3];
		random.nextBytes(keyRandom);

		String key = Base64.encode(keyRandom);
		
		key += Long.toString(System.currentTimeMillis());

		String result = Base64.encode(key.getBytes());
	
		return result;
	}


}
