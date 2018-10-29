package com.cf.sl.mk.connector.connector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.json.JSONObject;
import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.cf.sl.mk.connector.util.PropertyReader;

import filenet.vw.api.VWAttachment;
import filenet.vw.api.VWAttachmentType;
import filenet.vw.api.VWDataField;
import filenet.vw.api.VWFetchType;
import filenet.vw.api.VWFieldType;
import filenet.vw.api.VWLibraryType;
import filenet.vw.api.VWParameter;
import filenet.vw.api.VWRoster;
import filenet.vw.api.VWRosterQuery;
import filenet.vw.api.VWSession;
import filenet.vw.api.VWStepElement;
import filenet.vw.api.VWWorkObject;

public class PEConnector {

	private VWSession  session = null;
	private String  uname = null;
	private String  password = null;
	private String  cp = null;
	private String  ceuri = null;
	private String objName=null;
	public static Logger log = Logger.getLogger(PEConnector.class);


	public PEConnector() {
		Properties prop;
		try {
			PropertyReader pr = new PropertyReader();
			prop = pr.loadPropertyFile();
			uname =prop.getProperty("USERNAME");
			password =prop.getProperty("PASSWORD");
			ceuri =prop.getProperty("CEURI");
			cp =prop.getProperty("CONNECTION");
			this.objName=prop.getProperty("OBJECTSTORENAME");
			log.debug("Properties from Connection file is "+"Uname is "+uname+" Password is "+password+" CE URI "+ceuri+" Connection point "+cp );
			System.out.println("Properties from Connection file is "+"Uname is "+uname+" Password is "+password+" CE URI "+ceuri+" Connection point "+cp );
		} catch (Exception e) {
			log.error("Error Occured while initiating connector class");
		}
	}

	public VWSession getPESession(String username,String password,String cpName,String ceuri)
	{
		log.debug("Inside Get PE connection Method");
		System.out.println("Inside Get PE Connection Method");

		//if (!session.isLoggedOn())
		{
			session = new VWSession();
			session.setBootstrapCEURI(ceuri);
			session.logon(username,password,cpName);

			System.out.println("Seesion in "+session.isLoggedOn());
		}

		System.out.println("Get PE Connection END");
		return session;


	}

	public Map<String, Object> getWorkObject(String WobNumber,String rost)
	{
		Map<String, Object> obj = new HashMap<String, Object>() ;
		VWSession pesession=null;
		try
		{
			
			pesession= getPESession(uname, password, cp, ceuri);
			pesession.isLoggedOn();
			final VWRoster roster = pesession.getRoster(rost);           
			roster.setBufferSize(1);
			final Object[] queryMin = new Object[5];
			final Object[] queryMax = new Object[5];
			queryMin[0] = WobNumber;
			queryMax[0] = WobNumber;
			//final VWRosterQuery query = roster.createQuery(null, null, null, queryFlags, filter1, null, VWFetchType.FETCH_TYPE_WORKOBJECT);
			final VWRosterQuery query = roster.createQuery("F_WobNum", queryMin, queryMax, VWRoster.QUERY_READ_UNWRITABLE+VWRoster.QUERY_MIN_VALUES_INCLUSIVE+VWRoster.QUERY_MAX_VALUES_INCLUSIVE, null, null, VWFetchType.FETCH_TYPE_WORKOBJECT);

			log.info("queries returned for this Query  "+query.fetchCount());
			System.out.println("queries returned for this Query  "+query.fetchCount());

			VWWorkObject vwStepElement;
			vwStepElement = (VWWorkObject) query.next();
			VWDataField[] ss = vwStepElement.getDataFields(VWFieldType.ALL_FIELD_TYPES, VWWorkObject.FIELD_USER_DEFINED);
			log.info("Retrived Field  Legnth is "+ss.length);


			for(int i=0;i<ss.length;i++)
			{
				log.trace("Name"+ss[i].getName());
				log.trace("value"+ss[i].getStringValue());

				if( ss[i].getValue() instanceof Double)
				{
					obj.put(ss[i].getName(), (Double)ss[i].getValue());
				}
				else if( ss[i].getValue() instanceof Boolean)
				{
					obj.put(ss[i].getName(), (Boolean)ss[i].getValue());

				}else if( ss[i].getValue() instanceof Float)
				{
					obj.put(ss[i].getName(), (Float)ss[i].getValue());
				}
				else if( ss[i].getValue() instanceof String)
				{
					obj.put(ss[i].getName(), ss[i].getStringValue());
				}
				else if( ss[i].getValue() instanceof Date)
				{
					obj.put(ss[i].getName(), ss[i].getValue().toString());
				}
				else if( ss[i].getValue() instanceof Integer)
				{
					obj.put(ss[i].getName(), (Integer)ss[i].getValue());
				}
				else if( ss[i].getValue() instanceof String[])
				{
					JSONArray list = new JSONArray((String[])ss[i].getValue());
					obj.put(ss[i].getName(), list.toList());
				}
				else if( ss[i].getValue() instanceof Integer[])
				{
					JSONArray list = new JSONArray((Integer[])ss[i].getValue());
					obj.put(ss[i].getName(), list.toList());
				}
				else if( ss[i].getValue() instanceof Date[])
				{	
					JSONArray list = new JSONArray((Date[])ss[i].getValue());
					obj.put(ss[i].getName(), list.toList());
				}
				else if( ss[i].getValue() instanceof Double[])
				{
					JSONArray list = new JSONArray((Double[])ss[i].getValue());
					obj.put(ss[i].getName(), list.toList());
				}
				else {
					log.info("Key Not Avilable "+ss[i].getName());
				}

			}
			JSONObject sss = new JSONObject(obj);
			log.info("Final JSON Object is "+sss);
			System.out.println(sss);

			//final VWDataField dataFields[] = workObject.getDataFields(VWFieldType.ALL_FIELD_TYPES, VWWorkObject.FIELD_USER_AND_SYSTEM_DEFINED);

		} catch (Exception e) {
			log.error("Error Occured While Getting WorkObject");
		}finally {
			disconnect(pesession);
		}
		return obj;

	}



	public Map<String, Object> getWorkObject1(String WobNumber,String rost)
	{
		Map<String, Object> obj = new HashMap<String, Object>() ;
		VWSession pesession=null;
		try
		{

			pesession= getPESession(uname, password, cp, ceuri);
			pesession.isLoggedOn();
			final VWRoster roster = pesession.getRoster(rost);           
			roster.setBufferSize(1);
			final Object[] queryMin = new Object[5];
			final Object[] queryMax = new Object[5];
			queryMin[0] = WobNumber;
			queryMax[0] = WobNumber;
			//final VWRosterQuery query = roster.createQuery(null, null, null, queryFlags, filter1, null, VWFetchType.FETCH_TYPE_WORKOBJECT);
			final VWRosterQuery query = roster.createQuery("F_WobNum", queryMin, queryMax, VWRoster.QUERY_READ_UNWRITABLE+VWRoster.QUERY_MIN_VALUES_INCLUSIVE+VWRoster.QUERY_MAX_VALUES_INCLUSIVE, null, null, VWFetchType.FETCH_TYPE_WORKOBJECT);

			log.info("queries returned for this Query  "+query.fetchCount());
			System.out.println(query.fetchCount());

			VWWorkObject vwStepElement;
			vwStepElement = (VWWorkObject) query.next();
			VWDataField[] ss = vwStepElement.getDataFields(VWFieldType.ALL_FIELD_TYPES, VWWorkObject.FIELD_USER_DEFINED);
			log.info("Retrived Field  Legnth is "+ss.length);


			for(int i=0;i<ss.length;i++)
			{
				log.trace("Name" + ss[i].getName());
				log.trace("value" + ss[i].getStringValue());
				if ((ss[i].getValue() instanceof Double)) {
					obj.put(ss[i].getName(), (Double)ss[i].getValue());
				} else if ((ss[i].getValue() instanceof Boolean)) {
					obj.put(ss[i].getName(), (Boolean)ss[i].getValue());
				} else if ((ss[i].getValue() instanceof Float)) {
					obj.put(ss[i].getName(), (Float)ss[i].getValue());
				} else if ((ss[i].getValue() instanceof String)) {
					obj.put(ss[i].getName(), ss[i].getStringValue());
				} else if ((ss[i].getValue() instanceof Date)) {
					obj.put(ss[i].getName(), ss[i].getValue().toString());
				} else if ((ss[i].getValue() instanceof Integer)) {
					obj.put(ss[i].getName(), (Integer)ss[i].getValue());
				} else if ((ss[i].getValue() instanceof String[])) {
					obj.put(ss[i].getName(), (String[]) ss[i].getValue());
				} else if ((ss[i].getValue() instanceof Integer[])) {
					obj.put(ss[i].getName(), (Integer[]) ss[i].getValue());
				} else if ((ss[i].getValue() instanceof Date[])) {
					obj.put(ss[i].getName(), (Date[]) ss[i].getValue());
				} else if ((ss[i].getValue() instanceof Double[])) {
					obj.put(ss[i].getName(), (Double[]) ss[i].getValue());
				} else if ((ss[i].getValue() instanceof Float[])) {
					obj.put(ss[i].getName(), (Float[]) ss[i].getValue());
				} else if ((ss[i].getValue() instanceof Boolean[])) {
					obj.put(ss[i].getName(), (Boolean[]) ss[i].getValue());
				} else {
					log.info("Key Not Avilable " + ss[i].getName());
				}
			}
			JSONObject sss = new JSONObject(obj);
			log.info("Final JSON Object is "+sss);
			System.out.println(sss);

			//final VWDataField dataFields[] = workObject.getDataFields(VWFieldType.ALL_FIELD_TYPES, VWWorkObject.FIELD_USER_AND_SYSTEM_DEFINED);

		} catch (Exception e) {
			log.error("Error Occured While Getting WorkObject");
		}finally {
			disconnect(pesession);
		}
		return obj;

	}

	public void addAttachment(String docID,String wobNum,String rost) throws Exception
	{

		VWSession pesession=null;
		try
		{

			pesession= getPESession(uname, password, cp, ceuri);
			pesession.isLoggedOn();
			final VWRoster roster = pesession.getRoster(rost);           
			roster.setBufferSize(1);
			final Object[] queryMin = new Object[5];
			final Object[] queryMax = new Object[5];
			queryMin[0] = wobNum;
			queryMax[0] = wobNum;
			final VWRosterQuery query = roster.createQuery("F_WobNum", queryMin, queryMax, VWRoster.QUERY_READ_UNWRITABLE+VWRoster.QUERY_MIN_VALUES_INCLUSIVE+VWRoster.QUERY_MAX_VALUES_INCLUSIVE, null, null, VWFetchType.FETCH_TYPE_WORKOBJECT);

			log.info("queries returned for this Query  "+query.fetchCount());
			System.out.println(query.fetchCount());

			VWWorkObject vwWorkObject;
			vwWorkObject = (VWWorkObject) query.next();
		
			/*final VWDataField[] vwDataFields = vwWorkObject.getDataFields(VWFieldType.FIELD_TYPE_ATTACHMENT, VWWorkObject.FIELD_USER_DEFINED);
			
			VWAttachment[] finalatt=null;
			for(VWDataField qwe : vwDataFields)
			{
				if(qwe.getName().equalsIgnoreCase("LoanDocuments"))
				{
					finalatt = (VWAttachment[]) qwe.getValue();
				}
			}*/
			
			VWDataField ss = vwWorkObject.getDataField("LoanDocuments");
			VWAttachment[] finalatt = (VWAttachment[]) ss.getValue();
			VWAttachment settingAttachments[]=new VWAttachment[finalatt.length+1]; 
			int j=0;
			for(VWAttachment attach: finalatt)
			{
				settingAttachments[j]=attach;
				j++;
			} 
			VWAttachment myattach = new VWAttachment();

			settingAttachments[j]=myattach;
			myattach.setId(docID);    /// this should be document version series id after uploading document to CE
			myattach.setLibraryName(objName);
			myattach.setLibraryType(VWLibraryType.LIBRARY_TYPE_CONTENT_ENGINE);
			myattach.setAttachmentDescription("Case Details Document");
			myattach.setType(VWAttachmentType.ATTACHMENT_TYPE_DOCUMENT);
			//vwWorkObject.doLock(true);
			vwWorkObject.setFieldValue("LoanDocuments", settingAttachments, false);
			vwWorkObject.doSave(true);
		}

		catch (Exception e) {
			log.error("Error Occured While Getting WorkObject "+e.fillInStackTrace());
			 throw e;
		}
		finally {
			disconnect(pesession);
		}

	}

	public void disconnect(VWSession session) 
	{
		if (log.isDebugEnabled())
		{
			log.debug("> disconnect PE");
		}
		if (session != null && session.isLoggedOn())
		{
			log.info("Inside PE Logging OFF");
			session.logoff();
			session = null;
		}

		if (log.isDebugEnabled())
		{
			log.debug("< disconnected PE Successfully");
		}

	}

	public static void main(String[] args) {
		PEConnector ss = new PEConnector();

		ss.getWorkObject("0x1C1BD473FAF95B4495FBF16AD8F9285C","DefaultRoster");
	}

}
