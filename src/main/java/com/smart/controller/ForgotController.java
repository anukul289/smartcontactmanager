package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	Random random =new Random();
	

	//email id form open handler
	@GetMapping("/forgot")
	public String openEmailForm(Model m)
	{
		m.addAttribute("title","Forgot Password");
		return "forgot_email_form";
	}
	
	//send otp
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,Model m,HttpSession session)
	{
		m.addAttribute("title","Verify OTP");
		System.out.println("EMAIL "+email);
		
		User user = this.userRepository.getUserByUserName(email);
		
		if(user==null)
		{
			session.setAttribute("message", new Message("User does not exists with this email Id !!","alert-danger"));
			return "forgot_email_form";
		}
		
		//generating otp of 4 digits
		String text="0123456789";
		String otp="";
		for(int i=0;i<4;i++)
		{
			otp+=text.charAt(random.nextInt(10));
		}
		
		System.out.println("OTP "+otp);
		
		String subject="OTP from SCM";
		String message=""
				+ "<div style='border:1px solid #e2e2e2; padding:20px'>"
				+ "<h1>"
				+ "OTP is "
				+ "<b>"+otp
				+ "</b>"
				+ "</h1>"
				+ "</div>";
		String to=email;
		
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag)
		{
			session.setAttribute("message", new Message("OTP has been sent to your mail","alert-success"));
			session.setAttribute("my_otp", otp);
			session.setAttribute("email", email);
			
			return "verify_otp";
		}
		else
		{
			session.setAttribute("message", new Message("Something went wrong !!","alert-danger"));
			return "redirect:/forgot";
		}
	}
	
	//verify otp
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp") String otp,HttpSession session)
	{
		String myOtp = (String) session.getAttribute("my_otp");
		if(myOtp.equals(otp))
		{
			//send change password form
			session.setAttribute("message", new Message("OTP verified successfully !!","alert-success"));
			return "password_change_form";
		}
		else
		{
			session.setAttribute("message", new Message("Incorrect otp !! Entere OTP again","alert-danger"));
			return "verify_otp";
		}
		
	}
	
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword,HttpSession session)
	{
		String email = (String) session.getAttribute("email");
		
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(user);

		return "redirect:/signin?change= Password changed successfully..";
		
	}
}
