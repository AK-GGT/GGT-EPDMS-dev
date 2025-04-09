package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.service.model.IDataSetListVO;
import de.iai.ilcd.model.nodes.NetworkNode;
import de.iai.ilcd.model.utils.DistributedSearchLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class ForeignDataSetsQueryController<T extends IDataSetListVO> {

    private static final Logger logger = LogManager.getLogger(ForeignDataSetsQueryController.class);

    ForeignDataSetsQueryMonitor monitor = new ForeignDataSetsQueryMonitor();

    private List<T> results;

    private List<ForeignDataSetsQueryThread<T>> threads = new ArrayList<ForeignDataSetsQueryThread<T>>();

    private List<Boolean> signals = new ArrayList<Boolean>();

    private DistributedSearchLog log = new DistributedSearchLog();

    private Class<T> searchResultClassType;

    private MultivaluedMap<String, String> paramMap;

    public void registerThread(NetworkNode node, String baseUrl, Class<T> searchResultClassType, MultivaluedMap<String, String> paramMap) {
        ForeignDataSetsQueryThread<T> t = new ForeignDataSetsQueryThread<T>(threads.size(), this, node, baseUrl);
        threads.add(t);
        signals.add(false);
    }

    public void runThreads() {
        for (ForeignDataSetsQueryThread<T> t : threads) {
            Thread thread = new Thread(t, "thread " + t.getId());
            thread.start();
        }
    }

    public void doWait(long timeout) {
        synchronized (monitor) {
            if (!allSignalled()) {
                try {
                    monitor.wait(timeout);
                    if (!allSignalled()) {
                        for (int i = 0; i < signals.size(); i++) {
                            if (!signals.get(i)) {
                                logger.error("no signal found");
                                try {
                                    this.log.logException(this.threads.get(i).getNode(), null);
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error("operation was interrupted", e);
                }
            }
        }
    }

    public void doNotify(int id, List<T> results) {
        synchronized (monitor) {

            this.results.addAll(results);
            signals.set(id, true);

            if (allSignalled()) {
                monitor.notify();
            }
        }
    }

    private boolean allSignalled() {
        for (Boolean b : signals) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public DistributedSearchLog getLog() {
        return log;
    }

    public void setLog(DistributedSearchLog log) {
        this.log = log;
    }

    public Class<T> getSearchResultClassType() {
        return searchResultClassType;
    }

    public void setSearchResultClassType(Class<T> searchResultClassType) {
        this.searchResultClassType = searchResultClassType;
    }

    public MultivaluedMap<String, String> getParamMap() {
        return paramMap;
    }

    public void setParamMap(MultivaluedMap<String, String> paramMap) {
        this.paramMap = paramMap;
    }

    public void logException(NetworkNode node, Exception ex) {
        synchronized (monitor) {
            this.log.logException(node, ex);
        }
    }

}
