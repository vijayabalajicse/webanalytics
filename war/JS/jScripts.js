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
      
     
  google.load("visualization", "1", {packages:["corechart","table"]}); 

   
  $(function()
		    { 
                $.ajax({
                	type:"GET",
                	url:"/getAccountInfo",
                	contentType: "application/json"
                	
                })
                 .done(function(msg){
                	 var json = jQuery.parseJSON(msg);
                	 
                	 console.log(json);
                	 var accountName = [];
                	 var accountId = [];
                	 var profileId= [];
                	 var propName;
                	 var profName;
                	 var loc;
                	 accountName = json.AccountName;   
                     accountId = json.AccountId;
              // script load data start  
                     
                     $('#accountName').empty();  
                     $('#propName').empty();       
                     $('#tabl').empty();                                                                                                                                                                                                                                                                                                                                                                                                     
                     for(var index in accountName){
                       $('#accountName').append("<option value='"+index+"'>"+accountName[index]+"</option>");                  
                     }
                                                                                                                                                                                                                                                                                                                                                                                                                  
                                                                                                                                                                                                                                                                                                                                                                                                                  
                   $('#accountName').click(function(){
                     propName = accountId[$('#accountName').val()]+"_wname";
                     console.log($('#accountName').val());      
                     console.log(json[propName]);  
                     $('#propName').empty();     
                     $('#tabl').empty();                                                                                                                                                                                                                                                                                                                                                             
                       for(var index in json[propName]){
                         $('#propName').append("<option value='"+index+"'>"+json[propName][index]+"</option>");                                                                           
                       }
                    });                                                                                                                                                                                                                                                                                                                                                                                                     
                                                                                                                                                                                                                                                                                                                                                                                                                  
                 $('#propName').click(function(){
                   loc =accountId[$('#accountName').val()];
                   console.log($('#propName').val());
                   console.log(json[loc]);
                   var proploc =json[loc][$('#propName').val()]+"_pname";                                                                                                                                                                                                                                                                                                                                                                                                       
                   $('#profName').empty();
                   $('#tabl').empty();                                                                                                                                                                                                                                                                                                                                                                                                       
                     for( var index in json[proploc]){
                       $('#profName').append("<option value='"+index+"'>"+json[proploc][index]+"</option>");                                                                                                                                                                                                                                                                                                                                                                                                     
                     }
                 });
                 $('#profName').click(function(){
                   var profName = accountId[$('#accountName').val()];                                                                                                                                                                                                                                                                                                                                                                                                       
                   //var tabl = json[profName];
                   var profLocation = json[profName][$('#propName').val()];   
                   console.log(json[profLocation][$('#profName').val()]);                                                                                                                                                                                                                                                                                                                                                                                                       
                   $('#tabl').empty();                                                                                                                                                                                                                                                                                                                                                                                                       
                   $('#tabl').val("ga:"+ json[profLocation][$('#profName').val()]);
                 });                                                                                                                                                                                                                                                                                                                                                                                                       
                     
                     
              
              // script load data end  	 
                	 
                	 
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
