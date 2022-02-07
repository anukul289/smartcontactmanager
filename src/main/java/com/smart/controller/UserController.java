package com.smart.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smart.dao.UserRepository;
import com.smart.entities.User;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/index")
	public String dashboard(Model m,Principal principal)
	{
		String userName = principal.getName();
		
		//get user using username(Email)
		User user=userRepository.getUserByUserName(userName);
		
		m.addAttribute("user", user);
		
		return "normal/user_dashboard";
	}
}
