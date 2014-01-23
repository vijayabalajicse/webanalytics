/**
 * 
 */


    var dataType =[];
    var columnType = [];
    var columnName = [];
    var rowsValue = new Array();
    var tablestr = '';
    var accountName = [];
    var accountId = [];
    var profileId= [];
    var propName;
    var profName;
    var loc;
    var chartData=[];
    var tbl_id,endDate,startDate;
	var dimension;
	var metrics;
	var data;
	var queryString;
	var filter;
	var segment;
	var sort;
      
   // load the Google Visualization api for chart  
  google.load("visualization", "1", {packages:["corechart","table"]}); 

   
  $(function()
		    { 
	  
	  $('#emailId').hide();
	  $('#sendEmail').hide(); 
	  
	  /** Sending the Email
	   * Param QueryString (EmailId added)
	   * retrun success message
	   *  */		  
	  //email start point
	  $('#email').click(function(){
		  $('#emailId').show();
		  $('#sendEmail').show();
		  
	  });
	  
	  $('#sendEmail').click(function(){
		  if (/^\w+([\.-]?\w+)@\w+([\.-]?\w+)(\.\w{2,3})+$/.test($('#emailId').val()))
		  {
			  console.log("Email ready to send");
			  console.log(queryString);
			  if(queryString != null){
				  queryString.emailId = $('#emailId').val();
				  console.log(queryString);
				  console.log(typeof queryString)
				  $.ajax({
					  type: "POST",
					  url: "/emailRequest",
					  contentType: "application/json",
					  dataType: "json",
					  data: JSON.stringify(queryString)
					  	  
				  })
				  .done(function(msg){
					  console.log(msg.status);
					  alert(msg.status);
					  $('#emailId').hide();
					  $('#sendEmail').hide();
					  
				  });
			  }
			  else{
				  alert("Attachment Data is empty");
			  }
		  }
		  else{
		    alert("e-mail address not valid format");  
		    }
		  
			  
		  
		  
		  
	  });
	  
	  
	  //email end point
	  
	  
/** Ajax for GetAccount Infomation on PageLoad
Start Ajax for GetAccount Info on PageLoad  */
                $.ajax({
                	type:"GET",
                	url:"/getAccountInfo",
                	contentType: "application/json"
                	
                })
                 .done(function(msg){
                	 console.log("Msessage"+msg);
                	 console.log(typeof msg);
                	 if(!msg){
                		 $('#accountName').append("<option value='0'>No account found</option>"); 
                	 }
                	 var json = jQuery.parseJSON(msg);                	 
                		 console.log(json);
                		 var accountName = [];
                    	 
                    	 accountName = json   
                        console.log(accountName);
                   
                         
                         $('#accountName').empty();  
                         $('#propName').empty();       
                         
                         $('#tabl').empty();   
                        var account_opt = " <option value=0>Select AccountName</option>";
                         for(var index in accountName){
                        	 
                        	 account_opt += "<option value='"+index+"'>"+accountName[index]+"</option>";                  
                         }
                         $('#accountName').append( account_opt);  
                 }); 
 /** End Ajax for GetAccount Info on PageLoad   */
                
 /** 
  * Get the Properties detail of coressponding Account
 Start       */         
                $('#accountName').change(function(){
                	console.log("opertion property"+$('#accountName option:selected').val());
                	var acc_data = $('#accountName option:selected').val();     
                	var propeties =[];
                	$('#propName').empty();  
                	console.log(acc_data);
                	$.ajax({
                		type:"POST",
                		url:"/getPropertyInfo",                  		
                		data: acc_data
                		
                	})
                	.done(function(msg){
                		console.log(msg);
                		var json_prop = jQuery.parseJSON(msg);
                		console.log(json_prop);
                		properties = json_prop; 
                		var prop_opt = "<option value=0>Select Properties</option>";
                		for( var index in properties){
                			prop_opt +="<option value='"+index+"'>" + properties[index] + "</option>";
                			console.log(index);
                		}
                		$('#propName').append(prop_opt);
                	});
                });
                
                // account change end
    /**
     * Getting the profile Infomation from Servlet
     * Sending the WebProperties Information
     */            
                //properties change event
                $('#propName').change(function(){
                	console.log("opertion property"+$('#accountName option:selected').val());
                	console.log("opertion property"+$('#propName option:selected').val());
                	var properties_data = {AccountId:$('#accountName option:selected').val(),PropertiesID:$('#propName option:selected').val()};               	
                	properties_data = JSON.stringify(properties_data);
                	console.log(properties_data);
                	$("#profName").empty();
                	var profileInfo =[];
                	$.ajax({
                		type:"POST",
                		url:"/getProfileInfo",   
                		dataType: "json",
                		contentType : "application/json",	
                		data: properties_data
                		
                	})
                	.done(function(msg){
                		console.log(msg);
                		var json_prof = msg;
                		console.log(json_prof);
                		profileInfo = json_prof;
                		var prof_opt= "<option value=0>Select ProfileName</option>";
                		for( var index in  profileInfo){
                			prof_opt += "<option value='" + index + "'>" + profileInfo[index] + "</option>";
                		}
                		$("#profName").append(prof_opt);
                		//$('#tabl').val("ga:"+$('#profName').val());
                		
                	});
                });
                
                //end properties changes
                
       /**
        * Getting the ProfileId Information
        */         
                //start to get profile Id
                $('#profName').change(function(){
                	$('#tabl').val("ga:"+$('#profName option:selected').val());
                });
	  
	            $( ".datepicker" ).datepicker({ dateFormat: 'yy-mm-dd',maxDate: '+0D' });
	            
        /**
         * Get the G Data from Servelt
         * Complusary Param tableid,metrics,startdate,enddate
         * retrun the GData Result
         */
				$('#getdata').click(function()
				{  
					tbl_id = $('#tabl').val();
					dimension = $('#dimension_val').val();
					metrics = $('#metric_value').val();
					segment = $('#segment').val();
					filter =  $('#filter').val();
					sort = $('#sort').val();
					startDate = $('#startdate').val();
					endDate = $('#enddate').val();
					console.log(metrics.lastIndexOf(','));
					console.log(metrics.length-1);
					if(metrics.lastIndexOf(',')==metrics.length-1){
						metrics = metrics.substring(0,metrics.length-1);
						console.log(metrics);
					}
					if(dimension.lastIndexOf(',')==dimension.length-1){
						dimension = dimension.substring(0,dimension.length-1);
						console.log(dimension);
					}
					queryString = new Object();
					queryString.tableid = tbl_id;
					queryString.metrics = metrics;
					queryString.dimension = dimension;
					queryString.startDate = startDate;
					queryString.endDate = endDate;
					queryString.filter = filter;
					queryString.segment = segment;
					queryString.sort = sort;
						
					//	'{"tableid":"'+tbl_id + '","metrics":"' + metrics + '","dimension":"' + dimension + '","startDate":"' + startDate + '","endDate":"' + endDate
					//+ '","filter":"'+filter+'","segment":"'+segment+'","sort":"'+sort+'"}' ;
					console.log(queryString);
					
					
					
					if($('#startdate').val() == '' || $('#tabl').val() == ''  || $('#metric_value').val() == '' || $('#enddate').val() == '' ){
					   
						alert("Please Enter All the value");
						
					}else{
					      //if()
						console.log(dimension.substring(0,3));
						if(dimension.substring(0,3) =="ga:" || metrics.substring(0,3) =="ga:"  || filter.substring(0,3) =="ga:"  || sort.substring(0,3) =="ga:" )
							{
							//ajax started
							$.ajax(
						    		  {
						       			 type : 'POST',
						        		 url : '/getdataAjax',
						        		 dataType : 'json',
						        		 contentType: "application/json",
						       		     data : JSON.stringify(queryString)
						       		  
						     		  })
									.done(function (msg) 
						    		  { 
										console.log(msg);
										
//										try{											
										console.log(JSON.stringify(msg));
										var outputvalue = msg;
										console.log(outputvalue);
										console.log(outputvalue.message);
										console.log(outputvalue.cols.length);
										columnName = [];
										for(var i =0; i < outputvalue.cols.length; i++ )
								         {
								            dataType[i] = outputvalue.cols[i].type;							            
								             columnName[i] = outputvalue.cols[i].id;
								           
								         }    
								         rowsValue = [];
								         console.log("Row value: " + outputvalue.rows);
								         if(outputvalue.rows == "undefined"){
								        	$('#tablechart').html("No record found for this report");
								        	console.log("undefined");
								         }else if(outputvalue.rows == null){
								        	 $('#tablechart').html("No record found for this report");
								        	 console.log("null");
								         }
								         for(var i = 0; i < outputvalue.rows.length ; i++)
								         {
								           
								            rowsValue[i] = new Array();
								           for( var j=0; j < outputvalue.cols.length ; j++)
								           {
								            
								             if(dataType[j] == 'string')
								             {
								             rowsValue[i][j] =outputvalue.rows[i].c[j].v;
								             }
								             else if(dataType[j] == 'number')
								             {
								             rowsValue[i][j] = eval( outputvalue.rows[i].c[j].v );  
								             }  
								           }
								          
								           
								        }
								        
										console.log("column count: "+ columnName.length);
										console.log("Row count: " + rowsValue.length );
										console.log("column : "+ columnName);
										console.log("Row : " + rowsValue );
										console.log("Row count: " + chartData.length );
										chartData.length = 0 ;
										console.log("chartdata count: " + chartData );
										chartData = [ columnName ];
										for( var index in rowsValue ) 
										{
											chartData.push( rowsValue[index] );
										}
										console.log(rowsValue.length)
										console.log(chartData);
								        console.log( chartData.length ); 
								        display(chartData);

										
										
						      			$('#output').html(msg);
										
										
//										if(typeof outputvalue == 'object'){
//											console.log("message type"+typeof outputvalue);
//										}
//										else{
//											if(outputvalue === false){
//											console.log(typeof outputvalue);
//											console.log(msg);
//											}
//										}
										
//										}
//										catch(e){
//							    			  var abc;
//							    			    console.log(typeof msg)
//							    			    abc=JSON.stringify(msg);
//												console.log(abc);
//												abc=('"'+abc.substring(abc.indexOf('{')));//.replace('/\n', "");
//												//abc=('"'+abc.substring(abc.indexOf('{'))).replace('/\n', "");
//												console.log(typeof(abc));
//												console.log(abc);
//												console.log("message: "+JSON.parse(abc));
//												var jsonObjectvalue = new Object();
//												var tempvar = jQuery.parseJSON(abc);
//												var temp1 = tempvar.substring(0,tempvar.length-1);
//												console.log(tempvar);
//												console.log(typeof tempvar);
//												jsonObjectvalue = JSON.parse(tempvar);
//												console.log(jsonObjectvalue);
//												console.log(typeof jsonObjectvalue)
//												console.log(jsonObjectvalue.code);
//												 $('#tablechart').html("Error:  "+jsonObjectvalue.message);
//							    			  
//							    		  }
						      		  })
						      		  .fail(function (msg){
						      			  var jsonmsg;
						      			  console.log(msg);
						      			  
						      			  console.log(msg.responseText);
						      			 if(msg.responseText.substring(0,3) == '400'){
						      				console.log(typeof msg.responseText);
							      			  jsonmsg=JSON.stringify(msg.responseText);
							      			  console.log(jsonmsg);
							      			  jsonmsg=('"'+jsonmsg.substring(jsonmsg.indexOf('{')));							      			
							      			  console.log(jsonmsg + typeof jsonmsg);
							      			  var tempmsg = JSON.parse(jsonmsg);
							      			  console.log(tempmsg + typeof tempmsg);
							      			  var errormsg = JSON.parse(tempmsg);
							      			  console.log(errormsg);
							      			  $('#tablechart').html("Error:  "+errormsg.message);
						      			 }
						      			 else{
						      				$('#tablechart').html("Error:  "+msg.responseText);
						      			 }
						      			  
						      			  
								
							});
						    		  
							}else{
								alert("The field start with ga:")
							}
						
						
						//end ajax
					}
					
					
		      
				});
				
	/**
	 * Downaload the CSV File Data
	 * Using JsonInformation from Servlet
	 */			
					$('#download').click(function(){
						var data = chartData;
						if (data == '') {
							alert("No data found");
						}else{
							console.log("no data");
							csvDownload(data,"Report");
						}
					});
				
		    }); 
		      
   //function start for csv download
   
   /**
    * Creating the CSV File And Download Option
    */
function csvDownload(data,ReportTitle){

	var jsonData = typeof data != 'object' ? JSON.parse(data) : data ;
	var CSV = '';
	CSV += ReportTitle + '\r\n\n';	
	for(var i=0; i < jsonData.length ; i++ ){
		row = "";
		for(var index in jsonData[i]){
			row += '"'+ jsonData[i][index] + '",';
		}
		row.slice(0,row.length -1);
		CSV += row + '\r\n';
		
	}
	if(CSV == ''){
		alert('data invalid');

	}
	var fileName = ReportTitle;
	var uri = 'data:text/csv;charset=utf-8,' + escape(CSV);;
	var link = document.createElement('a');
	link.href = uri;
	link.style = "visibility:hidden";
	link.download = fileName + ".csv";
	document.body.appendChild(link);
	link.click();
	document.body.removeChild(link);
}
   
/**
 * Displaying the Table CharDAta and Area Chat
 * @param chartData
 */
  //function end for csv
  function display(chartData)
  {
		
     
       google.setOnLoadCallback(drawChart(chartData));
    
    		function drawChart(chartData) 
    		{   
//      		console.log("displaydata:"+chartData);      		
//      		console.log(chartData.length);
//      		console.log(data);      		
      		data = google.visualization.arrayToDataTable(chartData);
//      		console.log(typeof data);
     			 var options = {
        			title: 'Sample Report',
        			hAxis: {title: 'Dimension', titleTextStyle: {color: 'red'}}
     				 };

      		var chart = new google.visualization.AreaChart(document.getElementById('chartdraw'));
      		chart.draw(data, options);
      		var tablechart = new google.visualization.Table(document.getElementById('tablechart'));
      		tablechart.draw(data, null);
//      		console.log(data);
//      		data = {};
//      		chartData.length = 0;
//      		console.log(data);
//      		console.log(chartData);
         };
//         data = {};
//   		chartData.length = 0;
//   		console.log(data);
//   		console.log(chartData);
  };
