package org.openmuc.framework.app.pcharge;

import java.io.FileReader;
import java.util.Properties;

import com.sun.media.jfxmedia.logging.Logger;

public class PChargeConfig {
	
	private final static String CONFIG_FILE= "org.openmuc.framework.app.pcharge.configurationFile"; 
	
	public static void main(String[] args)throws Exception{
		String configFile = System.getProperty(CONFIG_FILE, null);
		Properties property = new Properties(); 
		FileReader reader = new FileReader(configFile);
		
		property.load(reader);
		
		if (configFile == null) {
			configFile = "config/pcharge.properties";
			if (configFile == null){
				Logger.ERROR("Config File nicht gefunden");
			}
		}
	}
}
