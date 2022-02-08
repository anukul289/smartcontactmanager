package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model m, Principal principal)
	{
		String userName = principal.getName();
		
		//get user using username(Email)
		User user=userRepository.getUserByUserName(userName);
		
		m.addAttribute("user", user);
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model m,Principal principal)
	{
		m.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	//open add contact form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model m,HttpSession session)
	{
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		if(m.containsAttribute("session_msg"))
		{
			session.setAttribute("message", m.getAttribute("session_msg"));
		}
		return "normal/add_contact";
	}
	
	//processing add contact
	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute("contact") Contact contact,BindingResult result,@RequestParam("profileImage") MultipartFile file ,Model m, Principal principal,HttpSession session)
	{
		try {
			
			if(result.hasErrors())
			{
				m.addAttribute("contact", contact);
				return "normal/add_contact";
			}
			
			//Save contact to database and update user
			String name = principal.getName();
			User user=this.userRepository.getUserByUserName(name);
			
			//processing and uploading file
			if(file.isEmpty())
			{
				System.out.println("File is empty");
			}
			else
			{
				//upload file to folder and update name to imageUrl in contact
				contact.setImageUrl(file.getOriginalFilename());
				File saveFile=new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename()+"_"+user.getId()+"_"+contact.getEmail());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image uploaded");
			}
			
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Contact saved successfully !!","alert-success"));
			m.addAttribute("session_msg", session.getAttribute("message"));

			System.out.println("DATA "+contact);
			
			return "redirect:/user/add-contact";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			m.addAttribute("contact", contact);
			
			session.setAttribute("message", new Message("Something went wrong !! "+e.getMessage(),"alert-danger"));
			m.addAttribute("session_msg", session.getAttribute("message"));
			
			return "redirect:/user/add-contact";
		}
	}
	
	
	//show contacts handler
	@GetMapping("show-contacts")
	public String showContacts(Model m,Principal principal)
	{
		m.addAttribute("title", "View Contacts");
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		List<Contact> contacts = this.contactRepository.findContactsByUser(user.getId());
		m.addAttribute("contacts", contacts);
		
		return "normal/show_contacts";
	}
}
