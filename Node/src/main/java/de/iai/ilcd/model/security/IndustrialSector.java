package de.iai.ilcd.model.security;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "industrialsector")
public class IndustrialSector implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String sector;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSector() {
        return this.sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.sector == null) ? 0 : this.sector.hashCode());
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
        if (!(obj instanceof IndustrialSector)) {
            return false;
        }
        IndustrialSector other = (IndustrialSector) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.sector == null) {
            if (other.sector != null) {
                return false;
            }
        } else if (!this.sector.equals(other.sector)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.security.IndustrialSector[sector=" + this.sector + "]";
    }

}
