package com.ggt.epdm.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "portal_exported_xml")
public class PortalExportedXml {
    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "XMLFILE_ID")
    private Xmlfile xmlfile;

    @Column(name = "IS_EXPORTED")
    private Boolean isExported;

    @Column(name = "UUID")
    private String uuid;

    @Column(name = "DATA_SET_TYPE", nullable = false, length = 100)
    private String dataSetType;

}