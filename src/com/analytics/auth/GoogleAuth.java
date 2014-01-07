package com.analytics.auth;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.extensions.appengine.auth.oauth2.AppEngineCredentialStore;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.appengine.tools.admin.OAuth2Native;

public class GoogleAuth {
	 static final String CALLBACK_URL = "http://analytics-demo-work.appspot.com/oauth2callback";
	 static final JsonFactory JSON_FACTORY = new  JacksonFactory();
	 static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	 static final String SESSION_USERID = "emailId";
	// static CredentialStore credentialStore = new CredentialStore();
	 static AppEngineDataStoreFactory dataStore = AppEngineDataStoreFactory.getDefaultInstance();
	// CredentialManager credentialManager ;
	
     GoogleClientSecrets clientSecrets = getClientSecrets();
     
     //scopes
     HashSet<String> scopes = new HashSet<String>(); 	
	 
     
     
	 //Get ClientSecrets for json file
	 private GoogleClientSecrets getClientSecrets(){
		 
		 try {
			return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(GoogleAuth.class.getResourceAsStream("client_json")));
		} catch (IOException e) {
			
			throw new RuntimeException("client_json fil not found");
		}
		
		 
	 }
	 
	 //build Login url for new user
	 void buildLoginUrl(HttpServletRequest req, HttpServletResponse resp){
		 
		 try {
			resp.sendRedirect(getAuthorizationUrl());
		} catch (IOException e) {
			
			System.out.println("Error at creating authorization url");
		}
		 
	 }
	 
	 //callback part for get access code
	 void callbackAccess(HttpServletRequest req, HttpServletResponse resp){
		 String code = req.getParameter("code");
		 Credential credential_in = retriveAccesscode(code);
		 Oauth2 userService = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential_in).build();
		 try {
			Userinfo userInfo = userService.userinfo().get().execute();
			String emailid = userInfo.getEmail();
			storeCredential(credential_in,emailid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error at getting userInfo");
		}
		 
	 }
	 
	 //Store the credential along with emailid
    private void storeCredential(Credential credential_in, String emailid) {
//		dataStore.
		
	}

	//retrive the accesscode
	private Credential retriveAccesscode(String code) {
		
		try {
			GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(HTTP_TRANSPORT,JSON_FACTORY,clientSecrets.getWeb().getClientId(),clientSecrets.getWeb().getClientSecret(),code,clientSecrets.getWeb().getRedirectUris().get(0)).execute();
			return new GoogleCredential.Builder().setClientSecrets(clientSecrets).setTransport(HTTP_TRANSPORT).setJsonFactory(JSON_FACTORY).build().setAccessToken(tokenResponse.getAccessToken());
		} catch (IOException e) {
			
			System.out.println("Problem at getting Access token");
		}
		
		return null;
	}

	private String getAuthorizationUrl() {
		scopes.add(AnalyticsScopes.ANALYTICS_READONLY);
		GoogleAuthorizationCodeRequestUrl url = new GoogleAuthorizationCodeRequestUrl(clientSecrets.getWeb().getClientId(),clientSecrets.getWeb().getRedirectUris().get(0), scopes).setAccessType("offline");
		return url.build();
	}
}
