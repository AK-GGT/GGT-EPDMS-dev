package de.iai.ilcd.security.role;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;

import static de.iai.ilcd.configuration.CachingConfig.LICENSE_CACHEMANAGER;

@Controller("sodaRemoteRole")
public class RemoteRole {

    private static final Logger log = LoggerFactory.getLogger(RemoteRole.class);

//    private final RoleMapping roleMapping;

//    @Autowired
//    public RemoteRole(RoleMapping roleMapping){
//        this.roleMapping = roleMapping;
//    }

    // TODO: autowire httpclient from somewhere
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    @Cacheable(LICENSE_CACHEMANAGER)
    public List<String> licenseRoles(String licenseURL, String authHeader) {
        var erg = new LinkedList<String>();

        if (licenseURL == null)
            return erg;

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(licenseURL))
                .setHeader("Authorization", "Bearer " + authHeader)
                .build();

        try {
            log.error("Asking license server for roles...");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper(); // TODO: autowire this?
            JsonNode json = mapper.readTree(response.body());

            var json_roles = json.get("roles");
            for (var r : json_roles)
                erg.add(r.textValue());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            log.warn("Failed to contact the license server " + licenseURL);
        }

        return erg;
    }
}
