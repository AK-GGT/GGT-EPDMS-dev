package de.iai.ilcd.db.migrations;

import de.fzk.iai.ilcd.service.client.impl.vo.types.common.GlobalReferenceType;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.common.ILString;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.model.common.DataSetVersion;
import de.iai.ilcd.model.common.XmlFile;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.xml.read.CommonConstructsReader;
import de.iai.ilcd.xml.read.DataSetParsingHelper;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.xml.JDOMParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This migration goes through the db and extracts/persists
 * <ol>
 * 		<li> publication date, </li>
 * 		<li> references to publishers </li>
 * 		<li> registration number </li>
 * 		<li> reference to registration authority </li>
 * 		<li> references to original epds </li>
 * </ol>
 * where available from known xml files.
 */
public class V22_6__ExtractEPD12Data implements SpringJdbcMigration {

    private static final Logger LOGGER = LogManager.getLogger(V22_6__ExtractEPD12Data.class);
    private final Namespace processNamespace = Namespace.getNamespace("ilcd", "http://lca.jrc.it/ILCD/Process");
    private final Namespace commonNamespace = Namespace.getNamespace("common", "http://lca.jrc.it/ILCD/Common");
    private final Namespace epd2Namespace = Namespace.getNamespace("epd2", "http://www.indata.network/EPD/2019");
    private JdbcTemplate jdbcTemplate;
    private JDOMParser parser = new JDOMParser();
    /**
     * Although we can't persist GlobalReferences in batch (we need to keep
     * track of individual, generated Ids to map relations) we can do all the
     * shortDescriptions as a batch, which is why we cache them here.
     */
    private List<SimpleDescriptionWithId> descriptions = new ArrayList<SimpleDescriptionWithId>();

    private byte[] processRawXmlCache;

    /**
     * For each process data set in the db we fetch the corresponding xml file, extract new content
     * where available, wrap it (together with the process-id) into a wrapper class and add them to a list.
     * <p>
     * Once the extraction is complete we go on to persist the new content in several batches.
     */
    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        this.jdbcTemplate = jdbcTemplate;
        List<EPD12AdditionalProcessData> extractedData = new ArrayList<>();

        List<Long> processIds = jdbcTemplate.queryForList("SELECT ID FROM `process`", Long.class);

        for (Long processId : processIds) {
            try {
                EPD12AdditionalProcessData d = extractRelevantDataForProcess(processId);
                d.setProcessId(processId);
                extractedData.add(d);
            } catch (Exception e) {
                LOGGER.error("Error occured while trying to extract data from process " + processId);
                throw e;
            }
        }
        persistAdditionalProcessData(extractedData);
    }

    /**
     * Steps:
     * <ol>
     * 		<li>We simply update the process table by filling in publication dates and registration numbers.</li>
     * 		<li> We persist references to registration authority, publishers and original EPDs each in two steps:
     * 		By persisting the references while collecting relation tuples of the form (processId, referenceId) and then
     * 		persisting these in corresponding cross-reference tables.</li>
     * 		<li> We persist all shortDescriptions for all GlobalReference that have been persisted. </li>
     * </ol>
     *
     * @param extractedData
     */
    private void persistAdditionalProcessData(List<EPD12AdditionalProcessData> extractedData) {

        //Step 1
        this.batchUpdateProcessTable(extractedData);

//			//Step 2
        List<IdPair> identifiedForeignKeys = persistReferencesToRegistrationAuthorities(extractedData);
        persistForeignKeysInProcessTable(identifiedForeignKeys);

        List<IdPair> processPublisherRelations = persistPublisherReferences(extractedData);
        persistRelationsInto("process_reference_to_publisher", processPublisherRelations);

        List<IdPair> processOriginRelations = persistReferencesToOriginalEPDs(extractedData);
        persistRelationsInto("process_reference_to_original_epd", processOriginRelations);

//			//Step 3
        persistShortDescriptions(this.descriptions);

    }

    /**
     * Runs a batchUpdate given a sql-statement and a list of modelled by IdPair.
     * Note: The sql-statement is expected to have the global reference parameter
     * first, and the process parameter second.
     *
     * @param sqlStatement
     * @param relations
     */
    private void runBatchUpdate(String sqlStatement, List<IdPair> relations) {
        jdbcTemplate.batchUpdate(sqlStatement, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                IdPair pair = relations.get(i);
                ps.setLong(1, pair.referenceId);
                ps.setLong(2, pair.processId);
            }

            @Override
            public int getBatchSize() {
                return relations.size();
            }
        });
    }

    private void persistForeignKeysInProcessTable(List<IdPair> identifiedForeignKeys) {
        final String sqlStatement = "UPDATE process SET registration_authority_reference_ID =? WHERE ID =?";

        runBatchUpdate(sqlStatement, identifiedForeignKeys);
    }

    /**
     * Method used to store process-publisher relations or process-original
     * relations into the corresponding cross-reference table.
     *
     * @param tableName
     * @param relations
     */
    private void persistRelationsInto(String tableName, List<IdPair> relations) {
        final String sqlStatement = "INSERT INTO " + tableName + " (globalreference_ID, process_ID) VALUES (?, ?)";

        runBatchUpdate(sqlStatement, relations);
    }

    private void persistShortDescriptions(List<SimpleDescriptionWithId> descriptions) {
        final String sqlStatement = "INSERT INTO globalreference_shortdescription (globalreference_id, value, lang) VALUES (?,?, ?)";

        jdbcTemplate.batchUpdate(sqlStatement, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SimpleDescriptionWithId description = descriptions.get(i);
                ps.setLong(1, description.getId());
                ps.setString(2, description.getDescriptionValue());
                ps.setString(3, description.getLanguage());
            }

            @Override
            public int getBatchSize() {
                return descriptions.size();
            }
        });
    }

    /**
     * We persist all references to original EPD data sets and return a list of
     * relation tupels. These can then be added to the db in a batch.
     *
     * @param extractedData
     * @return
     */
    private List<IdPair> persistReferencesToOriginalEPDs(List<EPD12AdditionalProcessData> extractedData) {
        List<IdPair> relations = new ArrayList<IdPair>();

        for (EPD12AdditionalProcessData d : extractedData) {
            if (d.referencesToOriginalEPDs != null && !d.referencesToOriginalEPDs.isEmpty()) {
                for (IGlobalReference originReference : d.referencesToOriginalEPDs) {
                    Long referenceId = persistGlobalReference(originReference);
                    relations.add(new IdPair(d.processId, referenceId));
                }
            }
        }
        return relations;
    }

    /**
     * We persist all references to publishers and return a list of relation tupels.
     * These can then be added to the db in a batch.
     *
     * @param extractedData
     * @return
     */
    private List<IdPair> persistPublisherReferences(List<EPD12AdditionalProcessData> extractedData) {
        List<IdPair> relations = new ArrayList<IdPair>();

        for (EPD12AdditionalProcessData d : extractedData) {
            if (d.referencesToPublishers != null && !d.referencesToPublishers.isEmpty()) {
                for (IGlobalReference publisherReference : d.referencesToPublishers) {
                    Long referenceId = persistGlobalReference(publisherReference);
                    relations.add(new IdPair(d.processId, referenceId));
                }
            }
        }
        return relations;
    }

    /**
     * We persist all references to registration authorities and return a list of
     * identified foreign key tupels. These can then be added to the db in a batch.
     *
     * @param extractedData
     * @return List of foreign keys (with row ids) to be inserted in process table
     * (by batchUpdate)
     */
    private List<IdPair> persistReferencesToRegistrationAuthorities(List<EPD12AdditionalProcessData> extractedData) {
        List<IdPair> identifiedForeignKeys = new ArrayList<IdPair>();

        for (EPD12AdditionalProcessData d : extractedData) {
            if (d.referenceToRegistrationAuthority != null) {
                Long referenceId = persistGlobalReference(d.referenceToRegistrationAuthority);
                identifiedForeignKeys.add(new IdPair(d.processId, referenceId));
            }
        }
        return identifiedForeignKeys;
    }

    /**
     * Inserts a new (single) GlobalReference into the corresponding db table and
     * caches the corresponding short description data, which has to be persisted
     * later (in a batchUpdate).
     * <p>
     * Furthermore we return the (sql-generated) id for convenience.
     *
     * @param GlobalReference
     * @return sql-generated id for the reference
     */
    private Long persistGlobalReference(IGlobalReference ref) {
        final String sqlStatement = "INSERT INTO `globalreference` (`TYPE`, `URI`, `UUID`, `MAJORVERSION`, `MINORVERSION`, `SUBMINORVERSION`, `VERSION`) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            String type = null;
            if (ref.getType() != null)
                type = ref.getType().name();
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

            final String typeFinal = type;
            final String uriFinal = uri;
            final String uuidFinal = uuid;
            final Integer vmajFinal = vmaj;
            final Integer vminFinal = vmin;
            final Integer vsubFinal = vsub;
            final Integer versionFinal = version;

            jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    final PreparedStatement ps = connection.prepareStatement(sqlStatement,
                            Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, typeFinal);
                    ps.setString(2, uriFinal);
                    ps.setString(3, uuidFinal);

                    if (vmajFinal != null)
                        ps.setInt(4, vmajFinal);
                    else
                        ps.setNull(4, java.sql.Types.INTEGER);

                    if (vminFinal != null)
                        ps.setInt(5, vminFinal);
                    else
                        ps.setNull(5, java.sql.Types.INTEGER);

                    if (vsubFinal != null)
                        ps.setInt(6, vsubFinal);
                    else
                        ps.setNull(6, java.sql.Types.INTEGER);

                    if (versionFinal != null)
                        ps.setInt(7, versionFinal);
                    else
                        ps.setNull(7, java.sql.Types.INTEGER);

                    return ps;
                }
            }, keyHolder);

            Long referenceId = keyHolder.getKey().longValue();

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Inserted row into globalreference with id " + referenceId);

            this.descriptions.addAll(getSimpleDescriptions(referenceId, ref.getShortDescription()));
            return referenceId;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Persisting the 'publicationDateOfEPD' and the 'registrationNumber' is
     * straightforward.
     *
     * @param extractedData
     */
    private void batchUpdateProcessTable(List<EPD12AdditionalProcessData> extractedData) {
        final String sqlStatement = "UPDATE `process` SET publicationDateOfEPD =?, registrationNumber =?, epdFormatVersion =? WHERE ID =?";

        jdbcTemplate.batchUpdate(sqlStatement, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                EPD12AdditionalProcessData d = extractedData.get(i);
                if (d != null && d.publicationDate != null) {
                    ps.setDate(1, new java.sql.Date(d.publicationDate.getTime()));
                } else {
                    ps.setDate(1, null);
                }
                ps.setString(2, d.registrationNumber);
                ps.setString(3, d.epdFormatVersion);
                ps.setLong(4, d.processId);
            }

            @Override
            public int getBatchSize() {
                return extractedData.size();
            }
        });
    }


    //**		Extraction			**//

    private EPD12AdditionalProcessData extractRelevantDataForProcess(Long processId) {
        final String sqlGetXmlId = "SELECT XMLFILE_ID FROM `process` WHERE ID = ?";
        final Long xmlId = jdbcTemplate.queryForObject(sqlGetXmlId, Long.class, processId);

        Object xmlDocument = this.loadXml(xmlId);
        EPD12AdditionalProcessData parsedData = this.parseDataFrom(xmlDocument);
        return parsedData;
    }


    private EPD12AdditionalProcessData parseDataFrom(Object xmlDocument) {
        EPD12AdditionalProcessData data = new EPD12AdditionalProcessData();

        JXPathContext context = JXPathContext.newContext(xmlDocument);
        context.setLenient(true);
        context.registerNamespace(this.processNamespace.getPrefix(), this.processNamespace.getURI());
        context.registerNamespace(this.commonNamespace.getPrefix(), this.commonNamespace.getURI());
        context.registerNamespace(this.epd2Namespace.getPrefix(), this.epd2Namespace.getURI());

        DataSetParsingHelper parserHelper = new DataSetParsingHelper(context);
        CommonConstructsReader commonReader = new CommonConstructsReader(parserHelper);

        String epdFormatVersion = parserHelper.getElement("ilcd:processDataSet").getAttributeValue("epd-version", this.epd2Namespace);
        Date publicationDateOfEPD = parserHelper.getSimpleDate("ilcd:processDataSet/ilcd:processInformation/ilcd:time/common:other/epd2:publicationDateOfEPD");
        String registrationNumber = parserHelper.getStringValue("/ilcd:processDataSet/ilcd:administrativeInformation/ilcd:publicationAndOwnership/common:registrationNumber");
        IGlobalReference referenceToRegistrationAuthority = parseReferenceToRegistrationAuthority(commonReader, parserHelper,
                "/ilcd:processDataSet/ilcd:administrativeInformation/ilcd:publicationAndOwnership", "referenceToRegistrationAuthority");

        List<IGlobalReference> referencesToOriginalEPDs = parseReferences(commonReader, parserHelper,
                "/ilcd:processDataSet/ilcd:modellingAndValidation/ilcd:dataSourcesTreatmentAndRepresentativeness/common:other", "referenceToOriginalEPD");

        List<IGlobalReference> referencesToPublishers = parseReferences(commonReader, parserHelper,
                "/ilcd:processDataSet/ilcd:administrativeInformation/ilcd:publicationAndOwnership/common:other", "referenceToPublisher");

        data.setEpdFormatVersion(epdFormatVersion);
        data.setPublicationDate(publicationDateOfEPD);
        data.setRegistrationNumber(registrationNumber);
        data.setReferenceToRegistrationAuthority(referenceToRegistrationAuthority);
        data.setReferencesToPublishers(referencesToPublishers);
        data.setReferencesToOriginalEPDs(referencesToOriginalEPDs);
        return data;
    }

    private List<IGlobalReference> parseReferences(CommonConstructsReader commonReader,
                                                   DataSetParsingHelper parserHelper, String parentPath, String childName) {
        Element parent = parserHelper.getElement(parentPath);

        if (parent == null)
            return null;

        List<IGlobalReference> references = new ArrayList<>();
        Namespace childrenNamespace = Namespace.getNamespace("epd2", "http://www.indata.network/EPD/2019");
        try {
            references = commonReader.getGlobalReferences(parent, childName, childrenNamespace, GlobalReferenceType.class, new PrintWriter(System.out));
        } catch (NullPointerException e) {
        }

        return references;
    }

    private IGlobalReference parseReferenceToRegistrationAuthority(CommonConstructsReader commonReader, DataSetParsingHelper parserHelper, String parentPath, String childName) {
        Element parent = parserHelper.getElement(parentPath);
        if (parent == null)
            return null;

        IGlobalReference reference = null;
        Namespace childNamespace = Namespace.getNamespace("common", "http://lca.jrc.it/ILCD/Common");
        try {
            reference = commonReader.getGlobalReference(parent, childName, childNamespace, GlobalReferenceType.class, new PrintWriter(System.out));
        } catch (NullPointerException e) {
        }
        return reference;
    }

    private Object loadXml(Long xmlId) {
        Object xmlDocument = null;
        final String sqlSelectXml = "SELECT COMPRESSEDCONTENT FROM `xmlfile` WHERE ID = ? ";
        byte[] compressedXml = jdbcTemplate.queryForObject(sqlSelectXml, (rs, rowNum) -> rs.getBytes(1), xmlId);

        ZipFile zip;
        try {
            zip = new ZipFile(new SeekableInMemoryByteChannel(compressedXml), StandardCharsets.UTF_8.displayName());
            ZipArchiveEntry xmlEntry = zip.getEntry(XmlFile.ZIP_DEFAULT_ENTRY_NAME);
            InputStream xmlInputStream = zip.getInputStream(xmlEntry);

            xmlDocument = this.parser.parseXML(xmlInputStream);
            xmlInputStream.close();
            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xmlDocument;
    }


    //**	For convenience:	**//

    /**
     * Convenience method...
     *
     * @param globalReferenceId
     * @param shortDescription
     * @return
     */
    public List<SimpleDescriptionWithId> getSimpleDescriptions(Long globalReferenceId,
                                                               IMultiLangString shortDescription) {
        List<SimpleDescriptionWithId> result = new ArrayList<SimpleDescriptionWithId>();

        for (ILString simpleDescription : shortDescription.getLStrings()) {
            result.add(new SimpleDescriptionWithId(globalReferenceId, simpleDescription));
        }
        return result;
    }

    /**
     * Inner class just to keep track of pairs of Ids (modeling rows in
     * cross-reference tables)..
     */
    private class IdPair {

        private Long processId;

        private Long referenceId;

        public IdPair(Long processId, Long referenceId) {
            this.processId = processId;
            this.referenceId = referenceId;
        }
    }

    /**
     * Helper class to model a row in the table globalreference_shortdescription.
     */
    private class SimpleDescriptionWithId {

        private Long globalReferenceId;

        private ILString simpleDescription;

        public SimpleDescriptionWithId(Long globalReferenceId, ILString simpleDescription) {
            this.globalReferenceId = globalReferenceId;
            this.simpleDescription = simpleDescription;
        }

        public String getLanguage() {
            return this.simpleDescription.getLang();
        }

        public String getDescriptionValue() {
            return this.simpleDescription.getValue();
        }

        public Long getId() {
            return this.globalReferenceId;
        }
    }

    /**
     * Wrapper for newly extracted data from/for each process data set.
     */
    private class EPD12AdditionalProcessData {

        private Long processId;

        private String epdFormatVersion;

        private Date publicationDate;

        private String registrationNumber;

        private IGlobalReference referenceToRegistrationAuthority;

        private List<IGlobalReference> referencesToPublishers;

        private List<IGlobalReference> referencesToOriginalEPDs;

        public void setProcessId(Long processId) {
            this.processId = processId;
        }

        public void setPublicationDate(Date publicationDate) {
            this.publicationDate = publicationDate;
        }

        public void setRegistrationNumber(String registrationNumber) {
            this.registrationNumber = registrationNumber;
        }

        public void setReferenceToRegistrationAuthority(IGlobalReference referenceToRegistrationAuthority) {
            this.referenceToRegistrationAuthority = referenceToRegistrationAuthority;
        }

        public void setReferencesToPublishers(List<IGlobalReference> referencesToPublishers) {
            this.referencesToPublishers = referencesToPublishers;
        }

        public void setReferencesToOriginalEPDs(List<IGlobalReference> referencesToOriginalEPDs) {
            this.referencesToOriginalEPDs = referencesToOriginalEPDs;
        }

        public void setEpdFormatVersion(String epdFormatVersion) {
            this.epdFormatVersion = epdFormatVersion;
        }
    }
}
