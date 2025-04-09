package eu.europa.ec.jrc.lca.registry.view.beans;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${theme}")
    private String theme;

    @PostConstruct
    private void init() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        version = getVersionFromManifest(session);
    }

    private String getVersionFromManifest(HttpSession session) {
        String retval = "unknown version";
        try {
            ServletContext application = session.getServletContext();
            InputStream inputStream = application.getResourceAsStream("/META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(inputStream);
            Attributes atts = manifest.getMainAttributes();
            retval = atts.getValue("artifactId") + " v" + atts.getValue("version");
        } catch (Exception e) {
        }
        return retval;
    }

    public String getVersion() {
        return version;
    }

    public String getTheme() {
        return theme;
    }

}
