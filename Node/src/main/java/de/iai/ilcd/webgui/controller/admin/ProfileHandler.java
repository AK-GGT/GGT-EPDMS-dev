package de.iai.ilcd.webgui.controller.admin;

import com.okworx.ilcd.validation.exception.InvalidProfileException;
import com.okworx.ilcd.validation.profile.Profile;
import com.okworx.ilcd.validation.profile.ProfileManager;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ManagedBean(name = "profileHandler", eager = true)
@SessionScoped
public class ProfileHandler extends AbstractHandler {

    /**
     *
     */
    private static final long serialVersionUID = -5715497813429246777L;

    public static Logger logger = LogManager.getLogger(ProfileHandler.class);

    private List<Profile> selectedProfiles = new ArrayList<Profile>();

    private Profile selectedProfile;

    public ProfileHandler() {
    }

    //TODO: add Profile.getDescription() and show in validation.xhtml
    public synchronized void handleProfileUpload(FileUploadEvent event) throws AbortProcessingException {
        logger.debug("Profile Upload handler routine called");
        UploadedFile file = event.getFile();
        if (file != null) { // i.e. we have success
            try {
                File copiedFile = this.analyzeAndCopyProfile(file);
                if (copiedFile != null) {
                    if (logger.isDebugEnabled())
                        ProfileHandler.logger.info("Uploaded profile: " + file.getFileName());
                    Profile profile = this.registerProfile(copiedFile.toURI().toURL());
                    if (logger.isDebugEnabled())
                        ProfileHandler.logger.info("Profile " + profile.getURL() + " registered");
                    this.addI18NFacesMessage("facesMsg.import.profileuploadSuccess", FacesMessage.SEVERITY_INFO);

                } else {
                    this.addI18NFacesMessage("facesMsg.import.filealreadyExist", FacesMessage.SEVERITY_ERROR);
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                this.addI18NFacesMessage("facesMsg.import.fileuploadError1", FacesMessage.SEVERITY_ERROR);
            }
        } else {
            this.addI18NFacesMessage("facesMsg.import.fileuploadError2", FacesMessage.SEVERITY_ERROR);
        }
    }

    private File analyzeAndCopyProfile(UploadedFile file) throws IOException {

        /*
         * CG / 2012-01-06: don't use only file.getFileName() - which uses
         * FileItem.getName() - because IE will return the whole path but not
         * just the file name, see
         * http://commons.apache.org/fileupload/faq.html#whole-path-from-IE
         */
        File copiedFile = null;
        String fileName = FilenameUtils.getName(file.getFileName());

        String targetDirectory = this.getUploadProfileDirectory();
        InputStream in = file.getInputStream();
        copiedFile = this.copyFile(in, targetDirectory, fileName);
        in.close();

        return copiedFile;

    }

    public Collection<Profile> getUploadedProfiles() {
        return ProfileManager.getInstance().getProfiles();

    }

    public Profile getDefaultProfile() {
        return ProfileManager.getInstance().getDefaultProfile();
    }

    public boolean isDefaultProfile(Profile profile) {
        return profile.equals(getDefaultProfile());
    }

    private File copyFile(InputStream inStream, String directory, String fileName) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            FileUtils.forceMkdir(dir);
        } else {
            // cleanup the directory first
            // FileUtils.cleanDirectory(dir);
        }
        String outputFilePath;
        if (directory.endsWith("/")) {
            outputFilePath = directory + fileName;
        } else {
            outputFilePath = directory + "/" + fileName;
        }
        File outputFile = new File(outputFilePath);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.equals(outputFile))
                return null;
        }
        FileOutputStream outputStream = new FileOutputStream(outputFile);

        IOUtils.copy(inStream, outputStream);
        outputStream.close();

        return outputFile;
    }

    private Profile registerProfile(URL url) {
        Profile profile = null;
        try {
            profile = ProfileManager.getInstance().registerProfile(url);
        } catch (InvalidProfileException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid profile.", null));
        }
        return profile;
    }

    private String getUploadProfileDirectory() {
        return ConfigurationService.INSTANCE.getProfileDirectory();
    }

    public List<Profile> getSelectedProfiles() {
        List<Profile> profiles = selectedProfiles;
        profiles.remove(getDefaultProfile());
        return profiles;
    }

    public void setSelectedProfiles(List<Profile> selectedProfiles) {
        this.selectedProfiles = selectedProfiles;
    }

    public Profile getSelectedProfile() {
        return selectedProfile;
    }

    public void setSelectedProfile(Profile selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    public String getSelectedProfileDescription() {
        if (selectedProfile != null)
            return selectedProfile.getDescription();
        if (!getUploadedProfiles().isEmpty())
            return getUploadedProfiles().iterator().next().getDescription();
        return "";
    }

    public void deleteProfile() {
        File fileToDelete = null;

        for (Profile profile : selectedProfiles) {
            if (profile.equals(this.getDefaultProfile()))
                continue;
            ProfileManager.getInstance().deregisterProfile(profile);
            fileToDelete = new File(profile.getURL().getFile());

            if (fileToDelete.delete()) {
                logger.debug(profile.getURL() + " deleted");
            } else
                logger.error("error while deleting file");
        }

        selectedProfiles.clear();

    }

}
