package com.ggt.epdm.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.ggt.epdm.service.DataExportService;

/**
 * @see DataExportController
 *
 *
 * @author Akshay Benny GGT
 * @since 1.0
 * 
 */


@RestController
@RequestMapping("/epd")
//@RequiredArgsConstructor
public class DataExportController {
	
	@Autowired
    private DataExportService dataExportService;

    @GetMapping(value = "/upload", produces = MediaType.APPLICATION_XML_VALUE)
    public Object uploadFlowDataSet() throws FileNotFoundException, IOException {
        
    	Object pendingXmlFiles = dataExportService.getAllPendingXmlFiles();

    	return pendingXmlFiles;
    }
}
