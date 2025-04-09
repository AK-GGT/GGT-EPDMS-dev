package de.iai.ilcd.model.datastock;

import de.iai.ilcd.webgui.controller.util.ExportMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * This class is used to track the cache status of datastock ZIP exports.
 * <p>
 * If a datastock changes, the modified flag is set to true. Once an export is performed, this flag is set to false and
 * the path of the generated ZIP file is stored along with a time stamp.
 */
@Entity
@Table(name = "datastock_export_tag")
public class ExportTag implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5620038505677318648L;

    /**
     * Database ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * stock has been modified since last export
     */
    @Basic
    @Column(name = "modified")
    private boolean modified = true;

    /**
     * path of the cached file
     */
    @Basic
    @Column(name = "file")
    private String file;

    /**
     * time stamp of last export
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp")
    private Date timeStamp = null;

    @Basic
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "type")
    private ExportType type;

    @Basic
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "mode")
    private ExportMode mode;

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void update(String file) {
        this.file = file;
        this.modified = false;
        this.timeStamp = new Date();
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public ExportType getType() {
        return type;
    }

    public void setType(ExportType type) {
        this.type = type;
    }

    public ExportMode getMode() {
        return mode;
    }

    public void setMode(ExportMode mode) {
        this.mode = mode;
    }

}