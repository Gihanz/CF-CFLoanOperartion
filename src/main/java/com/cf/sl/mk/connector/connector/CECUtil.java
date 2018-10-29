package com.cf.sl.mk.connector.connector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;

import com.cf.sl.mk.connector.util.PropertyReader;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.util.UserContext;

import filenet.vw.api.VWAttachment;
import filenet.vw.api.VWAttachmentType;
import filenet.vw.api.VWFieldType;
import filenet.vw.api.VWLibraryType;
import filenet.vw.api.VWParameter;
import filenet.vw.api.VWRoster;
import filenet.vw.api.VWRosterQuery;
import filenet.vw.api.VWSession;
import filenet.vw.api.VWStepElement;
import filenet.vw.api.VWWorkObject;

public class CECUtil {

	private String uname = null;
	private String password = null;
	private String cp = null;
	private String ceuri = null;
	private String objName=null;
	private String folderpath=null;
	private String mimeType=null;
	public static Logger log = Logger.getLogger(CECUtil.class);

	public static ObjectStore objectStore = null;

	public static Domain domain = null;
	public static Connection connection = null;
	private String jaaspath = null;


	public CECUtil()
	{
		try
		{
			PropertyReader pr = new PropertyReader();
			Properties prop = pr.loadPropertyFile();
			this.uname = prop.getProperty("USERNAME");
			this.password = prop.getProperty("PASSWORD");
			this.ceuri = prop.getProperty("CEURI");
			this.cp = prop.getProperty("CONNECTION");
			this.objName=prop.getProperty("OBJECTSTORENAME");
			this.folderpath=prop.getProperty("BUSINESSDOCPATH");
			this.mimeType=prop.getProperty("MIMETYPE");
			this.jaaspath = prop.getProperty("JAAS_PATH");
			System.out.println("Properties from Connection file is Uname is " + this.uname + " Password is " + this.password + " CE URI " + this.ceuri + " Connection point " + this.cp);
		}
		catch (Exception e)
		{
			log.info("Error Occured while initiating connector class");
		}
	}


	public String  addDocumentWithStream(ByteArrayOutputStream fip,  String docName, String docClass, String WobNum) throws EngineRuntimeException{
		
		System.setProperty("java.security.auth.login.config", jaaspath);

		connection = Factory.Connection.getConnection(ceuri);
		Subject sub = UserContext.createSubject(connection, uname,password,null);
		UserContext.get().pushSubject(sub);
		domain = Factory.Domain.getInstance(connection, null);
		objectStore = Factory.ObjectStore.fetchInstance(domain, objName, null);
		Folder folder = Factory.Folder.fetchInstance(objectStore,folderpath, null);

		log.info("Loan Folder for saving PDF Folder ID : " + folder.get_Id());

		Document doc = Factory.Document.createInstance(objectStore, null);

		ContentElementList contEleList = Factory.ContentElement.createList();
		ContentTransfer ct = Factory.ContentTransfer.createInstance();

		ct.setCaptureSource(new ByteArrayInputStream(fip.toByteArray()));
		ct.set_ContentType(mimeType);
		ct.set_RetrievalName(docName);
		contEleList.add(ct);

		doc.set_ContentElements(contEleList);
		doc.getProperties().putValue("DocumentTitle", docName);
	//	doc.getProperties().putValue("BusinessLoanDocType", "Credit Proposal Form");
		

		doc.set_MimeType(mimeType);
		doc.checkin(AutoClassify.AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
		doc.save(RefreshMode.REFRESH);

		ReferentialContainmentRelationship rcr = folder.file(doc,AutoUniqueName.AUTO_UNIQUE, docName, DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
		rcr.save(RefreshMode.REFRESH);
		log.info("Version Series ID of new document is : "+doc.get_VersionSeries().get_Id());


		return doc.get_VersionSeries().get_Id().toString();

	}

}
