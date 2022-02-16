package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	
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
				contact.setImageUrl("contact.png");
				System.out.println("File is empty");
			}
			else
			{
				//upload file to folder and update name to imageUrl in contact
				
				String originalFileName=file.getOriginalFilename();
				
				String timeMillis = String.valueOf(System.currentTimeMillis());
				LocalDate currDate = LocalDate.now();
				
				String currDateTime = currDate+timeMillis;
				
				String fileName = originalFileName.substring(0,originalFileName.indexOf(".")) + "_" +user.getId()+currDateTime;
				String fileExtension = originalFileName.substring(originalFileName.indexOf(".")+1);
				
				System.out.println("FILE NAME "+fileName);
				
				File newFile = new File(new ClassPathResource("static/img").getURI());
				
				System.out.println("NEW FILE "+newFile);
				
				//OutputStream outputStream = new FileOutputStream(newFile);
				
				Path path = Paths.get(newFile+File.separator+fileName+"."+fileExtension);
				
				System.out.println("PATH "+path);
						
				//IOUtils.copy(file.getInputStream(),outputStream);
				
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);

				contact.setImageUrl(fileName+"."+fileExtension);
				
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
			
			session.setAttribute("message", new Message("Something went wrong !! "+e.toString(),"alert-danger"));
			m.addAttribute("session_msg", session.getAttribute("message"));
			
			return "redirect:/user/add-contact";
		}
	}
	
	
	//show contacts handler
	//per page = 5 contacts
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal)
	{
		m.addAttribute("title", "View Contacts");
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	
	//showing particular contact detail
	@GetMapping("/contact/{cId}")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model m,Principal principal,HttpSession session)
	{
		
		
		
		try {
			
			Optional<Contact> contactOptional = this.contactRepository.findById(cId);
			Contact contact = contactOptional.get();
			
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			m.addAttribute("title", user.getName());
			if(user.getId()==contact.getUser().getId())
			{
				m.addAttribute("contact", contact);
			}
			return "normal/contact_detail";
			
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/user/show-contacts/0";
		}
		
	}
	
	
	//delete contact handler
	@GetMapping("/delete/{cId}")
	@Transactional
	public String deleteContact(@PathVariable("cId") Integer cId,Model m,Principal principal,HttpSession session)
	{
		
		Contact contact = this.contactRepository.findById(cId).get();
		User user = this.userRepository.getUserByUserName(principal.getName());

		try {
			//check if user is trying to delete its own contact or not
			if(user.getId()==contact.getUser().getId())
			{
				//deleting photo from server
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,contact.getImageUrl());
				file1.delete();
				
				user.getContacts().remove(contact);
				this.userRepository.save(user);
				
				session.setAttribute("message", new Message("Contact deleted successfully...","alert-success"));
			}
			
		} catch (Exception e) {
			session.setAttribute("message", new Message("Something went wrong !!","alert-danger"));
			e.printStackTrace();
		}
		
		
		return "redirect:/user/show-contacts/0";
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cId}")
	public String openUpdateForm(@PathVariable("cId") Integer cId,Model m)
	{
		m.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(cId).get();
		m.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	
	//process update form handler
	@PostMapping("/process-update")
	public String processUpdateForm(@ModelAttribute("contact") Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal principal)
	{
		try {
			
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			if(!file.isEmpty())
			{
				//delete old photo from server
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldContactDetail.getImageUrl());
				file1.delete();

				
				
				//update new photo to server and database
				File saveFile=new ClassPathResource("static/img").getFile();
				
				String originalFileName=file.getOriginalFilename();
				String fileName = originalFileName.substring(0,originalFileName.indexOf(".")) + "_" +user.getId()+ "_"+contact.getEmail();
				String fileExtension = originalFileName.substring(originalFileName.indexOf(".")+1);
						
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+fileName+"."+fileExtension);
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImageUrl(fileName+"."+fileExtension);

			}
			else
			{
				contact.setImageUrl(oldContactDetail.getImageUrl());
			}
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Contact updated successfully...","alert-success"));

			
		}catch (Exception e) {
			session.setAttribute("message", new Message("Something went wrong !!","alert-danger"));
			e.printStackTrace();
		}
		return "redirect:/user/contact/"+contact.getcId();
	}
	
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m)
	{
		m.addAttribute("title","Profile");
		return "normal/profile";
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings(Model m)
	{
		m.addAttribute("title","Settings");
		return "normal/settings";
	}
	
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,@RequestParam("confirmPassword") String confirmPassword,Principal principal,HttpSession session)
	{
		
		try {
			User user=this.userRepository.getUserByUserName(principal.getName());
			
			//old password is equal to currentPassword or not
			if(this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword()))
			{
				//new password and confirm password match or not
				if(newPassword.equals(confirmPassword))
				{
					//old password and new password should not be same
					if(oldPassword.equals(newPassword))
					{
						//throw error
						session.setAttribute("message", new Message("Old password and new password can't be same !!","alert-danger"));
						return "redirect:/user/settings";
					}
					else
					{
						//change the password
						user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
						this.userRepository.save(user);
						session.setAttribute("message", new Message("Password changed successfully...","alert-success"));
					}
				}
				else
				{
					//throw error
					session.setAttribute("message", new Message("New password and confirm password don't match !!","alert-danger"));
					return "redirect:/user/settings";
				}
			}
			else
			{
				//throw error
				session.setAttribute("message", new Message("Please enter correct details in old password field","alert-danger"));
				return "redirect:/user/settings";
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !!","alert-danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
	
}
