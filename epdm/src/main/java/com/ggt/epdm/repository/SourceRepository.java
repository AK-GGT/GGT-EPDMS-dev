package com.ggt.epdm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ggt.epdm.model.Source;

public interface SourceRepository extends JpaRepository<Source, Long> {
	
	List<Source> findAllByXmlfileIdIn(List<Long> ids);
	
}
