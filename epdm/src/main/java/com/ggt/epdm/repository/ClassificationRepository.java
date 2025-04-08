package com.ggt.epdm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ggt.epdm.model.Classification;

public interface ClassificationRepository extends JpaRepository<Classification, Long> {
	
}
