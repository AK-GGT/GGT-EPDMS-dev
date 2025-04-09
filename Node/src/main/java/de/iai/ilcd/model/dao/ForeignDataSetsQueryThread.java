package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.service.client.FailedAuthenticationException;
import de.fzk.iai.ilcd.service.client.FailedConnectionException;
import de.fzk.iai.ilcd.service.client.impl.ILCDNetworkClient;
import de.fzk.iai.ilcd.service.client.impl.vo.Result;
import de.fzk.iai.ilcd.service.model.IDataSetListVO;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.nodes.NetworkNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class ForeignDataSetsQueryThread<T extends IDataSetListVO> implements Runnable {

    private static final Logger logger = LogManager.getLogger(ForeignDataSetsQueryThread.class);

    private int id;
    private ForeignDataSetsQueryController<T> threadControl;
    private NetworkNode node = null;
    private String baseUrl;

    public ForeignDataSetsQueryThread(int id, ForeignDataSetsQueryController<T> threadControl, NetworkNode node, String baseUrl) {

        this.id = id;
        this.threadControl = threadControl;
        this.node = node;
        this.baseUrl = baseUrl;
    }

    @Override
    public void run() {
        List<T> results = new ArrayList<T>();
        try {
            // Thread.sleep( 5000 ); // for testing only!

            ILCDNetworkClient ilcdClient = new ILCDNetworkClient(baseUrl);
            ilcdClient.setOrigin(ConfigurationService.INSTANCE.getNodeInfo().getBaseURLwithResource());

            logger.debug("querying remote node at {} with params {}", baseUrl, threadControl.getParamMap());

            Result<T> resultList = (Result<T>) ilcdClient.query(threadControl.getSearchResultClassType(), (MultivaluedMap<String, String>) threadControl.getParamMap());

            logger.debug("found {} results, {} on page 1", resultList.getTotalSize(), resultList.getDataSets().size());

            for (T result : resultList.getDataSets()) {
                String href = baseUrl.concat("processes/").concat(result.getUuidAsString()).concat("?version=").concat(result.getDataSetVersion());
                logger.trace("setting href {} {}", node.getNodeId(), href);
                result.setSourceId(node.getNodeId());
                result.setHref(href);
                results.add(result);
            }
        } catch (FailedConnectionException ex) {
            logger.error("connection to {} failed", baseUrl, ex);
        } catch (FailedAuthenticationException ex) {
            logger.error("authentication failed", ex);
        } catch (ProcessingException ex) {
            threadControl.logException(node, ex);
        } catch (Exception ex) {
            logger.error("Error querying foreign node", ex);
        }

        threadControl.doNotify(this.id, results);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public NetworkNode getNode() {
        return node;
    }

    public void setNode(NetworkNode node) {
        this.node = node;
    }

}
