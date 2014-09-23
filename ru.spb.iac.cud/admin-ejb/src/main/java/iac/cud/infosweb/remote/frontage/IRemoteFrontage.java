package iac.cud.infosweb.remote.frontage;

import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.Context;
import javax.naming.InitialContext;

import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.local.service.IHLocal;
import iac.cud.infosweb.local.service.ReestrItem;

@Stateless
public class IRemoteFrontage implements IRemoteFrontageLocal {

	private Context ctx = null;

	@PostConstruct
	public void init() {
		try {
			if (ctx == null) {
				ctx = new InitialContext();
			}
		} catch (Exception e) {
		}
	}

	@PreDestroy
	public void remove() {
		try {
			if (ctx != null) {
				ctx.close();
			}
		} catch (Exception e) {
		}
	}

	public BaseParamItem run(BaseParamItem paramMap) throws Exception {

		BaseParamItem jpi = new BaseParamItem();
		String service = null;

		String gtype = (String) paramMap.get("gtype");

		System.out.println("IRemoteFrontage:run:gtype:" + gtype);

		String url = ReestrItem.getUrl(gtype);

		
		if (url == null) {
			return null;
		}

		return ((IHLocal) ctx.lookup(url)).run(paramMap);
	}

}
