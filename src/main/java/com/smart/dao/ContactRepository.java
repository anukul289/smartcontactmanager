package com.smart.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer>{

	@Query("from Contact as c where c.user.id =:userId")
	//Pageable interface will have two things : 
	//currentPage - page variable
	//contact per page - 5
	public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);
	
	
}
