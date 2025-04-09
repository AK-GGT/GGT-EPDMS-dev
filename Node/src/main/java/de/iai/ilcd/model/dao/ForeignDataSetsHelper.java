package de.iai.ilcd.model.dao;


import de.fzk.iai.ilcd.service.client.DatasetNotFoundException;
import de.fzk.iai.ilcd.service.client.FailedAuthenticationException;
import de.fzk.iai.ilcd.service.client.FailedConnectionException;
import de.fzk.iai.ilcd.service.client.ILCDServiceClientException;
import de.fzk.iai.ilcd.service.client.impl.ILCDNetworkClient;
import de.fzk.iai.ilcd.service.model.IDataSetListVO;
import de.fzk.iai.ilcd.service.model.IDataSetVO;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.nodes.NetworkNode;
import de.iai.ilcd.model.utils.DistributedSearchLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.tools.generic.ValueParser;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author clemens.duepmeier
 */
public class ForeignDataSetsHelper {

    public static final int DIST_MAX_RESULTS = 10000;

    private static final String REST_SERVLET_PREFIX = "resource/";

    private static final Logger logger = LogManager.getLogger(ForeignDataSetsHelper.class);

    private boolean cachedResult = false;

    private DataSetDao<?, ?, ?> dao;

    public ForeignDataSetsHelper(DataSetDao<?, ?, ?> dao) {
        this.dao = dao;
    }

    @SuppressWarnings("unchecked")
    public <T extends IDataSetListVO> List<T> foreignSearch(Class<T> searchResultClassType, ValueParser params, DistributedSearchLog log) {
        logger.debug("searching foreign nodes with params {}", params);

        List<T> results = new ArrayList<T>();

        // do not include these parameters in cache lookup key

        MultivaluedMap<String, String> paramMap = stripParams(params, "search", "distributed", "virtual", "registeredIn", "pageSize", "startIndex", "sortBy", "sortOrder");

        // cache lookup - is result already in cache?

        MultivaluedMap<String, String> cacheKey = new MultivaluedHashMap<>(paramMap);
        if (ForeignDataSetsHelperCache.INSTANCE.hasItem(cacheKey)) {
            this.cachedResult = true;
            return (List<T>) ForeignDataSetsHelperCache.INSTANCE.get(cacheKey);
        }

        this.cachedResult = false;

        // limit to 10000 results from each node (for now)
        paramMap.putSingle("pageSize", String.valueOf(DIST_MAX_RESULTS));

        NetworkNodeDao nodeDao = new NetworkNodeDao();
        if (logger.isDebugEnabled())
            logger.debug(nodeDao.getAllCount() + " nodes in nodes list");

        List<NetworkNode> nodes = null;
        if (params.getBoolean("virtual") == null || Boolean.FALSE.equals(params.getBoolean("virtual"))) {
            nodes = nodeDao.getRemoteNetworkNodesFromRegistry(params.getString("registeredIn"));
            logger.trace("found {} foreign nodes from registry", nodes.size());
        } else {
            nodes = nodeDao.getRemoteNetworkNodes();
            logger.trace("found {} foreign nodes", nodes.size());
        }

        // if nodeid params are given, query only these nodes
        String[] includedNodes = params.getStrings("nodeid");
        if (!(includedNodes == null || includedNodes.length == 0)) {
            List<String> includedNodeIdsList = Arrays.asList(includedNodes);

            Iterator<NetworkNode> i = nodes.iterator();
            while (i.hasNext()) {
                NetworkNode node = i.next();
                if (!includedNodeIdsList.contains(node.getNodeId()))
                    i.remove();
            }
        }

        // do not propagate nodeid param
        MultivaluedMap<String, String> queryParamMap = stripParams(paramMap, "nodeid");

        logger.info("querying {} foreign nodes", nodes.size());

        logger.debug("param  keys:  {}", queryParamMap.keySet());
        logger.debug("param values: {}", queryParamMap.values());

        ForeignDataSetsQueryController<T> threadControl = new ForeignDataSetsQueryController<T>();
        threadControl.setResults(results);
        threadControl.setLog(log);
        threadControl.setSearchResultClassType(searchResultClassType);
        threadControl.setParamMap(queryParamMap);

        for (NetworkNode node : nodes) {

            logger.debug("querying node {}", node.getBaseUrl());

            String baseUrl = node.getBaseUrl();
            if (!node.getBaseUrl().endsWith("/")) {
                baseUrl = baseUrl + "/";
            }

            if (!baseUrl.endsWith(REST_SERVLET_PREFIX))
                baseUrl += REST_SERVLET_PREFIX;

            if (StringUtils.isNotBlank(node.getDataStockID()))
                baseUrl = baseUrl + "datastocks/" + node.getDataStockID() + "/";

            threadControl.registerThread(node, baseUrl, searchResultClassType, queryParamMap);
        }

        threadControl.runThreads();
        threadControl.doWait(ConfigurationService.INSTANCE.getSearchDistTimeout());

        logger.trace("proxy mode is {}", ConfigurationService.INSTANCE.isProxyMode());

        // in proxy mode, it is safe to do de-duping with only the remote results
        if (ConfigurationService.INSTANCE.isProxyMode()) {
            logger.trace(" {} results before deduping", results.size());
            results = dao.processDupes((List<IDataSetListVO>) results);
            logger.trace(" {} results after deduping", results.size());
        }

        // put results in cache
        ForeignDataSetsHelperCache.INSTANCE.put(cacheKey, (List<IDataSetListVO>) results);

        return results;
    }

    /**
     * Strip given parameters from ValueParser instance, returning a MultivaluedMap
     *
     * @param params
     * @param toStrip
     * @return
     */
    public MultivaluedMap<String, String> stripParams(ValueParser params, String... toStrip) {
        MultivaluedMap<String, String> paramMap = new MultivaluedHashMap<>();
        for (Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();

            if (matches(key, toStrip))
                continue;
            if (key.equals("format")) {
                // fetch search results from upstream nodes always as XML
                paramMap.putSingle(key, "xml");
                continue;
            }
            String[] values = params.getStrings(key);
            if (values != null && values.length > 0) {
                List<String> valueList = new ArrayList<String>();
                for (String value : values) {
                    valueList.add(value);
                }
                paramMap.put(key, valueList);
            } else {
                String singleValue = params.getString(key);
                if (singleValue != null && !singleValue.isEmpty()) {
                    paramMap.putSingle(key, singleValue);
                }
            }

        }
        return paramMap;
    }

    /**
     * Strip given parameters from Map instance, returning a MultivaluedMap
     *
     * @param params
     * @param toStrip
     * @return
     */

    public MultivaluedMap<String, String> stripParams(Map<String, String> params, String... toStrip) {

        MultivaluedMap<String, String> paramMap = new MultivaluedHashMap<>();
        for (Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();

            if (matches(key, toStrip))
                continue;
            if (key.equals("format")) {
                // fetch search results from upstream nodes always as XML
                paramMap.putSingle(key, "xml");
                continue;
            }
            String singleValue = params.get(key);
            if (singleValue != null && !singleValue.isEmpty()) {
                paramMap.putSingle(key, singleValue);
            }

        }
        return paramMap;
    }

    private boolean matches(String toMatch, String... toStrip) {
        for (String s : toStrip)
            if (toMatch.equals(s))
                return true;
        return false;
    }

    /**
     * Strip given parameters from MultivaluedMap instance, returning a MultivaluedMap
     *
     * @param params
     * @param toStrip
     * @return
     */

    public MultivaluedMap<String, String> stripParams(MultivaluedMap<String, String> params, String... toStrip) {

        MultivaluedMap<String, String> paramMap = new MultivaluedHashMap<>();
        for (Entry<String, List<String>> entry : params.entrySet()) {
            String key = entry.getKey();

            if (matches(key, toStrip))
                continue;
            if (key.equals("format")) {
                // fetch search results from upstream nodes always as XML
                paramMap.putSingle(key, "xml");
                continue;
            }
            paramMap.put(key, entry.getValue());
        }
        return paramMap;
    }

    public <T extends IDataSetVO> T getForeignDataSet(Class<T> dataSetClassType, String nodeShortName, String uuid, Long registryId) {

        // we need node name and uuid
        if (nodeShortName == null || uuid == null) {
            return null;
        }
        logger.info("get foreign process from node {} with uuid {}", nodeShortName, uuid);
        NetworkNodeDao nodeDao = new NetworkNodeDao();
        NetworkNode node = nodeDao.getNetworkNode(nodeShortName, registryId);
        if (node == null) {
            return null;
        }

        String baseUrl = node.getBaseUrl();
        if (!node.getBaseUrl().endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        if (!baseUrl.endsWith(REST_SERVLET_PREFIX))
            baseUrl += REST_SERVLET_PREFIX;

        T dataSet = null;

        try {
            ILCDNetworkClient targetConnection = new ILCDNetworkClient(baseUrl);
            targetConnection.setOrigin(ConfigurationService.INSTANCE.getNodeInfo().getBaseURLwithResource());

            dataSet = (T) targetConnection.getDataSetVO(dataSetClassType, uuid);
            // attach node information
            // if (dataSet.getSourceId() == null)
            if (logger.isTraceEnabled()) {
                logger.trace("setting sourceId to {} and href to {}", node.getNodeId(), baseUrl.concat("processes/").concat(uuid));
            }
            dataSet.setSourceId(node.getNodeId());
            dataSet.setHref(baseUrl.concat("processes/").concat(uuid));
            // process = new Process(poProcess);
        } catch (FailedConnectionException ex) {
            logger.error("Connection to {} failed", baseUrl, ex);
            return null;
        } catch (FailedAuthenticationException ex) {
            logger.error("Authentication to {} failed", baseUrl, ex);
            return null;
        } catch (IOException ex) {
            logger.error("There were some I/O-errors accessing a dataset from {}", baseUrl, ex);
        } catch (DatasetNotFoundException ex) {
            logger.error("Dataset with uuid {} not found", uuid, ex);
            return null;
        } catch (ILCDServiceClientException e) {
            logger.error("other error", e);
        }

        return dataSet;
    }

    public boolean isCachedResult() {
        return cachedResult;
    }

    public void setCachedResult(boolean cachedResult) {
        this.cachedResult = cachedResult;
    }

}
