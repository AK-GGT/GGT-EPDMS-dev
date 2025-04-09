package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.registry.DatasetDeregistrationReason;
import org.springframework.stereotype.Repository;

@Repository(value = "datasetDeregistrationReasonDao")
public class DatasetDeregistrationReasonDaoImpl extends GenericDAOImpl<DatasetDeregistrationReason, Long> implements DatasetDeregistrationReasonDao {

}
