package de.iai.ilcd.model.registry;

import de.iai.ilcd.model.common.DataSetVersion;

public interface CommonRegistrationData {

    public Long getId();

    public void setId(Long id);

    public String getUuid();

    public void setUuid(String uuid);

    public DataSetVersion getVersion();

    public void setVersion(DataSetVersion version);

    public boolean isGlad();

}
