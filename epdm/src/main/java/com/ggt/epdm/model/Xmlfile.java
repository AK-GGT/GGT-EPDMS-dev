package com.ggt.epdm.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "xmlfile")
public class Xmlfile {
    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "COMPRESSEDCONTENT")
    private byte[] compressedcontent;

}