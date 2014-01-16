package com.google.webAnalytics;



import java.io.IOException;
import java.io.InputStreamReader;
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
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.Accounts;
import com.google.api.services.analytics.model.Profiles;
import com.google.api.services.analytics.model.Webproperties;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;


@Controller
@RequestMapping("/")
public class WebAnalyticsServlet  {
	final static JacksonFactory JSON_FACTORY = new  JacksonFactory();
    final static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static HashSet<String> scopes = new HashSet<String>();
    static GoogleAuthorizationCodeRequestUrl url;
    String location;
    Analytics analytics;
    ObjectMapper jsonMap = new ObjectMapper();
    JSONObject jsonObj = new JSONObject();
	JSONParser parser = new JSONParser();    
	Logger log = Logger.getLogger("LogInfo");	
	
	@RequestMapping("/oauth2callback")
	public String callback(HttpServletRequest req, HttpServletResponse resp,ModelMap model){
		
		Credential credential;
		String code = req.getParameter("code");
		Oauth2 userService;
		Userinfo userInfo;
		HttpSession session;
		String emailid;
		Accounts account;
		
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
				analytics = new Analytics.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Web Analytics").build();
				account= analytics.management().accounts().list().execute();			
				log.info(account.getItems().get(0).getName());				
				System.out.println("Analytics Obj: "+analytics);
				System.out.println("Refresh token: "+ credential.getRefreshToken());
				location = "redirect:/home";				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.info("Redirect to LoginPage");
				try {
					resp.sendRedirect("/index.html");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					log.info("login page");
				}
			}
			
			
		}
		else{
		    log.warning("code not get");
			
		}
		return location;
		
	}

	

	

//Redirection Home Page
@RequestMapping("/home")
public String homeRedirect(HttpServletRequest req,HttpServletResponse resp,ModelMap model){
		HttpSession session =  req.getSession(false);
		String urlLocation = null;
		if(session == null){
			try {
				resp.sendRedirect("/index.html");
			} catch (IOException e) {
				System.out.println("Problem in response redirection");
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

     //Send the Authroziation code and getting the accesstoken, creating Credential Object
	private Credential getCredential(String code) {
		GoogleTokenResponse tokenResponse;
		try {			
			System.out.println("Authorization code: "+code);
			tokenResponse = new GoogleAuthorizationCodeTokenRequest(HTTP_TRANSPORT,JSON_FACTORY,clientSecrets().getWeb().getClientId(),clientSecrets().getWeb().getClientSecret(),code,clientSecrets().getWeb().getRedirectUris().get(0)).execute();
			return new GoogleCredential.Builder().setClientSecrets(clientSecrets()).setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).build().setAccessToken(tokenResponse.getAccessToken()).setRefreshToken(tokenResponse.getRefreshToken());
		} catch (IOException e) {
			System.out.println("Problem in sending code or creating credential object");
		}
		return null;
	}

	//Login URL
	@RequestMapping("/googleLogin")
	public void googleLoginUrl(HttpServletResponse response){
		try {
			response.sendRedirect(getBuildinLoginUrl());
		} catch (IOException e) {			
			System.out.println("Probelm in response redirection");
		}
		
	}
	//Handle Ajax url request for get the Property info
	@RequestMapping("/getPropertyInfo")
	public @ResponseBody String getPropertyInfo(@RequestBody String ajaxdata,HttpServletRequest req){
		Webproperties webProperties;
		Analytics analytics;
		String accessToken;
		String accountId;
		String properties ="";
		Map<String,String> propertiesInfo =  new LinkedHashMap<String,String>();
		accessToken= (String) req.getSession().getAttribute("USER_ACCESSTOKEN");
		analytics= getAnalayticsObj(accessToken);
		System.out.println(ajaxdata);
		accountId=ajaxdata.substring(0, ajaxdata.length()-1);		
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
		}
		 return properties;
	}
	
	//Handle Ajax url request for get the Profile info
	@RequestMapping("/getProfileInfo")
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
		analytics = getAnalayticsObj(accessToken);
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

	//User logout
	@RequestMapping("/logout")
	public String logout(HttpServletRequest req, HttpServletResponse resp){
		HttpSession session = req.getSession(false);
		session.invalidate();		
		return "logout";
	}
	
	
	//Handle Ajax url request for get the account info
	@RequestMapping("/getAccountInfo")
	public @ResponseBody String getAccountInfo(HttpServletRequest req){
		log.info("return the acccountinfo");
		String accessToken = (String) req.getSession().getAttribute("USER_ACCESSTOKEN");			
		Analytics analytics = getAnalayticsObj(accessToken);	
		System.out.println("Analytics obj: "+analytics);	
		String 	accountInfo = getAccountInfo(analytics);
	 return accountInfo;	
	}
	
	

	
	//Creating the Analytics Object 
    private Analytics getAnalayticsObj(String accesstoken){
    	Credential credential = new GoogleCredential.Builder().setClientSecrets(clientSecrets()).setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).build();
    	credential.setAccessToken(accesstoken);
    	Analytics analytics = new Analytics.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Web Anlaytics").build();
    	return analytics;
    	
    }

	@RequestMapping("/getdataAjax")
	public @ResponseBody String getData(@RequestBody String data,HttpServletRequest req) {
		
		String accessToken = (String) req.getSession().getAttribute("USER_ACCESSTOKEN");	
		System.out.println("Accesstoken"+accessToken);      	
		Analytics analytics = getAnalayticsObj(accessToken);		
		log.info("Parsing the data from clent");		
		String data_result = null;
		try
		{
		jsonObj = (JSONObject) parser.parse(data);		
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
		System.out.println(table_id + " " + metrics + " " + dimension + " " + startDate + " " + endDate);
		data_result = getResultsData(table_id,metrics,dimension,startDate,endDate,analytics);
		//System.out.println(data_result);
		
		return data_result;
		
	}
	
	//Getting the GaData from Anlaytics
	private String getResultsData(String table_id, String metrics,String dimension, String startDate, String endDate,Analytics analytics) 
	{
	
	log.info("Get the result for analytics");
		String value = null;
		try {
		value = analytics.data().ga().get(table_id, startDate, endDate, metrics).setDimensions(dimension).setOutput("dataTable").setMaxResults(50).execute().toPrettyString();
		} catch (IOException e) {
		
		value = "no data found";
		}
	return value;
	}
	
	//Getting Client Secrects form  json file
	static private GoogleClientSecrets clientSecrets(){
		try{
			return GoogleClientSecrets.load(JSON_FACTORY,new InputStreamReader(WebAnalyticsServlet.class.getResourceAsStream("client_secrets.json")));
		}catch(Exception e){
			System.out.println("Problem in getting Client Secrets");
		}
		return null;
		
	}
	
	//Building Login Url
	static private String getBuildinLoginUrl() {		
		scopes.add(AnalyticsScopes.ANALYTICS_READONLY);
        scopes.add("https://www.googleapis.com/auth/userinfo.email");
        url = new GoogleAuthorizationCodeRequestUrl(clientSecrets().getWeb().getClientId(),clientSecrets().getWeb().getRedirectUris().get(0),scopes).setAccessType("offline");
		return url.build();
	}
	
	
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
			System.err.println(e.getStackTrace());
		}
		
		return accountInfo;
	}

	
}
