package com.ggt.epdm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ggt.epdm.model.Process;

public interface ProcessRepository extends JpaRepository<Process, Long> {
	
	List<Process> findAllByXmlfileIdIn(List<Long> ids);
	
}
