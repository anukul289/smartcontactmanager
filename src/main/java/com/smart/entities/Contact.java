package com.smart.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Contact {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int cId;
	
	@NotBlank(message="Name is required !!")
	@Size(min=2,max=20,message="Length should be between 2-20 characters !!")
	private String name;
	
	private String secondName;
	private String work;
	
	@NotBlank(message="Email is required !!")
	@Pattern(regexp = "[a-z0-9]+@[a-z]+.[a-z]{2,3}",message="Email should be in proper format")
	private String email;
	
	@NotBlank(message="Phone is required !!")
	@Size(min=10,max=10,message="Length should be 10 digits")
	private String phone;
	private String imageUrl;
	
	@Column(length = 1000)
	private String description;
	
	@ManyToOne
	@JsonIgnore
	private User user;

	public Contact() {
		// TODO Auto-generated constructor stub
	}


	public int getcId() {
		return cId;
	}

	public void setcId(int cId) {
		this.cId = cId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSecondName() {
		return secondName;
	}

	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}

	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this.cId==((Contact)obj).getcId();
	}

	@Override
	public String toString() {
		return "Contact [cId=" + cId + ", name=" + name + ", secondName=" + secondName + ", work=" + work + ", email="
				+ email + ", phone=" + phone + ", imageUrl=" + imageUrl + ", description=" + description + "]";
	}
	
	
	
	
}

