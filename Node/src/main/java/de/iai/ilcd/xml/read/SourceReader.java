package de.iai.ilcd.xml.read;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.PublicationTypeValue;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.DigitalFile;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.source.Source;
import org.apache.commons.jxpath.JXPathContext;

import java.io.PrintWriter;
import java.util.List;

/**
 * Reader for {@link Source} data sets
 */
public class SourceReader extends DataSetReader<Source> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Source parse(JXPathContext context, PrintWriter out) {
        context.registerNamespace("ilcd", "http://lca.jrc.it/ILCD/Source");

        Source source = new Source();

        // OK, now read in all fields common to all DataSet types
        this.readCommonFields(source, DataSetType.SOURCE, context, out);

        IMultiLangString shortName = this.parserHelper.getIMultiLanguageString("/ilcd:sourceDataSet/ilcd:sourceInformation/ilcd:dataSetInformation/common:shortName");
        IMultiLangString citation = this.parserHelper.getIMultiLanguageString("/ilcd:sourceDataSet/ilcd:sourceInformation/ilcd:dataSetInformation/ilcd:sourceCitation");
        String publicationType = this.parserHelper.getStringValue("/ilcd:sourceDataSet/ilcd:sourceInformation/ilcd:dataSetInformation/ilcd:publicationType");
        PublicationTypeValue publicationTypeValue = PublicationTypeValue.UNDEFINED;
        if (publicationType != null) {
            try {
                publicationTypeValue = PublicationTypeValue.fromValue(publicationType);
            } catch (Exception e) {
                if (out != null) {
                    out.println("Warning: the field publicationType has an illegal value " + publicationType);
                }
            }
        }

        IMultiLangString description = this.parserHelper.getIMultiLanguageString("/ilcd:sourceDataSet/ilcd:sourceInformation/ilcd:dataSetInformation/ilcd:sourceDescriptionOrComment");

        source.setShortName(shortName);
        source.setName(shortName);
        source.setCitation(citation);
        source.setPublicationType(publicationTypeValue);
        source.setDescription(description);

        List<String> digitalFileReferences = this.parserHelper.getStringValues("/ilcd:sourceDataSet/ilcd:sourceInformation/ilcd:dataSetInformation/ilcd:referenceToDigitalFile", "uri");
        for (String digitalFileReference : digitalFileReferences) {
            DigitalFile digitalFile = new DigitalFile();
            digitalFile.setFileName(digitalFileReference);
            source.addFile(digitalFile);
        }

        List<GlobalReference> contacts = this.commonReader.getGlobalReferences("/ilcd:sourceDataSet/ilcd:sourceInformation/ilcd:dataSetInformation/ilcd:referenceToContact", out);
        for (GlobalReference contact : contacts) {
            source.addContact(contact);
        }

        return source;
    }

}
