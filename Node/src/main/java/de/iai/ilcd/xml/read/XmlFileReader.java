package de.iai.ilcd.xml.read;

import de.iai.ilcd.model.common.XmlFile;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class XmlFileReader {

    private static final Logger logger = LogManager.getLogger(XmlFileReader.class);

    public void readFile(InputStream inStream) throws IOException {

        byte[] contents = null;
        try {
            contents = IOUtils.toByteArray(inStream);
        } catch (IOException e) {
            logger.error("cannot read the whole input stream into byte array");
            throw e;
        }
        IOUtils.closeQuietly(inStream);

        XmlFile xmlFile = new XmlFile();
        String xml;
        try {
            xml = new String(contents, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("cannot parse stream input into an UTF-8 string");
            logger.error("Exception is: ", e);
            throw e;
        }
        // if BOM is in front of ByteArray, we may have some unknown characters in front of <xml because
        // UTF-8 conversion in Standard Java has problems with BOM, let's just filter these nasty characters
        if (!xml.startsWith("<")) {
            xml = xml.trim().replaceFirst("^([\\W]+)<", "<"); // remove junk at front of dataset
        }
        xmlFile.setContent(xml);

    }

}
