package com.google.webAnalytics;



import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.servlet.http.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

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
@RequestMapping("/")
public class WebAnalyticsServlet  {
	final static JacksonFactory JSON_FACTORY = new  JacksonFactory();
    final static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static HashSet<String> scopes = new HashSet<String>();
    static GoogleAuthorizationCodeRequestUrl url;
    String location;
	Logger log = Logger.getLogger("LogInfo");
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
				req.getSession().setAttribute("SESSION_USEREMAILID", emailid);
				req.getSession().setAttribute("USER_ACCESSTOKEN", credential.getAccessToken());
				req.getSession().setAttribute("USER_AUTH_CODE", code);
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
public String homeRedirect(HttpServletRequest req,ModelMap model){
	HttpSession session =  req.getSession(false);
	String urlLocation;
 	if(session.getAttribute("SESSION_USEREMAILID") == null || session.getAttribute("SESSION_USEREMAILID") == ""){
 		
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
			System.out.println(clientSecrets().getWeb().getClientId());
			System.out.println(clientSecrets().getWeb().getClientSecret());
			System.out.println(clientSecrets().getWeb().getRedirectUris().get(0));
			System.out.println(code);
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

	@RequestMapping("/logout")
	public String logout(HttpServletRequest req, HttpServletResponse resp){
		HttpSession session = req.getSession(false);
		session.invalidate();
		
		return "logout";
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
	
	
	
	
}
