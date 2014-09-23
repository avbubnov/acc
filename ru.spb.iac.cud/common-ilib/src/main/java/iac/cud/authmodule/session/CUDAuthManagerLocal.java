package iac.cud.authmodule.session;

import iac.cud.authmodule.dataitem.AuthItem;
import iac.cud.authmodule.dataitem.PageItem;

import java.util.ArrayList;
import java.util.Map;

import javax.ejb.Local;

/**
 * Предоставление информации о наличии прав доступа, jndi name:
 * authModule.AuthManager.local
 * 
 * @author bubnov
 */
@Local
public interface CUDAuthManagerLocal {
	public Map<String, ArrayList<String>[]> authComplete(Long appCode,
			String login, String password) throws Exception;

	/**
	 * Аутентификация пользователя
	 * 
	 * @param login
	 *            логин
	 * @param password
	 *            пароль
	 * @return ид пользователя, если упешно, иначе null
	 * @throws Exception
	 */
	public Long authenticate(String login, String password) throws Exception;

	public ArrayList<String>[] access(Long appCode, String pageCode, Long idUser)
			throws Exception;

	/**
	 * Аутентификация и авторизация пользователя
	 * 
	 * @param appCode
	 *            ид приложения
	 * @param login
	 *            логин
	 * @param password
	 *            пароль
	 * @return AuthItem, если упешно, иначе null
	 * @throws Exception
	 */
	public AuthItem authCompleteItem(Long appCode, String login, String password)
			throws Exception;

	/**
	 * Аутентификация и авторизация пользователя
	 * 
	 * @param appCode
	 *            ид приложения
	 * @param login
	 *            логин
	 * @param password
	 *            пароль
	 * @return AuthItem, если упешно, иначе null
	 * @throws Exception
	 */
	public PageItem accessItem(Long appCode, String pageCode, Long idUser)
			throws Exception;

}
