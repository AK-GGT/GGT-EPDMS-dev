package com.ggt.epdm.utils;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import feign.Response;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "sodaApiClient", url = "http://localhost:80/resource")
public interface SodaApiClientService {
    
    //@GetMapping("/authenticate/getToken")
    //String getAuthenticatedToken(@RequestParam("userName") String username, @RequestParam("password") String password);
    
    @GetMapping("/{datasetType}/{uuid}")
    Response getDatasetById(@PathVariable("datasetType") String datasetType, @PathVariable("uuid") String uuid, @RequestParam("format") String format);
    
}
