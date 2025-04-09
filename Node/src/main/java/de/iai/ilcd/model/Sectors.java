package de.iai.ilcd.model;

import de.iai.ilcd.configuration.ConfigurationService;
import de.schlichtherle.io.FileInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * @author Vilmantas Baranauskas
 */
@ManagedBean(name = "sectors")
@ApplicationScoped
public class Sectors {

    public static final String DEFAULT_SECTORS_FILE = "sectors.txt";
    private final static Logger log = LogManager.getLogger(Sectors.class);
    private final List<String> sectors;

    public Sectors() {
        sectors = Collections.unmodifiableList(loadSectors());
    }

    private List<String> loadSectors() {
        InputStream in = null;
        String sectorsFile = null;
        try {
            String overrideSectorsDef = ConfigurationService.INSTANCE.getProperties().getString("user.registration.sectors.definition", null);
            if (StringUtils.isNotBlank(overrideSectorsDef)) {
                sectorsFile = overrideSectorsDef;
                in = new FileInputStream(new File(overrideSectorsDef));
            } else {
                sectorsFile = DEFAULT_SECTORS_FILE;
                in = new ClassPathResource(DEFAULT_SECTORS_FILE).getInputStream();
            }
            List<String> sectors = IOUtils.readLines(in, "UTF-8");
            log.info("Loaded {} sectors", sectors.size());
            return sectors;
        } catch (IOException e) {
            log.error("Cannot read {} file", sectorsFile, e);
            return Collections.emptyList();
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public List<String> getSectors() {
        return sectors;
    }

}
