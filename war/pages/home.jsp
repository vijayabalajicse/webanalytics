<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Web Analytics</title>
<link href="http://getbootstrap.com/2.3.2/assets/css/bootstrap.css" rel="stylesheet" type="text/css" />
<link href="http://getbootstrap.com/2.3.2/assets/css/bootstrap-responsive.css" rel="stylesheet" type="text/css" />
<script src="http://getbootstrap.com/2.3.2/assets/js/bootstrap.js"></script>
<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css">
  <script src="http://code.jquery.com/jquery-1.9.1.js"></script>
  <script src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"></script>
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
  <span onload= "_gaq.push(['_trackEvent', 'UserLoged', 'UserEmailid', this.value]);"> <%= session.getAttribute("SESSION_USEREMAILID") %> </span><a href="/logout">Logout</a>
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
    <input type="text" placeholder="Select the Dimesion Name" id='dimension_val' required>     
    </div><br/>
    <div class="form-group">
     <label class="control-label">Metrics &nbsp&nbsp </label>
    <input type="text" placeholder="Select the Dimesion Name" id='metric_value' required>      
    </div>
    <br/>
    <div class="form-group">
      <label class="control-label" >Segment &nbsp&nbsp </label>
    <input type="text" id="segment" required>
    </div>
    <br/>
    <div class="form-group">
      <label class="control-label" >Filter &nbsp&nbsp </label>
    <input type="text" id="filter" required>
    </div>
    <br/>
    <div class="form-group">
      <label class="control-label" >Sort &nbsp&nbsp </label>
    <input type="text" id="sort" required>
    </div>
    <br/>
    <br/>
    <div class="form-group">
      <label class="control-label" >StartDate &nbsp&nbsp </label>
    <input type="text"  class="datepicker" id="startdate" required>
    </div>
    <br/>
    <div class="form-group">
      <label class="control-label" >EndDate &nbsp&nbsp </label>
    <input type="text"  class="datepicker" id="enddate" required>
    </div>
    <br/>
    
     <div class="form-group">
       <button type="button" class="btn btn-info"  id='getdata'>Get Data</button>
    </div>
    
  </div>  
  <input type = "button" value ="Email" id = "email" />
  <input type='button' value='Download' id='download' />
<div id="tablechart" style="width:800px;height:500px;">
</div>

<div id="chartdraw" style="width:800px;height:500px;">
</div>


</body>
</html>