package de.iai.ilcd.xml.read;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import org.apache.commons.jxpath.JXPathContext;

import java.io.PrintWriter;

/**
 * @author clemens.duepmeier
 */
public class FlowPropertyReader extends DataSetReader<FlowProperty> {

    @Override
    public FlowProperty parse(JXPathContext context, PrintWriter out) {

        context.registerNamespace("ilcd", "http://lca.jrc.it/ILCD/FlowProperty");

        FlowProperty flowProperty = new FlowProperty();

        // OK, now read in all fields common to all DataSet types
        this.readCommonFields(flowProperty, DataSetType.FLOWPROPERTY, context, out);

        IMultiLangString synonyms = this.parserHelper.getIMultiLanguageString("/ilcd:flowPropertyDataSet/ilcd:flowPropertiesInformation/ilcd:dataSetInformation/common:synonyms");
        flowProperty.setSynonyms(synonyms);

        GlobalReference refToUnitGroup = this.commonReader.getGlobalReference("/ilcd:flowPropertyDataSet/ilcd:flowPropertiesInformation/ilcd:quantitativeReference/ilcd:referenceToReferenceUnitGroup", out);
        flowProperty.setReferenceToUnitGroup(refToUnitGroup);

        return flowProperty;
    }
}
