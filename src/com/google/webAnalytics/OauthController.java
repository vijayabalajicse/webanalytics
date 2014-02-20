package com.google.webAnalytics;



import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.analytics.JDO.PMFSingleton;
import com.analytics.JDO.UserDetails;
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
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;


@Controller

public class OauthController  {
	final static JacksonFactory JSON_FACTORY = new  JacksonFactory();
    final static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static HashSet<String> scopes = new HashSet<String>();
    static GoogleAuthorizationCodeRequestUrl url;
    String location;    
    ObjectMapper jsonMap = new ObjectMapper();
    JSONObject jsonObj = new JSONObject();
	JSONParser parser = new JSONParser();    
	static Logger log = Logger.getLogger("LogInfo");	
	static Date printTime = new Date();
	static GoogleClientSecrets clientSecrets = clientSecrets();
	static String clientId = clientSecrets.getWeb().getClientId();
	static String clientSecret =clientSecrets.getWeb().getClientSecret();
	static String redirectedurl=clientSecrets.getWeb().getRedirectUris().get(0);
	
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
			//	session.setAttribute("EXPIRES_MILISECOND", credential.getExpirationTimeMilliseconds());
			//	session.setAttribute("USER_AUTH_CODE", code);	
				log.info(printTime+"Refresh token: "+ credential.getRefreshToken());
				location = "redirect:/home";				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.info("Redirect to LoginPage");
				try {
					resp.sendRedirect("/index.html");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					log.warning("login page error");
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
		if(session != null && session.getAttribute("SESSION_USEREMAILID")!=null){
			log.info(printTime+"redircted home");
			storeRefreshtoken((String)session.getAttribute("SESSION_USEREMAILID"),(String)session.getAttribute("USER_REFRESHTOKEN"));
			urlLocation = "home";
			
		}
		
		else{
			try {
				resp.sendRedirect("/index.html");
			} catch (IOException e) {
				log.warning(printTime+"Problem in response redirection");
			}
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
			log.info(printTime+"Authorization code: "+code);
			tokenResponse = new GoogleAuthorizationCodeTokenRequest(HTTP_TRANSPORT,JSON_FACTORY,clientId,clientSecret,code,redirectedurl).execute();
			//tokenResponse = new GoogleAuthorizationCodeTokenRequest(HTTP_TRANSPORT,JSON_FACTORY,clientSecrets.getWeb().getClientId(),clientSecrets.getWeb().getClientSecret(),code,clientSecrets.getWeb().getRedirectUris().get(0)).execute();
			return new GoogleCredential.Builder().setClientSecrets(clientSecrets).setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).build().setAccessToken(tokenResponse.getAccessToken()).setRefreshToken(tokenResponse.getRefreshToken());
		} catch (IOException e) {
			log.warning(printTime+"Problem in sending code or creating credential object");
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
			log.info(printTime+"started login"); 
			response.sendRedirect(getBuildinLoginUrl());
		} catch (IOException e) {			
			log.warning(printTime+"Probelm in response redirection");
		}
		
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
	 * Saving User Profile
	 */
	@RequestMapping(value="/profilesave")
	public @ResponseBody String saveProfile(@RequestBody String profileData,HttpServletRequest req){
		log.info(profileData);
		try {
			jsonObj = (JSONObject) parser.parse(profileData);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}
		
		
		return profileData;
		
	}
	

	
	/**
	 * Getting Application Information from client_Secrets.json file
	 * @return ClientSecrets Information
	 */
	//Getting Client Secrects form  json file
	static private GoogleClientSecrets clientSecrets(){
		try{
			log.info(printTime+"clientSecrets");
			return GoogleClientSecrets.load(JSON_FACTORY,new InputStreamReader(OauthController.class.getResourceAsStream("client_secrets.json")));
		}catch(Exception e){
			log.warning("Problem in getting Client Secrets");
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
        url = new GoogleAuthorizationCodeRequestUrl(clientId,redirectedurl,scopes).setAccessType("offline");
 //       url = new GoogleAuthorizationCodeRequestUrl(clientSecrets.getWeb().getClientId(),clientSecrets.getWeb().getRedirectUris().get(0),scopes).setAccessType("offline");
	     log.info(printTime+"buildlogin"); 
        return url.build();
	}
	
	/**
	 * Method for store the refresh token for corresponding user
	 * @param userId
	 * @param refreshtoken
	 */
	private void storeRefreshtoken(String userId, String refreshtoken) {
		// TODO Auto-generated method stub
		PersistenceManager  pm = PMFSingleton.getPMF().getPersistenceManager();
		System.out.println("refreshtoken"+refreshtoken);
		if(refreshtoken!=null && refreshtoken!=""){
			System.out.println("refreshtoken"+refreshtoken);
			
			UserDetails details = new UserDetails();
			details.setEmailid(userId);
			details.setRefreshtoken(refreshtoken);
			try{
				pm.makePersistent(details);
			}			
			finally{
				pm.close();
			}
			
		}
		 else
			log.info("No refreshtoken");
		
	}
	

	
}
