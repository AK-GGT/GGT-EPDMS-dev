package edu.kit.soda4lca.test.dao;

import de.fzk.iai.ilcd.service.client.impl.vo.dataset.ProcessDataSetVO;
import de.fzk.iai.ilcd.service.model.IDataSetListVO;
import de.iai.ilcd.model.dao.ForeignDataSetsHelper;
import de.iai.ilcd.model.dao.ForeignDataSetsHelperCache;
import de.iai.ilcd.model.dao.ProcessDao;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ForeignDataSetsHelperCacheTest {

    private ForeignDataSetsHelperCache cache = ForeignDataSetsHelperCache.INSTANCE;

    public void createEntries() {
        Map<String, String> query1 = new HashMap<String, String>();
        query1.put("name", "m");

        Map<String, String> query2 = new HashMap<String, String>();
        query2.put("name", "men");

        List<IDataSetListVO> datasetsQ1 = new ArrayList<IDataSetListVO>();
        ProcessDataSetVO qp1 = new ProcessDataSetVO();
        qp1.setName("Aluminium");
        ProcessDataSetVO qp2 = new ProcessDataSetVO();
        qp2.setName("Bitumen");
        ProcessDataSetVO qp3 = new ProcessDataSetVO();
        qp3.setName("Cement");
        datasetsQ1.add(qp1);
        datasetsQ1.add(qp2);
        datasetsQ1.add(qp3);

        List<IDataSetListVO> datasetsQ2 = new ArrayList<IDataSetListVO>();
        datasetsQ2.add(qp2);
        datasetsQ2.add(qp3);

        MultivaluedMap<String, String> paramsQ1 = new ForeignDataSetsHelper(new ProcessDao()).stripParams(query1, "search");
        MultivaluedMap<String, String> paramsQ2 = new ForeignDataSetsHelper(new ProcessDao()).stripParams(query2, "search");
        this.cache.put(paramsQ1, datasetsQ1);
        this.cache.put(paramsQ2, datasetsQ2);
    }

    @Test
    public void atestEmpty() {
        assertEquals(0, this.cache.getSize());
    }

    @Test
    public void btestCount() {
        createEntries();
        assertEquals(2, this.cache.getSize());
    }

    @Test
    public void ctestRetrieve() {
        Map<String, String> query = new HashMap<String, String>();
        query.put("name", "men");

        List<IDataSetListVO> datasets = this.cache.get(new ForeignDataSetsHelper(new ProcessDao()).stripParams(query, "search"));

//		for (IDataSetVO ds : datasets) {
//			System.out.println(ds.getName().getValue());
//		}

        assertEquals(2, datasets.size());

        assertEquals(2, this.cache.getSize());
    }

}
