package de.iai.ilcd.db.migrations;

import de.fzk.iai.ilcd.service.client.impl.vo.types.common.GlobalReferenceType;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.common.ILString;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.xml.read.CommonConstructsReader;
import de.iai.ilcd.xml.read.DataSetParsingHelper;
import de.iai.ilcd.xml.read.ProcessReader;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.xml.JDOMParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
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
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Initialize datastock display properties with ascending ordinal values to
 * preserve original order
 */
public class V7_1_1__ExtractProcessDataSources implements SpringJdbcMigration {

    /**
     * Logger to use
     */
    private static final Logger LOGGER = LogManager.getLogger(V7_1_1__ExtractProcessDataSources.class);

    private final String sqlSelectProcessIds = "SELECT ID FROM `process`";

    private final String sqlSelectProcessXmlId = "SELECT XMLFILE_ID FROM `process` WHERE ID = ?";

    private final String sqlInsertGlobalReference = "INSERT INTO `globalreference` (`TYPE`, `URI`, `UUID`, `MAJORVERSION`, `MINORVERSION`, `SUBMINORVERSION`, `VERSION`) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private InputStream rawXml;

    private JdbcTemplate jdbcTemplate;

    private JDOMParser parser = new JDOMParser();

    private List<Map<String, Object>> relationships = new ArrayList<Map<String, Object>>();

    private List<Map<String, Object>> shortDescriptions = new ArrayList<Map<String, Object>>();

    private Long extractionTime = 0L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {

        this.jdbcTemplate = jdbcTemplate;

        this.parser.setValidating(false);

        List<Long> processIds = jdbcTemplate.queryForList(sqlSelectProcessIds, Long.class);

        Long start = System.currentTimeMillis();

        for (Long processId : processIds) {

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("process with id " + processId);

            Long processXmlId = jdbcTemplate.queryForObject(sqlSelectProcessXmlId, Long.class, processId);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("xmlfile id " + processXmlId);

            getXml(jdbcTemplate, processXmlId);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("extract datasources");

            HashSet<IGlobalReference> dataSources = extractDataSources();

            persistDataSources(dataSources, processId);
        }

        persistRelationships();
        persistShortDescriptions();

        Long duration = System.currentTimeMillis() - start;

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("datasources extraction completed in " + duration + " ms");
            LOGGER.info("XML parsing took " + this.extractionTime + " ms");
        }
    }

    private void persistDataSources(HashSet<IGlobalReference> dataSources, Long processId) {
        /*
         * these are the steps that are carried out:
         *
         * INSERT INTO globalreference
         *
         * INSERT INTO globalreference_shortdescription
         *
         * INSERT INTO process_datasource (process_ID, datasource_ID)
         *
         * the latter two are executed as batch inserts
         */

        for (IGlobalReference g : dataSources) {
            if (LOGGER.isDebugEnabled()) {
                try {
                    LOGGER.debug("datasource " + g.getRefObjectId() + " " + g.getShortDescription().getValue());
                } catch (Exception e) {
                }
            }

            Long globalRefId = persistGlobalReference(g);

            addRelationship(processId, globalRefId);

            for (ILString lstring : g.getShortDescription().getLStrings()) {
                addShortDescription(globalRefId, lstring);
            }
        }

    }

    private void addRelationship(Long processId, Long globalRefId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("process_ID", processId);
        map.put("datasource_ID", globalRefId);
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
    private void persistRelationships() {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("process_datasource").usingColumns("process_ID", "datasource_ID");
        simpleJdbcInsert.executeBatch(this.relationships.toArray(new Map[this.relationships.size()]));
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

            if (!ref.getVersionAsString().strip().isEmpty()) {
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


    private HashSet<IGlobalReference> extractDataSources() {

        Long start = System.currentTimeMillis();

        Object doc = parser.parseXML(this.rawXml);
        JXPathContext context = JXPathContext.newContext(doc);
        context.setLenient(true);
        context.registerNamespace("common", "http://lca.jrc.it/ILCD/Common");
        context.registerNamespace("ilcd", "http://lca.jrc.it/ILCD/Process");

        DataSetParsingHelper parserHelper = new DataSetParsingHelper(context);
        CommonConstructsReader commonReader = new CommonConstructsReader(parserHelper);
        ProcessReader processReader = new ProcessReader();
        processReader.setParserHelper(parserHelper);
        processReader.setCommonReader(commonReader);

        HashSet<IGlobalReference> dataSources = processReader.getDataSources(GlobalReferenceType.class, new PrintWriter(System.out));

        Long duration = System.currentTimeMillis() - start;
        this.extractionTime += duration;

        return dataSources;
    }

    private void getXml(JdbcTemplate jdbcTemplate, Long id) {
        final String sqlSelectXml = "SELECT COMPRESSEDCONTENT FROM `xmlfile` WHERE ID = ? ";

        LobHandler lobHandler = new DefaultLobHandler();

        jdbcTemplate.query(sqlSelectXml, new Object[]{id}, new AbstractLobStreamingResultSetExtractor<Void>() {
            protected void streamData(ResultSet rs) throws SQLException, IOException {
                V7_1_1__ExtractProcessDataSources.this.rawXml = new GZIPInputStream(
                        lobHandler.getBlobAsBinaryStream(rs, 1));
            }
        });
    }

}
