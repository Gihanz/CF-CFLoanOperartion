package com.cf.sl.mk.connector.connector;

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

public class DocumentUtil {

	/*private String uname = null;
	private String password = null;
	private String cp = null;
	private String ceuri = null;
	private String objName=null;
	public static Logger log = Logger.getLogger(DocumentUtil.class);

	public static ObjectStore objectStore = null;

	public static Domain domain = null;
	public static Connection connection = null;

	public DocumentUtil()
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
			System.out.println("Properties from Connection file is Uname is " + this.uname + " Password is " + this.password + " CE URI " + this.ceuri + " Connection point " + this.cp);
		}
		catch (Exception e)
		{
			System.out.println("Error Occured while initiating connector class");
		}
	}



	public void initialize()
	{

		connection = Factory.Connection.getConnection(ceuri);
		Subject sub = UserContext.createSubject(connection, uname,password,"FileNetP8WSI");
		UserContext.get().pushSubject(sub);
		domain = Factory.Domain.getInstance(connection, null);
		objectStore = Factory.ObjectStore.fetchInstance(domain, objName, null);
		System.out.println("objectStore : " + objectStore.get_DisplayName());
	}



	public void addDocumentWithStream(String folderPath,
			InputStream inputStream, String mimeType, 
			String docName, String docClass, String WobNum) {

		Folder folder = Factory.Folder.fetchInstance(objectStore,
				folderPath, null);

		System.out.println("Folder ID : " + folder.get_Id());
		// Document doc = Factory.Document.createInstance(os, classId);

		Document doc = Factory.Document.createInstance(objectStore, null);

		ContentElementList contEleList = Factory.ContentElement.createList();
		ContentTransfer ct = Factory.ContentTransfer.createInstance();

		ct.setCaptureSource(inputStream);
		ct.set_ContentType(mimeType);
		ct.set_RetrievalName(docName);
		contEleList.add(ct);

		doc.set_ContentElements(contEleList);
		doc.getProperties().putValue("DocumentTitle", docName);

		doc.set_MimeType(mimeType);
		doc.checkin(AutoClassify.AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
		doc.save(RefreshMode.REFRESH);

		ReferentialContainmentRelationship rcr = folder.file(doc,
				AutoUniqueName.AUTO_UNIQUE, docName, 
				DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
		rcr.save(RefreshMode.REFRESH);
		System.out.println("Version Series ID : "+doc.get_VersionSeries().get_Id());
		VWSession vwSession = null;
		Connection conn = Factory.Connection.getConnection(ceuri);
		Subject subject = UserContext.createSubject(conn, uname, password, "FileNetP8WSI");
		System.out.println("Subject Created");
		UserContext uc = UserContext.get();
		uc.pushSubject(subject);
		System.out.println(">>>>>>>>>>>>>>>>>>>>> "+uname+" - "+password+" - "+cp);
		try
		{   	

			vwSession = new VWSession();
			vwSession.setBootstrapCEURI(ceuri);
			vwSession.logon(uname, password, cp);

			System.out.println("Is Logged to CE : "+vwSession.isLoggedOn());

			VWRoster roster = vwSession.getRoster("BusinessLoanRoster");
			System.out.println(roster);
			roster.setBufferSize(1);
			Object[] queryMin = new Object[5];
			Object[] queryMax = new Object[5];
			queryMin[0] = WobNum;
			queryMax[0] = WobNum;

			VWRosterQuery query = roster.createQuery("F_WobNum", queryMin, queryMax, 100, null, null, 1);

			log.info("queries returned for this Query  " + query.fetchCount());
			System.out.println("fetchCount : "+query.fetchCount());

			VWWorkObject vwStepElement = (VWWorkObject)query.next();

			VWStepElement stepElement1 = vwStepElement.fetchStepElement();
			stepElement1.doLock(true);
			// VWAttachment[] vwattach = (VWAttachment[]) stepElement1.getParameterValue("LoanDocuments");
			VWParameter[] ss =stepElement1.getParameters(VWFieldType.FIELD_TYPE_ATTACHMENT, VWStepElement.FIELD_USER_DEFINED);
			VWAttachment[] finalatt=null;

			for(VWParameter qwe : ss)
			{

				if(qwe.getName().equalsIgnoreCase("LoanDocuments"))
				{
					finalatt = (VWAttachment[]) qwe.getValue();
				}
			}
			VWAttachment settingAttachments[]=new VWAttachment[finalatt.length+1]; 
			int j=0;
			for(VWAttachment attach: finalatt)
			{
				settingAttachments[j]=attach;
				j++;
			} 
			VWAttachment myattach = new VWAttachment();

			settingAttachments[j]=myattach;
			myattach.setId(doc.get_VersionSeries().get_Id().toString());    /// this should be document version series id after uploading document to CE
			myattach.setLibraryName(objName);
			myattach.setLibraryType(VWLibraryType.LIBRARY_TYPE_CONTENT_ENGINE);
			myattach.setAttachmentDescription("Case Details Document");
			myattach.setType(VWAttachmentType.ATTACHMENT_TYPE_DOCUMENT);

			//finalatt[0]=myattach;
			stepElement1.setParameterValue("LoanDocuments", settingAttachments, false);
			stepElement1.doSave(true);
		}catch (Exception e)
		{
			e.fillInStackTrace();
			log.error("Error Occured While Getting WorkObject");
		}finally
		{
			disconnect(vwSession);
		}

	}

	public void disconnect(VWSession session)
	{
		if (log.isDebugEnabled()) {
			log.debug("> disconnect PE");
		}
		if ((session != null) && (session.isLoggedOn()))
		{
			log.info("Inside PE Logging OFF");
			session.logoff();
			session = null;
		}
		if (log.isDebugEnabled()) {
			log.debug("< disconnected PE Successfully");
		}
	}*/



}