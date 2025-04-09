package de.iai.ilcd.xml.read;

import de.fzk.iai.ilcd.service.model.common.ILString;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.model.common.*;
import de.iai.ilcd.model.common.exception.FormatException;
import de.iai.ilcd.model.dao.LanguageDao;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.xml.JDOMParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Reader for data sets
 *
 * @param <T> type of data set
 */
public abstract class DataSetReader<T extends DataSet> {

    /**
     * Logger
     */
    private static final Logger logger = LogManager.getLogger(DataSetReader.class);

    /**
     * Parsing helper
     */
    protected DataSetParsingHelper parserHelper = null;

    /**
     * Reader for common constructs
     */
    protected CommonConstructsReader commonReader = null;

    protected LanguageDao languageDao = new LanguageDao();

    /**
     * Parse the data set
     *
     * @param context context
     * @param out     output writer
     * @return parsed data set
     */
    protected abstract T parse(JXPathContext context, PrintWriter out);

    /**
     * Read data set from file
     *
     * @param pathString path of the file
     * @param out        output writer
     * @return created data set
     * @throws FileNotFoundException        if file was not found
     * @throws IOException                  on I/O errors
     * @throws UnsupportedEncodingException if encoding is not supported
     */
    public T readFromFile(String pathString, PrintWriter out) throws FileNotFoundException, IOException, UnsupportedEncodingException {
        Path filePath = Paths.get(pathString).toAbsolutePath();
        return this.readDataSetFromFile(filePath, out);
    }

    /**
     * Read data set from file
     *
     * @param filePath file to read from
     * @param out      output writer
     * @return created data set
     * @throws FileNotFoundException        if file was not found
     * @throws IOException                  on I/O errors
     * @throws UnsupportedEncodingException if encoding is not supported
     */
    public T readDataSetFromFile(Path filePath, PrintWriter out) throws FileNotFoundException, IOException, UnsupportedEncodingException {
        // Nonsense handling
        if (filePath == null)
            throw new IllegalArgumentException("Warning: File must not be null");
        else if (!Files.exists(filePath)) {
            String msg = "Warning: File '" +
                    filePath.getFileName() +
                    "' was not found at " +
                    filePath.getParent();

            out.println(msg);
            throw new FileNotFoundException(msg);
        }

        T dataSet;
        String errorMsg = "Warning: Failed to process file '" + filePath.getFileName() + "'.";
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            dataSet = this.readDataSetFromStream(inputStream, out);
        } catch (UnsupportedEncodingException uee) {
            out.println(errorMsg + " Encoding not supported.");
            throw uee;
        } catch (IOException ioe) {
            out.println(errorMsg);
            throw ioe;
        }

        return dataSet;
    }


    /**
     * Read data set from stream
     *
     * @param inStream input stream
     * @param out      output writer
     * @return created data set
     * @throws IOException                  on I/O errors
     * @throws UnsupportedEncodingException if encoding is not supported
     */
    public T readDataSetFromStream(InputStream inStream, PrintWriter out) throws IOException, UnsupportedEncodingException {
        byte[] contents;
        try (InputStream in = inStream) {
            contents = IOUtils.toByteArray(in);
        } catch (IOException e) {
            logger.error("cannot read the whole input stream into byte array");
            throw e;
        }

        T dataSet;
        try (InputStream inBytes = new ByteArrayInputStream(contents)) {
            dataSet = this.parseStream(inBytes, out);
        }

        XmlFile xmlFile = new XmlFile();
        String xml;
        xml = new String(contents, StandardCharsets.UTF_8);
        // if BOM is in front of ByteArray, we may have some unknown characters in front of <xml because
        // UTF-8 conversion in Standard Java has problems with BOM, let's just filter these nasty characters
        if (!xml.startsWith("<")) {
            xml = xml.trim().replaceFirst("^([\\W]+)<", "<"); // remove junk at front of dataset
        }
        xmlFile.setContent(xml);
        dataSet.setXmlFile(xmlFile);

        return dataSet;
    }

    /**
     * Parse input stream
     *
     * @param inputStream input stream to parse
     * @param out         output writer
     * @return parsed data set
     */
    public T parseStream(InputStream inputStream, PrintWriter out) {
        JDOMParser parser = new JDOMParser();
        parser.setValidating(false);
        Object doc = parser.parseXML(inputStream);
        JXPathContext context = JXPathContext.newContext(doc);
        context.setLenient(true);
        context.registerNamespace("common", "http://lca.jrc.it/ILCD/Common");

        this.parserHelper = new DataSetParsingHelper(context);
        this.commonReader = new CommonConstructsReader(this.parserHelper);

        return this.parse(context, out);
    }

    /**
     * Read the common fields
     *
     * @param dataset     data set to assign values to
     * @param dataSetType type of the data set
     * @param context     context
     * @param out         output writer
     */
    public void readCommonFields(DataSet dataset, DataSetType dataSetType, JXPathContext context, PrintWriter out) {

        IMultiLangString name = this.parserHelper.getIMultiLanguageString("/*/*[1]/*[local-name()='dataSetInformation']/common:name");
        Uuid uuid = this.commonReader.getUuid();
        IMultiLangString generalComment = this.parserHelper.getIMultiLanguageString("/*/*[1]/*[local-name()='dataSetInformation']/common:generalComment");
        String permanentUri = this.parserHelper.getStringValue("/*/*[local-name()='administrativeInformation']/*[local-name()='publicationAndOwnership']/common:permanentDataSetURI");
        String versionString = this.parserHelper.getStringValue("/*/*[local-name()='administrativeInformation']/*[local-name()='publicationAndOwnership']/common:dataSetVersion");

        dataset.setName(name);

        // set supported languages
        for (ILString l : name.getLStrings()) {
            Language lang = languageDao.getByLanguageCode(l.getLang());
            dataset.getSupportedLanguages().add(lang);
        }

        dataset.setUuid(uuid);
        dataset.setDescription(generalComment);
        dataset.setPermanentUri(permanentUri);
        DataSetVersion version = new DataSetVersion();
        try {
            if (versionString != null) {
                version = DataSetVersion.parse(versionString);
            } else {
                if (out != null) {
                    out.println("Warning: This data set has no version number; version will be set to 00.00.000");
                }
            }
        } catch (FormatException ex) {
            if (out != null) {
                out.println("Warning: The version number has an invalid format. See below for details.");
                out.println("Exception output: " + ex.getMessage());
            }
        }
        dataset.setVersion(version);

        // get the list of classifications and add them
        List<Classification> classifications = this.commonReader.getClassifications(dataSetType);
        for (Classification c : classifications) {
            dataset.addClassification(c);
        }
    }

    public DataSetParsingHelper getParserHelper() {
        return parserHelper;
    }

    public void setParserHelper(DataSetParsingHelper parserHelper) {
        this.parserHelper = parserHelper;
    }

    public CommonConstructsReader getCommonReader() {
        return commonReader;
    }

    public void setCommonReader(CommonConstructsReader commonReader) {
        this.commonReader = commonReader;
    }
}