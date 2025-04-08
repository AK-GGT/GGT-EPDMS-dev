package com.ggt.epdm.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.ggt.epdm.model.Xmlfile;

public interface XmlfileRepository extends JpaRepository<Xmlfile, Long> {
	
	@Query("SELECT x FROM Xmlfile x LEFT JOIN PortalExportedXml pex on pex.xmlfile.id = x.id WHERE pex.xmlfile.id IS NULL")
    List<Xmlfile> findXmlfileNotInPortalExportedXml();
	
}
