package com.ggt.epdm.utils;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;

@FeignClient(name = "ecoPortalApiClient", url = "${ecoportal.node.url}")
public interface EcoPortalApiService {
    
    @GetMapping("/authenticate/getToken")
    String getAuthenticatedToken(@RequestParam("userName") String username, @RequestParam("password") String password);
    
    @PostMapping(value = "/lciamethods", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response saveLciamethodDataset(@RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader("stock") String stock,@RequestPart("file") MultipartFile file);
    
    @PostMapping(value = "/processes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response saveProcessesDataset(@RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader("stock") String stock,@RequestPart("file") MultipartFile file);

    @PostMapping(value = "/sources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response saveSourceDataset(@RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader("stock") String stock,@RequestPart("file") MultipartFile file);
    
    @PostMapping(value = "/flows", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response saveFlowDataset(@RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader("stock") String stock,@RequestPart("file") MultipartFile file);
    
    @PostMapping(value = "/flowproperties", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response saveFlowPropertyDataset(@RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader("stock") String stock,@RequestPart("file") MultipartFile file);
    
    @PostMapping(value = "/contacts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response saveContactDataset(@RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader("stock") String stock,@RequestPart("file") MultipartFile file);
    
    @PostMapping(value = "/unitgroups", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response saveUnitGroupDataset(@RequestHeader("Authorization") String authorizationHeader,
            @RequestHeader("stock") String stock,@RequestPart("file") MultipartFile file);
}
