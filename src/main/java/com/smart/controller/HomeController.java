package com.smart.controller;

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

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

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
	@PostMapping("/do_register")
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
			
			User res = this.userRepository.save(user);
			
			m.addAttribute("user", new User());
			
			session.setAttribute("message", new Message("Successfully registered !!","alert-success"));
			return "signup";
			
		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !! "+e.getMessage(),"alert-danger"));
			return "signup";
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
