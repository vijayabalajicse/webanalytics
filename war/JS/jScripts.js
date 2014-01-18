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
	var dimen_val =[];
	var metrics = [];
	var data;
      
   // load the Google Visualization api for chart  
  google.load("visualization", "1", {packages:["corechart","table"]}); 

   
  $(function()
		    { 
/** Email the data  */	  
	  
	  //email start point
	  $('#email').click(function(){
		  console.log("Email ready to send");
		  console.log(chartData);
		  $.ajax({
			  type: "POST",
			  url: "/emailRequest",
			  contentType: "application/json",
			  dataType: "json",
			  data: chartData
			  	  
		  })
		  .done(function(msg){
			  console.log(msg);
		  });
		  
	  });
	  
	  //email end point
	  
	  
/** Ajax for GetAccount Info on PageLoad
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
 /** End Ajax for GetAccount Info on PageLoad  
                
 /** Get the Properties detail of coressponding Account
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
                
                //start to get profile Id
                $('#profName').change(function(){
                	$('#tabl').val("ga:"+$('#profName option:selected').val());
                });
	  
	            $( ".datepicker" ).datepicker({ dateFormat: 'yy-mm-dd',maxDate: '+0D' });
	            
	            $("#metrics").focusout(function(){
	            	$('#metric_value').val('');
	            	metrics.length = 0;
	            	
	            	$("#metrics :selected").each(function(i,selected)
	  		    		  {
	  		     			 metrics[i] = $(selected).text();     
	  		               });
	  		      
	  						console.log(metrics); 
	  						if(metrics.length > 7)
    						{
    							alert("Max 7 Selection");
    						}
    						else
    						{
    							$('#metric_value').val(metrics);
    						}
	  						
	  		      
	  					
	            	
	            });
	           
	            $("#dimension").focusout(function(){
	            	$('#dimension_val').val('');
	            	dimen_val = [];
	            	$("#dimension :selected").each(function(i,selected)
	    		    		{
	    		      			dimen_val[i] = $(selected).text();     
	    		            });
	    		      
	    						console.log(dimen_val); 
	    		      
	    						if(dimen_val.length > 5)
	    						{
	    							alert("Max 7 Selection");
	    						}
	    						else
	    						{
	    							$('#dimension_val').val(dimen_val);
	    						}
	            	
	            });
	              
				$('#getdata').click(function()
				{   
					
					tbl_id = $('#tabl').val();
					startDate = $('#startdate').val();
					endDate = $('#enddate').val();
					var str =  '{"tableid":"'+tbl_id + '","metrics":"' + metrics + '","dimension":"' + dimen_val + '","startDate":"' + startDate + '","endDate":"' + endDate + '"}' ;
					console.log(str);
					
					if($('#startdate').val() == '' || $('#tabl').val() == ''  || $('#metric_value').val() == '' || $('#dimension_val').val() == '' || $('#enddate').val() == '' ){
					   
						alert("Please Enter All the value");
						
					}else{
					
						$.ajax(
					    		  {
					       			 type : 'POST',
					        		 url : '/getdataAjax',
					        		 
					        		 contentType: "application/json",
					       		     data : str
					       		  
					     		  })
								.done(function (msg) 
					    		  { 
									var outputvalue = jQuery.parseJSON(msg);
									console.log(outputvalue);
									console.log(outputvalue.columnHeaders.length);
									columnName = [];
									for(var i =0; i < outputvalue.columnHeaders.length; i++ )
							         {
							            dataType[i] = outputvalue.columnHeaders[i].dataType;
							            columnType[i] = outputvalue.columnHeaders[i].columnType;
							             columnName[i] = outputvalue.columnHeaders[i].name;
							           
							         }       
							         rowsValue = [];
							         console.log("Row value: " + outputvalue.dataTable.rows);
							         if(outputvalue.dataTable.rows == "undefined"){
							        	$('#tablechart').html("No record found for this report");
							        	console.log("undefined");
							         }else if(outputvalue.dataTable.rows == null){
							        	 $('#tablechart').html("No record found for this report");
							        	 console.log("null");
							         }
							         for(var i = 0; i < outputvalue.dataTable.rows.length ; i++)
							         {
							           
							            rowsValue[i] = new Array();
							           for( var j=0; j < outputvalue.columnHeaders.length ; j++)
							           {
							            
							             if(dataType[j] == 'STRING')
							             {
							             rowsValue[i][j] =outputvalue.dataTable.rows[i].c[j].v;
							             }
							             else if(dataType[j] == 'INTEGER')
							             {
							             rowsValue[i][j] = eval( outputvalue.dataTable.rows[i].c[j].v );  
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
					      
					      		  });
					}
					
					
		      
				});
				
				
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
