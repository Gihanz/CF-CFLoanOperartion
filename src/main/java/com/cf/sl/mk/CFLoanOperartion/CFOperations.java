package com.cf.sl.mk.CFLoanOperartion;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.PropertyConfigurator;

import com.cf.sl.mk.connector.connector.CECUtil;
import com.cf.sl.mk.connector.connector.PEConnector;
import com.cf.sl.mk.connector.util.PropertyReader;
import com.sl.mk.cf.PDFGenerator.GeneratePDF;

import org.apache.log4j.Logger;

public class CFOperations {

	public static Properties prop;
	//private Logger log;
	public static Logger log = Logger.getLogger(CFOperations.class);

	public String MARegistration(String wobNum)
	{
		String returnVal ="";
		try {
			PropertyReader pr;

			pr = new PropertyReader();

			String pathSep = System.getProperty("file.separator");
			prop=pr.loadPropertyFile();
			String logpath = prop.getProperty("LOG4J_FILE_PATH");
			String activityRoot= prop.getProperty("LOG_PATH");
			String logPropertyFile =logpath+pathSep+"log4j.properties"; 



			PropertyConfigurator.configure(logPropertyFile);
			log = Logger.getLogger(CFOperations.class);

			PropertyReader.loadLogConfiguration(logPropertyFile, activityRoot+"/MARegistration/", wobNum+".log");
			log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> Entered into MARegistration >>>>>>>>>>>>>>>>>>>>>>>>>>>>");


			String rosterName = prop.getProperty("BUSINESSLOANROSTER");

			log.debug("WobNumber is "+wobNum);
			log.debug("Roster Name is "+rosterName);
			PEConnector pecon = new PEConnector();
			Map<String, Object> loanData = pecon.getWorkObject(wobNum, rosterName);
			JSONObject loanJsonData = new JSONObject(loanData);
			log.info("Json From Log is "+loanJsonData.toString());

			HttpClient client = new DefaultHttpClient();
			log.info("Url For MA registration is "+prop.getProperty("CFSERVICEURL")+ "/callCFDataPush");
			HttpPost post = new HttpPost(prop.getProperty("CFSERVICEURL")+ "/callCFDataPush");
			StringEntity input;
			log.info("Request is "+loanJsonData.toString());
			input = new StringEntity(loanJsonData.toString());

			input.setContentType("text/plain");

			post.setEntity(input);
			HttpResponse response;

			response = client.execute(post);

			HttpEntity entity = response.getEntity();

			returnVal = EntityUtils.toString(entity);
			log.info("Response is "+returnVal);

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error Occured while Data pushing : "+e.fillInStackTrace());
		}
		
		if(returnVal.contains("SUCCESS"))
		{
			log.info("Data has been pushed sucessfully");
			returnVal="SUCCESS";
		}else
		{
			log.error("Error occured while pushing data");
			returnVal="FAILED";
			throw new RuntimeException("Excpetion occured while pushing MA data");
		}

		log.info(">>>>>>>>>>>>>>>>>>>>>>>>> MA Registration Method exited Sucessfully >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		return returnVal;

	}


	public void generateCreditProposalPDF(String Wobnumber)

	{
		PropertyReader pr;
		String docid = null;
		CECUtil ceutil =null;
		GeneratePDF generatePDF=null;
		ByteArrayOutputStream fip=null;
		Map<String, Object> loanData=null;

		try {
			pr = new PropertyReader();


			String pathSep = System.getProperty("file.separator");
			prop=pr.loadPropertyFile();
			String logpath = prop.getProperty("LOG4J_FILE_PATH");
			String activityRoot= prop.getProperty("LOG_PATH");
			String logPropertyFile =logpath+pathSep+"log4j.properties"; 


			PropertyConfigurator.configure(logPropertyFile);
			log = Logger.getLogger(CFOperations.class);

			PropertyReader.loadLogConfiguration(logPropertyFile, activityRoot+"/PDFGeneration", Wobnumber+".log");
			log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> Entered into PDF Generation >>>>>>>>>>>>>>>>>>>>>>>>>>>>");

			String rosterName = prop.getProperty("BUSINESSLOANROSTER");
			String docClass=  prop.getProperty("DOCUMENTCLASS");
			log.debug("WobNumber is "+Wobnumber);
			log.debug("Roster Name "+rosterName);
			log.debug("Document class name is "+docClass);
			PEConnector pecon = new PEConnector();
			loanData = pecon.getWorkObject1(Wobnumber, rosterName);

			generatePDF = new GeneratePDF();

			fip = generatePDF.nlcpGenerator(loanData, true);
			ceutil = new CECUtil();
			docid=ceutil.addDocumentWithStream(fip, Wobnumber+"_CreditProposal", docClass, Wobnumber);
			pecon.addAttachment(docid, Wobnumber, rosterName);


			log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> PDF Generation Completed SucessFully >>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error Occured while generating PDF : "+e.fillInStackTrace());
		}

	}

	public static void main(String[] args) {
		
		CFOperations cc = new CFOperations();
		//cc.MARegistration("02BC78CFD4630E4291079A12C4FF1B4A");
		//cc.generateCreditProposalPDF("598B57C23F05154C84B51EC36AB18771");
	}

}
