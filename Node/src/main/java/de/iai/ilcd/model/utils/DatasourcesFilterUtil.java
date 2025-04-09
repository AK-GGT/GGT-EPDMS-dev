package de.iai.ilcd.model.utils;

import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.iai.ilcd.model.dao.SourceDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class DatasourcesFilterUtil {

    private SourceDao sd = new SourceDao();

    public List<IGlobalReference> filterDataSources(List<IGlobalReference> input) {
        List<IGlobalReference> ds = new ArrayList<IGlobalReference>();
        if (input != null) {
            HashSet<String> sources = sd.getDatabasesUuids();

            for (IGlobalReference g : input) {
                if (sources.contains(g.getRefObjectId())) {
                    ds.add(g);
                }
            }
        }
        Collections.sort(ds, new DataSourceGlobalReferenceComparator());
        return ds;
    }
}
