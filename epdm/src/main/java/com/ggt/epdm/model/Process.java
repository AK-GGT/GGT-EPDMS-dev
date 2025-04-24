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

    @Column(name = "branch")
    private Integer branch;

    @Column(name = "classification_cache", length = 100)
    private String classificationCache;

    @Column(name = "COMPLETENESS")
    private String completeness;

    @Column(name = "compliancesystem_cache", length = 1)
    private String compliancesystemCache;

    @Column(name = "containsproductmodel")
    private Boolean containsProductModel;

    @Column(name = "exchangesincluded")
    private Boolean exchangesincluded;

    @Column(name = "format")
    private String format;

    @Column(name = "lcimethodinformation_cache", length = 20)
    private String lcimethodinformationCache;

    @Column(name = "mostrecentversion")
    private Boolean mostrecentversion;

    @Column(name = "name_cache")
    private String nameCache;

    @Column(name = "parameterized")
    private Boolean parameterized;

    @Column(name = "permanenturi")
    private String permanenturi;

    @Column(name = "releasestate")
    private String releasestate;

    @Column(name = "resultsincluded")
    private Boolean resultsincluded;

    @Column(name = "subtype")
    private String subtype;

    @Column(name = "type")
    private String type;

    @Column(name = "margins")
    private Integer margins;

    @Column(name = "majorversion")
    private Integer majorversion;

    @Column(name = "minorversion")
    private Integer minorversion;

    @Column(name = "subminorversion")
    private Integer subminorversion;

    @Column(name = "version")
    private Integer version;

    @Column(name = "importdate")
    private Instant importdate;

    @Column(name = "publicationdateofepd")
    private Instant publicationDateOfEPD;

    @Column(name = "registrationnumber")
    private String registrationNumber;

    @Column(name = "epdformatversion")
    private String epdFormatVersion;

    @Column(name = "metadataonly")
    private Boolean metaDataOnly;

    @Column(name = "visible")
    private Boolean visible;

}