package com.google.webAnalytics;



import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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























import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


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
	static final String KIND = "AnalticsObject";
	@RequestMapping("/oauth2callback")
	public String callback(HttpServletRequest req, HttpServletResponse resp,ModelMap model){
		
		Credential credential;
		String code = req.getParameter("code");
		Oauth2 userService;
		Userinfo userInfo;	
		
		if(req.getParameter("code") != null || req.getParameter("code") != ""){
			   
			credential = getCredential(code);
			userService = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Web Analytics").build();
			try {
				userInfo = userService.userinfo().get().execute();
				String emailid =  userInfo.getEmail();
				HttpSession session=req.getSession(); 
				
				session.setAttribute("SESSION_USEREMAILID", emailid);
				session.setAttribute("USER_ACCESSTOKEN", credential.getAccessToken());
				session.setAttribute("USER_REFRESHTOKEN", credential.getRefreshToken());
				session.setAttribute("EXPIRES_MILISECOND", credential.getExpirationTimeMilliseconds());
				session.setAttribute("USER_AUTH_CODE", code);
			//	storeCredential(emailid,credential);
				
				analytics = new Analytics.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Web Analytics").build();
				Accounts account = analytics.management().accounts().list().execute();
			//	session.setAttribute("SessionGadate", account.);
				log.info(account.getItems().get(0).getName());
				//session.setAttribute("SESSION_OBJ" );
				//session.setAttribute("USERCredential", analytics);
				//storeAnalyticsObject(emailid,analytics.toString());
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

	

	


@RequestMapping("/home")
public String homeRedirect(HttpServletRequest req,HttpServletResponse resp,ModelMap model){
	HttpSession session =  req.getSession(false);
	String urlLocation = null;
	if(session == null){
    try {
		resp.sendRedirect("/index.html");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
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
	private Credential getCredential(String code) {
		// TODO Auto-generated method stub
		try {			
			System.out.println("Authorization code: "+code);
			GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(HTTP_TRANSPORT,JSON_FACTORY,clientSecrets().getWeb().getClientId(),clientSecrets().getWeb().getClientSecret(),code,clientSecrets().getWeb().getRedirectUris().get(0)).execute();
			return new GoogleCredential.Builder().setClientSecrets(clientSecrets()).setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).build().setAccessToken(tokenResponse.getAccessToken()).setRefreshToken(tokenResponse.getRefreshToken());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@RequestMapping("/googleLogin")
	public void googleLoginUrl(HttpServletResponse response){
		try {
			response.sendRedirect(getBuildinLoginUrl());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@RequestMapping("/getPropertyInfo")
	public @ResponseBody String getPropertyInfo(@RequestBody String ajaxdata,HttpServletRequest req){
		
		String accessToken = (String) req.getSession().getAttribute("USER_ACCESSTOKEN");
		Analytics analytics = getAnalayticsObj(accessToken);
		Map propertiesInfo =  new LinkedHashMap();
		String accountId =ajaxdata.substring(0, ajaxdata.length()-1);
		String properties ="";
		try {
			Webproperties webProperties = analytics.management().webproperties().list(accountId).execute();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return properties;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/getProfileInfo")
	public @ResponseBody String getProfileInfo(@RequestBody String propData,HttpServletRequest req){
		Map profileMap = new LinkedHashMap();
		String profileInfo ="";
		System.out.println(propData);
		try {
			
			jsonObj = (JSONObject) parser.parse(propData);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(jsonObj.get("AccountId"));
		System.out.println(jsonObj.get("PropertiesID"));
		String accountId = (String) jsonObj.get("AccountId");
		String propertyId= (String) jsonObj.get("PropertiesID");
		String accessToken = (String) req.getSession().getAttribute("USER_ACCESSTOKEN");
		Analytics analytics = getAnalayticsObj(accessToken);
		try {
			Profiles profile = analytics.management().profiles().list(accountId, propertyId).execute();
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

	@RequestMapping("/logout")
	public String logout(HttpServletRequest req, HttpServletResponse resp){
		HttpSession session = req.getSession(false);
		session.invalidate();
		
		return "logout";
	}
	
	@RequestMapping("/getAccountInfo")
	public @ResponseBody String getAccountInfo(HttpServletRequest req){
		log.info("return the acccountinfo");
		String accessToken = (String) req.getSession().getAttribute("USER_ACCESSTOKEN");
		//String refreshcode = (String) req.getSession().getAttribute("USER_REFRESHTOKEN");
		//long expiresMiliSeconds = (long) req.getSession().getAttribute("EXPIRES_MILISECOND");
		//System.out.println(accessToken + "   " );
      //  Credential credential = new GoogleCredential.Builder().setClientSecrets(clientSecrets()).setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).build();
       // credential.setAccessToken(accessToken);
      //  credential.setRefreshToken(refreshcode);
      //  credential.setExpirationTimeMilliseconds(expiresMiliSeconds);		
	Analytics analytics = getAnalayticsObj(accessToken);	
	//Analytics analytics_1 = (Analytics) req.getSession().getAttribute("SESSION_OBJ");	
	System.out.println("Analytics obj: "+analytics);	
	//System.out.println("Analytics client: " +analytics.getGoogleClientRequestInitializer());
	//System.out.println("Analytics Reg: "+analytics.getRequestFactory());
	String 	accountInfo = getProfileId(analytics);
	 return accountInfo;	
	}
	
	private Analytics getAnalaytics(String emailid) {
		Credential credential = new GoogleCredential.Builder().setClientSecrets(clientSecrets()).setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).build();
		Analytics analytics = null;
		if(getCredentialformStore(emailid,credential)){
			analytics = new Analytics.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
		}
		
		
		
		return  analytics;
	}


	private boolean getCredentialformStore(String emailid, Credential credential) {
		// TODO Auto-generated method stub
		DatastoreService getcredential = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey(KIND, emailid);
		try {
			Entity entity = getcredential.get(key);
			credential.setAccessToken((String) entity.getProperty("accessToken"));
			credential.setRefreshToken((String) entity.getProperty("refreshToken"));
			return true;
			
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}	
		
		
		
	}
    private Analytics getAnalayticsObj(String accesstoken){
    	Credential credential = new GoogleCredential.Builder().setClientSecrets(clientSecrets()).setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).build();
    	credential.setAccessToken(accesstoken);
    	Analytics analytics = new Analytics.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
    	return analytics;
    	
    }

	@RequestMapping("/getdataAjax")
	public @ResponseBody String getData(@RequestBody String data,HttpServletRequest req) {
		
		String accessToken = (String) req.getSession().getAttribute("USER_ACCESSTOKEN");
		//String refreshcode = (String) req.getSession().getAttribute("USER_REFRESHTOKEN");
	//	long expiresMiliSeconds = (long) req.getSession().getAttribute("EXPIRES_MILISECOND");
		System.out.println(accessToken + "   " );
      //  Credential credential = new GoogleCredential.Builder().setClientSecrets(clientSecrets()).setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).build();
       // credential.setAccessToken(accessToken);
       // credential.setRefreshToken(refreshcode);
      //  credential.setExpirationTimeMilliseconds(expiresMiliSeconds);		
		Analytics analytics = getAnalayticsObj(accessToken);
		//String table_id,metrics,dimension,startDate,endDate;
		log.info("Parsing the data from clent");
		//JSONObject jsonObj = new JSONObject();
		//JSONParser parser = new JSONParser();
		String data_result = null;
		try
		{
		jsonObj = (JSONObject) parser.parse(data);
		String table_id = (String) jsonObj.get("tableid");
		String metrics = (String) jsonObj.get("metrics");
		String dimension = (String) jsonObj.get("dimension");
		String startDate =  (String) jsonObj.get("startDate");
		String endDate =  (String) jsonObj.get("endDate");
		System.err.println(table_id + " " + metrics + " " + dimension + " " + startDate + " " + endDate);
		data_result = getResultsData(table_id,metrics,dimension,startDate,endDate,analytics);
		System.out.println(data_result);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return data_result;
		
	}
	
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
	
	static private GoogleClientSecrets clientSecrets(){
		try{
			return GoogleClientSecrets.load(JSON_FACTORY,new InputStreamReader(WebAnalyticsServlet.class.getResourceAsStream("client_secrets.json")));
		}catch(Exception e){
			
		}
		return null;
		
	}
	static private String getBuildinLoginUrl() {
		// TODO Auto-generated method stub
		scopes.add(AnalyticsScopes.ANALYTICS_READONLY);
        scopes.add("https://www.googleapis.com/auth/userinfo.email");
        url = new GoogleAuthorizationCodeRequestUrl(clientSecrets().getWeb().getClientId(),clientSecrets().getWeb().getRedirectUris().get(0),scopes).setAccessType("offline");
		return url.build();
	}
	
	
	
	private String getProfileId(Analytics analytics)  
	{
		log.info("Get profile info");
		Map accountNameInfo = new LinkedHashMap();
		ObjectMapper jsonMap = new ObjectMapper();
		List list = new ArrayList();
		//String profileId = null;
		String accountInfo = "";
		//String accountName ="",webProp = "",profileName ="",profId="",accountId="",propId="",defaultvalue="";
		try
		{
			Accounts account = analytics.management().accounts().list().execute();
			if(account.getItems().isEmpty()){
			System.out.println("Sorry no Account founded");
			System.err.println("Account name: "+account.getItems().get(0).getName());
			log.info("No Account found");
			}
			else{
				for ( int i =0; i< account.getItems().size();i++){
					//accountNameInfo.put( "AccountName: ", "\""+i"\"");
					accountNameInfo.put(account.getItems().get(i).getId(), account.getItems().get(i).getName());
					
					//list.add(account.getItems().get(i).getId(), account.getItems().get(i).getName());
					//list.add(account.getItems().get(i).setId(id), element);
				}
				//list = account.getItems();
				accountInfo = jsonMap.writeValueAsString(accountNameInfo);
				System.out.println(accountInfo);
			//	System.out.println(accountNameInfo.toString());
				//System.out.println(list);
				
				
		
		}
		
		}catch(Exception e){
			System.err.println(e.getStackTrace());
		}
		
		return accountInfo;
	}

	
}
