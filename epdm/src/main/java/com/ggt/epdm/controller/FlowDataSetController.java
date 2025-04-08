package com.ggt.epdm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ggt.epdm.model.Classification;
import com.ggt.epdm.service.ClassificationService;
import com.ggt.epdm.service.FlowDataSetService;
import com.ggt.epdm.utils.SodaApiClientService;

@RestController
@RequestMapping("/epd/flows")
//@RequiredArgsConstructor
public class FlowDataSetController {
	
	@Autowired
    private FlowDataSetService flowDataSetService;
	
	@Autowired
    private ClassificationService classificationService;
	
	@Autowired
	private SodaApiClientService sodaApiClientService;

    @PostMapping("/upload")
    public String uploadFlowDataSet(@RequestParam("file") MultipartFile file) {
        try {
            flowDataSetService.processXml(file);
            return "XML data uploaded successfully!";
        } catch (Exception e) {
            return "Error processing XML: " + e.getMessage();
        }
    }
    
    @GetMapping("/test")
    public List<Classification> testApi() {
        return classificationService.getAllClassifications();
    }
    
    @GetMapping("/getToken")
    public String getToken() {
        //return sodaApiClientService.getAuthenticatedToken("admin", "default");
    	return "";
    }
}
