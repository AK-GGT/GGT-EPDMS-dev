package de.iai.ilcd.service.glad;

import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.registry.CommonRegistrationData;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "glad_registration_data")
public class GLADRegistrationData implements CommonRegistrationData, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -324110704441714560L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    @Embedded
    private DataSetVersion version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public DataSetVersion getVersion() {
        return version;
    }

    public void setVersion(DataSetVersion version) {
        this.version = version;
    }

    public boolean isGlad() {
        return true;
    }

}
