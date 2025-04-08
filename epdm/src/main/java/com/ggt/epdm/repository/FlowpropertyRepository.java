package com.ggt.epdm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ggt.epdm.model.Flowproperty;

public interface FlowpropertyRepository extends JpaRepository<Flowproperty, Long> {
	
	List<Flowproperty> findAllByXmlfileIdIn(List<Long> ids);
	
}
