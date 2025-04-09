package eu.europa.ec.jrc.lca.registry.util;

import eu.europa.ec.jrc.lca.registry.domain.DataSetAuditLog;
import eu.europa.ec.jrc.lca.registry.domain.Node;
import eu.europa.ec.jrc.lca.registry.domain.NodeAuditLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileLogger {
    public static final Logger LOGGER = LogManager
            .getLogger(FileLogger.class);

    @Autowired
    private ILCDMessageSource messageSource;

    public void log(NodeAuditLog nodeAuditLog, Node node) {
        if (nodeAuditLog.getDatasetLog().isEmpty()) {
            logNodeOperation(nodeAuditLog, node);
        } else {
            logNodeWithDatasetsOperation(nodeAuditLog, node);
        }
    }

    private void logNodeOperation(NodeAuditLog nodeAuditLog, Node node) {
        StringBuilder sb = new StringBuilder();
        sb.append(messageSource.getTranslation(nodeAuditLog.getOperationType().name()));
        sb.append(", node id: ").append(node.getNodeId());
        LOGGER.info(sb.toString());
    }

    private void logNodeWithDatasetsOperation(NodeAuditLog nodeAuditLog, Node node) {
        StringBuilder sb = new StringBuilder();
        sb.append(messageSource.getTranslation(nodeAuditLog.getOperationType().name()));
        sb.append(", node id: ").append(node.getNodeId());
        for (DataSetAuditLog dsal : nodeAuditLog.getDatasetLog()) {
            sb.append("<br>").append("Data set UUID: ").append(dsal.getUuid()).append(", version: ").append(dsal.getVersion());
        }
        LOGGER.info(sb.toString());
    }
}
