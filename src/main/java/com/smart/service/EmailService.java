package com.smart.service;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	

	static String username;
	static String password;

	public boolean sendEmail(String subject,String message,String to)
	{
		boolean flag=false;
		
		
		
		 Resource resource = new ClassPathResource("/application.properties");
		 Properties props = null; 
		 try { 
			 props = PropertiesLoaderUtils.loadProperties(resource);
		 
		 } catch (IOException e1) {
		  
		 e1.printStackTrace(); }
		 
		 username = props.getProperty("spring.mail.username"); 
		 password = props.getProperty("spring.mail.password");
		 
	    String from=username;
		
		System.out.println(username);
		System.out.println(password);
		
		String host="smtp.gmail.com";
		
		//get the system properties
		Properties properties = System.getProperties();
		System.out.println("PROPERTIES "+ properties);
		
		//setting important information to properties object
		
		//host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		


		//Step 1 : to get the session object
		Session session = Session.getInstance(properties,new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				
				return new PasswordAuthentication(EmailService.username,EmailService.password);
			}
			
		});
		
		session.setDebug(true);
		
		//Step 2 : 
		MimeMessage m = new MimeMessage(session);
		
		try
		{
			//from email
			m.setFrom(new InternetAddress(from, "Smart Contact Manager"));
			
			//adding recipient to message
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//adding subject to message
			m.setSubject(subject);
			
			//adding text to message
			m.setContent(message, "text/html");
			
			//send
			
			//Step 3 : send message using Transport class
			Transport.send(m);
			flag=true;
			
			System.out.println("Email Sent successfully ......");
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return flag;
	}

}
