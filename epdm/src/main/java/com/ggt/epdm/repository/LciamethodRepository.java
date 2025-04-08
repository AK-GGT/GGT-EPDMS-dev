package com.ggt.epdm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ggt.epdm.model.Lciamethod;

public interface LciamethodRepository extends JpaRepository<Lciamethod, Long> {
	
	List<Lciamethod> findAllByXmlfileIdIn(List<Long> ids);
	
}
