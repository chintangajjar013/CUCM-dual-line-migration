package com.cisco.ccat.tools;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

public class AXLUtility {

	private BufferedReader br;

	public LineReturn getLineInfo(String extension,String partition,LogOp log){			
		LineReturn lr= new LineReturn();
	 	try {
				String ucmHst = "https://"+AXLProperties.ucmHost+":8443/axl/";	
		 		String bdy=
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://www.cisco.com/AXL/API/12.0\">"
				+"<soapenv:Header/>"
				+ "<soapenv:Body>"
				+ "<ns:getLine>"
				+ "<pattern>"+extension+"</pattern>"
				+ "<routePartitionName>"+partition+"</routePartitionName>"
			    + "</ns:getLine>"
				+ "</soapenv:Body>"
				+ "</soapenv:Envelope>";

		//prop.addtolog("Using the request xml for the device count:"+bdy);	 		
		HttpResponse<String> response = Unirest.post(ucmHst)
				  .header("cache-control", "no-cache")
				  .header("Authorization", "Basic "+AXLProperties.ucmHeader)
				  .body(bdy)
				  .asString();
		
		if(response.getStatus()==200) {
	  		String xml = response.getBody();
	  		org.jdom2.input.SAXBuilder saxBuilder = new SAXBuilder();		      
          	org.jdom2.Document document = saxBuilder.build(new StringReader(xml));		  		
  			Element e = document.getRootElement();			  		
  			List<Element> c=  e.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren();
  			int i=c.size();	
  			if(i==0){
  				lr.status = "ERROR";
  				log.addtolog("Inside getLineInfo():No lines Found for extension:"+ extension);
  			}	  			
  			if(i==1){
  				lr.uuid = c.get(0).getAttribute("uuid").getValue();
  				lr.status = "SUCCESS";
  				log.addtolog("Inside getLineInfo(): Successfully fetched line uuid from the CUCM:"+lr.uuid);
  			}
  			else {
  				lr.status = "ERROR";
  				log.addtolog("Inside getLineInfo():More than 1 Line Found for extension:"+ extension);
  			}
		  }
		else{	
			lr.status = "ERROR";
			log.addtolog("Inside getLineInfo(): Error while getting List for given extension:"+response.getStatus());				
		}					
	} 
		catch (Exception e) {
			lr.status = "ERROR";
			log.addtolog("Inside getLineList(): Exception while getting list of line failed:"+ e.getLocalizedMessage());
			e.printStackTrace();
		}	
	 	return lr;
	}
	
	public String getXML(LineReturn line1, LineReturn line2,String phone) {
		String remXML="",mainXML="";
		remXML="<removeLines>"
			+ "<line>"
			+ "<index>1</index>"
			+ "<dirn uuid=\""+line1.uuid+"\"/>"
			+ "</line>"
			+"</removeLines>";
		
		
		String addXML="";
		String line1XML="<line>"+
						"<index>1</index>"+
						"<label>"+line2.label+"</label>"+
						"<display>"+line2.display+"</display>"+
						"<dirn uuid=\""+line2.uuid+"\"/>"+
						"<displayAscii>"+line2.display+"</displayAscii>"+
						"<e164Mask>"+line2.e164Mask+"</e164Mask>"+
						"<maxNumCalls>"+line2.maxCalls+"</maxNumCalls>"+
						"<busyTrigger>"+line2.busyTrigger+"</busyTrigger>"+
						"<monitoringCssName>"+line2.monitorCSS+"</monitoringCssName>" + 
						getrecordingXML(line2.isRecorded,line2)+
						this.getUserXML(line1.user)+
						"</line>";
		String line2XML = 
				"<line>"+
						"<index>2</index>"+
						"<label>"+line1.label+"</label>"+
						"<display>"+line1.display+"</display>"+
						"<dirn uuid=\""+line1.uuid+"\"/>"+
						"<displayAscii>"+line1.display+"</displayAscii>"+
						"<e164Mask>"+line1.e164Mask+"</e164Mask>"+
						"<maxNumCalls>"+line1.maxCalls+"</maxNumCalls>"+
						"<busyTrigger>"+line1.busyTrigger+"</busyTrigger>"+
						"<monitoringCssName>"+line1.monitorCSS+"</monitoringCssName>" + 
						this.getrecordingXML(line1.isRecorded,line1)+
						this.getUserXML(line1.user)+											
				"</line>";
		
		addXML="<addLines>"
				+line1XML
				+line2XML
				+"</addLines>";
		
		mainXML= "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://www.cisco.com/AXL/API/12.0\">"
				+"<soapenv:Header/>"
				+"<soapenv:Body>"
				+"<ns:updatePhone>"
				+"<name>"+phone+"</name>"
				+remXML
				+addXML
				+ "</ns:updatePhone>"
				+ "</soapenv:Body>"
				+ "</soapenv:Envelope>";		
		return mainXML;
	}
	
	public String getrecordingXML(boolean b,LineReturn lrt) {
		if(b) {
			return 	"<recordingProfileName>"+lrt.recordingProfile+"</recordingProfileName>"+
					"<recordingFlag>"+lrt.recordingFlag+"</recordingFlag>"+
					"<recordingMediaSource>Phone Preferred</recordingMediaSource>";
		}
		else {
			return "<recordingFlag>Call Recording Disabled</recordingFlag>"+
					"<recordingMediaSource>Phone Preferred</recordingMediaSource>";
		}
	}
	
	public String getUserXML(String u) {
		if(u==null||u=="") {
			return "";
		}
		else {
			return "<associatedEndusers><enduser><userId>"+u+"</userId></enduser></associatedEndusers>";
		}
	
	}
	
	public String readProperties(){
		
		try {
			
			InputStream input = new FileInputStream("config.properties");
			Properties prop = new Properties();
			prop.load(input);			
			String axlUN=prop.getProperty("axlUserName");
			String axlPass=prop.getProperty("axlPassword");
			String ucmHost=prop.getProperty("ucmHost");
			if(axlUN.equalsIgnoreCase("null")||axlPass.equalsIgnoreCase("null")||ucmHost.equalsIgnoreCase("null")) {
				return "ERROR";
			}
			AXLProperties.ucmHost=ucmHost;
			String cucmEncodeString = axlUN+":"+axlPass;
			AXLProperties.ucmHeader=Base64.getEncoder().encodeToString(cucmEncodeString.getBytes("UTF-8"));
			
			return "SUCCESS";		
		} 
		catch (Exception e) {
			e.printStackTrace();
			return "ERROR";
		}
	
	}	
	
	public ArrayList<String> readInputfile(){
		
		ArrayList<String> als = new ArrayList<String>();
		try {
			
			br = new BufferedReader(new FileReader("input.csv"));
            String line;
                while ((line = br.readLine()) != null) {
                    als.add(line);
                }
             return als;
		} 
		catch (Exception e) {
			e.printStackTrace();
			als.add("ERROR");
		}
		return als;	
	}
	
	public void writeLog(LogOp log) {
		
		try {
				
			  DateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY-HH-mm-ss");
		  	  Date date = new Date();
		  	  FileWriter fileWriter;
		  	  fileWriter = new FileWriter("Line-Migration-"+dateFormat.format(date)+".txt");
		  	  fileWriter.append("\n");
		  	  fileWriter.append(log.log);
		  	  fileWriter.flush();
		  	  fileWriter.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		
		}
		
	//New method starts from
	public LogOp switchLines(String s1) {
	    	
		LogOp templog = new LogOp();
	    	try {
	    		
	    		String phone, partition, cLine1, nLine1, extMask, maxCalls, busyTrigger, isCLineRecorded, isNLineRecorded, recordingProfile, recordingFlag, recordingSource, monitorCSS, user;
	    		phone=partition=cLine1=nLine1=extMask=maxCalls=busyTrigger=isCLineRecorded=isNLineRecorded=recordingProfile=recordingFlag=recordingSource=monitorCSS=user="";
	    		templog.log="";
	    		templog.addtolog("Starting the Line Switching operation for input: "+s1); 
			   	String[] sPhone = s1.split(",", 0);
			   	
			   	if(sPhone[0].trim().length()==0||sPhone[1].trim().length()==0||sPhone[2].trim().length()==0||sPhone[3].trim().length()==0) {
		    		templog.addtolog("Input is Not Valid(Fail, Error validation error), skipping the operation for this line and continueing to next one"); 
		    		templog.addtolog("Please make sure the input csv is in correct format.");
		    		return templog;
		    	 }
			   	
		    	if(sPhone.length!=14) {
		    		
		    		System.out.println(Arrays.toString(sPhone));
		    		templog.addtolog("Input is Not Valid(Fail, Error Not Enough paramters"+sPhone.length+"), skipping the operation for this line and continueing to next one"); 
		    		templog.addtolog("Please make sure the input csv is in correct format.");
		    		return templog;
		    	}
	    	
		    	phone = sPhone[0];
		    	partition=sPhone[1];
	    		cLine1 = sPhone[2];
	    		nLine1 = sPhone[3];
	  	    	extMask = sPhone[4];
	  	    	maxCalls = sPhone[5];
	  	    	busyTrigger = sPhone[6];
	  	    	isCLineRecorded=sPhone[7];
	  	    	isNLineRecorded=sPhone[8];
	  	    	recordingProfile = sPhone[9];
	  	    	recordingFlag=sPhone[10];
	  	    	recordingSource=sPhone[11];
	  	    	monitorCSS=sPhone[12];
	  	    	user=sPhone[13];
	  	    	
	  	    	if(!extMask.matches("^[0-9*#+X]{0,24}$")){
	  	    		templog.addtolog("Ext Mask formate is invalid, will be set to null");
	  	      		extMask = "";
	  	    	}
	    	
	  	    	templog.addtolog("Using Phone:"+phone+",using current Line:"+cLine1+",using new Line:"+nLine1+",using extMask:"+extMask+",user:"+user+",recordCurrentLine="+isCLineRecorded+",recordNewLine"+isNLineRecorded);	  		    
	  	    	templog.addtolog("Getting Line1 info");
		    	LineReturn lrt1=this.getLineInfo(cLine1, partition, templog);
		    	
		    	if(lrt1.status=="ERROR") {
		    		templog.addtolog("skipping this row, fail, error, trying next one.");
		    		return templog;
		    	}
		    	
		    	templog.addtolog("Getting Line2 info");
		    	LineReturn lrt2=this.getLineInfo(nLine1, partition, templog);
		    	if(lrt2.status=="ERROR") {
		    		templog.addtolog("No configured line found for "+nLine1+" ,trying to add one.");
		    		lrt2=this.addPhoneLine(nLine1, partition, templog);
		    		if(lrt2.status=="ERROR") {		    			
			    		templog.addtolog("error, skipping the operation for this line, trying next one.");
			    		return templog;
		    		}
		    	}
		    	
		    	lrt1.label=cLine1;
		    	lrt2.label=nLine1;
		    	lrt1.display=cLine1;
		    	lrt2.display=nLine1;		    	
		    	lrt1.e164Mask=extMask;
		    	lrt2.e164Mask=extMask;
		    	lrt2.isRecorded=false;
		    	lrt1.isRecorded=false;
		    	
		    	if(isCLineRecorded.equalsIgnoreCase("Y")) {
		    		lrt1.isRecorded=true;
		    	}
		    	if(isNLineRecorded.equalsIgnoreCase("Y")) {
		    		lrt2.isRecorded=true;
		    	}		    	
		    	if(!(recordingProfile==null||recordingProfile.equalsIgnoreCase("")||recordingProfile.equalsIgnoreCase("NULL"))){
		    		lrt1.recordingProfile=recordingProfile;
		    		lrt2.recordingProfile=recordingProfile;
		    	}		    	
		    	if(!(recordingFlag==null||recordingFlag.equalsIgnoreCase("")||recordingFlag.equalsIgnoreCase("NULL"))){
		    		lrt1.recordingFlag=recordingFlag;
		    		lrt2.recordingFlag=recordingFlag;
		    	}				    	
		    	if(!(recordingSource==null||recordingSource.equalsIgnoreCase("")||recordingSource.equalsIgnoreCase("NULL"))){
		    		lrt1.recordingSource=recordingFlag;
		    		lrt2.recordingSource=recordingFlag;
		    	}
		    	if(!(maxCalls==null||maxCalls.equalsIgnoreCase("")||maxCalls.equalsIgnoreCase("NULL"))){
		    		lrt1.maxCalls=maxCalls;
		    		lrt2.maxCalls=maxCalls;		    		
		    	}
		    	if(!(busyTrigger==null||busyTrigger.equalsIgnoreCase("")||busyTrigger.equalsIgnoreCase("NULL"))){
		    		lrt1.busyTrigger=busyTrigger;
		    		lrt2.busyTrigger=busyTrigger;		    		
		    	}
		    	if(!(user==null||user.equalsIgnoreCase("")||user.equalsIgnoreCase("NULL"))){
		    		lrt1.user=user;
		    		lrt2.user=user;		    		
		    	}
		    	if(!(monitorCSS==null||monitorCSS.equalsIgnoreCase("")||user.equalsIgnoreCase("NULL"))){
		    		lrt1.monitorCSS=monitorCSS;
		    		lrt2.monitorCSS=monitorCSS;		    		
		    	}
		    	
		    	String finalXML=this.getXML(lrt1, lrt2, phone);		    	
		    	templog.addtolog("trying to send update phone request with and and remove line params");
	    	
		    	HttpResponse<String> response = Unirest.post("https://"+AXLProperties.ucmHost+":8443/axl/")
						  .header("cache-control", "no-cache")
						  .header("Authorization", "Basic "+AXLProperties.ucmHeader)
						  .body(finalXML)
						  .asString();
		    	
		    	if(response.getStatus()==200) {
					templog.addtolog("lines were succesfully updated:"+response.getBody());
					
				}
				else {
					templog.addtolog("fail, error in updating lines:"+response.getStatusText()+","+response.getBody());			
				}		    	
		    	templog.addtolog("");
		    	return templog;
	    	}
	    	catch (Exception e) {
	    		e.printStackTrace();
	    		templog.addtolog("fail, error in updating lines:"+e.getLocalizedMessage());	
	    		templog.addtolog("");
	    		return templog;
			}	
		    	  
	
		}
	
	////////////// New Method

	public LineReturn addPhoneLine(String extension, String partition,LogOp log){
		LineReturn lr = new LineReturn();
	try {
			String ucmHst = "https://"+AXLProperties.ucmHost+":8443/axl/";	
	 		String bdy=
			"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://www.cisco.com/AXL/API/12.0\">"
			+"<soapenv:Header/>"
			+ "<soapenv:Body>"
			+ 	"<ns:addLine>"
			+ 		"<line>"
			+ 			"<pattern>"+extension+"</pattern>"
			+ 			"<routePartitionName>"+partition+"</routePartitionName>"
			+ 		"</line>"
		    + 	"</ns:addLine>"
			+ "</soapenv:Body>"
			+ "</soapenv:Envelope>";

	//prop.addtolog("Using the request xml for the device count:"+bdy);	 		
	HttpResponse<String> response = Unirest.post(ucmHst)
			  .header("cache-control", "no-cache")
			  .header("Authorization", "Basic "+AXLProperties.ucmHeader)
			  .body(bdy)
			  .asString();
	
	if(response.getStatus()==200) {
	  		String xml = response.getBody();
	  		org.jdom2.input.SAXBuilder saxBuilder = new SAXBuilder();		      
	      	org.jdom2.Document document = saxBuilder.build(new StringReader(xml));		  		
			Element e = document.getRootElement();			  		
			lr.uuid= e.getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue();
			lr.status = "SUCCESS";
			log.addtolog("Inside addPhoneInfo(): SuccessFully added new phone line with uuid:"+lr.uuid);							
	  	}
	else{	
			lr.status = "ERROR";
			log.addtolog("Inside addPhoneInfo(): Error while getting List for given extension:"+response.getStatus()+" "+response.getBody());
		}					
	} 
	catch (Exception e) {
		lr.status = "ERROR";
		log.addtolog("Inside addPhoneInfo(): Exception while getting list of line failed:"+ e.getLocalizedMessage());
		e.printStackTrace();
	}	
 	return lr;

	}
		
	
}
