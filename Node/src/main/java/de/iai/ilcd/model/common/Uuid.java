package de.iai.ilcd.model.common;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * @author clemens.duepmeier
 */
@Embeddable
public class Uuid implements Serializable {

    private static final long serialVersionUID = -4211695348554871155L;

    @Basic
    @Column(name = "UUID")
    private String uuid;

    public Uuid() {
        this.uuid = UUID.randomUUID().toString();
    }

    public Uuid(String uuid) {
        // check UUID
        UUID.fromString(uuid);
        this.uuid = uuid;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        // check uuid
        UUID.fromString(uuid);
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Uuid other = (Uuid) obj;
        return Objects.equals(this.uuid, other.uuid);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.uuid != null ? this.uuid.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return getUuid();
    }

}
