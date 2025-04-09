package de.iai.ilcd.model.common;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "categorydefinition")
public class CategoryDefinition {

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "XMLFILE_ID")
    XmlFile xmlFile;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date importDate = null;

    @Basic
    private boolean mostRecentVersion;

    public XmlFile getXmlFile() {
        return xmlFile;
    }

    public void setXmlFile(XmlFile xmlFile) {
        this.xmlFile = xmlFile;
    }

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    public boolean isMostRecentVersion() {
        return mostRecentVersion;
    }

    public void setMostRecentVersion(boolean mostRecentVersion) {
        this.mostRecentVersion = mostRecentVersion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
