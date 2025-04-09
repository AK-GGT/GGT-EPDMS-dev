package de.iai.ilcd.util;

import de.fzk.iai.ilcd.api.binding.helper.DatasetHelper;
import de.iai.ilcd.model.common.DataSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class UnmarshalHelper {

    private static final Logger LOGGER = LogManager.getLogger(UnmarshalHelper.class);

    public de.fzk.iai.ilcd.api.dataset.DataSet unmarshal(DataSet dataset) {

        de.fzk.iai.ilcd.api.dataset.DataSet xmlDataset = null;

        try {
            DatasetHelper helper = new DatasetHelper();
            String xmlFile = dataset.getXmlFile().getContent();
            InputStream bais = new ByteArrayInputStream(xmlFile.getBytes("UTF-8"));
            xmlDataset = helper.unmarshal(bais, dataset.getDataSetType().getILCDType());
        } catch (JAXBException ex) {
            LOGGER.error("cannot unmarshal xml information from type " + dataset.getDataSetType().getILCDType());
            LOGGER.error("stack trace is: ", ex);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("cannot unmarshal xml information from type " + dataset.getDataSetType().getILCDType());
            LOGGER.error("stack trace is: ", ex);
        }

        return xmlDataset;
    }
}
