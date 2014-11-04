package iac.cud.infosweb.ws;

import iac.grn.infosweb.session.audit.actions.ActionsMap;
import iac.grn.infosweb.session.audit.actions.ResourcesMap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.exceptions.InvalidCredentials;
import ru.spb.iac.cud.exceptions.TokenExpired;
import ru.spb.iac.cud.items.Attribute;
import ru.spb.iac.cud.items.AuditFunction;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.Token;
import ru.spb.iac.cud.services.access.AccessServices;
import ru.spb.iac.cud.services.sync.SyncServices;

@Name("ssClient")
public class SyncServiceClient {

	@Logger
	private Log log;

	static String endpointURI = "http://cudvm/CudServices/SyncService?wsdl";

	static String idIS = "urn:eis:cud";

	private SyncServices syncServices = null;

	

	public void invoke() {
		
		

	}

	public void sync_roles() throws Exception {
		try {
			List<Role> roles = new ArrayList<Role>();

			Role role = new Role();
			role.setCode("urn:role:operator");
			role.setName("���� ���������");
			role.setDescription("������� �������");
			roles.add(role);

			role = new Role();
			role.setCode("urn:role:manager");
			role.setName("���� ���������");
			role.setDescription("���������� ��������");
			roles.add(role);

			role = new Role();
			role.setCode("urn:role:user");
			role.setName("���� ������������");
			role.setDescription("�������� �������");
			roles.add(role);

			getPort(endpointURI).sync_roles(idIS, roles);

		} catch (GeneralFailure e1) {
			log.error("sync_roles:error1:" + e1);
			throw e1;
		}
	}

	public void sync_functions() throws Exception {

		List<Function> functions = new ArrayList<Function>();

		// ������������
		Function func = new Function();
		
		   func.setCode(ResourcesMap.USER.getCode()+":"+ActionsMap.CREATE.
		   getCode()); 
		   func.setName("�������� ������������");
		   func.setDescription("�������� ������������"); 
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.USER.getCode()+
		   ":"+ActionsMap.UPDATE.getCode());
		   func.setName("�������������� ������������");
		   func.setDescription("�������������� ������������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.USER.getCode()+
		   ":"+ActionsMap.UPDATE_ROLE.getCode());
		   func.setName("�������������� ����� ������������");
		   func.setDescription("�������������� ����� ������������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.USER.getCode()+
		   ":"+ActionsMap.UPDATE_GROUP.getCode());
		   func.setName("�������������� ����� ������������");
		   func.setDescription("�������������� ����� ������������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.USER.getCode()+
		   ":"+ActionsMap.DELETE.getCode());
		   func.setName("�������� ������������");
		   func.setDescription("�������� ������������"); 
		   functions.add(func);
		   
		   //���� 
		   func = new Function();
		   func.setCode(ResourcesMap.ROLE.getCode
		   ()+":"+ActionsMap.CREATE.getCode()); func.setName("�������� ����");
		   func.setDescription("�������� ����"); 
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.ROLE.getCode()+
		   ":"+ActionsMap.UPDATE.getCode());
		   func.setName("�������������� ����");
		   func.setDescription("�������������� ����"); 
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.ROLE.getCode()+
		   ":"+ActionsMap.DELETE.getCode()); 
		   func.setName("�������� ����");
		   func.setDescription("�������� ����");
		   functions.add(func);
		   
		   //������  ������������� 
		   func = new Function();
		   func.setCode(ResourcesMap
		   .UGROUP.getCode()+":"+ActionsMap.CREATE.getCode());
		   func.setName("�������� ������ �������������");
		   func.setDescription("�������� ������ �������������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.UGROUP.getCode(
		   )+":"+ActionsMap.UPDATE.getCode());
		   func.setName("�������������� ������ �������������");
		   func.setDescription("�������������� ������ �������������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.UGROUP.getCode(
		   )+":"+ActionsMap.UPDATE_ROLE.getCode());
		   func.setName("�������������� ����� ������ �������������");
		   func.setDescription("�������������� ����� ������ �������������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.UGROUP.getCode(
		   )+":"+ActionsMap.UPDATE_USER.getCode());
		   func.setName("�������������� ������������� ������ �������������");
		   func
		   .setDescription("�������������� ������������� ������ �������������!"
		   ); functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.UGROUP.getCode(
		   )+":"+ActionsMap.DELETE.getCode());
		   func.setName("�������� ������ �������������");
		   func.setDescription("�������� ������ �������������");
		   functions.add(func);
		   
		   //�� 
		   func = new Function();
		   func.setCode(ResourcesMap.IS.getCode
		   ()+":"+ActionsMap.CREATE.getCode()); 
		   func.setName("�������� ��");
		   func.setDescription("�������� ��"); functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.IS.getCode()+":"
		   +ActionsMap.UPDATE.getCode()); 
		   func.setName("�������������� ��");
		   func.setDescription("�������������� ��"); functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.IS.getCode()+":"
		   +ActionsMap.DELETE.getCode()); 
		   func.setName("�������� ��");
		   func.setDescription("�������� ��");
		   functions.add(func);
		   
		   //������� 
		   func = new Function();
		   func.setCode(ResourcesMap.RES.getCode
		   ()+":"+ActionsMap.CREATE.getCode());
		   func.setName("�������� �������");
		   func.setDescription("�������� �������"); 
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.RES.getCode()+":"
		   +ActionsMap.UPDATE.getCode());
		   func.setName("�������������� �������");
		   func.setDescription("�������������� �������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.RES.getCode()+":"
		   +ActionsMap.DELETE.getCode()); func.setName("�������� �������");
		   func.setDescription("�������� �������");
		   functions.add(func);
		   
		   //��������� ������������ 
		   func = new Function();
		   func.setCode(ResourcesMap
		   .CONF_PARAM.getCode()+":"+ActionsMap.UPDATE.getCode());
		   func.setName("�������������� ��������� ������������");
		   func.setDescription("�������������� ��������� ������������");
		   functions.add(func);
		   
		   //������ �������� ������������ 
		   func = new Function();
		   func.setCode
		   (ResourcesMap.AUDIT_USER.getCode()+":"+ActionsMap.START.getCode());
		   func.setName("������ ������������� ������ �������� ������������");
		   func
		   .setDescription("������ ������������� ������ �������� ������������");
		   functions.add(func);
		   
		   //������ ������� �������� 
		   func = new Function();
		   func.setCode(ResourcesMap
		   .AUDIT_SYS.getCode()+":"+ActionsMap.START.getCode());
		   func.setName("������ ������������� ������� ��������");
		   func.setDescription("������ ������������� ������� ��������");
		   functions.add(func);
		   
		   //������� ������������� ������ �������� ������������ 
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_ARCH_AUDIT_USER.getCode
		   ()+":"+ActionsMap.START.getCode());
		   func.setName("������ �������� ������������� ������ �������� ������������"
		   ); func.setDescription(
		   "������ �������� ������������� ������ �������� ������������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_ARCH_AUDIT_USER
		   .getCode()+":"+ActionsMap.PAUSE.getCode());
		   func.setName("����� �������� ������������� ������ �������� ������������"
		   ); func.setDescription(
		   "����� �������� ������������� ������ �������� ������������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_ARCH_AUDIT_USER
		   .getCode()+":"+ActionsMap.STOP.getCode()); 
		   func.setName(
		   "������� �������� ������������� ������ �������� ������������");
		   func.setDescription
		   ("������� �������� ������������� ������ �������� ������������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_ARCH_AUDIT_USER
		   .getCode()+":"+ActionsMap.SET_PARAM.getCode());
		   func.setName(
		   "��������� �������� ������������� ������ �������� ������������");
		   func.setDescription(
		   "��������� �������� ������������� ������ �������� ������������");
		   functions.add(func);
		   
		   //������� ������������� ������ ������ �������� 
		   func = new Function();
		   func
		   .setCode(ResourcesMap.PROC_ARCH_AUDIT_SYS.getCode()+":"+ActionsMap
		   .START.getCode());
		   func.setName("������ �������� ������������� ������ ������ ��������");
		   func.setDescription("������ �������� ������������� ������ ��������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_ARCH_AUDIT_SYS
		   .getCode()+":"+ActionsMap.PAUSE.getCode());
		   func.setName("����� �������� ������������� ������ ������ ��������");
		   func.setDescription("����� �������� ������������� ������ ��������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_ARCH_AUDIT_SYS
		   .getCode()+":"+ActionsMap.STOP.getCode());
		   func.setName("������� �������� ������������� ������ ������ ��������"
		   ); func.setDescription(
		   "������� �������� ������������� ������ ������ ��������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_ARCH_AUDIT_SYS
		   .getCode()+":"+ActionsMap.SET_PARAM.getCode());
		   func.setName("��������� �������� ������������� ������ ������ ��������"
		   ); func.setDescription(
		   "��������� �������� ������������� ������ ������ ��������");
		   functions.add(func);
		   
		   //������� ������������� ������� 
		   func = new Function();
		   func.setCode
		   (ResourcesMap.PROC_ARCH_TOKEN.getCode()+":"+ActionsMap
		   .START.getCode());
		   func.setName("������ �������� ������������� �������");
		   func.setDescription("������ �������� ������������� �������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_ARCH_TOKEN
		   .getCode()+":"+ActionsMap.PAUSE.getCode());
		   func.setName("����� �������� ������������� �������");
		   func.setDescription("����� �������� ������������� �������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_ARCH_TOKEN
		   .getCode()+":"+ActionsMap.STOP.getCode());
		   func.setName("������� �������� ������������� �������");
		   func.setDescription("������� �������� ������������� �������");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_ARCH_TOKEN
		   .getCode()+":"+ActionsMap.SET_PARAM.getCode());
		   func.setName("��������� �������� ������������� �������");
		   func.setDescription("��������� �������� ������������� �������");
		   functions.add(func);
		 
		
		   //��� �� ����������� 
		   func = new Function();
		   func.setCode(ResourcesMap
		   .PROC_BIND_UNBIND.getCode()+":"+ActionsMap.START.getCode());
		   func.setName("������ ��� �� ����������� ��� � ��� ����");
		   func.setDescription("������ ��� �� �����������  ��� � ��� ����");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_BIND_UNBIND
		   .getCode()+":"+ActionsMap.PAUSE.getCode());
		   func.setName("����� ��� �� �����������  ��� � ��� ����");
		   func.setDescription("����� ��� �� �����������  ��� � ��� ����");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_BIND_UNBIND
		   .getCode()+":"+ActionsMap.STOP.getCode());
		   func.setName("������� ��� �� �����������  ��� � ��� ����");
		   func.setDescription("������� ��� �� �����������  ��� � ��� ����");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_BIND_UNBIND
		   .getCode()+":"+ActionsMap.SET_PARAM.getCode());
		   func.setName("��������� ��� �� �����������  ��� � ��� ����");
		   func.setDescription("��������� ��� �� �����������  ��� � ��� ����");
		   functions.add(func);
		   
		   //��� �� �������� 
		   func = new Function();
		   func.setCode(ResourcesMap
		   .PROC_BIND_NOACT.getCode()+":"+ActionsMap.START.getCode());
		   func.setName("������ ��� �� �������� ��� � ��� ����");
		   func.setDescription("������ ��� �� ��������  ��� � ��� ����");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_BIND_NOACT
		   .getCode()+":"+ActionsMap.PAUSE.getCode());
		   func.setName("����� ��� �� ��������  ��� � ��� ����");
		   func.setDescription("����� ��� �� ��������  ��� � ��� ����");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_BIND_NOACT
		   .getCode()+":"+ActionsMap.STOP.getCode());
		   func.setName("������� ��� �� ��������  ��� � ��� ����");
		   func.setDescription("������� ��� �� ��������  ��� � ��� ����");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.PROC_BIND_NOACT
		   .getCode()+":"+ActionsMap.SET_PARAM.getCode());
		   func.setName("��������� ��� �� ��������  ��� � ��� ����");
		   func.setDescription("��������� ��� �� ��������  ��� � ��� ����");
		   functions.add(func);
		   
		   // �������� ��� � ��� ���� 
		   func = new Function();
		   func.setCode(ResourcesMap
		   .BINDING_IOGV.getCode()+":"+ActionsMap.CREATE.getCode());
		   func.setName("�������� �������� ��� � ��� ����");
		   func.setDescription("�������� �������� ��� � ��� ����");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.BINDING_IOGV.getCode
		   ()+":"+ActionsMap.UPDATE.getCode());
		   func.setName("��������� �������� ��� � ��� ����");
		   func.setDescription("��������� �������� ��� � ��� ����");
		   functions.add(func);
		   
		   func = new Function();
		   func.setCode(ResourcesMap.BINDING_IOGV.getCode
		   ()+":"+ActionsMap.UPDATE.getCode());
		   func.setName("�������� �������� ��� � ��� ����");
		   func.setDescription("�������� �������� ��� � ��� ����");
		   functions.add(func);
		 

		func = new Function();
		func.setCode(ResourcesMap.ROLE.getCode() + ":"
				+ ActionsMap.UPDATE_USER.getCode());
		func.setName("�������������� ������������� ����");
		func.setDescription("�������������� ������������� ����");
		functions.add(func);

		func = new Function();
		func.setCode(ResourcesMap.ROLE.getCode() + ":"
				+ ActionsMap.UPDATE_GROUP.getCode());
		func.setName("�������������� ����� ����");
		func.setDescription("�������������� ����� ����");
		functions.add(func);

		try {

			getPort(endpointURI).sync_functions(idIS, functions);

		} catch (GeneralFailure e1) {
			log.error("sync_functions:error1:" + e1.getMessage());
			throw e1;
		}
	}

	private SyncServices getPort(String endpointURI)
			throws MalformedURLException {

		if (this.syncServices == null) {
			QName serviceName = new QName("http://sync.services.cud.iac/",
					"SyncServicesImplService");
			URL wsdlURL = new URL(endpointURI);

			Service service = Service.create(wsdlURL, serviceName);

			QName portName = new QName("http://sync.services.cud.iac/",
					"SyncServicesImplPort");

			this.syncServices = service.getPort(portName, SyncServices.class);
		}
		return this.syncServices;
	}
}
