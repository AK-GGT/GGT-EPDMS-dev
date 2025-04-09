package edu.kit.soda4lca.test.dao;

import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.ForeignDataSetsHelper;
import de.iai.ilcd.model.dao.ProcessDao;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DataSetDaoTest {

    @Test
    public void createEntries() {
        Map<String, String> query1 = new HashMap<String, String>();
        query1.put("name", "m");
        query1.put("search", "true");
        query1.put("distributed", "true");
        query1.put("pageSize", "10");
        query1.put("startIndex", "0");

        MultivaluedMap<String, String> paramsQ1 = new ForeignDataSetsHelper(new ProcessDao()).stripParams(query1, "search", "distributed", "virtual", "pageSize", "startIndex", "sortBy", "sortOrder");

        assertNull(paramsQ1.get("search"));
        assertNull(paramsQ1.get("distributed"));
        assertNull(paramsQ1.get("pageSize"));
        assertNull(paramsQ1.get("startIndex"));
    }


    @Test
    public void MultiContainClauseWithContext() {
        Map<String, Object> paramValues = new LinkedHashMap<String, Object>();
        Map<String, Object> controlMap = new LinkedHashMap<String, Object>();
        String userSearch = "report 2005 steel";
        String[] fields = {"name", "desc"};
        String multipleFields0 = "(foo.bar.name LIKE :foo.barnamedesc0) OR (foo.bar.desc LIKE :foo.barnamedesc0)";
        String multipleFields1 = "(foo.bar.name LIKE :foo.barnamedesc1) OR (foo.bar.desc LIKE :foo.barnamedesc1)";
        String multipleFields2 = "(foo.bar.name LIKE :foo.barnamedesc2) OR (foo.bar.desc LIKE :foo.barnamedesc2)";
        String whereClauseMultipleFields = "((" + multipleFields0 + ") AND (" + multipleFields1 + ") AND (" + multipleFields2 + "))";

        String whereClause0 = "(foo.bar.name LIKE :foo.barname0)";
        String whereClause1 = "(foo.bar.name LIKE :foo.barname1)";
        String whereClause2 = "(foo.bar.name LIKE :foo.barname2)";
        String whereClause = "((" + whereClause0 + ") AND (" + whereClause1 + ") AND (" + whereClause2 + "))";

        controlMap.put("foo.barnamedesc0", "%report%");
        controlMap.put("foo.barnamedesc1", "%2005%");
        controlMap.put("foo.barnamedesc2", "%steel%");
        controlMap.put("foo.barname0", "%report%");
        controlMap.put("foo.barname1", "%2005%");
        controlMap.put("foo.barname2", "%steel%");

        assertEquals(whereClauseMultipleFields, DataSetDao.splitMultiContainClause("foo.bar", fields, "OR", userSearch, "AND", paramValues));
        assertEquals(whereClause, DataSetDao.splitMultiContainClause("foo.bar", "name", userSearch, "AND", paramValues));
        assertEquals(controlMap, paramValues);
    }

    @Test
    public void MultiContainClause() {
        Map<String, Object> paramValues = new LinkedHashMap<String, Object>();
        Map<String, Object> controlMap = new LinkedHashMap<String, Object>();

        String userSearch = "report 2005 steel";
        String[] fields = {"name", "desc"};
        String whereClauseMultipleFields0 = "(name LIKE :namedesc0) OR (desc LIKE :namedesc0)";
        String whereClauseMultipleFields1 = "(name LIKE :namedesc1) OR (desc LIKE :namedesc1)";
        String whereClauseMultipleFields2 = "(name LIKE :namedesc2) OR (desc LIKE :namedesc2)";
        String whereClauseMultipleFields = "((" + whereClauseMultipleFields0 + ") AND (" + whereClauseMultipleFields1 + ") AND (" + whereClauseMultipleFields2 + "))";

        String whereClause0 = "(name LIKE :name0)";
        String whereClause1 = "(name LIKE :name1)";
        String whereClause2 = "(name LIKE :name2)";
        String whereClause = "((" + whereClause0 + ") AND (" + whereClause1 + ") AND (" + whereClause2 + "))";

        controlMap.put("barnamedesc0", "%report%");
        controlMap.put("barnamedesc1", "%2005%");
        controlMap.put("barnamedesc2", "%steel%");
        controlMap.put("barname0", "%report%");
        controlMap.put("barname1", "%2005%");
        controlMap.put("barname2", "%steel%");

        assertEquals(whereClauseMultipleFields, DataSetDao.splitMultiContainClause(fields, "OR", userSearch, "AND", paramValues));
        assertEquals(whereClause, DataSetDao.splitMultiContainClause("name", userSearch, "AND", paramValues));
    }

}
