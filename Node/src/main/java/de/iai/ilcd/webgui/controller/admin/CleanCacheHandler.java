package de.iai.ilcd.webgui.controller.admin;

import com.okworx.ilcd.validation.profile.ProfileManager;
import de.iai.ilcd.configuration.ConfigurationService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.Period;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class CleanCacheHandler implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(CleanCacheHandler.class);


    @Inject
    private JdbcTemplate jdbcTemplate;

    private List<String> cacheLabels;
    private List<String> selectedCacheLabels;

    /**
     * Populate the cacheLabels with all the values of each CacheRepo.
     * <p>
     * Primefaces checks this.cacheLabes and generate a checkbox next to each one in
     * XHTML view.
     */

    @PostConstruct
    void init() {
        this.cacheLabels = EnumSet.allOf(CacheRepo.class)
                .stream()
                .map(x -> x.checkboxLabel)
                .collect(Collectors.toList());
    }

    /**
     * clearCache() called from the view when the user selects
     * repositories to clean and clicks on the "Clean" button.
     */

    public void clearCache() {

        FacesContext context = FacesContext.getCurrentInstance();
        String msg;

        for (String s : selectedCacheLabels) {
            CacheRepo cl = CacheRepo.fromCheckboxLabel(s);
            if (cl.equals(CacheRepo.EXPORTTag))
                deleteTagTable(); // ugly workaround, cannot autowire a static jdbctemplate
            if (cl.clean())
                msg = s + " have been cleared\n";
            else
                msg = "Failed to clear " + s + "\n";
            context.addMessage(null, new FacesMessage(msg));
        }
    }

    /**
     * Delete all rows from the <code>datastock_export_tag</code> table;
     *
     * @return boolean value indicates whether the SQL query ran successful or not
     */

    private boolean deleteTagTable() {
        final String SQL = "DELETE FROM datastock_export_tag;";
        try {
            jdbcTemplate.update(SQL); // returns number of deleted records
        } catch (Exception e) {
            LOGGER.error("Failed to clear SQL table", e);
            return false;
        }
        return true;
    }

    @Bean
    public String getCRON() {
        return ConfigurationService.INSTANCE.getCleanCacheCron();
    }

    @Scheduled(cron = "#{@getCRON}")
    private void cronClean() {
        LOGGER.warn("CRON schedule cache clean job has been fired");

        CacheRepo.UploadDir.clean();
        cleanExpiredZips();
    }

    /**
     * <ol>
     * 		<li>Delete only the expired tags from <code>datastock_export_tag</code></li>
     * 		<li>Then delete any zip files from the filesystem that are not referenced by any export tag.</li>
     * </ol>
     */
    private void cleanExpiredZips() {
        LOGGER.info("cleaning export cache (expired ZIPs/CSVs)");

        LOGGER.debug("removing unreferenced exports from cache dir");

        // iterate over all rows and delete corresponding files
        final String filesQuery = "SELECT file FROM datastock_export_tag WHERE modified = 0";
        try {
            List<String> filesToSkip = jdbcTemplate.queryForList(filesQuery, String.class);
            LOGGER.debug("found {} files", filesToSkip.size());
            LOGGER.trace("skipping: {}", filesToSkip.parallelStream().map(a -> StringUtils.substringAfterLast(a, File.separator)).collect(Collectors.toList()));

            Path zipsDir = Paths.get(CacheRepo.ZIPDOWNLOAD.directory);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(zipsDir)) {
                for (Path file : stream) {
                    LOGGER.debug("processing export file {}", file.getFileName());
                    if (filesToSkip.contains(file.toString()) || Files.getLastModifiedTime(file).toInstant().isAfter(Instant.now().minus(Period.ofDays(1)))) {
                        LOGGER.debug("    (skipping as it's still valid)");
                    } else {
                        try {
                            LOGGER.debug("    deleting file {}", file.getFileName());
                            boolean success = Files.deleteIfExists(file);
                            if (success) {
                                LOGGER.debug("    successfully deleted file {}", file.getFileName());
                            } else {
                                LOGGER.warn("    could NOT delete file {}", file.getFileName());
                            }
                        } catch (IOException e) {
                            LOGGER.warn("    Cannot delete {} (possibly due to lack of permissions)", file.getFileName());
                        }
                    }

                }
            } catch (IOException e) {
                LOGGER.error("Error accessing ZIP files directory ", e);
            }

            LOGGER.debug("deleting modified export tags");
            if (LOGGER.isDebugEnabled()) {
                final String sizeQuery = "SELECT COUNT(file) FROM datastock_export_tag WHERE modified = 1";
                final Integer modifiedCount = jdbcTemplate.queryForObject(sizeQuery, Integer.class);
                LOGGER.debug("found {} modified records", modifiedCount);
            }
            jdbcTemplate.update("DELETE FROM datastock_export_tag WHERE modified = 1");

        } catch (Exception e) {
            LOGGER.error("Failed to execute given SQL query", e);
        }
    }

    public List<String> getCacheLabels() {
        return cacheLabels;
    }

    public void setCacheLabels(List<String> cacheLabels) {
        this.cacheLabels = cacheLabels;
    }

    public List<String> getSelectedCacheLabels() {
        return selectedCacheLabels;
    }

    public void setSelectedCacheLabels(List<String> selectedCacheLabels) {
        this.selectedCacheLabels = selectedCacheLabels;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private enum CacheRepo {
        ZIPDOWNLOAD("Zip Downloads", ConfigurationService.INSTANCE.getZipFileDirectory()),
        UploadDir("Uploads Directory", ConfigurationService.INSTANCE.getUploadDirectory()),
        ValidationProfiles("Validation Profiles", ConfigurationService.INSTANCE.getProfileDirectory()) {
            @Override
            boolean clean() {
                // first clean the target directory
                boolean result = super.clean();

                // then reset Profile Manager, this will re-register default profiles
                ProfileManager.getInstance().reset();

                return result;
            }
        },

        // cannot autowire a static jdbctemplate
        EXPORTTag("Export Tags")
//		{
//			@Override
//			boolean clean() {
//				final String SQL = "DELETE FROM datastock_export_tag;";
//				try {
//					jdbcTemplate.update(SQL); // returns number of deleted records
//				} catch (Exception e) {
//					LOGGER.error("Failed to clear SQL table", e);
//					return false;
//				}
//				return true;
//			}
//		};
                {
                    @Override
                    boolean clean() {
                        return true;
                    }
                };

        String checkboxLabel, directory;

        /**
         * Special constructor for Cache Repositories with no location on the file
         * system. If that is the case, You <b>MUST</b> override clean.
         *
         * @param checkboxLabel name that appears to user in the view
         */
        CacheRepo(String checkboxLabel) {
            this.checkboxLabel = checkboxLabel;
        }

        /**
         * Default constructor for Cache Repositories with a location on the filesystem.
         *
         * @param checkboxLabel name that appears to user in the view
         * @param dir           location of the repository on the filesystem
         */
        CacheRepo(String checkboxLabel, String dir) {
            this.checkboxLabel = checkboxLabel;
            this.directory = dir;
        }

        public static CacheRepo fromCheckboxLabel(String checkboxLabel) {
            for (CacheRepo c : CacheRepo.values())
                if (c.checkboxLabel.equalsIgnoreCase(checkboxLabel))
                    return c;
            return null;
        }

        boolean clean() {
            Path dir = Paths.get(this.directory);
            try {
                FileUtils.cleanDirectory(dir.toFile());
            } catch (IOException e) {
                LOGGER.error(this.checkboxLabel + " not found or you don't have permissions.");
                return false;
            } catch (IllegalArgumentException e) {
                LOGGER.error(e.getMessage());
            }
            return true;
        }
    }

}
