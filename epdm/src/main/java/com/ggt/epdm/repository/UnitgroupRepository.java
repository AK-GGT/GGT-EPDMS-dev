package com.ggt.epdm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ggt.epdm.model.Unitgroup;

public interface UnitgroupRepository extends JpaRepository<Unitgroup, Long> {
	
	List<Unitgroup> findAllByXmlfileIdIn(List<Long> ids);
	
}
