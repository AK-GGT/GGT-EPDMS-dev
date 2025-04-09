package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.registry.RegistrationData;
import org.springframework.stereotype.Repository;

@Repository(value = "registrationDataDao")
public class RegistrationDataDaoImpl extends GenericDAOImpl<RegistrationData, Long> implements RegistrationDataDao {

}
