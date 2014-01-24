<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Web Analytics</title>
   <script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<link href="http://getbootstrap.com/2.3.2/assets/css/bootstrap.css" rel="stylesheet" type="text/css" />
<link href="http://getbootstrap.com/2.3.2/assets/css/bootstrap-responsive.css" rel="stylesheet" type="text/css" />
<script src="/JS/bootstrap.js"></script>
<link rel="stylesheet" href="/css/jquery-ui.css">

  <script src="/JS/jquery-ui.js"></script>
  <script type="text/javascript" src="https://www.google.com/jsapi"></script>
  <script type="text/javascript" src="/JS/jScripts.js"></script>
  <script type="text/javascript" src="/JS/util.js"></script>
  <script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-47321332-1', 'analytics-demo-work.appspot.com');
  ga('send', 'pageview');

</script>
  <link rel="stylesheet" href="/css/style.css">
  
</head>
<body>
   <div id="Header">
   You are Authorized as 
  <span id="emailInfo" onload= "_gaq.push(['_trackEvent', 'UserLoged', 'UserEmailid', this.value]);"> <%= session.getAttribute("SESSION_USEREMAILID") %> </span><a href="/logout">Logout</a>
   </div>
   
    <div class="form-horizontal">
    <div class="form-group">
      <label class="control-label">Account Name &nbsp &nbsp </label>
      
    <select class="form-control" id="accountName">
   
    </select>
    </div><br/>
     <div class="form-group">
      <label class="control-label" >Webproperties Name &nbsp &nbsp </label>
    <select class="form-control" id="propName"></select>
    
    </div>
    <br/>
    <div class="form-group">
      <label class="control-label">Profile Name &nbsp &nbsp </label>
    <select class="form-control" id="profName"></select>
    </div>
    <br/><br/>
    <div class="form-group">
      <label class="control-label" >Table Id &nbsp&nbsp </label>
    <input type="text" placeholder="Select the Profile Name" id="tabl" required>
    </div>
    <br/>
      <div class="form-group">
      <label class="control-label">Dimension &nbsp&nbsp </label>
    <input type="text" placeholder="Ex. ga:visitCount" id='dimension_val' required>     
    </div><br/>
    <div class="form-group">
     <label class="control-label">Metrics &nbsp&nbsp </label>
    <input type="text" placeholder="Ex. ga:visits" id='metric_value' required>      
    </div>
    <br/>
    <div class="form-group">
      <label class="control-label" >Segment &nbsp&nbsp </label>
    <input type="text" id="segment" placeholder="Ex. dynamic::ga:country=~Pakistan" required>
    </div>
    <br/>
    <div class="form-group">
      <label class="control-label" >Filter &nbsp&nbsp </label>
    <input type="text" id="filter" placeholder="Ex. ga:city==Chennai" required>
    </div>
    <br/>
    <div class="form-group">
      <label class="control-label" >Sort &nbsp&nbsp </label>
    <input type="text" id="sort" placeholder="Ex. ga:visits" required>
    </div>
    <br/>
    <br/>
    <div class="form-group">
      <label class="control-label" >StartDate &nbsp&nbsp </label>
    <input type="text"  class="datepicker" id="startdate" placeholder="Click & Select Start Date" required>
    </div>
    <br/>
    <div class="form-group">
      <label class="control-label" >EndDate &nbsp&nbsp </label>
    <input type="text"  class="datepicker" id="enddate" placeholder="Click & Select End Date" required>
    </div>
    <br/>
    <div class="form-group">
      <label class="control-label" >MaxResults </label>
    <input type="text" id="maxResults" value=100 required>
    </div>
    <br/>
    
    
     <div class="form-group">
       <button type="button" class="btn btn-info"  id='getdata'>Get Data</button>
    </div><br/>
    <div class="form-group">
       <button type="button" class="btn btn-info"  id='saveProfile'>Save Into Profile</button>
    </div>
    
  </div> 
   
  <br/>
  <input type = "button" value ="Email" id = "email" /><input type="email" id="emailId"><input type="button" value="Send" id="sendEmail"><br/>
 <br/> <input type='button' value='Download' id='download' />
<div id="tablechart" style="width:100%;height:500px;">
</div>




</body>
</html>