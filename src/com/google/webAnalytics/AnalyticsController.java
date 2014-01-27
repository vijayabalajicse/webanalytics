package com.google.webAnalytics;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.sf.jsr107cache.Cache;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.Analytics.Data.Ga.Get;
import com.google.api.services.analytics.model.Accounts;
import com.google.api.services.analytics.model.Profiles;
import com.google.api.services.analytics.model.Webproperties;


@Controller
public class AnalyticsController {
	
	static Logger log = Logger.getLogger("LogInfo");
	static ObjectMapper jsonMap = new ObjectMapper();
    static JSONObject jsonObj = new JSONObject();
	static JSONParser parser = new JSONParser();    
	static Date printTime = new Date();
	Cache cache;
	
	/**
	 * Getting List of Account Id using Analytics Object
	 * @param req
	 * @return List of Accounts
	 */
	//Handle Ajax url request for get the account info
	@RequestMapping(value="/getAccountInfo")
	public @ResponseBody String getAccountInfo(HttpServletRequest req){
		log.info("return the acccountinfo");
		String accessToken = (String) req.getSession().getAttribute("USER_ACCESSTOKEN");			
		Analytics analytics = getAnalayticsObject(accessToken);	
		//log.info("Analytics obj: "+analytics);	
		String 	accountInfo = getAccountInfo(analytics);
	 return accountInfo;	
	}
	/**
	 * Getting the Analtics Account Inforamtion using Analytics Object
	 * @param analytics
	 * @return List of Account Details
	 */
	//Get the Account info from google anlytics 
	private String getAccountInfo(Analytics analytics)  
	{
		log.info("Get profile info");
		Map<String,String> accountNameInfo = new LinkedHashMap<String,String>();
		ObjectMapper jsonMap = new ObjectMapper();		
		String accountInfo = "";		
		try
		{
			Accounts account = analytics.management().accounts().list().execute();
			if(account.getItems().isEmpty()){
			log.info("Sorry no Account founded");
			log.info("Account name: "+account.getItems().get(0).getName());
			log.info("No Account found");
			}
			else{
				for ( int i =0; i< account.getItems().size();i++){					
					accountNameInfo.put(account.getItems().get(i).getId(), account.getItems().get(i).getName());					
				}				
				accountInfo = jsonMap.writeValueAsString(accountNameInfo);
				log.info(accountInfo);	
				
		
		}
		
		}catch(Exception e){
			log.warning("Problem in getting Account details");
		}
		
		return accountInfo;
	}
	
	
	/**
	 * Getting the WebPropeties Using Account ID from GoogleAnalytics
	 * @param ajaxdata
	 * @param req
	 * @return  List of WebProperties ID
	 */
	//Handle Ajax url request for get the Property info
	@RequestMapping(value="/getPropertyInfo")
	public @ResponseBody String getPropertyInfo(@RequestBody String ajaxdata,HttpServletRequest req){
		Webproperties webProperties;
		Analytics analytics;
		String accessToken;
		String accountId;
		String properties ="";
		Map<String,String> propertiesInfo =  new LinkedHashMap<String,String>();
		accessToken= (String) req.getSession().getAttribute("USER_ACCESSTOKEN");
		analytics= getAnalayticsObject(accessToken);
		
		log.info(printTime+ajaxdata);
		log.info(printTime + ""+ajaxdata.length());
		accountId=ajaxdata.substring(0, ajaxdata.length()-1);	
		log.info(accountId+""+accountId.length());
		try {
			 webProperties = analytics.management().webproperties().list(accountId).execute();
			if(webProperties.getItems().isEmpty()){
				log.info("No data found");
			}else{
				for (int i=0;i<webProperties.getItems().size();i++){
				propertiesInfo.put(webProperties.getItems().get(i).getId(),webProperties.getItems().get(i).getName());
				}
				properties = jsonMap.writeValueAsString(propertiesInfo);
				log.info(properties);
			}
				
		} catch (IOException e) {			
			log.warning("Problem in getting the WebProperties from Analytics");
			e.printStackTrace();
		}
		 return properties;
	}
	/**
	 * Getting ProfileView Id Using WebProperties Id
	 * @param propData
	 * @param req
	 * @return list of ProfileId
	 */
	//Handle Ajax url request for get the Profile info
	@RequestMapping(value="/getProfileInfo")
	public @ResponseBody String getProfileInfo(@RequestBody String propData,HttpServletRequest req){
		Map<String,String> profileMap = new LinkedHashMap<String,String>();
		String profileInfo ="";
		Analytics analytics;
		Profiles profile;
		String accessToken, propertyId,accountId;
		log.info(propData);
		try {
			
			jsonObj = (JSONObject) parser.parse(propData);
		} catch (ParseException e) {			
			log.warning("Problem in parse the json Data");
		}
		
		log.info((String) jsonObj.get("AccountId"));
		log.info((String) jsonObj.get("PropertiesID"));
		accountId  = (String) jsonObj.get("AccountId");
		propertyId = (String) jsonObj.get("PropertiesID");
		accessToken = (String) req.getSession().getAttribute("USER_ACCESSTOKEN");
		analytics = getAnalayticsObject(accessToken);
		try {
			 profile = analytics.management().profiles().list(accountId, propertyId).execute();
				if(profile.getItems().isEmpty()){
					log.info("Empty");
				}else{
				for(int i=0;i< profile.getItems().size();i++){
					profileMap.put(profile.getItems().get(i).getId(),profile.getItems().get(i).getName());
				}
				
				profileInfo = jsonMap.writeValueAsString(profileMap);
				log.info(profileInfo);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return profileInfo;
	}
	
	
	 /**
	    * Get the GaData QueryString
	    * @param data
	    * @param req
	    * @return response String
	    */
		@RequestMapping(value="/getdataAjax")
		public @ResponseBody String getData(@RequestBody String QueryData,HttpServletRequest req) {		
			String responseData;		
			responseData = getGaData(QueryData,req);
			return responseData;		
			
		}
	
		/**
		 * Parsing the QueryData and Calling the G Data Result
		 * @param QueryData
		 * @param req
		 * @return G Data Results
		 */
		//Sending data with queryInformation and analytics obj
		static String getGaData(String QueryData,HttpServletRequest req){
			JSONObject objJson= new JSONObject();
			
			String accessToken = (String) req.getSession().getAttribute("USER_ACCESSTOKEN");	
			log.info("Accesstoken"+accessToken);      	
			Analytics analytics = getAnalayticsObject(accessToken);		
			log.info("Parsing the data from clent");
			log.info("Query data"+QueryData);
			String data_result = null;
			try
			{
			jsonObj = (JSONObject) parser.parse(QueryData);		
			}
			catch(ParseException e)
			{
				e.printStackTrace();
			}
			String table_id = (String) jsonObj.get("tableid");
			String metrics = (String) jsonObj.get("metrics");
			String dimension = (String) jsonObj.get("dimension");
			String startDate =  (String) jsonObj.get("startDate");
			String endDate =  (String) jsonObj.get("endDate");
			String segment = (String) jsonObj.get("segment");
			String filter = (String) jsonObj.get("filter");
			String sort = (String) jsonObj.get("sort");
			String maxResult =  (String) jsonObj.get("maxResults");
			log.info(table_id + " " + metrics + " " + dimension.length() + " " + startDate + " " + endDate+" "+filter.length()+" "+ segment.length()+" "+sort.length()+""+maxResult);
			
			try {			
				data_result = getResultsData(table_id,metrics,dimension,startDate,endDate,filter,segment,sort,maxResult,analytics);
				//log.info(data_result);
				jsonObj = (JSONObject) parser.parse(data_result);
				objJson =  (JSONObject) jsonObj.get("dataTable");
				data_result = objJson.toJSONString();
				//log.info(data_result);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.warning("json Parsing error");
			}
			
			
			
			return data_result ;
			
			
		}
		
		
		/**
		 * Getting the G Data from Analytics
		 * @param table_id
		 * @param metrics
		 * @param dimension
		 * @param startDate
		 * @param endDate
		 * @param filter
		 * @param segment
		 * @param sort
		 * @param maxResult 
		 * @param analytics
		 * @return
		 */
		//Getting the GaData from Anlaytics
		private static String getResultsData(String table_id, String metrics,String dimension, String startDate, String endDate,String filter, String segment, String sort, String maxResult, Analytics analytics) 
		{
		
			log.info("Get the result for analytics");
			String value = null;
			int maxResults = Integer.parseInt(maxResult);
			Get apiQuery;
			try {
			//value = analytics.data().ga().get(table_id, startDate, endDate, metrics).setDimensions(dimension).setSegment(segment).setFilters(filter).setSort(sort).setOutput("dataTable").setMaxResults(50).execute().toPrettyString();
				apiQuery = analytics.data().ga().get(table_id, startDate, endDate, metrics);
				if(dimension.length() !=0){
					 apiQuery.setDimensions(dimension);
				 }
				if(filter.length()!=0){
					apiQuery.setFilters(filter);
				}
				if(segment.length()!=0){
					apiQuery.setSegment(segment);
				}
				if(sort.length()!=0){
					apiQuery.setSort(sort);
				}
//				if(maxResults < 1000){
//					apiQuery.setMaxResults(maxResults);
//				}else{
//					apiQuery.setMaxResults(999);
//				}
				value = apiQuery.setOutput("dataTable").setMaxResults(maxResults).execute().toPrettyString();
				return value;
			} catch (Exception e) {	
				log.warning(e.getMessage());
				return e.getMessage();
			}
		
		}
	
	/**
	    * Getting Analytics Object using Accesstoken
	    * @param accesstoken
	    * @return Analytics Object for Coressponding user
	    */
		
		//Creating the Analytics Object 
	    private static Analytics getAnalayticsObject(String accesstoken){
	    	Credential credential = new GoogleCredential.Builder().setClientSecrets(OauthController.clientSecrets).setJsonFactory(OauthController.JSON_FACTORY).setTransport(OauthController.HTTP_TRANSPORT).build();
	    	credential.setAccessToken(accesstoken);
	    	//log.info("credentil accesstoken"+credential.getAccessToken());
	    	Analytics analytics = new Analytics.Builder(OauthController.HTTP_TRANSPORT, OauthController.JSON_FACTORY, credential).setApplicationName("Web Anlaytics").build();
	    	//log.info("Analytics obj"+analytics.getRootUrl()+""+analytics.getBaseUrl());
	    	return analytics;
	    	
	    }
	    

}
