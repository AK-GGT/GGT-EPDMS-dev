package com.ggt.epdm.model;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class DataSet {
	
    @Column(name = "UUID")
    private String uuid;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "XMLFILE_ID")
    private Xmlfile xmlfile;

}
