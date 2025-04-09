package de.iai.ilcd.xml.read;

import de.fzk.iai.ilcd.service.model.common.ILString;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.TypeOfFlowValue;
import de.iai.ilcd.db.migrations.V1_4__ProcessMaterialProperties;
import de.iai.ilcd.model.common.*;
import de.iai.ilcd.model.flow.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author clemens.duepmeier
 */
public class FlowReader extends DataSetReader<Flow> {

    public static Logger logger = LogManager.getLogger(de.iai.ilcd.xml.read.FlowReader.class);

    private final Namespace flowNamespace = Namespace.getNamespace("ilcd", "http://lca.jrc.it/ILCD/Flow");

    private final Namespace commonNamespace = Namespace.getNamespace("common", "http://lca.jrc.it/ILCD/Common");

    private final Namespace epdNamespace = Namespace.getNamespace("epd", "http://www.iai.kit.edu/EPD/2013");

    private final Namespace matmlNamespace = Namespace.getNamespace("matml", "http://www.matml.org/");

    /**
     * Read the material properties.
     * <p>
     * <i>This method is static because it is also used by migration {@link V1_4__ProcessMaterialProperties} and returns
     * a list of material properties in order not to create flow dummy objects during migration.</i>
     * </p>
     *
     * @param parserHelper   helper for parsing
     * @param matmlNamespace name space for MatML
     * @return list of material properties
     */
    public static List<MaterialProperty> readMaterialProperties(DataSetParsingHelper parserHelper, Namespace matmlNamespace) {
        List<MaterialProperty> foo = new ArrayList<MaterialProperty>();
        /*
         * for each //mm:Metadata/mm:PropertyDetails
         * read @id
         * read mm:Name
         * for each id
         * read //mm:Material/mm:BulkDetails/mm:PropertyData[@property='id']/mm:Data/text()
         */

        List<Element> declaredPropertiesElements = parserHelper.getElements("/ilcd:flowDataSet/ilcd:flowInformation/ilcd:dataSetInformation/common:other/matml:MatML_Doc/matml:Metadata/matml:PropertyDetails");

        String[][] properties = new String[declaredPropertiesElements.size()][4];

        for (int i = 0; i < declaredPropertiesElements.size(); i++) {
            Element e = declaredPropertiesElements.get(i);
            properties[i][0] = e.getAttributeValue("id");
            properties[i][1] = e.getChildTextTrim("Name", matmlNamespace).toLowerCase();
            Element u = e.getChild("Units", matmlNamespace);
            if (u != null) {
                properties[i][2] = u.getAttributeValue("name");
                properties[i][3] = u.getAttributeValue("description");
            }
        }

        for (int i = 0; i < properties.length; i++) {
            MaterialPropertyDefinition propDefinition = new MaterialPropertyDefinition();
            propDefinition.setName(properties[i][1]);
            propDefinition.setUnit(properties[i][2]);
            propDefinition.setUnitDescription(properties[i][3]);
            MaterialProperty matProperty = new MaterialProperty();
            matProperty.setDefinition(propDefinition);
            String value = parserHelper.getElement("/ilcd:flowDataSet/ilcd:flowInformation/ilcd:dataSetInformation/common:other/matml:MatML_Doc/matml:Material/matml:BulkDetails/matml:PropertyData[@property='" + properties[i][0] + "']/matml:Data").getValue();
            try {
                matProperty.setValue(Double.parseDouble(value));
                foo.add(matProperty);
            } catch (NumberFormatException e) {
                logger.error(e.getMessage());
            }
        }

        if (FlowReader.logger.isDebugEnabled()) {
            for (MaterialProperty mp : foo) {
                FlowReader.logger.debug(mp.getDefinition().getName() + " # " + mp.getDefinition().getUnit() + " # " + mp.getDefinition().getUnitDescription()
                        + " # " + mp.getValue());
            }
        }

        return foo;
    }

    @Override
    public Flow parse(JXPathContext context, PrintWriter out) {

        context.registerNamespace("ilcd", "http://lca.jrc.it/ILCD/Flow");
        context.registerNamespace("epd", "http://www.iai.kit.edu/EPD/2013");
        context.registerNamespace("matml", "http://www.matml.org/");
        context.registerNamespace("ecn", "http://eplca.jrc.ec.europa.eu/ILCD/Extensions/2018/ECNumber");

        String flowTypeString = this.parserHelper.getStringValue("/ilcd:flowDataSet/ilcd:modellingAndValidation/ilcd:LCIMethod/ilcd:typeOfDataSet");
        TypeOfFlowValue flowTypeValue = null;
        try {
            flowTypeValue = TypeOfFlowValue.fromValue(flowTypeString);
        } catch (Exception e) {
            if (out != null) {
                out.println("flow doesn't have a valid flow type (" + flowTypeString + "); will ignore data set");
            }
            return null;
        }
        Flow flow;
        boolean isProduct = !TypeOfFlowValue.ELEMENTARY_FLOW.equals(flowTypeValue);
        if (isProduct) {
            flow = new ProductFlow();
            ((ProductFlow) flow).setType(flowTypeValue);
        } else {
            flow = new ElementaryFlow();
        }

        // OK, now read in all fields common to all DataSet types
        this.readCommonFields(flow, DataSetType.FLOW, context, out);

        // flows don't use standardized common:name tag
        IMultiLangString name = this.parserHelper.getIMultiLanguageString("/ilcd:flowDataSet/ilcd:flowInformation/ilcd:dataSetInformation/ilcd:name/ilcd:baseName");
        logger.debug("reading flow: " + name.getDefaultValue());
        flow.setName(name);

        IMultiLangString synonyms = this.parserHelper.getIMultiLanguageString("/ilcd:flowDataSet/ilcd:flowInformation/ilcd:dataSetInformation/common:synonyms");
        flow.setSynonyms(synonyms);

        if (isProduct) {
            try {
                ProductFlow pFlow = ((ProductFlow) flow);
                pFlow.setSpecificProduct(this.parserHelper.getBooleanValue("/ilcd:flowDataSet/ilcd:modellingAndValidation/ilcd:LCIMethod/common:other/epd:specificProduct"));
                pFlow.setSpecificProduct(this.parserHelper.getBooleanValue("/ilcd:flowDataSet/ilcd:modellingAndValidation/ilcd:LCIMethod/common:other/epd:vendorSpecificProduct"));
                pFlow.setVendorReference(this.commonReader.getGlobalReference("/ilcd:flowDataSet/ilcd:modellingAndValidation/ilcd:LCIMethod/common:other/epd:referenceToVendor", out));
                pFlow.setSourceReference(this.commonReader.getGlobalReference("/ilcd:flowDataSet/ilcd:modellingAndValidation/ilcd:LCIMethod/common:other/epd:referenceToSource", out));
            } catch (Exception e) {
                e.printStackTrace();
            }
            GlobalReference isAReference = this.readIsA(out);
            if (isAReference != null) {
                ((ProductFlow) flow).setIsAReference(isAReference);
            }

            // read material properties
            List<MaterialProperty> mprops = FlowReader.readMaterialProperties(this.parserHelper, this.matmlNamespace);
            if (CollectionUtils.isNotEmpty(mprops)) {
                // added one by one due to bi-directional relationship that is handled by addMaterialProperty
                for (MaterialProperty m : mprops) {
                    ((ProductFlow) flow).addMaterialProperty(m);
                }
            }

        } else {
            Classification flowCategorization = this.getFlowCategorization();
            ((ElementaryFlow) flow).setCategorization(flowCategorization);
        }

        String casNumber = this.parserHelper.getStringValue("/ilcd:flowDataSet/ilcd:flowInformation/ilcd:dataSetInformation/ilcd:CASNumber");
        flow.setCasNumber(casNumber);
        String sumFormula = this.parserHelper.getStringValue("/ilcd:flowDataSet/ilcd:flowInformation/ilcd:dataSetInformation/ilcd:sumFormula");
        flow.setSumFormula(sumFormula);

        String referencePropertyString = this.parserHelper.getStringValue("/ilcd:flowDataSet/ilcd:flowInformation/ilcd:quantitativeReference/ilcd:referenceToReferenceFlowProperty");
        Integer referenceIndex = (referencePropertyString != null ? Integer.parseInt(referencePropertyString) : null);

        List<FlowPropertyDescription> propertyDescriptions = this.readPropertyDescriptions(out);

        if (propertyDescriptions != null) {
            for (FlowPropertyDescription propertyDescription : propertyDescriptions) {
                flow.addPropertDesription(propertyDescription);
                if (referenceIndex != null && propertyDescription.getInternalId() == referenceIndex) {
                    flow.setReferenceProperty(propertyDescription);
                }
            }
        }

        String ecNumber = getECNumber();
        if (StringUtils.isNotBlank(ecNumber))
            flow.setEcNumber(ecNumber);

        IMultiLangString los = getLocationOfSupply();
        if (los != null)
            flow.setLocationOfSupply(los);

        return flow;
    }

    private Classification getFlowCategorization() {
        Element flowCategorization = this.parserHelper.getElement("/ilcd:flowDataSet/ilcd:flowInformation/ilcd:dataSetInformation/ilcd:classificationInformation/common:elementaryFlowCategorization");

        if (flowCategorization == null) {
            return null; // product, waste and other flows dont have elementary
            // flow categorization
        }

        String categorizationName = flowCategorization.getAttributeValue("name");
        if (categorizationName == null) {
            categorizationName = "ilcd";
        }
        Classification classification = new Classification(categorizationName);
        // logger.debug("found classification with name " + classificationName);

        // OK, now we have to read in the classification classes

        List categoryTags = flowCategorization.getChildren("category", this.commonNamespace);
        for (Object categoryTag : categoryTags) {
            Element categoryElement = (Element) categoryTag;
            String level = categoryElement.getAttributeValue("level");
            String uuidStr = categoryElement.getAttributeValue("uuid");
            String id = categoryElement.getAttributeValue("id");
            String className = categoryElement.getText();
            ClClass clClass = new ClClass(className, Integer.parseInt(level));
            if (uuidStr != null) {
                Uuid uuid = new Uuid(uuidStr);
                clClass.setUuid(uuid);
            }
            if (id != null) {
                clClass.setClId(id);
            }
            // logger.debug("found class with name=%s, level=%s\n",className,level);
            classification.addClass(clClass);
        }

        return classification;
    }

    /**
     * Read &quot;is a&quot; relation
     *
     * @param out print writer for output
     * @return list of &quot;is a&quot; relation
     */
    private GlobalReference readIsA(PrintWriter out) {
        Element other = this.parserHelper.getElement("/ilcd:flowDataSet/ilcd:flowInformation/ilcd:dataSetInformation/common:other");

        if (other == null) {
            return null;
        }

        GlobalReference isA = this.commonReader.getGlobalReference(other, "isA", this.epdNamespace, out);

        return isA;
    }

    private List<FlowPropertyDescription> readPropertyDescriptions(PrintWriter out) {
        List<FlowPropertyDescription> propertyDescriptions = new ArrayList<FlowPropertyDescription>();

        List<Element> propertyDescriptionElements = this.parserHelper.getElements("/ilcd:flowDataSet/ilcd:flowProperties/ilcd:flowProperty");

        for (Element elem : propertyDescriptionElements) {
            String internalId = elem.getAttributeValue("dataSetInternalID");
            GlobalReference flowPropertyRef = this.commonReader.getGlobalReference(elem.getChild("referenceToFlowPropertyDataSet", this.flowNamespace), out);
            String meanValueString = null;
            Element meanValueElement = elem.getChild("meanValue", this.flowNamespace);
            if (meanValueElement != null) {
                meanValueString = meanValueElement.getText();
            }
            String minValueString = null;
            Element minValueElement = elem.getChild("minValue", this.flowNamespace);
            if (minValueElement != null) {
                minValueString = minValueElement.getText();
            }
            String maxValueString = null;
            Element maxValueElement = elem.getChild("maximumValue", this.flowNamespace);
            if (maxValueElement != null) {
                maxValueString = maxValueElement.getText();
            }
            String distributionTypeString = null;
            Element dTypeElement = elem.getChild("uncertaintyDistributionType", this.flowNamespace);
            if (dTypeElement != null) {
                distributionTypeString = dTypeElement.getText();
            }
            String standardDeviationString = null;
            Element stdElement = elem.getChild("relativeStandardDeviation95In", this.flowNamespace);
            if (stdElement != null) {
                standardDeviationString = stdElement.getText();
            }
            String derivationTypeString = null;
            Element derivElement = elem.getChild("dataDerivationTypeStatus", this.flowNamespace);
            if (derivElement != null) {
                derivationTypeString = derivElement.getText();
            }
            // logger.debug("name of propertyDescription: " + name);
            IMultiLangString description = this.parserHelper.getIMultiLanguageString(elem, "generalComment", this.flowNamespace);
            // logger.debug("propertyDescription beschreibung: " +
            // description.getDefaultValue());

            FlowPropertyDescription propertyDescription = new FlowPropertyDescription();
            propertyDescription.setInternalId(Integer.parseInt(internalId));
            // logger.debug("parse meanValue: " +meanValue);
            propertyDescription.setFlowPropertyRef(flowPropertyRef);
            if (meanValueString != null) {
                propertyDescription.setMeanValue(Double.parseDouble(meanValueString));
            } else {
                propertyDescription.setMeanValue(0);
            }
            if (maxValueString != null) {
                propertyDescription.setMaxValue(Double.parseDouble(maxValueString));
            } else {
                propertyDescription.setMaxValue(0);
            }
            if (minValueString != null) {
                propertyDescription.setMinValue(Double.parseDouble(minValueString));
            } else {
                propertyDescription.setMinValue(0);
            }
            if (distributionTypeString != null) {
                propertyDescription.setUncertaintyType(distributionTypeString);
            }
            if (standardDeviationString != null) {
                propertyDescription.setStandardDeviation(Float.parseFloat(standardDeviationString));
            } else {
                propertyDescription.setStandardDeviation(0);
            }
            if (derivationTypeString != null) {
                propertyDescription.setDerivationType(derivationTypeString);
            }
            propertyDescription.setDescription(description);

            propertyDescriptions.add(propertyDescription);
        }

        return propertyDescriptions;
    }

    public String getECNumber() {
        Element ecNumber = this.parserHelper.getElement("/ilcd:flowDataSet/ilcd:flowInformation/ilcd:dataSetInformation/common:other/ecn:ECNumber");
        if (ecNumber != null)
            return ecNumber.getTextTrim();
        else
            return null;
    }

    public IMultiLangString getLocationOfSupply() {
        Element geo = this.parserHelper.getElement("/ilcd:flowDataSet/ilcd:flowInformation/ilcd:geography");

        // we're calling this during a migration, hence the call to getSimpleIMultiLanguageString() as this doesn't involve
        // reading the config's defaultLanguage property, which hasn't been initialized yet
        IMultiLangString los = this.parserHelper.getSimpleIMultiLanguageString(geo, "locationOfSupply", flowNamespace);

        if (los != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("LOS is " + los);
                for (ILString l : los.getLStrings())
                    logger.trace("  " + l.getLang() + " " + l.getValue());
            }
            return los;
        } else {
            logger.trace("LOS is null");
            return null;
        }
    }

}
