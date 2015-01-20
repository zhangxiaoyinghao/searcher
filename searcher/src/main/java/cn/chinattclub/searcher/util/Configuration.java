package cn.chinattclub.searcher.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * TODO Put here a description of what this class does.
 *
 * @author zhangying.
 *         Created 2015-1-20.
 */
public class Configuration {
	  public static final Configuration instance = new Configuration();

	  private static Properties prop = null;

	  public static String getConfig(String configName) {
	    if (prop == null) {
	      try {
	        Configuration config = new Configuration();
	        String configFile = config.getClass().getResource("/").getPath() + "config.properties";

	        FileInputStream fis = new FileInputStream(configFile);
	        prop = new Properties();
	        prop.load(fis);
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	    }
	    return prop.getProperty(configName);
	  }
}
