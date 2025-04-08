package com.ggt.epdm.service;

import com.ggt.epdm.model.Classification;
import com.ggt.epdm.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
//@RequiredArgsConstructor
public class ClassificationService {

	@Autowired
	private ClassificationRepository classificationRepository;

	public List<Classification> getAllClassifications() {

		return classificationRepository.findAll();

	}
}