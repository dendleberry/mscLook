package uk.gov.dvla.osg.msclookup.implementations;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import uk.gov.dvla.osg.msclookup.Main;
import uk.gov.dvla.osg.msclookup.interfaces.LookupMsc;

public class RmMscLookup implements LookupMsc{

	private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());
	private Map<String, String> mscs;
	private String lookupFile;
	private String delim = "\\|";

	@Inject
	public RmMscLookup(LookupMsc lookupMsc, @Named("lookupFile") String lookupFile)
	{
		this.lookupFile=lookupFile;
		init_RmMscLookup();
	}
	private void init_RmMscLookup(){
		mscs = new Hashtable<String, String>();
		String line;
		String[] parts;
		try {
			FileInputStream fis = new FileInputStream(lookupFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			while ( (line = br.readLine()) != null ){
				parts=line.split(delim);
				
				mscs.put(parts[0], parts[1]);
			}
			LOGGER.info("Map contains {} entries",mscs.size());
			br.close();
		} catch (IOException e) {
			LOGGER.fatal(e.getMessage());
			System.exit(1);
		}
	}
	
	public String getMsc(String pc, int noOfZeros){	
		String result="";
		String[] split;
		if(noOfZeros > 4){
			LOGGER.fatal("Number of zeros passed to {} cannot exceed 4",this.getClass().getName() +"."+ Thread.currentThread().getStackTrace()[1].getMethodName());
			System.exit(1);
		}
		try{
			LOGGER.debug("pc='{}'",pc);
			
			split = pc.split(" ");
			pc = pc.replaceAll("\\s", "");
			if((pc.length() < 4) || (split.length != 2)){
				result = null;
				LOGGER.debug("LOOKVAL=NULL");
			}else{
				//String lookupValue = pc.substring(0, pc.trim().length()-3) + " " + 
				//	pc.substring(pc.trim().length()-3,pc.trim().length()-2);
				String lookupValue = split[0] + " " + split[1].substring(0, 1);
				
				
				lookupValue = String.format("%-6.6s", lookupValue);
				LOGGER.debug("LOOKVAL='{}'",lookupValue);
				result = mscs.get(lookupValue);
			}
		}catch (Exception e){
			LOGGER.fatal("Format of postcode '{}' failed with error '{}'", pc, e.getMessage());
			System.exit(0);
		}
		
		if(result == null){
			result = "";
		}else{
			result = StringUtils.rightPad(result.substring(0,result.length() - noOfZeros), 5, "0");
		}
		
		return result;
	}
	
}
