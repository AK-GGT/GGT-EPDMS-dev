package de.iai.ilcd.webgui.controller.admin;

import de.fzk.iai.ilcd.api.dataset.ILCDTypes;

import java.io.File;
import java.io.Serializable;

public class UploadedFileInformation implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 7380920987598229033L;

    private String pathName = null;

    private String subFolder = null;

    private String fileName = null;

    private ILCDTypes ilcdType = null;

    private String fileType = null;

    private String message;

    private long fileSize = 0;

    private boolean addAsNew = true;

    private boolean overwrite = false;

    public UploadedFileInformation() {
        this.subFolder = Long.toString(System.currentTimeMillis());
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public File getFile() {
        return new File(pathName);
    }

    public ILCDTypes getIlcdType() {
        return ilcdType;
    }

    public void setIlcdType(ILCDTypes ilcdType) {
        this.ilcdType = ilcdType;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAddAsNew() {
        return addAsNew;
    }

    public void setAddAsNew(boolean addAsNew) {
        this.addAsNew = addAsNew;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public String getSubFolder() {
        return subFolder;
    }

    public void setSubFolder(String subFolder) {
        this.subFolder = subFolder;
    }

}
