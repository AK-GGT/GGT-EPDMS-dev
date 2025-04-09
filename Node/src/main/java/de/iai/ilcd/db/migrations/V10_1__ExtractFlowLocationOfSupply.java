package de.iai.ilcd.db.migrations;

import de.fzk.iai.ilcd.service.model.common.ILString;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
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
public class V10_1__ExtractFlowLocationOfSupply implements SpringJdbcMigration {

    /**
     * Logger to use
     */
    private static final Logger LOGGER = LogManager.getLogger(V10_1__ExtractFlowLocationOfSupply.class);

    private final String sqlSelectProductFlowIds = "SELECT ID FROM `flow_common` WHERE `flow_object_type` = 'p'";

    private final String sqlSelectFlowXmlId = "SELECT XMLFILE_ID FROM `flow_common` WHERE ID = ?";

    private final String sqlInsertValue = "INSERT INTO `flow_locationofsupply` (`flow_id`, `value`, `lang`) VALUES ( ?, ?, ? )";

    private InputStream rawXml;

    private JdbcTemplate jdbcTemplate;

    private JDOMParser parser = new JDOMParser();

    private List<Tripel> locationTableEntries = new ArrayList<Tripel>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {

        this.jdbcTemplate = jdbcTemplate;

        this.parser.setValidating(false);

        List<Long> flowIds = jdbcTemplate.queryForList(sqlSelectProductFlowIds, Long.class);

        Long start = System.currentTimeMillis();

        for (Long flowId : flowIds) {

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("flow with id " + flowId);

            Long flowXmlId = jdbcTemplate.queryForObject(sqlSelectFlowXmlId, Long.class, flowId);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("xmlfile id " + flowXmlId);

            getXml(jdbcTemplate, flowXmlId);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("extract location of supply");

            IMultiLangString los = extractLOS();

            if (los != null) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(los.getDefaultValue());

                for (ILString l : los.getLStrings()) {
                    this.locationTableEntries.add(new Tripel(flowId, l.getLang(), l.getValue()));
                }
            }
        }

        if (LOGGER.isDebugEnabled())
            LOGGER.debug(this.locationTableEntries.size() + " entries");

        insertBatch(this.locationTableEntries);

        Long duration = System.currentTimeMillis() - start;

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("flow locationofsupply extraction completed in " + duration + " ms");
        }
    }

    public void insertBatch(final List<Tripel> locations) {

        jdbcTemplate.batchUpdate(sqlInsertValue, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Tripel t = locations.get(i);
                ps.setLong(1, t.getDatasetId());
                ps.setString(2, t.getValue());
                ps.setString(3, t.getLang());
            }

            @Override
            public int getBatchSize() {
                return locations.size();
            }
        });
    }

    private IMultiLangString extractLOS() {

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

        IMultiLangString los = flowReader.getLocationOfSupply();

        return los;
    }

    private void getXml(JdbcTemplate jdbcTemplate, Long id) {
        final String sqlSelectXml = "SELECT COMPRESSEDCONTENT FROM `xmlfile` WHERE ID = ? ";

        LobHandler lobHandler = new DefaultLobHandler();

        jdbcTemplate.query(sqlSelectXml, new Object[]{id}, new AbstractLobStreamingResultSetExtractor<Void>() {
            protected void streamData(ResultSet rs) throws SQLException, IOException {
                V10_1__ExtractFlowLocationOfSupply.this.rawXml = new GZIPInputStream(
                        lobHandler.getBlobAsBinaryStream(rs, 1));
            }
        });
    }

    public class Tripel {
        private Long datasetId;

        private String lang;

        private String value;

        public Tripel(Long id, String lang, String value) {
            this.datasetId = id;
            this.lang = lang;
            this.value = value;
        }

        public Long getDatasetId() {
            return datasetId;
        }

        public void setDatasetId(Long datasetId) {
            this.datasetId = datasetId;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
