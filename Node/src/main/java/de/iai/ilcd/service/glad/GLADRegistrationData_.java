package de.iai.ilcd.service.glad;

import de.iai.ilcd.model.common.DataSetVersion;

import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel(de.iai.ilcd.service.glad.GLADRegistrationData.class)
public class GLADRegistrationData_ {

    public static volatile SingularAttribute<GLADRegistrationData, Long> id;

    public static volatile SingularAttribute<GLADRegistrationData, String> uuid;

    public static volatile SingularAttribute<GLADRegistrationData, DataSetVersion> version;

}
