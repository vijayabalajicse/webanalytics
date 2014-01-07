package com.spring.google;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.Analytics.Data.Ga.Get;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.Accounts;
import com.google.api.services.analytics.model.GaData;
import com.google.api.services.analytics.model.GaData.ColumnHeaders;
import com.google.api.services.analytics.model.GaData.ProfileInfo;
import com.google.api.services.analytics.model.GaData.Query;
import com.google.api.services.analytics.model.Profiles;
import com.google.api.services.analytics.model.Webproperties;
import com.google.api.services.tasks.TasksScopes;




@Controller
@RequestMapping("/")
public class SpringDemoServlet  {
	 static final String CALLBACK_URL = "http://analytics-demo-work.appspot.com/oauth2callback";
	 static final JsonFactory JSON_FACTORY = new  JacksonFactory();
	 static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	 static GoogleAuthorizationCodeFlow flow;
	 static GoogleClientSecrets clientSecrets;	
	 static String stateToken;	
	 static String AccountName,WebPropertiesName,ViewName,rowsValue,tableId;	
	 Analytics analytics;
	 GaData gaData;
	 String location;
	static Credential credential;
	static String accountInfo;
	static Logger log = Logger.getLogger(SpringDemoServlet.class.getName());
@RequestMapping("/oauth2callback")
public String callBack(HttpServletRequest req, HttpServletResponse resp,ModelMap model) throws IOException
{
	  	System.out.println("callback started");
		if(req.getParameter("code")==null )
			{
			log.info("No code parameter in request");
			resp.sendRedirect(buildLoginUrl());
			
			}
		else if(req.getParameter("code")!= null)
		    {
			log.info("code in request ");
			System.out.println(req.getParameter("code"));
			System.out.println(req.getParameter("state"));		
			GoogleTokenResponse response = flow.newTokenRequest(req.getParameter("code")).setRedirectUri(CALLBACK_URL).execute();	
			System.out.println(response.getAccessToken());
			System.out.println(response.getIdToken());
			System.out.println(response.getRefreshToken());
			System.out.println(response.getTokenType());
			System.out.println(response.toPrettyString());
			credential = flow.createAndStoreCredential(response, null);
			//credential = new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT).setJsonFactory(JSON_FACTORY).setClientSecrets(getClientSecrets()).build().setFromTokenResponse(response);
		    log.info("response stored in credential");			
			analytics =new Analytics.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Sample google Analytics ").build(); 
				
			String profileId = getProfileId(analytics);
//				if(profileId != null)
//				{
//					gaData = getResults(analytics,profileId);					
//					printProfileinfo(gaData);
//				}			
			
			location = "redirect:/home";
		   }
		
        return location;
}

//private void printProfileinfo(GaData gaData) {
//	ProfileInfo profileInfo = gaData.getProfileInfo();		
//	tableId = profileInfo.getTableId();
//}


//private GaData getResults(Analytics analytics, String profileId) throws IOException {
//	
//	return analytics.data().ga().get("ga:"+profileId, "2013-11-11", "2013-12-25", "ga:visits,ga:newVisits").setDimensions("ga:source,ga:keyword").setSort("-ga:visits").setFilters("ga:medium==organic").setMaxResults(25).execute();
//}

@RequestMapping("/home")
public  String home( ModelMap model){
	log.info("Return to Home Page");
	System.out.println(AccountName);
	System.out.println(WebPropertiesName);
	System.out.println(tableId);
	model.addAttribute("accountName", AccountName);
	model.addAttribute("webPropertiesName", WebPropertiesName);
	model.addAttribute("viewName", ViewName);	
	model.addAttribute("tableId", tableId);	
	return "home";
}


@RequestMapping("/getAccountInfo")
public @ResponseBody String getAccountInfo(){
	log.info("return the acccountinfo");
 return accountInfo;	
}

@RequestMapping("/getdataAjax")
public @ResponseBody String getData(@RequestBody String data) {
	
	System.err.println(data);	
	//String table_id,metrics,dimension,startDate,endDate;
	log.info("Parsing the data from clent");
	JSONObject jsonObj = new JSONObject();
	JSONParser parser = new JSONParser();
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
	data_result = getResultsData(table_id,metrics,dimension,startDate,endDate);
	System.out.println(data_result);
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	return data_result;
	
}

private String getResultsData(String table_id, String metrics,String dimension, String startDate, String endDate) 
	{
	
	log.info("Get the result for analytics");
		String value = null;
		try {
		value = analytics.data().ga().get(table_id, startDate, endDate, metrics).setDimensions(dimension).setOutput("dataTable").setMaxResults(50).execute().toPrettyString();
		} catch (IOException e) {
		
		e.printStackTrace();
		}
	return value;
	}






private String getProfileId(Analytics analytics)  
{
	log.info("Get profile info");
	String profileId = null;
	String accountName ="",webProp = "",profileName ="",profId="",accountId="",propId="";
	try
	{
		Accounts account = analytics.management().accounts().list().execute();
		if(account.getItems().isEmpty()){
		System.out.println("Sorry no Account founded");
		}
		else{
			accountName += "\"AccountName\" : [";
			accountId += "\"AccountId\" : [";
			for(int i=0; i < account.getItems().size();i++){
				System.out.println(account.getItems().get(i).getName());
				AccountName = account.getItems().get(i).getName();
				accountName +="\"" + account.getItems().get(i).getName() +"\",";
				accountId += "\"" + account.getItems().get(i).getId() +"\",";
				String firstAccountId = account.getItems().get(i).getId();
				
				Webproperties webProperties = analytics.management().webproperties().list(firstAccountId).execute();
				if(webProperties.getItems().isEmpty())
				{
			    	System.out.println("No webproperties found for this account");
				}
				else{
					propId  += "\""+account.getItems().get(i).getId()+"\" :[";
					webProp += "\""+account.getItems().get(i).getId()+"_wname\" :[";
					System.out.println(webProperties.getItems().size());
					for(int j=0;j < webProperties.getItems().size(); j++){
						
						String firstWebProperties = webProperties.getItems().get(j).getId();
						propId += "\"" + webProperties.getItems().get(j).getId() +"\","; 
						webProp += "\"" + webProperties.getItems().get(j).getName() +"\",";
						System.out.println(webProperties.getItems().toString());
						Profiles profile = analytics.management().profiles().list(firstAccountId, firstWebProperties).execute();
						if(profile.getItems().isEmpty()){
				        	System.out.println("No profile(view) found");
				        	
				        }
						else{
							
							profileName += "\""+webProperties.getItems().get(j).getId()+"_pname\" :[";
							profId += "\""+webProperties.getItems().get(j).getId()+"\" :[";
							System.out.println(profile.getItems().size());
							for(int k=0;k < profile.getItems().size();k++){
								profileId = profile.getItems().get(k).getId();
								profId  += "\"" + profile.getItems().get(k).getId() +"\",";
								profileName += "\"" + profile.getItems().get(k).getName() +"\",";
								System.out.println(profile.getItems().get(k).getName()); 
							}
							profId =profId.substring(0, profId.length()-1);
							profileName = profileName.substring(0, profileName.length()-1);
							profileName += "],";
							profId  += "],";
						}
					}
					webProp = webProp.substring(0, webProp.length()-1);
					propId = propId.substring(0, propId.length()-1);
					webProp += "],";
					propId  += "],";
				}
				
			}
			accountName = accountName.substring(0, accountName.length()-1);
			accountId = accountId.substring(0, accountId.length()-1);
			accountName  += "],";
			accountId += "],";
	accountInfo = accountName + webProp + profileName+accountId+propId +profId;
	accountInfo ="{"+ accountInfo.substring(0, accountInfo.length()-1)+"}";
	
	}
	
	}catch(Exception e){
		System.err.println(e.getStackTrace());
	}
	
	return profileId;
}

static void initialFlow()  
	{
	log.info("Setting the scopes");
	 HashSet<String> scopes = new HashSet<String>();
	
	 scopes.add(AnalyticsScopes.ANALYTICS_READONLY);
	flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, getClientSecrets(), scopes).setAccessType("offline").build();

	
	}

 public static String buildLoginUrl() throws IOException
 {
	 log.info("create laogin url");
	 initialFlow();
	 final GoogleAuthorizationCodeRequestUrl url =flow.newAuthorizationUrl();
	 return url.setRedirectUri(CALLBACK_URL).build();	
 }
private static GoogleClientSecrets getClientSecrets() 
{
	 
	if(clientSecrets == null)
	{
		try {
			clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(SpringDemoServlet.class.getResourceAsStream("client_secrets.json")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	return clientSecrets;
 }

}
