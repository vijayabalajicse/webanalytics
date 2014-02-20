package com.google.webAnalytics;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServletRequest;

import net.sf.jsr107cache.Cache;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.analytics.JDO.PMFSingleton;
import com.analytics.JDO.ShareProfile;
import com.analytics.JDO.UserDetails;
import com.analytics.JDO.UserProfile;
import com.analytics.util.CacheInital;
import com.analytics.util.DataCompress;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.util.Base64;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.Analytics.Data.Ga.Get;
import com.google.api.services.analytics.model.Accounts;
import com.google.api.services.analytics.model.GaData;
import com.google.api.services.analytics.model.Profiles;
import com.google.api.services.analytics.model.Webproperties;
import com.google.appengine.api.datastore.KeyFactory;



@Controller
public class AnalyticsController {
	
	static Logger log = Logger.getLogger("LogInfo");
	static ObjectMapper jsonMap = new ObjectMapper();
    static JSONObject jsonObj = new JSONObject();
	static JSONParser parser = new JSONParser();    
	static Date printTime = new Date();
	static Cache cache;
	
	
	
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
		try {
			jsonObj = (JSONObject)parser.parse(ajaxdata);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			log.warning(e1.getMessage());
		}
		log.info(printTime+ajaxdata);
		log.info(printTime + ""+ajaxdata.length());
		accountId=(String) jsonObj.get("accountid");	
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
			log.warning("Problem in getting the WebProperties from Analytics"+e.getMessage());			
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
			log.warning("Problem in getting the Profiles from Analytics"+e.getMessage());
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
			log.info("get querydata form client"+printTime.toString());
			String accessToken = (String) req.getSession().getAttribute("USER_ACCESSTOKEN");	
			String  userId = (String) req.getSession().getAttribute("SESSION_USEREMAILID");
			responseData = getGaData(QueryData,accessToken);
			saveCache(userId,responseData);
			return responseData;		
			
		}
		/**
		 * Storing the Custom profile in the Datastore
		 * @param inputdata
		 * @param req
		 * @return
		 */
		@RequestMapping(value="/storeprofiledata")
		public @ResponseBody String storeprofile(@RequestBody String inputdata,HttpServletRequest req){
			System.out.println(inputdata);
			String  userId = (String) req.getSession().getAttribute("SESSION_USEREMAILID");
			try
			{
			jsonObj = (JSONObject) parser.parse(inputdata);		
			}
			catch(ParseException e)
			{
				e.printStackTrace();
			}
			String table_id = (String) jsonObj.get("queryId");
			String metrics = (String) jsonObj.get("metrics");
			String dimension = (String) jsonObj.get("dimensions");
			String startDate =  (String) jsonObj.get("startDate");
			String endDate =  (String) jsonObj.get("endDate");
			jsonObj.get("segment");
			String filter = (String) jsonObj.get("filter");
			jsonObj.get("sort");
			jsonObj.get("maxResults");
			String profileName = (String) jsonObj.get("profileName");
			String profileIndex = (String) jsonObj.get("profileIndex");
			if(profileName != null){
				storeProfileDetails(table_id,metrics,dimension,startDate,endDate,userId,filter,profileName,profileIndex);
				return "{ \"status\":\"stored\" }";
			}else{
				return "{ \"status\":\"failed\" }";
			}
				
			
			
		}
		
		/**
		 * Get last Search results
		 * @param req
		 * @return
		 */
	   @RequestMapping(value="/getlastsearch")
	   public @ResponseBody String getResult(HttpServletRequest req){
		   
		   System.out.println("Stringvalue"+req.getParameter("emailId"));
		   String resultdata= getCachedata(req.getParameter("emailId").trim());
		   
		return resultdata;
		   
	   }
	   /**
	    * Get userprofile list from datastore
	    * @param req
	    * @return
	    */
	   @RequestMapping(value="/getuserlist")
	   public @ResponseBody String getuserlist(HttpServletRequest req){
		   String userid = (String) req.getSession().getAttribute("SESSION_USEREMAILID");
		   userid =  KeyFactory.createKeyString(UserProfile.class.getSimpleName(),userid);
		   PersistenceManager pm= PMFSingleton.getPMF().getPersistenceManager();
		   String profiledetail = null;
		   
		try {
			UserProfile userprofile = pm.getObjectById(UserProfile.class, userid);
			   System.out.println(userprofile.getQuerydetails().toString());			  
			profiledetail = jsonMap.writeValueAsString(userprofile.getQuerydetails());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warning(e.getMessage());
		}
		   return profiledetail;
	   }
	   /**
	    * Delete the profile in the datastore
	    * @param req
	    * @return
	    */
	   @RequestMapping(value = "/deleteprofile")
	   public @ResponseBody String deleteProfile(HttpServletRequest req){
		   String index = req.getParameter("index");
		   System.out.println(index);
		   String userid = (String) req.getSession().getAttribute("SESSION_USEREMAILID");
		   PersistenceManager pm = PMFSingleton.getPMF().getPersistenceManager();
		   
		   UserProfile profile = pm.getObjectById(UserProfile.class, userid);
		   LinkedHashMap <String,Map<String,String>> map = profile.getQuerydetails();
		   map.remove(index);
		   profile.setQuerydetails(map);
		   try{
			   pm.makePersistent(profile); 
		   }		   
		   finally{
			   pm.close();
		   }
		   
		return "success";
		   
	   }
	   
	   
	   
	   /**
	    * Adding the custom dimension to the profile
	    * @param customdata
	    * @return
	    */
	   @RequestMapping(value ="/addcustomdimension" )
		public @ResponseBody String addcustomdimension(@RequestBody String customdata){
		   PersistenceManager pm = PMFSingleton.getPMF().getPersistenceManager();
		  String userId, profileidindex,dimension, metrics;
		  System.out.println(customdata);
		  
		  try {
				jsonObj = (JSONObject) parser.parse(customdata);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				log.info(e.getMessage());
			} 
		  userId = (String) jsonObj.get("userid");
		  dimension = (String) jsonObj.get("dimensions");
		  metrics = (String) jsonObj.get("metrics");
		  profileidindex = (String) jsonObj.get("queryId_index");
		  UserProfile profile = pm.getObjectById(UserProfile.class, userId);
		  LinkedHashMap<String, Map<String, String>> map = profile.getQuerydetails(); 
		  Map<String, String> details = map.get(profileidindex);;
		  details.put("customDimensions", dimension);   
		  System.out.println(dimension);
		  details.put("customMetrics", metrics);
		  map.put(profileidindex, details);
		  profile.setQuerydetails(map);
		  pm.makePersistent(profile);
		  pm.close();
		   return "success";
	   }
	   /**
	    * Share the user profile to other users
	    * @param requestData
	    * @return
	    */
	   @RequestMapping(value = "/shareprofile")
	   public @ResponseBody String shareprofile(@RequestBody String requestData){
		   PersistenceManager pm = PMFSingleton.getPMF().getPersistenceManager();
		   String response = "{\"State\":\"Success\"}";
		   try {
			jsonObj = (JSONObject) parser.parse(requestData);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.warning(e.getMessage());
		}
		   String toAddress = (String) jsonObj.get("toAddress");
		   String fromAddress = (String) jsonObj.get("fromAddress");
		   JSONObject profile = (JSONObject) jsonObj.get("profile");
		   String profilename = (String) profile.get("profileName");
		   byte[] stringbyte = Base64.encodeBase64(profilename.getBytes());
		   String value = toAddress+"&"+fromAddress+"&"+profile.toString();
		  // UserProfile user = (UserProfile) pm.getObjectById(UserProfile.class,toAddress);
		   System.out.println(value);
		   ShareProfile share = new ShareProfile();
		   share.setFromAddress(fromAddress);
		   share.setToAddress(toAddress);
		   share.setProfile(profile.toJSONString());
		   try{			   

			   cache = CacheInital.getcacheInstance();
			   ArrayList list2 =(ArrayList) cache.get("emailnotification");
			
			   ArrayList list = new ArrayList();
			   list.add(value);
			   if(list2 != null)
			   list.addAll(list2);
			   cache.put("emailnotification", list);

		   }
		   catch(JDOObjectNotFoundException e){
			   response = "Emailid not in the list";
		   }
		   catch(Exception e){
			   log.warning(e.getMessage());
		   }
		   finally{
			   pm.close();
		   }
		   return response;
		   
	   }
	   /**
	    * Check the any profile shared or not
	    * @param req
	    * @return
	    */
	   @RequestMapping(value = "/checkshareProfile")
	   public @ResponseBody String checkshareProfile(HttpServletRequest req){
		   String user = (String) req.getSession().getAttribute("SESSION_USEREMAILID");
		  // PersistenceManager pm = PMFSingleton.getPMF().getPersistenceManager();
		   String response = "null ";
		       try{
		    	   cache = CacheInital.getcacheInstance();
		    	   ArrayList list = (ArrayList) cache.get("emailnotification");
		    	   ArrayList list1 = new ArrayList();
		    	   ArrayList list2 = new ArrayList();
		    	   Iterator it =list.iterator();
		    	   while(it.hasNext()){
		    		   String element =  it.next().toString();
		    		   String toAddress = element.toString().split("&")[0];
		    		   if(toAddress.equals(user)){
		    			   list1.add(element);
		    			   
		    		   }
		    		   else{
		    			   list2.add(element);
		    		   }
		    		   
		    	   }
		    	   cache.put("emailnotification", list2);		    	   
		    	   response = jsonMap.writeValueAsString(list1);
		    	  
		       }catch(Exception e){
		    	   
		    	   log.warning(e.getMessage());
		       }   
			

		   
		   return response;
	   }
	   
		/**
		 * Parsing the QueryData and Calling the G Data Result
		 * @param QueryData
		 * @param req
		 * @return G Data Results
		 */
		//Sending data with queryInformation and analytics obj
		static String getGaData(String QueryData,String accessToken){
			new JSONObject();			
			
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
			String table_id = (String) jsonObj.get("queryId");
			String metrics = (String) jsonObj.get("metrics");
			String dimension = (String) jsonObj.get("dimensions");
			String startDate =  (String) jsonObj.get("startDate");
			String endDate =  (String) jsonObj.get("endDate");
			String segment = (String) jsonObj.get("segment");
			String filter = (String) jsonObj.get("filter");
			String sort = (String) jsonObj.get("sort");
			String maxResult =  (String) jsonObj.get("maxResults");
			String startIndex = (String) jsonObj.get("startIndex");
			String profileName = (String) jsonObj.get("profileName");
			System.out.println("profilename"+profileName);
			log.info(table_id + " " + metrics + " " + dimension.length() + " " + startDate + " " + endDate+" "+filter+" "+ segment+" "+sort+""+maxResult);
			data_result = getResultsData(table_id,metrics,dimension,startDate,endDate,filter,segment,sort,maxResult,startIndex,analytics);
			
			
//			try {			
//				data_result = getResultsData(table_id,metrics,dimension,startDate,endDate,filter,segment,sort,maxResult,analytics);
//				//log.info(data_result);
//				jsonObj = (JSONObject) parser.parse(data_result);
//				objJson =  (JSONObject) jsonObj.get("dataTable");
//				data_result = objJson.toJSONString();
//				//log.info(data_result);
//				
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				log.warning("json Parsing error");
//			}
			
			
			log.info("Data Sent to client"+printTime.toString());
			return data_result ;
			
			
		}
		/**
		 * Get datafrom MemCache
		 * @param key
		 * @return
		 */
	  private static String getCachedata(String key){
		  cache = CacheInital.getcacheInstance();
		  System.out.println(key);
		  String outputdata =   (String) cache.get(key);
		//  System.out.println("byte value:"+bytevalue);
		  String querydata="";
		  if(outputdata !=null)
		  querydata= DataCompress.deCompression(outputdata);
		  return querydata;
	  }
		
		/**
		 * 
		 * @param userId
		 * @param data_result
		 */
		private static void saveCache(String userId, String data_result) {
			// TODO Auto-generated method stub
			cache = CacheInital.getcacheInstance();
			
			cache.put(userId, DataCompress.compression(data_result));
			
		}
        /**
         * Method for store the profile
         * @param table_id
         * @param metrics
         * @param dimension
         * @param startDate
         * @param endDate
         * @param userId
         * @param filter
         * @param profileName
         * @param profileIndex
         */
		private static void storeProfileDetails(String table_id,String metrics, String dimension, String startDate,	String endDate, String userId,String filter,String profileName,String profileIndex) {
			// TODO Auto-generated method stub
			System.out.println(userId);
			
			LinkedHashMap<String, Map<String, String>> dMap ;
			PersistenceManager pm = PMFSingleton.getPMF().getPersistenceManager();
			Transaction tx = pm.currentTransaction();
			tx.begin();
			tx.setNontransactionalRead(false);
			tx.setNontransactionalWrite(false);
			UserProfile userprofile = new UserProfile();			
			userprofile.setUserId(userId);
			userprofile.setDateAdded(printTime.toString());
			Map<String,String> map = new LinkedHashMap<String,String>();			
			map.put("queryId", table_id);
			map.put("metrics",metrics);
			map.put("customDimensions", "");
			map.put("customMetrics", "");
			map.put("dimensions", dimension);
			map.put("startDate", startDate);
			map.put("endDate", endDate);
			map.put("filter", filter);
			map.put("profileName", profileName);
			dMap = new LinkedHashMap<String,Map<String,String>>();
			try{
				System.out.println(pm.getObjectById(UserProfile.class, userId));
				UserProfile uf = pm.getObjectById(UserProfile.class, userId);	
				System.out.println(uf.getQuerydetails().toString());
				dMap.putAll(uf.getQuerydetails());
			}catch(Exception e){
				
			log.info("Key nofound");
				
			}			
			dMap.put(profileIndex,map);
			userprofile.setQuerydetails(dMap);
			
			try{				
				pm.makePersistent(userprofile);
				
			}finally{
				tx.commit();
				pm.close();
			}
			
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
				log.warning("Problem in getting Account details"+e.getMessage());
			}
			
			return accountInfo;
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
		private static String  getResultsData(String table_id, String metrics,String dimension, String startDate, String endDate,String filter, String segment, String sort, String maxResult, String startIndex,Analytics analytics) 
		{
		
			log.info("Get the result for analytics");
			GaData value = null;			
			if(segment==null)segment="" ;
			if(sort==null)sort = "" ;
			int maxResults;
			if(maxResult == null)
				maxResults =10000;
			else
				maxResults= Integer.parseInt(maxResult);
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
				if(startIndex != null){
					apiQuery.setStartIndex(Integer.parseInt(startIndex));
				}
//				if(maxResults < 1000){
//					apiQuery.setMaxResults(maxResults);
//				}else{
//					apiQuery.setMaxResults(999);
//				}
				log.info("Data send to analytics"+printTime.toString());
				value = apiQuery.setMaxResults(maxResults).execute();
				log.info("Data receive from analytics"+printTime.toString());
				//log.info(value.toString());
				return value.toString();
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
	    	Credential credential =null;
	    	Analytics analytics =null;
	    	credential= new GoogleCredential.Builder().setClientSecrets(OauthController.clientSecrets).setJsonFactory(OauthController.JSON_FACTORY).setTransport(OauthController.HTTP_TRANSPORT).build();
	    	credential.setAccessToken(accesstoken);
	    	//log.info("credentil accesstoken"+credential.getAccessToken());
	    	 analytics = new Analytics.Builder(OauthController.HTTP_TRANSPORT, OauthController.JSON_FACTORY, credential).setApplicationName("Web Anlaytics").build();
	    	//log.info("Analytics obj"+analytics.getRootUrl()+""+analytics.getBaseUrl());
	    	credential = null;
	    	return analytics;
	    	
	    }
	    
	    
	   

}
