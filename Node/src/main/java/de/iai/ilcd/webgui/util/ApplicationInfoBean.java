package de.iai.ilcd.webgui.util;

import de.iai.ilcd.configuration.ConfigurationService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.InputStream;
import java.io.Serializable;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Component
@Scope("application")
public class ApplicationInfoBean implements Serializable {

    private static final long serialVersionUID = 5116590227690230589L;

    private String version;

    private String versionTag;

    private String buildTimestamp;

    private String appTitle;

    private String appTitleShort;

    @PostConstruct
    private void init() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        version = getParamFromManifest(session, "VERSION");
        versionTag = getParamFromManifest(session, "VERSION-TAG");
        buildTimestamp = getParamFromManifest(session, "APP-TITLE");
        appTitle = getParamFromManifest(session, "APP-TITLE-SHORT");
        appTitleShort = getParamFromManifest(session, "BUILD-TIMESTAMP");
    }

    private String getParamFromManifest(HttpSession session, String param) {
        String result = "unknown param";
        try {
            ServletContext application = session.getServletContext();
            InputStream inputStream = application.getResourceAsStream("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(inputStream);
            Attributes atts = manifest.getMainAttributes();
            result = atts.getValue(param);
        } catch (Exception e) {
        }

        return result;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionTag() {
        return versionTag;
    }

    public String getBuildTimestamp() {
        return buildTimestamp;
    }

    public String getAppTitle() {
        return appTitle;
    }

    public String getAppTitleShort() {
        return appTitleShort;
    }

    public boolean isRegistryBasedNetworking() {
        return ConfigurationService.INSTANCE.isRegistryBasedNetworking();
    }

    public boolean isShowRegistrationControls() {
        return ConfigurationService.INSTANCE.isRegistryBasedNetworking() || ConfigurationService.INSTANCE.isGladEnabled();
    }

}
