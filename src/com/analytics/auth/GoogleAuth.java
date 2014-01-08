package com.analytics.auth;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
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
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class GoogleAuth {
	  final String CALLBACK_URL = "http://localhost:8888/oauth2callback";
	 final JsonFactory JSON_FACTORY = new  JacksonFactory();
	 final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	 final String SESSION_USERID = "emailId";
	//static CredentialStore credentialStore = new CredentialStore();
	// static AppEngineDataStoreFactory dataStore = AppEngineDataStoreFactory.getDefaultInstance();
	// CredentialManager credentialManager ;
	 static final String KIND = GoogleAuth.class.getName();
	 Oauth2 userService;
    // static GoogleClientSecrets clientSecrets = getClientSecrets();
     
     //scopes
     static HashSet<String> scopes = new HashSet<String>(); 	
	 
     
     
	 //Get ClientSecrets for json file
	 public  GoogleClientSecrets getClientSecrets(){
		 
		 try {
			return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(GoogleAuth.class.getResourceAsStream("client_secrets.json")));
		} catch (IOException e) {
			
			System.out.println("problem client_jsonFile");
		}
		
		 return null;
	 }
//	 
	 //build Login url for new user
	public  void buildLoginUrl(HttpServletRequest req, HttpServletResponse resp){
		System.out.println("credential entry point");
		Credential credential = getCredential(req,resp); 
		if(credential == null){
			try {
				System.out.println();
				resp.sendRedirect(getAuthorizationUrl());
			} catch (IOException e) {
				
				System.out.println("Error at creating authorization url");
			}
			
		}
		 
		 
	 }
	 
	 private  Credential getCredential(HttpServletRequest req,
			HttpServletResponse resp) {
		String emailId = (String) req.getSession().getAttribute("SESSION_USERID");
		System.out.println("Session value"+ emailId);
		if(emailId !=null){
			return loadCredential(emailId);
		}
		return null;
	}
	//get the credential data by emailid
	private  Credential loadCredential(String emailId) {
		Credential credential = new GoogleCredential.Builder().setClientSecrets(getClientSecrets()).setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).build();
		if(getCredentialDataStore(emailId,credential)){
			return credential;
		}
		
		return null;
	}
    //get credential form datastore
	private  boolean getCredentialDataStore(String emailId, Credential credential) {
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey(KIND, emailId);
		try{
		Entity entity = dataStore.get(key);
		credential.setAccessToken((String) entity.getProperty("accessToken"));
		credential.setRefreshToken((String) entity.getProperty("refreshToken"));
		credential.setExpirationTimeMilliseconds((Long) entity.getProperty("expirationTimeMillis"));
		return true;
		}catch(Exception e){
			System.out.println("value not fount in entity");
			return false;
		}
		
	}

	//callback part for get access code
	 public  Credential callbackAccess(HttpServletRequest req, HttpServletResponse resp){
		 String code = req.getParameter("code");
		 System.out.println(req.getParameter("code"));
		 System.out.println(code);
		 if(code != null){
			 Credential credential_in = retriveAccesscode(code);
			 userService = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential_in).setApplicationName("web anlytics").build();
			 try {
					Userinfo userInfo = userService.userinfo().get().execute();
					String emailid = userInfo.getEmail();
					System.out.println(emailid);
					storeCredential(credential_in,emailid);
					req.getSession().setAttribute("SESSION_USERID", emailid);
					return credential_in;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Error at getting userInfo");
					
				}
		 }
		 
		 return null;
		 
	 }
	 
	 //Store the credential in datastore
    private  void storeCredential(Credential credential_in, String emailid) {
//		dataStore.
		DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
		Entity entity = new Entity(KIND,emailid);
		entity.setProperty("accessToken", credential_in.getAccessToken());
		entity.setProperty("refreshToken", credential_in.getRefreshToken());
		entity.setProperty("expirationTimeMillis", credential_in.getExpirationTimeMilliseconds());
		dataStore.put(entity);
	}

	//retrive the accesscode
	private  Credential retriveAccesscode(String code) {
		//GoogleClientSecrets clientSecrets = getClientSecrets();
		try {
			GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(HTTP_TRANSPORT,JSON_FACTORY,getClientSecrets().getWeb().getClientId(),getClientSecrets().getWeb().getClientSecret(),code,getClientSecrets().getWeb().getRedirectUris().get(0)).execute();
			return new GoogleCredential.Builder().setClientSecrets(getClientSecrets()).setTransport(HTTP_TRANSPORT).setJsonFactory(JSON_FACTORY).build().setAccessToken(tokenResponse.getAccessToken()).setRefreshToken(tokenResponse.getRefreshToken()).setExpirationTimeMilliseconds(tokenResponse.getExpiresInSeconds());
		} catch (IOException e) {
			
			System.out.println("Problem at getting Access token");
		}
		
		return null;
	}

	private  String getAuthorizationUrl() {
		
		scopes.add(AnalyticsScopes.ANALYTICS_READONLY);
		scopes.add("https://www.googleapis.com/auth/userinfo.email");		
		GoogleAuthorizationCodeRequestUrl url = new GoogleAuthorizationCodeRequestUrl(getClientSecrets().getWeb().getClientId(),getClientSecrets().getWeb().getRedirectUris().get(0), scopes).setAccessType("offline").setApprovalPrompt("force");
		return url.build();
	}
}
