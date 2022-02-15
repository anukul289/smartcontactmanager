package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EmailService emailService;
	
	Random random = new Random();

	@RequestMapping("/")
	public String home(Model m)
	{
		m.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	
	
	@RequestMapping("/about")
	public String about(Model m)
	{
		m.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model m)
	{
		m.addAttribute("title", "Register - Smart Contact Manager");
		m.addAttribute("user", new User());
		return "signup";
	}
	
	
	//handler for registering user
	@PostMapping("/send-otp-register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result,@RequestParam(value="agreement",defaultValue = "false") boolean agreement, Model m,HttpSession session)
	{
		try {
			
			if(!agreement)
			{
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}
			
			if(result.hasErrors())
			{
				
				m.addAttribute("user", user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			session.setAttribute("user", user);
			
			
			//generating otp of 4 digits
			String text="0123456789";
			String otp="";
			for(int i=0;i<4;i++)
			{
				otp+=text.charAt(random.nextInt(10));
			}
			
			
			String subject="OTP SCM - Registration ";
			String message=""
					+ "<div style='border:1px solid #e2e2e2; padding:20px'>"
					+ "<h1>"
					+ "OTP is "
					+ "<b>"+otp
					+ "</b>"
					+ "</h1>"
					+ "</div>";
			String to=user.getEmail();
			
			boolean flag = this.emailService.sendEmail(subject, message, to);
			
			if(flag)
			{
				session.setAttribute("message", new Message("OTP has been sent to your mail","alert-success"));
				session.setAttribute("my_otp", otp);
				session.setAttribute("email", user.getEmail());
				
				return "verify_otp_register";
			}
			else
			{
				session.setAttribute("message", new Message("Something went wrong !! Email Id already exists","alert-danger"));
				return "redirect:/signup";
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !! "+e.getMessage(),"alert-danger"));
			return "redirect:/signup";
		}
		
		
	}
	
	
	
	//verify otp
	@PostMapping("/do-register")
	public String verifyOTP(@RequestParam("otp") String otp,HttpSession session,Model m)
	{
		try {
				String myOtp = (String) session.getAttribute("my_otp");
				User user = (User) session.getAttribute("user");
				if(myOtp.equals(otp))
				{
					//send confirmation email
					
					String subject="Account Creation Successful - SCM ";
					String message=""
							+ "<div class='container'>"
							+ "Hi, "+ user.getName().toUpperCase()
							+ "<br>"
							+ "Your account has been successfully created."
							+ "<br>"
							+ "Your User Id is "
							+ "<b>"+ user.getEmail()
							+ "</b>"
							+ "<p style='color:blue'>"
							+ "Note: Please do not reply back to this mail"
							+ "</p>"
							+ "</div>";
					String to=user.getEmail();
					
					boolean flag = this.emailService.sendEmail(subject, message, to);
					
					//if mail is sent successfully
					if(flag)
					{
						this.userRepository.save(user);
						session.setAttribute("message", new Message("Account creation successful ","alert-success"));
						
						return "redirect:/signin";
					}
					else
					{
						session.setAttribute("message", new Message("Something went wrong !! Email Id already exists","alert-danger"));
						return "redirect:/signup";
					}
	
				}
				else
				{
					session.setAttribute("message", new Message("Incorrect otp !! Entere OTP again","alert-danger"));
					return "verify_otp_register";
				}
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !! Please Try registering again","alert-danger"));
			return "redirect:/signup";
			
		}
		
	}

	
	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model m)
	{
		m.addAttribute("title","Login Page");
		return "login";
	}
}
