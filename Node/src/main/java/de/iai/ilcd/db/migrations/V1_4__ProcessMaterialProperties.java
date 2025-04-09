package de.iai.ilcd.db.migrations;

import de.iai.ilcd.model.flow.MaterialProperty;
import de.iai.ilcd.model.flow.MaterialPropertyDefinition;
import de.iai.ilcd.xml.read.DataSetParsingHelper;
import de.iai.ilcd.xml.read.FlowReader;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.xml.JDOMParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.jdom.Namespace;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Randomize initial default data stock UUID
 */
public class V1_4__ProcessMaterialProperties implements SpringJdbcMigration {

    /**
     * Logger to use
     */
    private static final Logger LOGGER = LogManager.getLogger(V1_4__ProcessMaterialProperties.class);

    /**
     * Rows per iteration
     */
    private final static int ROWS_PER_ITERATION = 5;

    /**
     * SQL code to get product flow ID and compressed XML data
     */
    private final static String SQL_GET_PRODFLOW_AND_XML = "SELECT `p`.`ID`,`x`.`COMPRESSEDCONTENT` FROM `flow_product` `p` LEFT JOIN `flow_common` `f` ON `p`.`ID`=`f`.`ID` LEFT JOIN `xmlfile` `x` ON (`f`.`XMLFILE_ID`=`x`.`ID`)";

    /**
     * SQL code to insert material properties
     */
    private final static String SQL_INSERT_MAT_PROP = "INSERT INTO flow_product_material_property (`definition_id`,`value`,`product_flow_id`) VALUES (?,?,?)";

    /**
     * {@inheritDoc}
     */
    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        V1_4__ProcessMaterialProperties.LOGGER.info("Processing existing material properties from XML data sets.");

        // definition cache
        final MatPropDefCache defCache = new MatPropDefCache();

        // extractor for ID/xml
        final MatPropInfoExtractor extractor = new MatPropInfoExtractor();

        final long cntProducts = this.getProductsCount(jdbcTemplate);
        long cntDone = 0;

        while (cntDone < cntProducts) {
            final String sql = V1_4__ProcessMaterialProperties.SQL_GET_PRODFLOW_AND_XML + " LIMIT " + Long.toString(cntDone) + "," + Long.toString(V1_4__ProcessMaterialProperties.ROWS_PER_ITERATION);

            final List<MaterialPropertyInformation> cList = jdbcTemplate.query(sql, extractor);

            final List<MatProperty> lstPropsToInsert = new ArrayList<MatProperty>();

            for (MaterialPropertyInformation info : cList) {
                if (CollectionUtils.isNotEmpty(info.matProps)) {
                    for (MaterialProperty prop : info.matProps) {
                        long defId = defCache.getMaterialPropertyDefinitionId(jdbcTemplate, prop.getDefinition());
                        lstPropsToInsert.add(new MatProperty(info.flowId, defId, prop.getValue()));
                    }
                }
            }

            BatchPreparedStatementSetter pstInsMatProp = new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    MatProperty p = lstPropsToInsert.get(i);

                    ps.setLong(1, p.defId);
                    ps.setDouble(2, p.value);
                    ps.setLong(3, p.pFlowId);
                }

                @Override
                public int getBatchSize() {
                    return lstPropsToInsert.size();
                }
            };

            jdbcTemplate.batchUpdate(V1_4__ProcessMaterialProperties.SQL_INSERT_MAT_PROP, pstInsMatProp);

            cntDone += V1_4__ProcessMaterialProperties.ROWS_PER_ITERATION;
        }

    }

    /**
     * Get count of product flows
     *
     * @param jdbcTemplate
     * @return count of product flows
     */
    private final long getProductsCount(JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM `flow_product`", long.class);
    }

    /**
     * Utility class for material properties
     */
    private static class MaterialPropertyInformation {

        /**
         * Value for product model flag column
         */
        public final long flowId;
        /**
         * List of material properties
         */
        public final List<MaterialProperty> matProps;
        /**
         * Namespace for MatML
         */
        private final Namespace matmlNamespace = Namespace.getNamespace("matml", "http://www.matml.org/");

        /**
         * Create the cache object for the name
         *
         * @param id   Database ID
         * @param data GZIP compressed data of XML
         */
        public MaterialPropertyInformation(long id, byte[] data) {
            super();
            this.flowId = id;
            this.matProps = this.getMaterialProperties(data);
        }

        /**
         * Determine the material properties (decompress GZIP data and read entries)
         *
         * @param data GZIP compressed data of XML
         */
        private List<MaterialProperty> getMaterialProperties(byte[] data) {
            try {
                JDOMParser parser = new JDOMParser();
                parser.setValidating(false);
                Object doc = parser.parseXML(new GZIPInputStream(new ByteArrayInputStream(data)));
                JXPathContext context = JXPathContext.newContext(doc);
                context.setLenient(true);
                context.registerNamespace("common", "http://lca.jrc.it/ILCD/Common");
                context.registerNamespace("ilcd", "http://lca.jrc.it/ILCD/Flow");
                context.registerNamespace("matml", "http://www.matml.org/");

                DataSetParsingHelper helper = new DataSetParsingHelper(context);

                return FlowReader.readMaterialProperties(helper, this.matmlNamespace);
            } catch (Exception e) {
            }
            return null;

        }
    }

    /**
     * Material property meta data
     */
    private static class MatProperty {

        /**
         * Product flow ID
         */
        public long pFlowId;

        /**
         * Material Property Definition ID
         */
        public long defId;

        /**
         * Value
         */
        public double value;

        /**
         * Create meta data object
         *
         * @param pFlowId Product flow ID
         * @param defId   Material Property Definition ID
         * @param value   Value
         */
        private MatProperty(long pFlowId, long defId, double value) {
            super();
            this.pFlowId = pFlowId;
            this.defId = defId;
            this.value = value;
        }

    }

    /**
     * Material property definition cache
     */
    private class MatPropDefCache extends HashMap<String, Long> {

        /**
         * Serialization ID
         */
        private static final long serialVersionUID = 3117732044289068626L;

        /**
         * Get the material property definition ID
         *
         * @param jdbcTemplate JDBC template for DB interaction
         * @param def          the definition in question
         * @return ID of the definition
         */
        public Long getMaterialPropertyDefinitionId(JdbcTemplate jdbcTemplate, MaterialPropertyDefinition def) {
            // know the name, use cached ID
            if (this.containsKey(def.getName())) {
                return this.get(def.getName());
            }

            // try to find in DB
            try {
                long id = jdbcTemplate.queryForObject("SELECT `id` FROM `flow_product_material_property_definition` WHERE `name`=?", long.class, def.getName());
                if (id > 0) {
                    this.put(def.getName(), id);
                    return id;
                }
            } catch (Exception e) {
                // error or not found
            }

            // not found in DB => create
            GeneratedKeyHolder keys = new GeneratedKeyHolder();
            jdbcTemplate.update(new InsertMatPropDefCreator(def), keys);

            final long key = keys.getKey().longValue();
            this.put(def.getName(), key);

            return key;
        }
    }

    /**
     * {@link PreparedStatementCreator} to create a material property definition
     */
    private class InsertMatPropDefCreator implements PreparedStatementCreator {

        /**
         * The definition
         */
        private final MaterialPropertyDefinition def;

        /**
         * Create the material property definition insertion PreparedStatementCreator
         */
        private InsertMatPropDefCreator(MaterialPropertyDefinition def) {
            super();
            this.def = def;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
            PreparedStatement pst = c
                    .prepareStatement("INSERT INTO `flow_product_material_property_definition` (`name`,`unit`,`unitDescription`) VALUES (?,?,?)",
                            PreparedStatement.RETURN_GENERATED_KEYS);
            pst.setString(1, this.def.getName());
            pst.setString(2, this.def.getUnit());
            pst.setString(3, this.def.getUnitDescription());
            return pst;
        }

    }

    /**
     * Extractor for {@link MaterialPropertyInformation}
     */
    private class MatPropInfoExtractor implements ResultSetExtractor<List<MaterialPropertyInformation>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public List<MaterialPropertyInformation> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<MaterialPropertyInformation> lst = new ArrayList<MaterialPropertyInformation>();
            while (rs.next()) {
                final long id = rs.getLong("ID");
                final byte[] data = rs.getBytes("COMPRESSEDCONTENT");
                lst.add(new MaterialPropertyInformation(id, data));
            }
            return lst;
        }

    }

}
