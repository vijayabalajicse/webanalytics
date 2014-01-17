package com.google.webAnalytics;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller

public class EmailSendUser {
	@RequestMapping("/sendemail")
	public static void emailSending(@RequestBody String emailid,@RequestBody String csvdata,HttpServletRequest req){
		
		String emailId;
		String csvData;
		
		emailId= req.getParameter("emailId");
		csvData = req.getParameter("csvdata");
		System.out.println(emailId);
		System.out.println("data"+csvData);
		Properties properties = new Properties();
		 Multipart multipart = new MimeMultipart();
		 MimeBodyPart htmlBodyPart = new MimeBodyPart();
		 MimeBodyPart attachment = new MimeBodyPart();
		 DataSource sourcedata;
		Session session = Session.getDefaultInstance(properties);
		byte[] attachmentData = csvData.getBytes();
		Message message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress("vijaya.balaji@a-cti.com","Vijayabalaji"));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailId,"Mr.User"));
			message.setSubject("Analytics Account Gadata");
			message.setText("Google Analytics data in the Body");
			htmlBodyPart.setContent(message,"text/html"); 
			multipart.addBodyPart(htmlBodyPart);
			attachment.setFileName("user.csv");
			sourcedata = new ByteArrayDataSource(attachmentData,"text/comma-separated-values");
			attachment.setDataHandler(new DataHandler(sourcedata));
			multipart.addBodyPart(attachment);
			message.setContent(multipart);
			Transport.send(message);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		   
	}

}
