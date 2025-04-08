package com.ggt.epdm.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "lciamethod")
public class Lciamethod  extends DataSet {
	
    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "BRANCH")
    private Integer branch;

    @Column(name = "classification_cache", length = 100)
    private String classificationCache;

    @Column(name = "IMPACTINDICATOR", length = 500)
    private String impactindicator;

    @Column(name = "MOSTRECENTVERSION")
    private Boolean mostrecentversion;

    @Column(name = "name_cache")
    private String nameCache;

    @Column(name = "PERMANENTURI")
    private String permanenturi;

    @Column(name = "RELEASESTATE")
    private String releasestate;

    @Column(name = "TYPE")
    private String type;

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