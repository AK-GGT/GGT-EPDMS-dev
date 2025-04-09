package de.iai.ilcd.db.migrations;

import de.iai.ilcd.xml.read.CommonConstructsReader;
import de.iai.ilcd.xml.read.DataSetParsingHelper;
import de.iai.ilcd.xml.read.FlowReader;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.xml.JDOMParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Initialize datastock display properties with ascending ordinal values to
 * preserve original order
 */
public class V8_1__ExtractECNumber implements SpringJdbcMigration {

    /**
     * Logger to use
     */
    private static final Logger LOGGER = LogManager.getLogger(V8_1__ExtractECNumber.class);

    private final String sqlSelectFlowIds = "SELECT ID FROM `flow_common`";

    private final String sqlSelectFlowXmlId = "SELECT XMLFILE_ID FROM `flow_common` WHERE ID = ?";

    private final String sqlUpdateFlow = "UPDATE `flow_common` SET `ecnumber` = ? WHERE ID = ?";

    private InputStream rawXml;

    private JdbcTemplate jdbcTemplate;

    private JDOMParser parser = new JDOMParser();

    private List<Tupel> ecNumbers = new ArrayList<Tupel>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {

        this.jdbcTemplate = jdbcTemplate;

        this.parser.setValidating(false);

        List<Long> flowIds = jdbcTemplate.queryForList(sqlSelectFlowIds, Long.class);

        Long start = System.currentTimeMillis();

        for (Long flowId : flowIds) {

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("flow with id " + flowId);

            Long flowXmlId = jdbcTemplate.queryForObject(sqlSelectFlowXmlId, Long.class, flowId);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("xmlfile id " + flowXmlId);

            getXml(jdbcTemplate, flowXmlId);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("extract EC number:");

            String ecNumber = extractECNumber();

            if (LOGGER.isDebugEnabled())
                LOGGER.debug(ecNumber);

            this.ecNumbers.add(new Tupel(flowXmlId, ecNumber));
        }

        insertBatch(this.ecNumbers);

        Long duration = System.currentTimeMillis() - start;

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("EC number extraction completed in " + duration + " ms");
        }
    }

    public void insertBatch(final List<Tupel> ecNumbers) {

        jdbcTemplate.batchUpdate(sqlUpdateFlow, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Tupel t = ecNumbers.get(i);
                ps.setString(1, t.getEcNumber());
                ps.setLong(2, t.getDatasetId());
            }

            @Override
            public int getBatchSize() {
                return ecNumbers.size();
            }
        });
    }

    private String extractECNumber() {

        Object doc = parser.parseXML(this.rawXml);
        JXPathContext context = JXPathContext.newContext(doc);
        context.setLenient(true);
        context.registerNamespace("common", "http://lca.jrc.it/ILCD/Common");
        context.registerNamespace("ilcd", "http://lca.jrc.it/ILCD/Flow");

        DataSetParsingHelper parserHelper = new DataSetParsingHelper(context);
        CommonConstructsReader commonReader = new CommonConstructsReader(parserHelper);
        FlowReader flowReader = new FlowReader();
        flowReader.setParserHelper(parserHelper);
        flowReader.setCommonReader(commonReader);

        String ecNumber = flowReader.getECNumber();

        return ecNumber;
    }

    private void getXml(JdbcTemplate jdbcTemplate, Long id) {
        final String sqlSelectXml = "SELECT COMPRESSEDCONTENT FROM `xmlfile` WHERE ID = ? ";

        LobHandler lobHandler = new DefaultLobHandler();

        jdbcTemplate.query(sqlSelectXml, new Object[]{id}, new AbstractLobStreamingResultSetExtractor<Void>() {
            protected void streamData(ResultSet rs) throws SQLException, IOException {
                V8_1__ExtractECNumber.this.rawXml = new GZIPInputStream(
                        lobHandler.getBlobAsBinaryStream(rs, 1));
            }
        });
    }

    public class Tupel {
        private Long datasetId;

        private String ecNumber;

        public Tupel(Long id, String ecNumber) {
            this.datasetId = id;
            this.ecNumber = ecNumber;
        }

        public Long getDatasetId() {
            return datasetId;
        }

        public void setDatasetId(Long datasetId) {
            this.datasetId = datasetId;
        }

        public String getEcNumber() {
            return ecNumber;
        }

        public void setEcNumber(String ecNumber) {
            this.ecNumber = ecNumber;
        }
    }
}
