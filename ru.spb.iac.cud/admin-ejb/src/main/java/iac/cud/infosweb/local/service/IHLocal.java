package iac.cud.infosweb.local.service;

import iac.cud.infosweb.dataitems.BaseParamItem;
import java.util.Date;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import javax.ejb.Local;

@Local
public interface IHLocal {
	public BaseParamItem run(BaseParamItem paramMap)throws Exception;
}
