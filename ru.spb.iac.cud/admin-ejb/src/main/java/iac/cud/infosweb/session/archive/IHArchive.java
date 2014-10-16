package iac.cud.infosweb.session.archive;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;

import iac.cud.infosweb.dataitems.BaseParamItem;
import iac.cud.infosweb.local.service.IHLocal;
import iac.cud.infosweb.local.service.ServiceReestr;
import iac.cud.infosweb.local.service.ServiceReestrAction;

@Stateless
public class IHArchive extends IHArchiveBase implements IHLocal {

	@PersistenceContext(unitName = "InfoSCUD-web")
	EntityManager em;

}
