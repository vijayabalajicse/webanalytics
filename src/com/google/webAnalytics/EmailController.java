package com.google.webAnalytics;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;


@Controller
public class EmailController {
	static Logger log = Logger.getLogger("LogInfo");
	 static JSONObject jsonObj = new JSONObject();
	static 	JSONParser parser = new JSONParser();    
	
	/**
	 * Send the Email to user with Attachment
	 * @param emailData
	 * @param req
	 * @return success message to client
	 */
	//Handle Email url Request
	@RequestMapping(value="/emailRequest")
	public @ResponseBody String emailTask(@RequestBody String emailData, HttpServletRequest req){
		String getDataTable;
		log.info("EmailData: "+emailData);
		try {
			jsonObj = (JSONObject) parser.parse(emailData);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.warning(e.getMessage());
		}
		String emailid = (String) jsonObj.get("emailId");
		log.info(emailid);
		getDataTable = AnalyticsController.getGaData(emailData,req);
		Queue taskQueue = QueueFactory.getQueue("EmailQueue");
		taskQueue.add(TaskOptions.Builder.withUrl("/sendemail").param("emailId", emailid).param("csvData", getDataTable));
		return "{\"status\":\"Success\"}";
	}
}
