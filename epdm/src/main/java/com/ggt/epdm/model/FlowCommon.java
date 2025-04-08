package com.ggt.epdm.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "flow_common")
public class FlowCommon extends DataSet {
	
    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "flow_object_type", length = 31)
    private String flowObjectType;

    @Column(name = "BRANCH")
    private Integer branch;

    @Column(name = "CASNUMBER")
    private String casnumber;

    @Column(name = "ecnumber", length = 9)
    private String ecnumber;

    @Column(name = "classification_cache", length = 100)
    private String classificationCache;

    @Column(name = "MOSTRECENTVERSION")
    private Boolean mostrecentversion;

    @Column(name = "name_cache")
    private String nameCache;

    @Column(name = "PERMANENTURI")
    private String permanenturi;

    @Column(name = "referenceProperty_cache", length = 20)
    private String referencepropertyCache;

    @Column(name = "referencePropertyUnit_cache", length = 10)
    private String referencepropertyunitCache;

    @Column(name = "RELEASESTATE")
    private String releasestate;

    @Column(name = "SUMFORMULA")
    private String sumformula;

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

    @Column(name = "visible")
    private Boolean visible;

}