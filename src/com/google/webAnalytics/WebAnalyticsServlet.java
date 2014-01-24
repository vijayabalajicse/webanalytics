package com.google.webAnalytics;



import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.Analytics.Data.Ga.Get;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.Accounts;
import com.google.api.services.analytics.model.Profiles;
import com.google.api.services.analytics.model.Webproperties;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;


@Controller

public class WebAnalyticsServlet  {
	final static JacksonFactory JSON_FACTORY = new  JacksonFactory();
    final static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static HashSet<String> scopes = new HashSet<String>();
    static GoogleAuthorizationCodeRequestUrl url;
    String location;    
    ObjectMapper jsonMap = new ObjectMapper();
    JSONObject jsonObj = new JSONObject();
	JSONParser parser = new JSONParser();    
	Logger log = Logger.getLogger("LogInfo");	
	Date printTime = new Date();
	static GoogleClientSecrets clientSecrets = clientSecrets();
	
	/**
	 *  /CallBack url for OAuth
	 *   @param req
	 *   @param resp	
	 *   retrun redirect /home   page url
	 */
	
	@RequestMapping(value="/oauth2callback")
	public String callback(HttpServletRequest req, HttpServletResponse resp){
		
		Credential credential;
		String code = req.getParameter("code");
		Oauth2 userService;
		Userinfo userInfo;
		HttpSession session;
		String emailid;			
		if(req.getParameter("code") != null || req.getParameter("code") != ""){			   
			credential = getCredential(code);
			userService = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Web Analytics").build();
			try {
				userInfo = userService.userinfo().get().execute();
				emailid =  userInfo.getEmail();
				session=req.getSession(); 				
				session.setAttribute("SESSION_USEREMAILID", emailid);
				session.setAttribute("USER_ACCESSTOKEN", credential.getAccessToken());
				session.setAttribute("USER_REFRESHTOKEN", credential.getRefreshToken());
				session.setAttribute("EXPIRES_MILISECOND", credential.getExpirationTimeMilliseconds());
				session.setAttribute("USER_AUTH_CODE", code);	
				System.out.println(printTime+"Refresh token: "+ credential.getRefreshToken());
				location = "redirect:/home";				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.info("Redirect to LoginPage");
				try {
					resp.sendRedirect("/index.html");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					log.info("login page error");
				}
			}
			
			
		}
		else{
		    log.warning("code not get");
			
		}
		return location;
		
	}

	

	/**
	 * Home Page Url
	 * @param req
	 * @param resp	 
	 * @return  Home Page
	 */

//Redirection Home Page
@RequestMapping(value="/home")
public String homeRedirect(HttpServletRequest req,HttpServletResponse resp){
		HttpSession session =  req.getSession(false);
		String urlLocation = null;
		if(session == null){
			try {
				resp.sendRedirect("/index.html");
			} catch (IOException e) {
				System.out.println(printTime+"Problem in response redirection");
			}
		}
		else if(session.getAttribute("SESSION_USEREMAILID") == null || session.getAttribute("SESSION_USEREMAILID") == ""){
			urlLocation = "logout";
		
		}
		else{
			urlLocation = "home";
		}
	
	return urlLocation;
}
/**
 * Using Authorization code getting accesstoken, Using Token creating Credential
 * 
 * @param Authorizationcode
 * @return Credential or null
 */
     //Send the Authroziation code and getting the accesstoken, creating Credential Object
	private Credential getCredential(String code) {
		GoogleTokenResponse tokenResponse;
		try {			
			System.out.println(printTime+"Authorization code: "+code);
			tokenResponse = new GoogleAuthorizationCodeTokenRequest(HTTP_TRANSPORT,JSON_FACTORY,clientSecrets.getWeb().getClientId(),clientSecrets.getWeb().getClientSecret(),code,clientSecrets.getWeb().getRedirectUris().get(0)).execute();
			return new GoogleCredential.Builder().setClientSecrets(clientSecrets).setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).build().setAccessToken(tokenResponse.getAccessToken()).setRefreshToken(tokenResponse.getRefreshToken());
		} catch (IOException e) {
			System.out.println(printTime+"Problem in sending code or creating credential object");
		}
		return null;
	}

	/**
	 * Calling LoginIn Url for OAuth
	 * @param response
	 */
	//Login URL
	@RequestMapping(value="/googleLogin")
	public void googleLoginUrl(HttpServletResponse response){
		try {
			response.sendRedirect(getBuildinLoginUrl());
		} catch (IOException e) {			
			System.out.println(printTime+"Probelm in response redirection");
		}
		
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
		
		System.out.println(printTime+ajaxdata);
		System.out.println(printTime + ""+ajaxdata.length());
		accountId=ajaxdata.substring(0, ajaxdata.length()-1);	
		System.out.println(accountId+""+accountId.length());
		try {
			 webProperties = analytics.management().webproperties().list(accountId).execute();
			if(webProperties.getItems().isEmpty()){
				System.out.println("No data found");
			}else{
				for (int i=0;i<webProperties.getItems().size();i++){
				propertiesInfo.put(webProperties.getItems().get(i).getId(),webProperties.getItems().get(i).getName());
				}
				properties = jsonMap.writeValueAsString(propertiesInfo);
				System.out.println(properties);
			}
				
		} catch (IOException e) {			
			System.out.println("Problem in getting the WebProperties from Analytics");
			e.printStackTrace();
		}
		 return properties;
	}
	
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
		System.out.println("EmailData: "+emailData);
		try {
			jsonObj = (JSONObject) parser.parse(emailData);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		String emailid = (String) jsonObj.get("emailId");
		System.out.println(emailid);
		getDataTable = getGaData(emailData,req);
		Queue taskQueue = QueueFactory.getQueue("EmailQueue");
		taskQueue.add(TaskOptions.Builder.withUrl("/sendemail").param("emailId", emailid).param("csvData", getDataTable));
		return "{\"status\":\"Success\"}";
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
		System.out.println(propData);
		try {
			
			jsonObj = (JSONObject) parser.parse(propData);
		} catch (ParseException e) {			
			System.out.println("Problem in parse the json Data");
		}
		
		System.out.println(jsonObj.get("AccountId"));
		System.out.println(jsonObj.get("PropertiesID"));
		accountId  = (String) jsonObj.get("AccountId");
		propertyId = (String) jsonObj.get("PropertiesID");
		accessToken = (String) req.getSession().getAttribute("USER_ACCESSTOKEN");
		analytics = getAnalayticsObject(accessToken);
		try {
			 profile = analytics.management().profiles().list(accountId, propertyId).execute();
				if(profile.getItems().isEmpty()){
					System.out.println("Empty");
				}else{
				for(int i=0;i< profile.getItems().size();i++){
					profileMap.put(profile.getItems().get(i).getId(),profile.getItems().get(i).getName());
				}
				
				profileInfo = jsonMap.writeValueAsString(profileMap);
				System.out.println(profileInfo);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return profileInfo;
	}
  
	/**
	 * Logout the application 
	 * @param req
	 * @param resp
	 * @return logout page
	 */
	//User logout
	@RequestMapping(value="/logout")
	public String logout(HttpServletRequest req, HttpServletResponse resp){
		HttpSession session = req.getSession(false);
		session.invalidate();		
		return "logout";
	}
	
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
		//System.out.println("Analytics obj: "+analytics);	
		String 	accountInfo = getAccountInfo(analytics);
	 return accountInfo;	
	}
	
	/**
	 * Saving User Profile
	 */
	@RequestMapping(value="/profilesave")
	public @ResponseBody String saveProfile(@RequestBody String profileData,HttpServletRequest req){
		System.out.println(profileData);
		try {
			jsonObj = (JSONObject) parser.parse(profileData);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}
		
		
		return profileData;
		
	}
	
   /**
    * Getting Analytics Object using Accesstoken
    * @param accesstoken
    * @return Analytics Object for Coressponding user
    */
	
	//Creating the Analytics Object 
    private Analytics getAnalayticsObject(String accesstoken){
    	Credential credential = new GoogleCredential.Builder().setClientSecrets(clientSecrets).setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).build();
    	credential.setAccessToken(accesstoken);
    	//System.out.println("credentil accesstoken"+credential.getAccessToken());
    	Analytics analytics = new Analytics.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Web Anlaytics").build();
    	//System.out.println("Analytics obj"+analytics.getRootUrl()+""+analytics.getBaseUrl());
    	return analytics;
    	
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
	private String getGaData(String QueryData,HttpServletRequest req){
		JSONObject objJson= new JSONObject();
		String dataTablevalue = null;
		String accessToken = (String) req.getSession().getAttribute("USER_ACCESSTOKEN");	
		System.out.println("Accesstoken"+accessToken);      	
		Analytics analytics = getAnalayticsObject(accessToken);		
		log.info("Parsing the data from clent");
		System.out.println("Query data"+QueryData);
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
		System.out.println(table_id + " " + metrics + " " + dimension.length() + " " + startDate + " " + endDate+" "+filter.length()+" "+ segment.length()+" "+sort.length()+""+maxResult);
		
		try {			
			data_result = getResultsData(table_id,metrics,dimension,startDate,endDate,filter,segment,sort,maxResult,analytics);
			System.out.println(data_result);
			jsonObj = (JSONObject) parser.parse(data_result);
			objJson =  (JSONObject) jsonObj.get("dataTable");
			data_result = objJson.toJSONString();
			System.out.println(data_result);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("json Parsing error");
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
	private String getResultsData(String table_id, String metrics,String dimension, String startDate, String endDate,String filter, String segment, String sort, String maxResult, Analytics analytics) 
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
			if(maxResults < 1000){
				apiQuery.setMaxResults(maxResults);
			}else{
				apiQuery.setMaxResults(100);
			}
			value = apiQuery.setOutput("dataTable").execute().toPrettyString();
			return value;
		} catch (Exception e) {	
			System.out.println(e);
			return e.getMessage();
		}
	
	}
	
	/**
	 * Getting Application Information from client_Secrets.json file
	 * @return ClientSecrets Information
	 */
	//Getting Client Secrects form  json file
	static private GoogleClientSecrets clientSecrets(){
		try{
			return GoogleClientSecrets.load(JSON_FACTORY,new InputStreamReader(WebAnalyticsServlet.class.getResourceAsStream("client_secrets.json")));
		}catch(Exception e){
			System.out.println("Problem in getting Client Secrets");
		}
		return null;
		
	}
	/**  
	 * Building the Login Url for OAuth
	 * @return
	 */
	//Building Login Url
	static private String getBuildinLoginUrl() {		
		scopes.add(AnalyticsScopes.ANALYTICS_READONLY);
        scopes.add("https://www.googleapis.com/auth/userinfo.email");
        url = new GoogleAuthorizationCodeRequestUrl(clientSecrets.getWeb().getClientId(),clientSecrets.getWeb().getRedirectUris().get(0),scopes).setAccessType("offline");
		return url.build();
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
			System.out.println("Sorry no Account founded");
			System.out.println("Account name: "+account.getItems().get(0).getName());
			log.info("No Account found");
			}
			else{
				for ( int i =0; i< account.getItems().size();i++){					
					accountNameInfo.put(account.getItems().get(i).getId(), account.getItems().get(i).getName());					
				}				
				accountInfo = jsonMap.writeValueAsString(accountNameInfo);
				System.out.println(accountInfo);	
				
		
		}
		
		}catch(Exception e){
			System.out.println("Problem in getting Account details");
		}
		
		return accountInfo;
	}

	
}
