package autonomousCarEass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import ajpf.MCAPLcontroller;

public class MotorwayConfig extends Properties {
	private static final long serialVersionUID = 1L;

	/**
	 * where did we initialize from
	 */
	ArrayList<Object> sources = new ArrayList<Object>();

	/**
	 * single source Config constructor (does not process stack)
	 * @param fileName - single properties filename to initialize from 
	 */
	public MotorwayConfig (String filename){
		  try {
			  String fileName = MCAPLcontroller.getFilename(filename);
			  loadProperties(fileName);
		  } catch (Exception e) {
			  System.err.println(e.getMessage());
		  }
	  }

	/**
	 * Load the properties from the file.
	 * @param fileName
	 * @return
	 * @throws AILexception
	 */
	protected boolean loadProperties (String fileName) throws Exception {
		    if (fileName != null && fileName.length() > 0) {
		      FileInputStream is = null;
		      try {
		        File f = new File(fileName);
		        if (f.isFile()) {

		          setConfigPathProperties(f.getAbsolutePath());
		          sources.add(f);
		          is = new FileInputStream(f);
		          load(is);
		          return true;
		        } else {
		          throw new Exception("property file does not exist: " + f.getAbsolutePath());
		        }
		      } catch (IOException iex) {
		        throw new Exception("error reading properties: " + fileName);
		      } finally {
		        if (is != null){
		          try {
		            is.close();
		          } catch (IOException iox1){
		            System.err.println("error closing input stream for file: " + fileName);
		          }
		        }
		      }
		    }

		    return false;
		  }
	  
	/**
	 * Set properties to do with the config path.
	 * @param fileName
	 */
	  protected void setConfigPathProperties (String fileName){
		    put("config", fileName);
		    int i = fileName.lastIndexOf(File.separatorChar);
		    if (i>=0){
		      put("config_path", fileName.substring(0,i));
		    } else {
		      put("config_path", ".");
		    }
		  }
	  
	  
	  /*
	   * (non-Javadoc)
	   * @see java.util.Hashtable#toString()
	   */
	  public String toString() {
		  String s = "----------- Config contents\n";

		    // just how much do you have to do to get a printout with keys in alphabetical order :<
		    TreeSet<String> kset = new TreeSet<String>();
		    for (Enumeration<?> e = propertyNames(); e.hasMoreElements();) {
		      Object k = e.nextElement();
		      if (k instanceof String) {
		        kset.add( (String)k);
		      }
		    }

		    for (String key : kset) {
		      String val = getProperty(key);
		      s += key;
		      s += " = ";
		      s += val;
		      s += "\n";
		    }
		    return s;
		  }

}
