package de.iai.ilcd.model.common;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "geographicalarea")
public class GeographicalArea implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String areaCode;

    @Column(name = "areaName")
    private String name;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAreaCode() {
        return this.areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.areaCode == null) ? 0 : this.areaCode.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
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
        if (!(obj instanceof GeographicalArea)) {
            return false;
        }
        GeographicalArea other = (GeographicalArea) obj;
        if (this.areaCode == null) {
            if (other.areaCode != null) {
                return false;
            }
        } else if (!this.areaCode.equals(other.areaCode)) {
            return false;
        }
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.common.GeographicalArea[areaCode=" + this.areaCode + "]";
    }

}
