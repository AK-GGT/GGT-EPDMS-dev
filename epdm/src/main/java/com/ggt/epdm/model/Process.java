package com.ggt.epdm.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "process")
public class Process extends DataSet {
	
    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "BRANCH")
    private Integer branch;

    @Column(name = "classification_cache", length = 100)
    private String classificationCache;

    @Column(name = "COMPLETENESS")
    private String completeness;

    @Column(name = "complianceSystem_cache", length = 1)
    private String compliancesystemCache;

    @Column(name = "containsProductModel")
    private Boolean containsProductModel;

    @Column(name = "EXCHANGESINCLUDED")
    private Boolean exchangesincluded;

    @Column(name = "FORMAT")
    private String format;

    @Column(name = "lciMethodInformation_cache", length = 20)
    private String lcimethodinformationCache;

    @Column(name = "MOSTRECENTVERSION")
    private Boolean mostrecentversion;

    @Column(name = "name_cache")
    private String nameCache;

    @Column(name = "PARAMETERIZED")
    private Boolean parameterized;

    @Column(name = "PERMANENTURI")
    private String permanenturi;

    @Column(name = "RELEASESTATE")
    private String releasestate;

    @Column(name = "RESULTSINCLUDED")
    private Boolean resultsincluded;

    @Column(name = "subtype")
    private String subtype;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "margins")
    private Integer margins;

    @Column(name = "MAJORVERSION")
    private Integer majorversion;

    @Column(name = "MINORVERSION")
    private Integer minorversion;

    @Column(name = "SUBMINORVERSION")
    private Integer subminorversion;

    @Column(name = "VERSION")
    private Integer version;

    @Column(name = "IMPORTDATE")
    private Instant importdate;

    @Column(name = "publicationDateOfEPD")
    private Instant publicationDateOfEPD;

    @Column(name = "registrationNumber")
    private String registrationNumber;

    @Column(name = "epdFormatVersion")
    private String epdFormatVersion;

    @Column(name = "metaDataOnly")
    private Boolean metaDataOnly;

    @Column(name = "visible")
    private Boolean visible;

}