package de.iai.ilcd.db.migrations;

import de.fzk.iai.ilcd.service.client.impl.vo.types.common.GlobalReferenceType;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.common.ILString;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.xml.read.CommonConstructsReader;
import de.iai.ilcd.xml.read.DataSetParsingHelper;
import de.iai.ilcd.xml.read.LCIAMethodReader;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.xml.JDOMParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.jdom.Element;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Initialize datastock display properties with ascending ordinal values to
 * preserve original order
 */
public class V15_1__ExtractReferenceQuantity implements SpringJdbcMigration {

    /**
     * Logger to use
     */
    private static final Logger LOGGER = LogManager.getLogger(V15_1__ExtractReferenceQuantity.class);

    private final String sqlSelectMethodIds = "SELECT ID FROM `lciamethod`";

    private final String sqlSelectMethodXmlId = "SELECT XMLFILE_ID FROM `lciamethod` WHERE ID = ?";

    private final String sqlInsertGlobalReference = "INSERT INTO `globalreference` (`TYPE`, `URI`, `UUID`, `MAJORVERSION`, `MINORVERSION`, `SUBMINORVERSION`, `VERSION`) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private final String sqlUpdateMethod = "UPDATE `lciamethod` SET referencequantity_ID = ? WHERE ID = ?";

    private InputStream rawXml;

    private JdbcTemplate jdbcTemplate;

    private JDOMParser parser = new JDOMParser();

    private List<Map<String, Long>> relationships = new ArrayList<Map<String, Long>>();

    private List<Map<String, Object>> shortDescriptions = new ArrayList<Map<String, Object>>();

    private Long extractionTime = 0L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {

        this.jdbcTemplate = jdbcTemplate;

        this.parser.setValidating(false);

        List<Long> methodIds = jdbcTemplate.queryForList(sqlSelectMethodIds, Long.class);

        Long start = System.currentTimeMillis();

        for (Long methodId : methodIds) {

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("method with id " + methodId);

            Long processXmlId = jdbcTemplate.queryForObject(sqlSelectMethodXmlId, Long.class, methodId);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("xmlfile id " + processXmlId);

            getXml(jdbcTemplate, processXmlId);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("extract reference quantity");

            IGlobalReference reference = extractReferenceQuantity();

            if (reference != null)
                persistReference(reference, methodId);
        }

        updateMethods(this.relationships);

        persistShortDescriptions();

        Long duration = System.currentTimeMillis() - start;

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("reference quantity extraction completed in " + duration + " ms");
            LOGGER.info("XML parsing took " + this.extractionTime + " ms");
        }
    }

    private void persistReference(IGlobalReference referenceFlowProperty, Long methodId) {
        /*
         * these are the steps that are carried out:
         *
         * INSERT INTO globalreference
         *
         * INSERT INTO globalreference_shortdescription
         *
         * the last one is executed as batch insert
         */

        if (LOGGER.isDebugEnabled()) {
            try {
                LOGGER.debug("reference flow property " + referenceFlowProperty.getRefObjectId() + " " + referenceFlowProperty.getShortDescription().getValue());
            } catch (Exception e) {
            }
        }

        Long globalRefId = persistGlobalReference(referenceFlowProperty);

        addRelationship(methodId, globalRefId);

        for (ILString lstring : referenceFlowProperty.getShortDescription().getLStrings()) {
            addShortDescription(globalRefId, lstring);
        }

    }

    private void addRelationship(Long methodId, Long globalRefId) {
        Map<String, Long> map = new HashMap<String, Long>();
        map.put("method_ID", methodId);
        map.put("flowproperty_ID", globalRefId);
        this.relationships.add(map);
    }

    private void addShortDescription(Long id, ILString lstring) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("globalreference_id", id);
        map.put("lang", lstring.getLang());
        map.put("value", lstring.getValue());
        this.shortDescriptions.add(map);
    }

    @SuppressWarnings("unchecked")
    private void persistShortDescriptions() {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("globalreference_shortdescription").usingColumns("globalreference_id", "lang", "value");
        simpleJdbcInsert.executeBatch(this.shortDescriptions.toArray(new Map[this.shortDescriptions.size()]));
    }

    private Long persistGlobalReference(IGlobalReference ref) {

        try {
            KeyHolder key = new GeneratedKeyHolder();

            String type = ref.getType().name();
            String uri = ref.getUri();
            String uuid = ref.getRefObjectId();

            Integer vmaj = null;
            Integer vmin = null;
            Integer vsub = null;
            Integer version = null;

            if (StringUtils.isNotBlank(ref.getVersionAsString())) {
                try {
                    DataSetVersion v;
                    v = DataSetVersion.parse(ref.getVersionAsString());
                    vmaj = v.getMajorVersion();
                    vmin = v.getMinorVersion();
                    vsub = v.getSubMinorVersion();
                    version = v.getVersion();
                } catch (FormatException e) {
                }
            }

            Integer vmaj2 = vmaj;
            Integer vmin2 = vmin;
            Integer vsub2 = vsub;
            Integer version2 = version;

            jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    final PreparedStatement ps = connection.prepareStatement(sqlInsertGlobalReference,
                            Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, type);
                    ps.setString(2, uri);
                    ps.setString(3, uuid);

                    if (vmaj2 != null)
                        ps.setInt(4, vmaj2);
                    else
                        ps.setNull(4, java.sql.Types.INTEGER);

                    if (vmin2 != null)
                        ps.setInt(5, vmin2);
                    else
                        ps.setNull(5, java.sql.Types.INTEGER);

                    if (vsub2 != null)
                        ps.setInt(6, vsub2);
                    else
                        ps.setNull(6, java.sql.Types.INTEGER);

                    if (version2 != null)
                        ps.setInt(7, version2);
                    else
                        ps.setNull(7, java.sql.Types.INTEGER);

                    return ps;
                }
            }, key);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("inserted row into globalreference with id " + key.getKey().longValue());
            return key.getKey().longValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateMethods(final List<Map<String, Long>> relationships) {

        jdbcTemplate.batchUpdate(sqlUpdateMethod, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, Long> n = relationships.get(i);
                ps.setLong(1, n.get("flowproperty_ID"));
                ps.setLong(2, n.get("method_ID"));
            }

            @Override
            public int getBatchSize() {
                return relationships.size();
            }
        });
    }


    private IGlobalReference extractReferenceQuantity() {

        Long start = System.currentTimeMillis();

        Object doc = parser.parseXML(this.rawXml);
        JXPathContext context = JXPathContext.newContext(doc);
        context.setLenient(true);
        context.registerNamespace("common", "http://lca.jrc.it/ILCD/Common");
        context.registerNamespace("ilcd", "http://lca.jrc.it/ILCD/LCIAMethod");

        DataSetParsingHelper parserHelper = new DataSetParsingHelper(context);
        CommonConstructsReader commonReader = new CommonConstructsReader(parserHelper);
        LCIAMethodReader methodReader = new LCIAMethodReader();
        methodReader.setParserHelper(parserHelper);
        methodReader.setCommonReader(commonReader);

        Element quantitativeReference = parserHelper.getElement("/ilcd:LCIAMethodDataSet/ilcd:LCIAMethodInformation/ilcd:quantitativeReference");

        if (quantitativeReference == null)
            return null;

        IGlobalReference refFP = null;
        try {
            refFP = commonReader.getGlobalReference(quantitativeReference, "referenceQuantity", LCIAMethodReader.NAMESPACE_LCIAMETHOD, GlobalReferenceType.class, new PrintWriter(System.out));
        } catch (NullPointerException e) {
        }

        Long duration = System.currentTimeMillis() - start;
        this.extractionTime += duration;

        return refFP;
    }

    private void getXml(JdbcTemplate jdbcTemplate, Long id) {
        final String sqlSelectXml = "SELECT COMPRESSEDCONTENT FROM `xmlfile` WHERE ID = ? ";

        LobHandler lobHandler = new DefaultLobHandler();

        jdbcTemplate.query(sqlSelectXml, new Object[]{id}, new AbstractLobStreamingResultSetExtractor<Void>() {
            protected void streamData(ResultSet rs) throws SQLException, IOException {
                V15_1__ExtractReferenceQuantity.this.rawXml = new GZIPInputStream(
                        lobHandler.getBlobAsBinaryStream(rs, 1));
            }
        });
    }

}
