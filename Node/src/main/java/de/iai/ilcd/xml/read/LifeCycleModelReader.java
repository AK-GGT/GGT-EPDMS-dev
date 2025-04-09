package de.iai.ilcd.xml.read;

import de.fzk.iai.ilcd.api.app.lifecyclemodel.LifeCycleModelDataSet;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Namespace;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * @author MK
 * @since soda4LCA 5.7.0
 */

public class LifeCycleModelReader extends DataSetReader<LifeCycleModel> {

    /**
     * Namespace for the {@link LifeCycleModel}
     */
    public final static Namespace NAMESPACE_LIFECYCLEMODEL = Namespace.getNamespace("ilcd",
            "http://eplca.jrc.ec.europa.eu/ILCD/LifeCycleModel/2017");
    private static final Logger logger = LogManager.getLogger(LifeCycleModelReader.class);

    /**
     * Since parse is an abstract method, it has to be implemented. parseStream
     * method handles everything and returns LifeCycleModel entity.
     */
    @Override
    protected LifeCycleModel parse(JXPathContext context, PrintWriter out) {
//		context.registerNamespace("ilcd", "http://eplca.jrc.ec.europa.eu/ILCD/LifeCycleModel/2017");

        logger.error("Old parse method has been called.");
        return null;
    }

    /**
     * before reading data from stream and returning a dataset a copy of the
     * contents is kept to be parsed with jaxb since jaxb doesn't use the
     * JXPathContext at all parse stream
     */
    @Override
    public LifeCycleModel parseStream(InputStream inputStream, PrintWriter out) {
        LifeCycleModelDataSet lcmds = null;

        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(LifeCycleModelDataSet.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            lcmds = (LifeCycleModelDataSet) jaxbUnmarshaller.unmarshal(inputStream);

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return new LifeCycleModel(lcmds);
    }

}
