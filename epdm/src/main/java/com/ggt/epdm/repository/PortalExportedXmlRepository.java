package com.ggt.epdm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ggt.epdm.model.PortalExportedXml;

public interface PortalExportedXmlRepository extends JpaRepository<PortalExportedXml, Long> {
	
	
}
