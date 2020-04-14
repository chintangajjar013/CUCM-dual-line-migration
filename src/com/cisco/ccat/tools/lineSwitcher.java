package com.cisco.ccat.tools;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

public class lineSwitcher {
	public static void main(String[] args) throws IOException {
				
		LogOp log = new LogOp();
		LogOp tempLog;
		AXLUtility au = new AXLUtility();
		ArrayList<String> als = new ArrayList<String>();
		
		//we are reading config.properties files for cucm username/password and host configurations
		if(!au.readProperties().equalsIgnoreCase("SUCCESS")) {
			log.addtolog("Unable to read config.properties, please make sure required parameters are set correctly or file is present ");
			System.out.print(log.log);
			return;
		}
		
		//Now lets read the input.csv, the fuction returns ArrayList for the input read from the file		
		als=au.readInputfile();
		if(als.get(0).equalsIgnoreCase("ERROR")) {
			log.addtolog("Unable to read input.csv file, Please make sure the input.csv file is present or input is formated correctly");
			System.out.print(log.log);
			return;
			
		}	
	    for(int a=0;a<als.size();a++) {
		    	tempLog=au.switchLines(als.get(a));
		    	System.out.println(tempLog.log);
		    	log.addtolog(tempLog.log);
	      }
	
	    au.writeLog(log);	
	}
	
}