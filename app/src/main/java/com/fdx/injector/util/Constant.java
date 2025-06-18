package com.fdx.injector.util;

public class Constant {
		
	public static String getUa()
	{
		String property = System.getProperty("http.agent");
        return property == null ? "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36" : property;
	}	

	//44 LockOvpn
}
