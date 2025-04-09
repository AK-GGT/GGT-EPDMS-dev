package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.GlobalReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class GlobalReferenceDao {

    private static final Logger LOGGER = LogManager.getLogger(GlobalReferenceDao.class);

    public GlobalReferenceDao() {
    }

    public DataSet getByReference(IGlobalReference ref) {
        DataSet ds = null;
        for (DataSetDao<?, ?, ?> dao : getDaos(ref)) {
            ds = dao.getByUuidAndVersion(ref.getRefObjectId(), ref.getVersionAsString());
            if (ds != null)
                break;
        }
        return ds;
    }

    public DataSet getByUuidAndVersion(String uuid, String version, GlobalReferenceTypeValue type) {
        DataSet ds = null;
        for (DataSetDao<?, ?, ?> dao : DataSetDao.getDaosForType(type)) {
            ds = dao.getByUuidAndVersion(uuid, version);
            if (ds != null)
                break;
        }
        return ds;
    }

    public Set<DataSet> getDependencies(GlobalReference ref, DependenciesMode mode) {
        DataSet dataset = getByReference(ref);
        DataSetDao<?, ?, ?> dao = dataset.getCorrespondingDSDao();
        return dao.getDependencies(dataset, mode);
    }

    private Set<DataSetDao<?, ?, ?>> getDaos(IGlobalReference ref) {
        GlobalReferenceTypeValue type = ref.getType();
        if (type == null) {
            LOGGER.trace("Guessing Reference type from it's URI");
            type = probeTypeFromURI(ref);
        }
        return DataSetDao.getDaosForType(type);
    }


    private GlobalReferenceTypeValue probeTypeFromURI(IGlobalReference ref) {
        String URI = ref.getUri().toLowerCase().trim();

        List<Function<String, GlobalReferenceTypeValue>> types = Arrays.asList(
                u -> u.contains("process") ? GlobalReferenceTypeValue.PROCESS_DATA_SET : null,
                u -> u.contains("source") ? GlobalReferenceTypeValue.SOURCE_DATA_SET : null,
                u -> u.contains("property") ? GlobalReferenceTypeValue.FLOW_PROPERTY_DATA_SET : null, // look for flow property first
                u -> u.contains("flow") ? GlobalReferenceTypeValue.FLOW_DATA_SET : null,
                u -> u.contains("unit") ? GlobalReferenceTypeValue.UNIT_GROUP_DATA_SET : null,
                u -> u.contains("contact") ? GlobalReferenceTypeValue.CONTACT_DATA_SET : null,
                u -> u.contains("lcia") ? GlobalReferenceTypeValue.LCIA_METHOD_DATA_SET : null,
                u -> u.contains("life") ? GlobalReferenceTypeValue.LIFE_CYCLE_DATA_SET : null,
                u -> u.contains("external") ? GlobalReferenceTypeValue.OTHER_EXTERNAL_FILE : null);

        GlobalReferenceTypeValue erg = null;
        for (Function<String, GlobalReferenceTypeValue> f : types) {
            erg = f.apply(URI);
            if (erg != null)
                break;
        }
        return erg;
    }

}
