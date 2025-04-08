package com.ggt.epdm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ggt.epdm.model.FlowCommon;

public interface FlowCommonRepository extends JpaRepository<FlowCommon, Long> {
	
	List<FlowCommon> findAllByXmlfileIdIn(List<Long> ids);
	
}
