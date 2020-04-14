package com.cisco.ccat.tools;
import java.io.UnsupportedEncodingException;


public class testmain {
	public static void main(String[] args) throws UnsupportedEncodingException {
            
		AXLUtility au = new AXLUtility();
		au.readProperties();
		LogOp l = new LogOp();
		LineReturn lrt=au.addPhoneLine("80899901", "CORE 8Digit", l);
		System.out.println(lrt.status+" "+lrt.uuid);
		System.out.println(l.log);	
	}

}
