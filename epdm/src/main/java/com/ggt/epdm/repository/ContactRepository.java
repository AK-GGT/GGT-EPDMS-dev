package com.ggt.epdm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ggt.epdm.model.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {
	
	List<Contact> findAllByXmlfileIdIn(List<Long> ids);
	
}
