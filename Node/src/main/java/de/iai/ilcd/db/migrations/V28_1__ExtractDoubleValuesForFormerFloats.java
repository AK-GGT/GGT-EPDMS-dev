package de.iai.ilcd.db.migrations;

import de.fzk.iai.ilcd.service.model.enums.TypeOfProcessValue;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
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

public class V28_1__ExtractDoubleValuesForFormerFloats implements SpringJdbcMigration {

    private static final Logger LOGGER = LogManager.getLogger(V28_1__ExtractDoubleValuesForFormerFloats.class);
    private static final double THREAD_RATIO = 1 / 1.2;
    private static final int THREAD_LIMIT = (int) (Runtime.getRuntime().availableProcessors() * THREAD_RATIO);
    private static final int BATCH_SIZE = 200; // Limited by the number of DataExtract classes that can be held
    private static final String PROCESS_NAMESPACE_URI = "http://lca.jrc.it/ILCD/Process";
    // in memory simultaneously

    // * For parsing * /
    private static final String COMMON_NAMESPACE_URI = "http://lca.jrc.it/ILCD/Common";
    private static final String EPD_NAMESPACE_URI = "http://www.iai.kit.edu/EPD/2013";
    XMLInputFactory inFact = XMLInputFactory.newFactory();
    private JdbcTemplate jdbcTemplate;

    // -------------- //
    // * MIGRATION * //
    // ------------ //

    /**
     * @param ps    the PreparedStatement instance
     * @param i     the position of the value (within the statement)
     * @param value
     * @throws SQLException
     */
    private static void nullsafeSetter(PreparedStatement ps, int i, Double value) throws SQLException {
        if (value == null)
            ps.setNull(i, java.sql.Types.DOUBLE);
        else
            ps.setDouble(i, value.doubleValue());
    }

    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        this.jdbcTemplate = jdbcTemplate;

        long start = System.currentTimeMillis();
        LOGGER.info("Starting to extract double values for process data sets.");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("THREAD_LIMIT set to " + THREAD_LIMIT);
            LOGGER.debug("BATCH_SIZE set to " + BATCH_SIZE);
        }

        List<Long> processIds = jdbcTemplate.queryForList("SELECT ID FROM `process`", Long.class);

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Processing a total of " + processIds.size() + " process datasets");

        List<Long> batch = new ArrayList<>();
        int n = 0;
        if (processIds != null) {
            for (Long id : processIds) {
                n++;
                batch.add(id);

                if (n == BATCH_SIZE) {
                    n = 0;
                    long batchStart = System.currentTimeMillis();

                    migrateProcesses(batch);
                    batch.clear();

                    long timeSpent = (long) ((System.currentTimeMillis() - batchStart) / 1000) + 1;

                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("Batch of " + BATCH_SIZE + " data sets took " + timeSpent + " seconds.");
                }
            }
            if (batch.size() > 0) { // Handling the last batch
                migrateProcesses(batch);
                batch.clear();
            }
        }
        long totalTimeSpent = (long) ((System.currentTimeMillis() - start) / 1000) + 1;
        long totalDsCount = processIds != null ? processIds.size() : 0;

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Total time spent for V28_1: " + totalTimeSpent + " seconds (" + totalDsCount + ")");

        LOGGER.info("Extraction of double values for process data sets finished.");
    }

    // -------------- //
    // ** Merging ** //
    // ------------ //

    void migrateProcesses(List<Long> ids) {

        // After parsing an exchange/lcia result, we want to resolve the row number, so we generate resolvers.
        Map<String, Long> exchangeIdResolver = generateExchangeIdResolver(ids);
        Map<String, Long> lciaResultIdResolver = generateLciaResultIdResolver(ids);

        if (exchangeIdResolver.size() > 0 || lciaResultIdResolver.size() > 0) { // otherwise we can't merge anything, so why bother parsing.
            List<DataExtract> allData = new ArrayList<DataExtract>();

            List<Long> parallelBatch = new ArrayList<Long>();
            List<XmlFile> xmls = new ArrayList<XmlFile>();

            int n = 0; // for batching..
            for (Long id : ids) {
                n++;
                parallelBatch.add(id);

                if (n == (THREAD_LIMIT > 0 ? THREAD_LIMIT : 1)) {
                    n = 0;
                    parallelBatch.stream().map(i -> loadXml(i)).filter(xml -> xml != null)
                            .forEach(xml -> xmls.add(xml));

                    if (xmls.size() > 0) {
                        List<DataExtract> parsedData = xmls.stream().parallel().map(xml -> {
                            try {
                                return xml.parseAndResolve(exchangeIdResolver, lciaResultIdResolver);
                            } catch (XMLStreamException e) {
                                LOGGER.error("Encountered a problem concerning process data set with id=" + xml.getProcess_id()
                                        + ", exception was:");
                                e.printStackTrace();
                                return null;
                            }
                        }).filter(d -> d != null).collect(Collectors.toList());

                        if (parsedData != null && parsedData.size() > 0)
                            allData.addAll(parsedData);
                        xmls.clear();
                    }
                    parallelBatch.clear();
                }
            }
            // last batch by hand
            if (!parallelBatch.isEmpty()) {
                parallelBatch.stream().map(i -> loadXml(i)).filter(xml -> xml != null)
                        .forEach(xml -> xmls.add(xml));

                if (xmls.size() > 0) {
                    List<DataExtract> parsedData = xmls.stream().parallel().map(xml -> {
                        try {
                            return xml.parseAndResolve(exchangeIdResolver, lciaResultIdResolver);
                        } catch (XMLStreamException e) {
                            LOGGER.error("Encountered a problem concerning process data set with id=" + xml.getProcess_id()
                                    + ", exception was:");
                            e.printStackTrace();
                            return null;
                        }
                    }).filter(d -> d != null).collect(Collectors.toList());

                    if (parsedData != null && parsedData.size() > 0)
                        allData.addAll(parsedData);
                    xmls.clear();
                }
                parallelBatch.clear();
            }

            merge(allData);
            allData.clear();
            exchangeIdResolver.clear();
            lciaResultIdResolver.clear();
        }
    }

    private void merge(List<DataExtract> data) {

        // Update exchanges table (in one batch)
        List<Exchange> exchanges = new ArrayList<Exchange>();
        data.stream().map(d -> d.getExchanges()).filter(es -> es != null).forEach(exchanges::addAll);
        updateExchangeTable(exchanges);

        // Update exchanges_amounts table (in two batches)
        exchanges.stream().filter(Exchange::hasAmounts);
        updateExchangeAmountsTable(exchanges);

        // Update lciaresults table (in one batch)
        List<LciaResult> lciaResults = new ArrayList<LciaResult>();
        data.stream().map(d -> d.getLciaResults()).filter(lrs -> lrs != null).forEach(lciaResults::addAll);
        updateLciaResultTable(lciaResults);

        // Update lciaresult_amounts table (in two batches)
        lciaResults.stream().filter(LciaResult::hasAmounts);
        updateLciaResultAmountsTable(lciaResults);
    }

    private void updateExchangeAmountsTable(List<Exchange> exchanges) {
        if (exchanges != null) {
            List<Amount> amounts = new ArrayList<Amount>();

            exchanges.stream().map(Exchange::getAmounts).filter(as -> as != null).forEach(amounts::addAll);
            amounts.stream().filter(Amount::hasForeignKey);

            Map<String, List<Amount>> batches = createAmountBatches(amounts);

            updateAmountsInTable(Exchange.AMOUNT_TABLE_NAME, batches, Exchange.AMOUNT_TABLE_FOREIGN_KEY_NAME);
        }
    }

    private void updateLciaResultAmountsTable(List<LciaResult> lciaResults) {
        if (lciaResults != null) {
            List<Amount> amounts = new ArrayList<>();

            lciaResults.stream().map(LciaResult::getAmounts).filter(as -> as != null).forEach(amounts::addAll);
            amounts.stream().filter(Amount::hasForeignKey);

            Map<String, List<Amount>> batches = createAmountBatches(amounts);

            updateAmountsInTable(LciaResult.AMOUNT_TABLE_NAME, batches, LciaResult.AMOUNT_TABLE_FOREIGN_KEY_NAME);
        }
    }

    private void updateAmountsInTable(String tableName, Map<String, List<Amount>> batches, String foreignKeyName) {
        String rootStatement = "UPDATE " + tableName + " SET value = ?";
        String foreignKeyConstraintStatement = " AND " + foreignKeyName + " = ?";

        // module != null && scenario != null (module+scenario)
        jdbcTemplate.batchUpdate(rootStatement + " WHERE module = ? AND scenario = ?" + foreignKeyConstraintStatement,
                new BatchPreparedStatementSetter() {

                    List<Amount> amountsCache = batches.get("module+scenario");

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        nullsafeSetter(ps, 1, amountsCache.get(i).getValue());
                        ps.setString(2, amountsCache.get(i).getModule());
                        ps.setString(3, amountsCache.get(i).getScenario());
                        ps.setLong(4, amountsCache.get(i).getForeignKey());
                    }

                    @Override
                    public int getBatchSize() {
                        return amountsCache.size();
                    }

                });

        // module != null && scenario == null (moduleOnly)
        jdbcTemplate.batchUpdate(
                rootStatement + " WHERE module = ? AND scenario IS NULL" + foreignKeyConstraintStatement,
                new BatchPreparedStatementSetter() {

                    List<Amount> amountsCache = batches.get("moduleOnly");

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        nullsafeSetter(ps, 1, amountsCache.get(i).getValue());
                        ps.setString(2, amountsCache.get(i).getModule());
                        ps.setLong(3, amountsCache.get(i).getForeignKey());
                    }

                    @Override
                    public int getBatchSize() {
                        return amountsCache.size();
                    }

                });

    }

    /**
     * We need to update the amounts tables in two batches, because we identify a
     * row by it's values in module and scenario columns of which scenario can be
     * NULL.
     *
     * @param allAmounts
     * @return
     */
    private Map<String, List<Amount>> createAmountBatches(List<Amount> allAmounts) {
        Map<String, List<Amount>> batches = new Hashtable<String, List<Amount>>();
        batches.put("module+scenario", new ArrayList<Amount>());
        batches.put("moduleOnly", new ArrayList<Amount>());

        if (allAmounts != null) {
            allAmounts.stream().filter(a -> a != null).forEach(a -> {
                if (a.getModule() != null && a.getScenario() != null)
                    batches.get("module+scenario").add(a);
                else if (a.getModule() != null)
                    batches.get("moduleOnly").add(a);
                // else: The amount can't be resolved in the db and it can't be used by anyone
                // anyway - let's ignore it!
            });
        }

        return batches;
    }

    private void updateLciaResultTable(List<LciaResult> lciaResults) {
        String sqlStatementExchanges = "UPDATE " + LciaResult.TABLE_NAME + " SET MEANAMOUNT = ? WHERE ID = ?";
        jdbcTemplate.batchUpdate(sqlStatementExchanges, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                nullsafeSetter(ps, 1, lciaResults.get(i).getMeanAmount());
                ps.setLong(2, lciaResults.get(i).getLciaResult_Id());
            }

            @Override
            public int getBatchSize() {
                return lciaResults.size();
            }

        });
    }

    // ---------------- //
    // ** EXTRACTION **//
    // -------------- //

    private void updateExchangeTable(List<Exchange> exchanges) {
        String sqlStatementExchanges = "UPDATE exchange SET MAXIMUMAMOUNT = ?, MEANAMOUNT = ?, MINIMUMAMOUNT = ?, RESULTINGAMOUNT = ? WHERE ID = ?";
        jdbcTemplate.batchUpdate(sqlStatementExchanges, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                nullsafeSetter(ps, 1, exchanges.get(i).getMaxAmount());
                nullsafeSetter(ps, 2, exchanges.get(i).getMeanAmount());
                nullsafeSetter(ps, 3, exchanges.get(i).getMinAmount());
                nullsafeSetter(ps, 4, exchanges.get(i).getResultingAmount());
                ps.setLong(5, exchanges.get(i).getExchange_id());
            }

            @Override
            public int getBatchSize() {
                return exchanges.size();
            }

        });
    }

    // -------------- //
    // ** PARSING ** //
    // ------------ //

    private XmlFile loadXml(Long processId) {
        XmlFile xmlFile = new XmlFile(processId);

        final String sqlSelectXml = "SELECT COMPRESSEDCONTENT as 'compressedXml' FROM `process` JOIN `xmlfile` ON process.XMLFILE_ID = xmlfile.ID WHERE process.ID = ? ";
        try {
            byte[] compressedXml = jdbcTemplate.queryForObject(sqlSelectXml, (rs, rowNum) -> rs.getBytes("compressedXml"), processId);
            xmlFile.setCompressedContent(compressedXml);

        } catch (Exception e) {
            LOGGER.warn("Unable to find xml for process with ID:" + processId);
            return null;
        }

        return xmlFile;
    }

    private DataExtract parseXml(InputStream is, Long process_id, Map<String, Long> exchangeIdResolver,
                                 Map<String, Long> lciaResultIdResolver) throws XMLStreamException, IOException {

        if (is == null)
            return null;
        DataExtract extract = new DataExtract(process_id);
        boolean epdFlag = true; // For EPDs more values are parsed. If in doubt we treat any process as EPD.

        XMLStreamReader reader = this.inFact.createXMLStreamReader(is);
        boolean exchangesParsed = false;
        boolean lciaResultsParsed = false;

        while (reader.hasNext()) {
            QName elementName = null;

            if (isNamedStartElement(reader)) {
                elementName = reader.getName();

                // case: typeOfProcess
                if (isNamedAs(elementName, PROCESS_NAMESPACE_URI, "typeOfDataSet")) {
                    try {
                        String processTypeString = reader.getElementText();
                        TypeOfProcessValue processTypeValue = TypeOfProcessValue.fromValue(processTypeString);
                        epdFlag = TypeOfProcessValue.EPD.equals(processTypeValue) ? true : false;
                    } catch (Exception e) {
                        if (LOGGER.isTraceEnabled())
                            LOGGER.error(
                                    "error setting type, unknown typeOfDataSet value at process with id=" + process_id,
                                    e.getMessage());
                    }
                }

                // case: Exchanges
                if (isNamedAs(elementName, PROCESS_NAMESPACE_URI, "exchanges")) {
                    List<Exchange> exchanges = parseExchanges(reader, epdFlag);
                    extract.addAllExchanges(exchanges, exchangeIdResolver);
                    exchangesParsed = true;
                }

                // case: lciaResults
                if (isNamedAs(elementName, PROCESS_NAMESPACE_URI, "LCIAResults")) {
                    List<LciaResult> lciaResults = parseLciaResults(reader, epdFlag);
                    extract.addAllLciaResults(lciaResults, lciaResultIdResolver);
                    lciaResultsParsed = true;
                }
            }

            if (exchangesParsed && lciaResultsParsed) {
                is.close();
                return extract;
            }

            if (isNamedEndElement(reader)) {
                elementName = reader.getName();

                if (isNamedAs(elementName, PROCESS_NAMESPACE_URI, "processDataSet")) {
                    is.close();
                    return extract;
                }
            }

            if (isEndDocument(reader))
                throw new XMLStreamException(
                        "XML malformed: Document ended before expected </processDataSet> at " + printLocation(reader));

            reader.next();
        }

        is.close();
        return extract;
    }

    private List<Exchange> parseExchanges(XMLStreamReader reader, boolean epdFlag) throws XMLStreamException {
        List<Exchange> exchanges = new ArrayList<Exchange>();

        while (reader.hasNext()) {
            reader.next(); // The reader is handed over already pointing at the start of 'exchanges'
            QName name;

            // Look out for start_elements for 'exchange' data sets
            if (isNamedStartElement(reader)) {
                name = reader.getName();

                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "exchange")) {
                    Exchange e = parseExchange(reader, epdFlag);
                    if (e != null) {
                        exchanges.add(e);
                    }
                }
            }

            // As soon as we reach the end of the envoloping 'exchanges' element, we return
            // what we've found.
            if (isNamedEndElement(reader)) {
                name = reader.getName();
                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "exchanges"))
                    return exchanges;

                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "processDataSet"))
                    throw new XMLStreamException("XML malformed: </processDataSet> before expected </exchanges> at "
                            + printLocation(reader));
            }

            if (isEndDocument(reader))
                throw new XMLStreamException(
                        "XML malformed: Document ended before expected </processDataSet> at " + printLocation(reader));
        }

        return exchanges;
    }

    private Exchange parseExchange(XMLStreamReader reader, boolean epdFlag) throws XMLStreamException {
        Exchange exchange = null;

        Long idWithinProcess = null;
        try {
            String idWithinProcessString = reader.getAttributeValue(null, "dataSetInternalID");
            idWithinProcess = Long.parseLong(idWithinProcessString.trim());
        } catch (Exception e) {
        }
        if (idWithinProcess == null)
            return null; // We cannot resolve this exchange anyway.

        exchange = new Exchange(idWithinProcess);

        while (reader.hasNext()) {
            reader.next();
            QName name;

            if (isNamedStartElement(reader)) {
                name = reader.getName();

                // min
                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "minimumAmount")) {
                    Double minAmount = parseDouble(reader.getElementText().trim());
                    exchange.setMinAmount(minAmount);
                }

                // max
                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "maximumAmount")) {
                    Double maxAmount = parseDouble(reader.getElementText().trim());
                    exchange.setMaxAmount(maxAmount);
                }

                // mean
                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "meanAmount")) {
                    Double meanAmount = parseDouble(reader.getElementText().trim());
                    exchange.setMeanAmount(meanAmount);
                }

                // resulting
                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "resultingAmount")) {
                    Double resultingAmount = parseDouble(reader.getElementText().trim());
                    exchange.setResultingAmount(resultingAmount);
                }

                // Amounts
                if (epdFlag && isNamedAs(name, COMMON_NAMESPACE_URI, "other")) {
                    List<Amount> epdAmounts = parseEpdAmounts(reader);

                    if (epdAmounts != null && !epdAmounts.isEmpty())
                        exchange.addAllAmounts(epdAmounts);
                }
            }

            // When we reach the end we return what we've collected
            if (isNamedEndElement(reader)) {
                name = reader.getName();

                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "exchange"))
                    return exchange;

                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "processDataSet"))
                    throw new XMLStreamException(
                            "XML malformed: </processDataSet> before expected </exchange> at " + printLocation(reader));
            }

            if (isEndDocument(reader))
                throw new XMLStreamException(
                        "XML malformed: Document ended before expected </processDataSet> at " + printLocation(reader));
        }

        return exchange;
    }

    private List<LciaResult> parseLciaResults(XMLStreamReader reader, boolean epdFlag) throws XMLStreamException {
        List<LciaResult> results = new ArrayList<LciaResult>();

        while (reader.hasNext()) {
            reader.next(); // The reader is handed over already pointing at the start of 'lciaResults'
            QName name;

            // Look out for start_elements for 'lciaResult' data sets
            if (isNamedStartElement(reader)) {
                name = reader.getName();

                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "LCIAResult")) {
                    LciaResult lr = parseLciaResult(reader, epdFlag);
                    if (lr != null) {
                        results.add(lr);
                    }
                }
            }

            // As soon as we reach the end of the envoloping 'lciaResults' element, we
            // return what we've found.
            if (isNamedEndElement(reader)) {
                name = reader.getName();
                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "LCIAResults"))
                    return results;

                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "processDataSet"))
                    throw new XMLStreamException("XML malformed: </processDataSet> before expected </lciaResults> at "
                            + printLocation(reader));
            }

            if (isEndDocument(reader))
                throw new XMLStreamException(
                        "XML malformed: Document ended before expected </processDataSet> at " + printLocation(reader));
        }

        return results;
    }

    private LciaResult parseLciaResult(XMLStreamReader reader, boolean epdFlag) throws XMLStreamException {
        String methodReferenceUuid = null;
        Double meanAmount = null;
        List<Amount> epdAmounts = new ArrayList<Amount>();

        while (reader.hasNext()) {
            reader.next();
            QName name;

            if (isNamedStartElement(reader)) {
                name = reader.getName();

                // methodReferenceUuid
                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "referenceToLCIAMethodDataSet")) {
                    methodReferenceUuid = reader.getAttributeValue(null, "refObjectId"); // qname logic regarding
                    // namespaces differs from
                    // reader.getAttribute --
                    // the latter models no
                    // explicit ns as NULL while
                    // the former assumes the
                    // general xml
                }

                // mean
                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "meanAmount")) {
                    meanAmount = parseDouble(reader.getElementText().trim());
                }

                // Amounts
                if (epdFlag && isNamedAs(name, COMMON_NAMESPACE_URI, "other")) {
                    List<Amount> parsedEpdAmounts = parseEpdAmounts(reader);

                    if (parsedEpdAmounts != null && !parsedEpdAmounts.isEmpty())
                        epdAmounts.addAll(parsedEpdAmounts);
                }
            }

            // When we reach the end we return what we've collected
            if (isNamedEndElement(reader)) {
                name = reader.getName();
                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "LCIAResult")) {
                    if (methodReferenceUuid == null || methodReferenceUuid.trim().isEmpty())
                        return null;

                    // else
                    LciaResult result = new LciaResult(methodReferenceUuid);
                    result.setMeanAmount(meanAmount);
                    result.addAllAmounts(epdAmounts);
                    return result;
                }

                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "processDataSet"))
                    throw new XMLStreamException("XML malformed: </processDataSet> before expected </lciaResult> at "
                            + printLocation(reader));
            }

            if (isEndDocument(reader))
                throw new XMLStreamException(
                        "XML malformed: Document ended before expected </processDataSet> at " + printLocation(reader));
        }

        return null;
    }

    // ------------ //
    // ** UTILS ** //
    // ---------- //

    private List<Amount> parseEpdAmounts(XMLStreamReader reader) throws XMLStreamException {
        List<Amount> amountList = new ArrayList<Amount>();

        while (reader.hasNext()) {
            reader.next(); // We entered with the reader pointing at enveloping 'common:other' element.
            QName name;

            if (isNamedStartElement(reader)) {
                name = reader.getName();

                if (isNamedAs(name, EPD_NAMESPACE_URI, "amount")) {
                    String module = reader.getAttributeValue(EPD_NAMESPACE_URI, "module");
                    String scenario = reader.getAttributeValue(EPD_NAMESPACE_URI, "scenario");

                    Amount amount = new Amount(module, scenario);
                    Double value = parseDouble(reader.getElementText().trim());
                    amount.setValue(value);
                    amountList.add(amount);
                }
            }

            if (isNamedEndElement(reader)) {
                name = reader.getName();
                if (isNamedAs(name, COMMON_NAMESPACE_URI, "other"))
                    return amountList;

                if (isNamedAs(name, PROCESS_NAMESPACE_URI, "processDataSet"))
                    throw new XMLStreamException(
                            "XML malformed: </processDataSet> before expected </other> at " + printLocation(reader));
            }

            if (isEndDocument(reader))
                throw new XMLStreamException(
                        "XML malformed: Document ended before expected </processDataSet> at " + printLocation(reader));
        }

        return amountList;
    }

    /**
     * For each Exchange in the database a tripel (exchangeId, exchangeInternalId
     * and processId) is collected and stored in the exchangeIdResolver Map, by
     * concatenating "processId" + ":" + "exchangeInternalId" as key and setting the
     * exchangeId as value.
     *
     * @return true if successful (alias data to resolve exchanges has been found)
     */
    private Map<String, Long> generateExchangeIdResolver(List<Long> ids) {

        String batchConstraintClause = ids.stream()
                .map(id -> "process_exchange.Process_ID = '" + String.valueOf(id) + "'")
                .collect(Collectors.joining(" OR "));

        List<Map<String, Long>> labeledIdTripels = jdbcTemplate
                .query("SELECT exchange.ID as 'exchange_id',"
                                + " exchange.INTERNALID as 'idWithinProcess',"
                                + " process_exchange.Process_ID as 'process_id'"

                                + " FROM exchange"
                                + " JOIN process_exchange ON exchange.ID = process_exchange.exchanges_ID "

                                + " WHERE (" + batchConstraintClause + ")",

                        (rs, rowNum) -> {
                            try {
                                Map<String, Long> labeledTypedResultSet = new Hashtable<String, Long>();

                                // The 'getString' method handles nulls more gracefully than 'getLong': It
                                // returns null instead of 0, because it doesn't need to cast to natives. We
                                // won't assume the db is 100% sound.
                                String exchange_idString = rs.getString("exchange_id");
                                String idWithinProcessString = rs.getString("idWithinProcess");
                                String process_idString = rs.getString("process_id");

                                if (isEmptyString(exchange_idString) || isEmptyString(idWithinProcessString) || isEmptyString(process_idString)) {
                                    LOGGER.warn(
                                            "DB seems to have inconsistencies: `exchange.ID`, `exchange.INTERNALID` and `process_exchange.Process_ID` should never be blank. Yet we found"
                                                    + "[`exchange.ID`:'" + exchange_idString
                                                    + "', `exchange.INTERNALID`:'" + idWithinProcessString
                                                    + "', `process_exchange.Process_ID`:'" + process_idString + "']");
                                    return null;
                                }

                                labeledTypedResultSet.put("exchange_id", Long.parseLong(exchange_idString));
                                labeledTypedResultSet.put("idWithinProcess", Long.parseLong(idWithinProcessString));
                                labeledTypedResultSet.put("process_id", Long.parseLong(process_idString));
                                return labeledTypedResultSet;
                            } catch (Exception e) {
                                return null;
                            }
                        });

        Map<String, Long> exchangeIdResolver = new Hashtable<String, Long>();
        labeledIdTripels.stream().filter(t -> t != null)
                .forEach(t -> {
                    String key = String.valueOf(t.get("process_id")) + ":" + String.valueOf(t.get("idWithinProcess"));

                    exchangeIdResolver.put(key, t.get("exchange_id"));
                });
        labeledIdTripels.clear();
        return exchangeIdResolver;
    }

    /**
     * For each LciaResult in the database a tripel (lciaResultId, processId,
     * methodReferenceId) is collected and stored in the lciaResultIdResolverMap, by
     * concatenating "processId" + ":" + "methodReferenceUuid" as key and setting
     * the lciaResultId as value.
     *
     * @return true if successful (alias data to resolve LciaResults has been found)
     */
    private Map<String, Long> generateLciaResultIdResolver(List<Long> ids) {

        String batchConstraintClause = ids.stream()
                .map(id -> "process_lciaresult.Process_ID = '" + String.valueOf(id) + "'")
                .collect(Collectors.joining(" OR "));

        List<Map<String, Object>> labeledIdTriples = jdbcTemplate
                .query("SELECT lciaresult.ID as 'lciaResult_id',"
                                + " process_lciaresult.Process_ID as 'process_id',"
                                + " globalreference.UUID as 'methodReferenceUuid',"
                                + " globalreference.ID as 'refID'" // This one is used for debugging only.

                                + " FROM lciaresult JOIN process_lciaresult ON process_lciaresult.lciaResults_ID = lciaresult.ID"
                                + " JOIN globalreference ON globalreference.ID = lciaresult.METHODREFERENCE_ID"

                                + " WHERE (" + batchConstraintClause + ")",

                        (rs, rowNum) -> {
                            try {
                                Map<String, Object> labeledResultSet = new Hashtable<String, Object>();

                                String methodReferenceUuid = rs.getString("methodReferenceUuid");

                                // The 'getString' method handles nulls more gracefully than 'getLong': It
                                // returns null instead of 0, because it doesn't need to cast to natives. We
                                // won't assume the db is 100% sound.
                                String lciaResult_idString = rs.getString("lciaResult_id");
                                String process_idString = rs.getString("process_id");

                                if (isEmptyString(methodReferenceUuid)) {
                                    LOGGER.warn("DB seems to have inconsistencies: We found the value '"
                                            + methodReferenceUuid
                                            + "' within Column `globalreference.UUID`. Note that `globalreference.ID` was '"
                                            + rs.getString("refID") + "'.");
                                    return null;
                                }

                                if (isEmptyString(lciaResult_idString) || isEmptyString(process_idString)) {
                                    LOGGER.warn(
                                            "DB seems to have inconsistencies: `lciaresult.ID` and `process_lciaresult.Process_ID` should never be blank. Yet we found [`lciaresult.ID`:'"
                                                    + lciaResult_idString + "', `process_lciaresult.Process_ID`:'"
                                                    + process_idString + "']");
                                    return null;
                                }

                                Long process_id = Long.parseLong(process_idString);
                                Long lciaResult_id = Long.parseLong(lciaResult_idString);

                                labeledResultSet.put("lciaResult_id", lciaResult_id);
                                labeledResultSet.put("process_id", process_id);
                                labeledResultSet.put("methodReferenceUuid", methodReferenceUuid.toLowerCase());

                                return labeledResultSet;
                            } catch (Exception e) {
                                return null;
                            }
                        });

        Map<String, Long> lciaResultIdResolver = new Hashtable<String, Long>();
        labeledIdTriples.stream().filter(t -> t != null)
                .forEach(t -> {
                    String key = String.valueOf((Long) t.get("process_id")) + ":" + (String) t.get("methodReferenceUuid");

                    lciaResultIdResolver.put(key, (Long) t.get("lciaResult_id"));
                });
        labeledIdTriples.clear();
        return lciaResultIdResolver;
    }

    private Double parseDouble(String s) {
        if (isEmptyString(s))
            return null;

        // Otherwise try
        Double d = null;
        try {
            d = Double.parseDouble(s);
        } catch (Exception ex) {
            // returns null.
        }
        return d;
    }

    private boolean isEmptyString(String s) {
        return (s == null || s.trim().isEmpty());
    }

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

    private String printLocation(XMLStreamReader reader) {
        Location location = reader.getLocation();
        StringBuilder sb = new StringBuilder("[line:");
        sb.append(location.getLineNumber());
        sb.append(", column:");
        sb.append(location.getColumnNumber());
        sb.append("]");
        return sb.toString();
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

        private long process_id;

        private byte[] compressedContent;

        XmlFile(long process_id) {
            this.process_id = process_id;
        }

        public void setCompressedContent(byte[] compressedXml) {
            this.compressedContent = compressedXml;
        }

        /**
         * The xml class handles parsing, because it handles decompression and we can
         * avoid reopening a stream that way.
         *
         * @param exchangeIdResolver
         * @param lciaResultIdResolver
         * @return
         * @throws XMLStreamException
         */
        public DataExtract parseAndResolve(Map<String, Long> exchangeIdResolver, Map<String, Long> lciaResultIdResolver)
                throws XMLStreamException {
            if (this.compressedContent == null || this.compressedContent.length == 0)
                return null;

            try (ZipFile z = new ZipFile(new SeekableInMemoryByteChannel(this.compressedContent),
                    StandardCharsets.UTF_8.displayName());) {
                Enumeration<ZipArchiveEntry> e = z.getEntries();
                InputStream is = z.getInputStream(e.nextElement());
                return parseXml(is, process_id, exchangeIdResolver, lciaResultIdResolver);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        long getProcess_id() {
            return process_id;
        }
    }

    /**
     * Holds all relevant data for a single process.
     */
    private class DataExtract {

        final Long process_id; // Id within db

        List<Exchange> exchanges = new ArrayList<Exchange>();

        List<LciaResult> lciaResults = new ArrayList<LciaResult>();

        DataExtract(Long process_id) {
            super();
            this.process_id = process_id;
        }

        /**
         * An instance of DataExtract always knows it's process id, which is the last
         * missing piece of information, to resolve the id (as in row number) of the
         * corresponding parsed sub-entities. So we resolve their ids at the moment we
         * add them.
         *
         * @param exs
         * @param resolver
         */
        void addAllExchanges(List<Exchange> exs, Map<String, Long> resolver) {
            if (exs != null && exs.size() > 0) {
                exs.stream().filter(e -> e != null).map(e -> e.resolveExchange_Id(this.process_id, resolver))
                        .filter(e -> e != null).forEach(this.exchanges::add);
            }
        }

        /**
         * An instance of DataExtract always knows it's process id, which is the last
         * missing piece of information, to resolve the id (as in row number) of the
         * corresponding parsed sub-entities. So we resolve their ids at the moment we
         * add them.
         *
         * @param lrs
         * @param resolver
         */
        void addAllLciaResults(List<LciaResult> lrs, Map<String, Long> resolver) {
            if (lrs != null && lrs.size() > 0) {
                lrs.stream().filter(lr -> lr != null).map(lr -> lr.resolveLciaResult_Id(this.process_id, resolver))
                        .filter(lr -> lr != null).forEach(this.lciaResults::add);
            }
        }

        List<Exchange> getExchanges() {
            return this.exchanges;
        }

        List<LciaResult> getLciaResults() {
            return this.lciaResults;
        }
    }

    /**
     * Holds the relevant data for a lcia result data set.
     */
    private class LciaResult {

        static final String TABLE_NAME = "lciaresult";

        static final String AMOUNT_TABLE_NAME = TABLE_NAME + "_amounts";

        static final String AMOUNT_TABLE_FOREIGN_KEY_NAME = TABLE_NAME + "_id";
        final String methodReferenceUuid;
        Long lciaResult_id; // Id within db
        Double meanAmount = null;

        List<Amount> amounts = null;

        LciaResult(String methodReferenceUuid) {
            super();
            if (!isEmptyString(methodReferenceUuid))
                this.methodReferenceUuid = methodReferenceUuid.toLowerCase();
            else
                this.methodReferenceUuid = null;
        }

        /**
         * Resolves the database id of this instance and signs all amounts.
         *
         * @param process_id
         * @return this instance or null. This behaviour is convenient when used in<br/>
         * <code>stream.map(lr -> lr.resolveLciaResult_Id(process_id)).filter(lr != null)...</code>
         */
        LciaResult resolveLciaResult_Id(Long process_id, Map<String, Long> resolver) {
            if (process_id != null && !isEmptyString(this.methodReferenceUuid)) {
                Long id = resolver.get(String.valueOf(process_id) + ":" + methodReferenceUuid);

                // if successful return this instance
                if (id != null) {
                    this.lciaResult_id = id;
                    signAmounts();
                    return this;
                }
            }
            return null; // We can't do anything with this set of data anyway.
        }

        /**
         * This method makes sure all amounts are signed before they are added.
         *
         * @param amountList
         */
        void addAllAmounts(List<Amount> amountList) {
            if (amountList != null && !amountList.isEmpty()) {
                amountList.stream().forEach(this::sign);

                if (this.amounts == null)
                    this.amounts = amountList;
                else
                    this.amounts.addAll(amountList);
            }
        }

        void signAmounts() {
            if (amounts != null) {
                amounts.stream().forEach(this::sign);
            }
        }

        Amount sign(Amount a) {
            if (a != null)
                a.resolveForeignKey(lciaResult_id);
            return a;
        }

        Double getMeanAmount() {
            return this.meanAmount;
        }

        void setMeanAmount(Double d) {
            this.meanAmount = d;
        }

        Long getLciaResult_Id() {
            return this.lciaResult_id;
        }

        List<Amount> getAmounts() {
            return amounts;
        }

        boolean hasAmounts() {
            return (amounts != null && amounts.size() > 0);
        }
    }

    /**
     * Holds the relevant data for an exchange data set.
     */
    private class Exchange {

        static final String TABLE_NAME = "exchange";

        static final String AMOUNT_TABLE_NAME = TABLE_NAME + "_amounts";

        static final String AMOUNT_TABLE_FOREIGN_KEY_NAME = TABLE_NAME + "_id";
        final Long idWithinProcess;
        Long exchange_id = null; // Id within db
        Double maxAmount = null;

        Double minAmount = null;

        Double meanAmount = null;

        Double resultingAmount = null;

        List<Amount> amounts = null;

        Exchange(Long idWithinProcess) {
            super();
            this.idWithinProcess = idWithinProcess;
        }

        /**
         * @param process_id
         * @return this instance or null. This behaviour is convenient when used in<br/>
         * <code>stream.map(e -> e.resolveLciaResult_Id(process_id)).filter(e != null)...</code>
         */
        Exchange resolveExchange_Id(Long process_id, Map<String, Long> resolver) {
            if (process_id != null && idWithinProcess != null) {
                Long id = resolver.get(String.valueOf(process_id) + ":" + String.valueOf(idWithinProcess));

                // if successful return this instance
                if (id != null) {
                    this.exchange_id = id;
                    signAmounts();
                    return this;
                }
            }
            return null; // We can't do anything with this set of data anyway.
        }

        /**
         * This method makes sure all amounts are signed before they are added.
         *
         * @param amountList
         */
        void addAllAmounts(List<Amount> amountList) {
            if (amountList != null && !amountList.isEmpty()) {
                amountList.stream().forEach(this::sign);

                if (this.amounts == null)
                    this.amounts = amountList;
                else
                    this.amounts.addAll(amountList);
            }
        }

        void signAmounts() {
            if (amounts != null) {
                amounts.stream().forEach(this::sign);
            }
        }

        Amount sign(Amount a) {
            if (a != null)
                a.resolveForeignKey(exchange_id);
            return a;
        }

        Long getExchange_id() {
            return this.exchange_id;
        }

        Double getMaxAmount() {
            return maxAmount;
        }

        void setMaxAmount(Double maxAmount) {
            this.maxAmount = maxAmount;
        }

        Double getMinAmount() {
            return minAmount;
        }

        void setMinAmount(Double minAmount) {
            this.minAmount = minAmount;
        }

        Double getMeanAmount() {
            return meanAmount;
        }

        void setMeanAmount(Double meanAmount) {
            this.meanAmount = meanAmount;
        }

        Double getResultingAmount() {
            return resultingAmount;
        }

        void setResultingAmount(Double resultingAmount) {
            this.resultingAmount = resultingAmount;
        }

        List<Amount> getAmounts() {
            return amounts;
        }

        boolean hasAmounts() {
            return (amounts != null && amounts.size() > 0);
        }
    }

    /**
     * Holds amount data.
     */
    private class Amount {

        final String module;
        final String scenario;
        Long foreignKey;
        Double value = null;

        Amount(String module, String scenario) {
            super();

            // The following are used for identification (unique in combination with id of
            // exchange resp. lciaResult)
            this.module = module;
            this.scenario = scenario;
        }

        void resolveForeignKey(Long ownerId) {
            foreignKey = ownerId;
        }

        Double getValue() {
            return value;
        }

        void setValue(Double value) {
            this.value = value;
        }

        String getModule() {
            return module;
        }

        String getScenario() {
            return scenario;
        }

        Long getForeignKey() {
            return foreignKey;
        }

        boolean hasForeignKey() {
            return foreignKey != null;
        }
    }
}
