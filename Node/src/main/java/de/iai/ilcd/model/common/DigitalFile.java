package de.iai.ilcd.model.common;

import de.iai.ilcd.model.source.Source;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "digitalfile")
public class DigitalFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String fileName;

    @ManyToOne()
    private Source source;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Source getSource() {
        return this.source;
    }

    public void setSource(Source source) {
        this.source = source;
        if (!source.getFiles().contains(this)) {
            source.addFile(this);
        }
    }

    public String getAbsoluteFileName() {

        String directory = this.source.getFilesDirectory();

        String path = directory + "/" + this.fileName;
        return path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.fileName == null) ? 0 : this.fileName.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.source == null) ? 0 : this.source.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DigitalFile)) {
            return false;
        }
        DigitalFile other = (DigitalFile) obj;
        if (this.fileName == null) {
            if (other.fileName != null) {
                return false;
            }
        } else if (!this.fileName.equals(other.fileName)) {
            return false;
        }
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.source == null) {
            if (other.source != null) {
                return false;
            }
        } else if (!this.source.equals(other.source)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.common.DigitalFile[fileName=" + this.fileName + "]";
    }

}
