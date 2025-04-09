package de.iai.ilcd.db.migrations;

import com.google.common.collect.Lists;
import de.iai.ilcd.xml.read.LCIAMethodReader;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class V28_5__ExtractDoubleCharFactorsForLCIAMethods implements SpringJdbcMigration {

    private static final Logger LOGGER = LogManager.getLogger(V28_5__ExtractDoubleCharFactorsForLCIAMethods.class);

    private static final String LCIAMETHOD_NAMESPACE_URI = LCIAMethodReader.NAMESPACE_LCIAMETHOD.getURI();

    private static final int BATCH_SIZE = 1; // It turns out a single data set can hold 80k+ characterisation factors - that's enough workload.

    private static XMLInputFactory inFact = XMLInputFactory.newFactory();

    JdbcTemplate jdbcTemplate;

    private boolean errorsOccured = false;

    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        LOGGER.info("Starting to extract characterisation factors for lcia methods.");
        long startTime = System.currentTimeMillis();

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Batch size is set to " + BATCH_SIZE);

        this.jdbcTemplate = jdbcTemplate;
        List<Long> lciaMethodIds = new ArrayList<Long>();
        lciaMethodIds.addAll(jdbcTemplate.queryForList("SELECT `ID` FROM lciamethod", Long.class));

        for (List<Long> idBatch : Lists.partition(lciaMethodIds, BATCH_SIZE)) {
            Map<String, String> idResolver = initIdResolver(idBatch);

            List<DataExtract> extractBatch = new ArrayList<DataExtract>();
            for (Long id : idBatch) {
                XmlFile xml = loadXml(id);
                DataExtract extract = null;

                if (xml != null)
                    extract = xml.parseAndResolve(idResolver);
                if (extract != null)
                    extractBatch.add(extract);
            }
            merge(extractBatch);
        }

        long timeSpent = (System.currentTimeMillis() - startTime) / 1000;

        LOGGER.info("Finished extracting characterisation factors for lcia methods. Time spent: " + timeSpent + " seconds.");
        if (errorsOccured)
            LOGGER.error("! Errors occured. Rerun with log level 'debug' to view stacktraces.");
    }

    private void merge(List<DataExtract> extractBatch) {
//		extractBatch.stream().forEach(e -> e.print());

        List<CharacterisationFactor> factors = new ArrayList<CharacterisationFactor>();
        extractBatch.stream().map(e -> e.characterisationFactors).forEach(factors::addAll);

        String sqlStatementExchanges = "UPDATE " + CharacterisationFactor.TABLE_NAME + " SET `MEANVALUE` = ? WHERE `ID` = ?";

        jdbcTemplate.batchUpdate(sqlStatementExchanges, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Double meanValue = factors.get(i).meanValue;
                Long id = factors.get(i).id;

                // Prepare mean value
                if (meanValue == null)
                    ps.setNull(1, java.sql.Types.DOUBLE);
                else
                    ps.setDouble(1, meanValue.doubleValue());

                // Prepare id
                if (id == null)
                    ps.setNull(2, java.sql.Types.BIGINT);
                else
                    ps.setDouble(2, id.longValue());
            }

            @Override
            public int getBatchSize() {
                return factors.size();
            }

        });
    }

    private Map<String, String> initIdResolver(List<Long> ids) {
        String batchConstraintClause = ids.stream()
                .map(id -> "lciamethod_lciamethodcharacterisationfactor.LCIAMethod_ID = '" + String.valueOf(id) + "'")
                .collect(Collectors.joining(" OR "));

        List<Map<String, String>> labeledIdTripels = jdbcTemplate
                .query("SELECT lciamethod_lciamethodcharacterisationfactor.LCIAMethod_ID as 'lciaMethod_id',"
                        + " lciamethod_lciamethodcharacterisationfactor.characterisationFactors_ID as 'characterisationFactor_id',"
                        + " globalreference.UUID as 'refObject_uuid',"
                        + " globalreference.ID as 'refObject_id'," // For debugging purposes
                        + " lciamethodcharacterisationfactor.location as 'location',"
                        + " lciamethodcharacterisationfactor.EXCHANGEDIRECTION as 'exchangeDirection'"
                        + " FROM  lciamethod_lciamethodcharacterisationfactor"
                        + " LEFT JOIN lciamethodcharacterisationfactor"
                        + " ON lciamethod_lciamethodcharacterisationfactor.characterisationFactors_ID = lciamethodcharacterisationfactor.ID"
                        + " LEFT JOIN globalreference"
                        + " ON globalreference.ID = lciamethodcharacterisationfactor.FLOWGLOBALREFERENCE_ID"
                        + " WHERE (" + batchConstraintClause + ")", (rs, rowNum) -> {
                    try {
                        Map<String, String> labeledTypedResultSet = new HashMap<String, String>();

                        String lciaMethod_id = rs.getString("lciaMethod_id");
                        String characterisationFactor_id = rs.getString("characterisationFactor_id");
                        String refObject_uuid = rs.getString("refObject_uuid");
                        String location = rs.getString("location");
                        String exchangeDirection = rs.getString("exchangeDirection");

                        if (isBlank(lciaMethod_id) || isBlank(characterisationFactor_id) || isBlank(refObject_uuid)) {
                            LOGGER.warn(
                                    "DB seems to have inconsistencies: `LCIAMethod_ID`, `characterisationFactors_ID` and `process_exchange.Process_ID` should never be blank. Yet we found"
                                            + "[`lciamethod_lciamethodcharacterisationfactor.LCIAMethod_ID`:'" + lciaMethod_id
                                            + "', `lciamethod_lciamethodcharacterisationfactor.characterisationFactors_ID`:'" + characterisationFactor_id
                                            + "', `globalreference.UUID`:'" + refObject_uuid
                                            + "', `globalreference.ID`:'" + rs.getString("refObject_id") + "']");
                            return null;
                        }


                        labeledTypedResultSet.put("lciaMethod_id", lciaMethod_id);
                        labeledTypedResultSet.put("characterisationFactor_id", characterisationFactor_id);
                        labeledTypedResultSet.put("refObject_uuid", refObject_uuid);
                        labeledTypedResultSet.put("location", location);
                        labeledTypedResultSet.put("exchangeDirection", exchangeDirection);
                        return labeledTypedResultSet;
                    } catch (Exception e) {
                        errorsOccured = true;

                        if (LOGGER.isDebugEnabled())
                            e.printStackTrace();
                        return null;
                    }
                });

        Map<String, String> idResolver = new Hashtable<String, String>();

        // Construct key value pairs
        labeledIdTripels.stream().filter(t -> t != null && t.get("characterisationFactor_id") != null && !t.get("characterisationFactor_id").trim().isEmpty())
                .forEach(t -> {
                    // semantically it makes sense to treat empty Strings as null... (If this is to be changed, change also the defaults in pseudo entities within this migration.)
                    String lciaMethod_id = nonBlankOrNull(t.get("lciaMethod_id"));
                    String refObject_uuid = nonBlankOrNull(t.get("refObject_uuid"));
                    String location = nonBlankOrNull(t.get("location"));
                    String exchangeDirection = nonBlankOrNull(t.get("exchangeDirection"));

                    // construct key
                    String key = lciaMethod_id + ":" + refObject_uuid + ":" + location + ":" + exchangeDirection;

                    // store in resolver
                    idResolver.put(key.toLowerCase(), t.get("characterisationFactor_id"));
                });
        labeledIdTripels.clear();

        return idResolver;
    }

    private String nonBlankOrNull(String s) {
        if (s == null || s.trim().isEmpty())
            return null;

        return s;
    }

    private boolean isBlank(String s) {
        return (s == null || s.trim().isEmpty());
    }

    // ---------------- //
    // ** EXTRACTION **//
    // -------------- //

    private XmlFile loadXml(Long lciaMethod_id) {
        XmlFile xmlFile = new XmlFile(lciaMethod_id);

        final String sqlSelectXml = "SELECT COMPRESSEDCONTENT FROM `lciamethod` JOIN `xmlfile` ON lciamethod.XMLFILE_ID = xmlfile.ID WHERE lciamethod.ID = ? ";
        try {
            byte[] compressedXml = jdbcTemplate.queryForObject(sqlSelectXml, (rs, rowNum) -> rs.getBytes(1), lciaMethod_id);
            xmlFile.setCompressedContent(compressedXml);

        } catch (Exception e) {
            LOGGER.warn("Unable to find xml for LCIA method with ID:" + lciaMethod_id);
            return null;
        }

        return xmlFile;
    }

    // ----------- //
    // ** UTILS **//
    // --------- //

    private boolean isNamedStartElement(XMLStreamReader reader) {
        return reader.getEventType() == XMLStreamReader.START_ELEMENT && reader.hasName();
    }

    private boolean isNamedEndElement(XMLStreamReader reader) {
        return reader.getEventType() == XMLStreamReader.END_ELEMENT && reader.hasName();
    }

    private boolean isEndDocument(XMLStreamReader reader) {
        return reader.getEventType() == XMLStreamReader.END_DOCUMENT;
    }

    private boolean isNamedAs(QName elementName, String namespace, String name) {
        if (elementName == null)
            return false;

        // else
        boolean b = true;

        // Compare namespace
        if (namespace != null)
            b &= namespace.equals(elementName.getNamespaceURI());
        else
            b &= (elementName.getNamespaceURI() == null);

        // Compare name
        if (name != null)
            b &= name.equals(elementName.getLocalPart());
        else
            b &= (elementName.getLocalPart() == null);

        return b;
    }

    // --------------------- //
    // ** PSEUDO ENTITIES **//
    // ------------------- //
    // They should make this migration self-contained. Changing, e.g., the
    // compression algorithm in the XmlFile-Entity would otherwise corrupt this
    // migration.

    /**
     * Wrapper for the byte array holding a compressed xml-file
     */
    private class XmlFile {

        private long lciaMethod_id; // id in db

        private byte[] compressedContent;

        XmlFile(long lciaMethod_id) {
            this.lciaMethod_id = lciaMethod_id;
        }

        public void setCompressedContent(byte[] compressedXml) {
            this.compressedContent = compressedXml;
        }

        /**
         * The xml class handles parsing, because it handles decompression and we can
         * avoid reopening a stream that way.
         *
         * @param idResolver
         * @return
         * @throws XMLStreamException
         */
        public DataExtract parseAndResolve(Map<String, String> idResolver) throws XMLStreamException {
            if (this.compressedContent == null || this.compressedContent.length == 0)
                return null;

            try (ZipFile z = new ZipFile(new SeekableInMemoryByteChannel(this.compressedContent),
                    StandardCharsets.UTF_8.displayName());) {
                Enumeration<ZipArchiveEntry> e = z.getEntries();
                InputStream is = z.getInputStream(e.nextElement());
                DataExtract extract = new DataExtract(lciaMethod_id);
                boolean success = extract.parseCharacterisationFactorsFrom(is, idResolver);
                if (success)
                    return extract;

            } catch (IOException e) {
                errorsOccured = true;
                e.printStackTrace();
            }

            return null;
        }
    }

    private class CharacterisationFactor {

        static final String TABLE_NAME = "lciamethodcharacterisationfactor";

        Long id = null;

        String refObjectId = null;

        String location = null;

        String exchangeDirection = null;

        Double meanValue = null;

        boolean idResolved = false;

        boolean initialiseFrom(XMLStreamReader reader) throws XMLStreamException {

            while (reader.hasNext()) {
                reader.next();
                QName name;

                if (isNamedStartElement(reader)) {
                    name = reader.getName();

                    // Parse UUID of flow reference
                    if (isNamedAs(name, LCIAMETHOD_NAMESPACE_URI, "referenceToFlowDataSet")) {
                        refObjectId = reader.getAttributeValue(null, "refObjectId"); // qname logic regarding namespaces
                        // differs from
                        // reader.getAttribute -- the
                        // latter models no explicit ns
                        // as NULL while the former
                        // assumes the general xml
                    }

                    // Parse location
                    if (isNamedAs(name, LCIAMETHOD_NAMESPACE_URI, "location")) {
                        location = reader.getElementText();
                    }

                    // Parse exchangeDirection
                    if (isNamedAs(name, LCIAMETHOD_NAMESPACE_URI, "exchangeDirection")) {
                        exchangeDirection = reader.getElementText();
                    }

                    // Parse mean value
                    if (isNamedAs(name, LCIAMETHOD_NAMESPACE_URI, "meanValue")) {
                        String meanString = reader.getElementText();
                        if (meanString != null && !meanString.trim().isEmpty())
                            try {
                                meanValue = Double.valueOf(meanString.trim());

                            } catch (Exception e) {
                                // meanValue remains null
                                errorsOccured = true;
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.warn("Failed to parse double from String value '" + meanString + "'.");
                                    e.printStackTrace();
                                }
                            }
                    }
                }

                if (isNamedEndElement(reader)) {
                    name = reader.getName();

                    if (isNamedAs(name, LCIAMETHOD_NAMESPACE_URI, "factor"))
                        break;
                }
            }

            if (refObjectId != null && meanValue != null)// else either no data or data can't be associated with any LCIA method.
                return true;

            return false;
        }

        public void resolveId(Long owner_id, Map<String, String> idResolver) {
            if (!this.idResolved) {
                String key = nonBlankOrNull(String.valueOf(owner_id)) + ":" + nonBlankOrNull(this.refObjectId) + ":" + nonBlankOrNull(this.location) + ":" + nonBlankOrNull(this.exchangeDirection);
                String idString = idResolver.get(key.toLowerCase());
                try {
                    this.id = Long.valueOf(idString);
                    this.idResolved = true;

                } catch (Exception e) {
                    // leave null, but log infos
                    errorsOccured = true;
                    String label = "refObjectId:" + this.refObjectId + ", meanValue:" + this.meanValue + ", location:" + this.location + ", exchangeDirection:" + this.exchangeDirection;
                    LOGGER.error("Failed to resolve id for lciamethod with id=" + owner_id + " with data[" + label + "]");
                    LOGGER.error("with key=" + key);

                    if (LOGGER.isDebugEnabled())
                        e.printStackTrace();
                }
            }
        }

    }

    private class DataExtract {

        Long lciaMethod_id; // id within db

        List<CharacterisationFactor> characterisationFactors = new ArrayList<CharacterisationFactor>();

        DataExtract(Long lciaMethod_id) {
            super();
            this.lciaMethod_id = lciaMethod_id;
        }

        private boolean resolveIds(Map<String, String> idResolver) {
            characterisationFactors.stream().forEach(f -> f.resolveId(lciaMethod_id, idResolver));
            this.characterisationFactors = this.characterisationFactors.stream().filter(f -> f.idResolved).collect(Collectors.toList());
            if (characterisationFactors.size() >= 0)
                return true;

            return false;
        }

        public boolean parseCharacterisationFactorsFrom(InputStream is, Map<String, String> idResolver) throws XMLStreamException {
            if (is == null)
                return false;

            XMLStreamReader reader = inFact.createXMLStreamReader(is);

            while (reader.hasNext()) {

                reader.next();
                QName name;

                if (isNamedStartElement(reader)) {
                    name = reader.getName();

                    if (isNamedAs(name, LCIAMETHOD_NAMESPACE_URI, "characterisationFactors")) {
                        this.parseCharacterisationFactors(reader);
                        break;
                    }
                }

                if (isNamedEndElement(reader)) {
                    name = reader.getName();

                    if (isNamedAs(name, LCIAMETHOD_NAMESPACE_URI, "LCIAMethodDataSet")) {
                        break;
                    }
                }

                if (isEndDocument(reader)) {
                    break;
                }
            }

            if (this.characterisationFactors.size() > 0 && this.resolveIds(idResolver)) {
                return true;
            }

            return false;
        }

        private void parseCharacterisationFactors(XMLStreamReader reader) throws XMLStreamException {

            while (reader.hasNext()) {
                reader.next();
                QName name;

                if (isNamedStartElement(reader)) {
                    name = reader.getName();

                    if (isNamedAs(name, LCIAMETHOD_NAMESPACE_URI, "factor")) {
                        CharacterisationFactor factor = new CharacterisationFactor();
                        boolean success = factor.initialiseFrom(reader);
                        if (success)
                            this.characterisationFactors.add(factor);
                    }
                }

                if (isNamedEndElement(reader)) {
                    name = reader.getName();

                    if (isNamedAs(name, LCIAMETHOD_NAMESPACE_URI, "characterisationFactors")) {
                        break;
                    }
                }
            }
        }

    }

}
