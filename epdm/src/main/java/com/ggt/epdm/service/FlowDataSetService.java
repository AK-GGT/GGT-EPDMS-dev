package com.ggt.epdm.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import com.ggt.epdm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class FlowDataSetService {

    //private final FlowDataSetRepository flowDataSetRepository;
    //private final FlowInformationRepository flowInformationRepository;
    //private final DataSetInformationRepository dataSetInformationRepository;
    //private final ClassificationInformationRepository classificationInformationRepository;
    //private final ModellingAndValidationRepository modellingAndValidationRepository;
    //private final AdministrativeInformationRepository administrativeInformationRepository;

    @SuppressWarnings("unused")
	@Transactional
    public void processXml(MultipartFile file) throws Exception {
        InputStream xmlInputStream = file.getInputStream();
        
        XmlMapper xmlMapper = new XmlMapper();
//
//        // Read XML and convert it into a FlowDataSet object
//		LinkedHashMap<String, Object> flowDataSet = xmlMapper.readValue(xmlInputStream, LinkedHashMap.class);
//		String xsiSchemaLocation = (String) flowDataSet.get("schemaLocation");
//		String version = (String) flowDataSet.get("version");
//		String locations = (String) flowDataSet.get("locations");
//        LinkedHashMap<String, Object> flowInformation = (LinkedHashMap<String, Object>) flowDataSet.get("flowInformation");
//        LinkedHashMap<String, Object> dataSetInformation = (LinkedHashMap<String, Object>) flowInformation.get("dataSetInformation");
//        String uuid = (String) dataSetInformation.get("UUID");
//        LinkedHashMap<String, Object> name = (LinkedHashMap<String, Object>) dataSetInformation.get("name");
//        ArrayList<LinkedHashMap<String, Object>> baseName = (ArrayList<LinkedHashMap<String, Object>>) name.get("baseName");
//        LinkedHashMap<String, Object> classificationInformation = (LinkedHashMap<String, Object>) dataSetInformation.get("classificationInformation");
//        LinkedHashMap<String, Object> classification = (LinkedHashMap<String, Object>) classificationInformation.get("classification");
//        ArrayList<LinkedHashMap<String, Object>> Class = (ArrayList<LinkedHashMap<String, Object>>) classification.get("class");
//        ArrayList<LinkedHashMap<String, Object>> generalComment = (ArrayList<LinkedHashMap<String, Object>>) dataSetInformation.get("generalComment");
//        LinkedHashMap<String, Object> modellingAndValidation = (LinkedHashMap<String, Object>) flowDataSet.get("modellingAndValidation");
//        LinkedHashMap<String, Object> LCIMethod = (LinkedHashMap<String, Object>) modellingAndValidation.get("LCIMethod");
//        String typeOfDataSet = (String) LCIMethod.get("typeOfDataSet");
//        LinkedHashMap<String, Object> administrativeInformation = (LinkedHashMap<String, Object>) flowDataSet.get("administrativeInformation");
//        LinkedHashMap<String, Object> dataEntryBy = (LinkedHashMap<String, Object>) administrativeInformation.get("dataEntryBy");
//        String timeStamp = (String) dataEntryBy.get("timeStamp");
//        LinkedHashMap<String, Object> publicationAndOwnership = (LinkedHashMap<String, Object>) administrativeInformation.get("publicationAndOwnership");
//        String dataSetVersion = (String) publicationAndOwnership.get("dataSetVersion");
        
        
        // Save FlowDataSet to DB
        //flowDataSetRepository.save(flowDataSet);
        //System.out.println(flowDataSet);
        
        
    }
}
