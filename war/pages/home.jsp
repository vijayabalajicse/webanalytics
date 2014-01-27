<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Web Analytics</title>
   <script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<link href="/css/bootstrap.css" rel="stylesheet" type="text/css" />
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
   <div>
   <div id="emaildivi"><div><input type = "button" value ="" id = "email" /><input type="email" id="emailId"><input type="button" value="Send" id="sendEmail">
 <input type='button' value='' id='download' /></div></div>  
   <div id="formvalues">
  
   <form class="form-horizontal" role="form">
        <div class="form-group">
          <label  class="col-sm-4 control-label">Account</label>
          <div class="col-sm-6">
            <select class="form-control" id="accountName"></select>
          </div>
        </div>
        <div class="form-group">
          <label  class="col-sm-4 control-label">Webproperties</label>
          <div class="col-sm-6">
            <select class="form-control" id="propName"></select>
          </div>
        </div>
        <div class="form-group">
          <label  class="col-sm-4 control-label">Profile</label>
          <div class="col-sm-6">
            <select class="form-control" id="profName"></select>
          </div>
        </div>
         <div class="form-group">
          <label  class="col-sm-4 control-label">Table Id</label>
          <div class="col-sm-6">
            <input type="text" class="form-control" placeholder="Select the Profile Name" id="tabl" required>
          </div>
        </div>
         <div class="form-group">
          <label  class="col-sm-4 control-label">Dimension</label>
          <div class="col-sm-6">
            <input type="text" class="form-control" placeholder="Ex. ga:visitCount" id='dimension_val' required>  
          </div>
        </div>
         <div class="form-group">
          <label  class="col-sm-4 control-label">Metrics</label>
          <div class="col-sm-6">
            <input type="text" class="form-control"  placeholder="Ex. ga:visits" id='metric_value' required> 
          </div>
        </div>
         <div class="form-group">
          <label  class="col-sm-4 control-label">Segment</label>
          <div class="col-sm-6">
            <input type="text" class="form-control" id="segment" placeholder="Ex. dynamic::ga:country=~Pakistan" >
          </div>
        </div>
         <div class="form-group">
          <label  class="col-sm-4 control-label">Filter</label>
          <div class="col-sm-6">
            <input type="text" class="form-control" id="filter" placeholder="Ex. ga:city==Chennai" >
          </div>
        </div>
         <div class="form-group">
          <label  class="col-sm-4 control-label">Sort</label>
          <div class="col-sm-6">
            <input type="text" class="form-control" id="sort" placeholder="Ex. ga:visits" >
          </div>
        </div>
         <div class="form-group">
          <label  class="col-sm-4 control-label">Start Date</label>
          <div class="col-sm-6">
            <input type="text" class="form-control datepicker" id="startdate" placeholder="Click & Select Start Date" required>
          </div>
        </div>
         <div class="form-group">
          <label  class="col-sm-4 control-label">End Date</label>
          <div class="col-sm-6">
            <input type="text" class="form-control datepicker" id="enddate" placeholder="Click & Select End Date" required>
          </div>
        </div>
        <div class="form-group">
          <label  class="col-sm-4 control-label">MaxResults</label>
          <div class="col-sm-6">
            <input type="text" class="form-control" id="maxResults" value=100>
          </div>
        </div>    
        <div class="form-group">
          <div class="col-sm-offset-7 col-sm-10">
            <button type="button" class="btn btn-info" id="getdata">Get Data</button>
          </div>
        </div>
      </form>
 </div>
 </div>
     <!-- <div class="form-group">
       <button type="button" class="btn btn-info"  id='saveProfile'>Save Into Profile</button>
    </div> --> 
  <div id="showQuery">ShowQuery</div>  
 
   
  <br/>
  
<div id="tablechart" >
</div>




</body>
</html>