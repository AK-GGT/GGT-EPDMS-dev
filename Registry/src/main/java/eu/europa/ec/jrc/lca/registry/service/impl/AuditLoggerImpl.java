package eu.europa.ec.jrc.lca.registry.service.impl;

import eu.europa.ec.jrc.lca.registry.dao.AuditLogDao;
import eu.europa.ec.jrc.lca.registry.domain.*;
import eu.europa.ec.jrc.lca.registry.service.AuditLogger;
import eu.europa.ec.jrc.lca.registry.util.FileLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Transactional
@Service("auditLogger")
public class AuditLoggerImpl implements AuditLogger {

    @Autowired
    private AuditLogDao auditLogDao;

    @Autowired
    private FileLogger fileLogger;

    @Override
    public void log(Node node, NodeOperation operation) {
        NodeAuditLog nal = new NodeAuditLog(node);
        nal.setOperationType(operation);
        auditLogDao.saveOrUpdate(nal);
        fileLogger.log(nal, node);
    }

    @Override
    public void log(List<DataSet> dataSets, Node node, NodeOperation operation) {
        NodeAuditLog nal = new NodeAuditLog(node);
        nal.setOperationType(operation);
        for (DataSet ds : dataSets) {
            nal.getDatasetLog().add(new DataSetAuditLog(ds));
        }
        auditLogDao.saveOrUpdate(nal);
        fileLogger.log(nal, node);
    }

    @Override
    public void log(List<DataSet> dataSets, NodeOperation operation) {
        Map<Node, List<DataSet>> groupped = groupByNode(dataSets);
        for (Entry<Node, List<DataSet>> e : groupped.entrySet()) {
            log(e.getValue(), e.getKey(), operation);
        }
    }

    private Map<Node, List<DataSet>> groupByNode(List<DataSet> dataSets) {
        Map<Node, List<DataSet>> groupped = new HashMap<Node, List<DataSet>>();
        for (DataSet ds : dataSets) {
            if (groupped.get(ds.getNode()) == null) {
                groupped.put(ds.getNode(), new ArrayList<DataSet>());
            }
            groupped.get(ds.getNode()).add(ds);
        }
        return groupped;
    }
}
