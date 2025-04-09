package de.iai.ilcd.xml.read;

import de.fzk.iai.ilcd.api.binding.generated.common.ExchangeDirectionValues;
import de.fzk.iai.ilcd.service.client.impl.vo.epd.ProcessSubType;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.common.ILString;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.*;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.common.Language;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.process.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @author clemens.duepmeier
 */
public class ProcessReader extends DataSetReader<Process> {

    public static Logger logger = LogManager.getLogger(de.iai.ilcd.xml.read.ProcessReader.class);

    private final Namespace processNamespace = Namespace.getNamespace("ilcd", "http://lca.jrc.it/ILCD/Process");

    private final Namespace commonNamespace = Namespace.getNamespace("common", "http://lca.jrc.it/ILCD/Common");

    private final Namespace epdNamespace = Namespace.getNamespace("epd", "http://www.iai.kit.edu/EPD/2013");

    private final Namespace epd2Namespace = Namespace.getNamespace("epd2", "http://www.indata.network/EPD/2019");

    private final Namespace ext17Namespace = Namespace.getNamespace("exd", "http://eplca.jrc.ec.europa.eu/ILCD/Extensions/2017");

    @Override
    public Process parse(JXPathContext context, PrintWriter out) {

        context.registerNamespace("ilcd", "http://lca.jrc.it/ILCD/Process");
        context.registerNamespace("pm", "http://iai.kit.edu/ILCD/ProductModel");
        context.registerNamespace(this.epdNamespace.getPrefix(), this.epdNamespace.getURI());
        context.registerNamespace(this.epd2Namespace.getPrefix(), this.epd2Namespace.getURI());

        Process process = new Process();

        // OK, now read in all fields common to all DataSet types
        this.readCommonFields(process, DataSetType.PROCESS, context, out);

        // build the complex name of the process
        IMultiLangString baseName = this.parserHelper.getIMultiLanguageString("/ilcd:processDataSet/ilcd:processInformation/ilcd:dataSetInformation/ilcd:name/ilcd:baseName");
        IMultiLangString treatmentName = this.parserHelper.getIMultiLanguageString("/ilcd:processDataSet/ilcd:processInformation/ilcd:dataSetInformation/ilcd:name/ilcd:treatmentStandardsRoutes");
        IMultiLangString mixAndLocationTypes = this.parserHelper.getIMultiLanguageString("/ilcd:processDataSet/ilcd:processInformation/ilcd:dataSetInformation/ilcd:name/ilcd:mixAndLocationTypes");
        IMultiLangString functionalUnitFlowProperties = this.parserHelper.getIMultiLanguageString("/ilcd:processDataSet/ilcd:processInformation/ilcd:dataSetInformation/ilcd:name/ilcd:functionalUnitFlowProperties");
        process.setBaseName(baseName);
        process.setNameRoute(treatmentName);
        process.setNameLocation(mixAndLocationTypes);
        process.setNameUnit(functionalUnitFlowProperties);

        // set supported languages
        for (ILString l : baseName.getLStrings()) {
            Language lang = languageDao.getByLanguageCode(l.getLang());
            process.getSupportedLanguages().add(lang);
        }

        IMultiLangString synonyms = this.parserHelper.getIMultiLanguageString("/ilcd:processDataSet/ilcd:processInformation/ilcd:dataSetInformation/common:synonyms");
        process.setSynonyms(synonyms);

        String processType = this.parserHelper.getStringValue("/ilcd:processDataSet/ilcd:modellingAndValidation/ilcd:LCIMethodAndAllocation/ilcd:typeOfDataSet");
        if (processType != null) {
            try {
                TypeOfProcessValue processTypeValue = TypeOfProcessValue.fromValue(processType);
                process.setType(processTypeValue);
            } catch (Exception e) {
                logger.error("error setting type, unknown value: " + processType, e.getMessage());
            }
        }

        if (this.parserHelper.getElements("/ilcd:processDataSet/ilcd:processInformation/ilcd:mathematicalRelations/ilcd:variableParameter").size() > 0) {
            process.setParameterized(true);
        } else {
            process.setParameterized(false);
        }

        if (this.parserHelper.getElements("/ilcd:processDataSet/ilcd:processInformation/common:other/pm:productModel").size() > 0) {
            process.setContainsProductModel(true);
        } else {
            process.setContainsProductModel(false);
        }

        String subType = this.parserHelper.getStringValue(
                "/ilcd:processDataSet/ilcd:modellingAndValidation/ilcd:LCIMethodAndAllocation/common:other/epd:subType");
        if (StringUtils.isNotEmpty(subType)) {
            try {
                process.setSubType(ProcessSubType.fromValue(subType));
            } catch (IllegalArgumentException e1) {
            }
        }
        IMultiLangString useAdvice = this.parserHelper.getIMultiLanguageString("/ilcd:processDataSet/ilcd:modellingAndValidation/ilcd:dataSourcesTreatmentAndRepresentativeness/ilcd:useAdviceForDataSet");
        process.setUseAdvice(useAdvice);

        IMultiLangString technicalPurpose = this.parserHelper.getIMultiLanguageString("/ilcd:processDataSet/ilcd:processInformation/ilcd:technology/ilcd:technologicalApplicability");
        process.setTechnicalPurpose(technicalPurpose);

        IMultiLangString technologyDescription = this.parserHelper.getIMultiLanguageString("/ilcd:processDataSet/ilcd:processInformation/ilcd:technology/ilcd:technologyDescriptionAndIncludedProcesses");
        process.setTechnologyDescription(technologyDescription);

        Element locationElement = this.parserHelper.getElement("/ilcd:processDataSet/ilcd:processInformation/ilcd:geography/ilcd:locationOfOperationSupplyOrProduction");
        if (locationElement != null) {
            Geography geography = new Geography();
            String location = locationElement.getAttributeValue("location");
            IMultiLangString locationDescription = this.parserHelper.getIMultiLanguageString(locationElement, "descriptionOfRestrictions",
                    this.processNamespace);
            geography.setLocation(location);
            geography.setDescription(locationDescription);
            process.setGeography(geography);
        }

        TimeInformation timeInformation = new TimeInformation();
        String referenceYearString = this.parserHelper.getStringValue("/ilcd:processDataSet/ilcd:processInformation/ilcd:time/common:referenceYear");
        if (referenceYearString != null && referenceYearString.length() > 0) {
            int referenceYear = Integer.parseInt(referenceYearString);
            timeInformation.setReferenceYear(referenceYear);
        }

        String dataSetValidUntilString = this.parserHelper.getStringValue("/ilcd:processDataSet/ilcd:processInformation/ilcd:time/common:dataSetValidUntil");
        if (dataSetValidUntilString != null && dataSetValidUntilString.length() > 0) {
            int dataSetValidUntil = Integer.parseInt(dataSetValidUntilString);
            timeInformation.setValidUntil(dataSetValidUntil);
        }

        IMultiLangString timeDescription = this.parserHelper.getIMultiLanguageString("/ilcd:processDataSet/ilcd:processInformation/ilcd:time/common:timeRepresentativenessDescription");
        timeInformation.setDescription(timeDescription);
        process.setTimeInformation(timeInformation);

        GlobalReference formatRef = this.commonReader.getGlobalReference("/ilcd:processDataSet/ilcd:administrativeInformation/common:referenceToDataSetFormat", out);
        if (formatRef != null) {
            String format = formatRef.getShortDescription().getDefaultValue();
            process.setFormat(format);
        }
        GlobalReference ownerOfDataSet = this.commonReader.getGlobalReference("/ilcd:processDataSet/ilcd:administrativeInformation/ilcd:publicationAndOwnership/common:referenceToOwnershipOfDataSet", out);
        process.setOwnerReference(ownerOfDataSet);

        List<GlobalReference> precedingDataSet = this.commonReader.getGlobalReferences("/ilcd:processDataSet/ilcd:administrativeInformation/ilcd:publicationAndOwnership/common:referenceToPrecedingDataSetVersion", out);
        if (precedingDataSet != null)
            process.setPrecedingDataSetVersions(new HashSet<GlobalReference>(precedingDataSet));

        LCIMethodInformation lciMethodInformation = new LCIMethodInformation();
        String lciMethodPrinciple = this.parserHelper.getStringValue("/ilcd:processDataSet/ilcd:modellingAndValidation/ilcd:LCIMethodAndAllocation/ilcd:LCIMethodPrinciple");

        if (lciMethodPrinciple != null) {
            LCIMethodPrincipleValue principleValue = LCIMethodPrincipleValue.OTHER;
            try {
                principleValue = LCIMethodPrincipleValue.fromValue(lciMethodPrinciple);
            } catch (IllegalArgumentException ex) {
                if (out != null) {
                    out.println("Warning: lciMethodPrinciple has the illegal value " + lciMethodPrinciple + "; will set it to 'Other'");
                }
            }

            lciMethodInformation.setMethodPrinciple(principleValue);
        }

        List<String> allocationApproaches = this.parserHelper.getStringValues("/ilcd:processDataSet/ilcd:modellingAndValidation/ilcd:LCIMethodAndAllocation/ilcd:LCIMethodApproaches");
        for (String allocationApproach : allocationApproaches) {
            LCIMethodApproachesValue approachValue = LCIMethodApproachesValue.fromValue(allocationApproach);
            lciMethodInformation.addApproach(approachValue);
        }
        process.setLCIMethodInformation(lciMethodInformation);

        for (IGlobalReference r : getDataSources(GlobalReference.class, out))
            process.getDataSources().add((GlobalReference) r);

        String completeness = this.parserHelper.getStringValue("/ilcd:processDataSet/ilcd:modellingAndValidation/ilcd:completeness/ilcd:completenessProductModel");

        CompletenessValue completenessValue = CompletenessValue.NO_STATEMENT;
        if (completeness != null) {
            try {
                completenessValue = CompletenessValue.fromValue(completeness);
            } catch (IllegalArgumentException e) {
                if (out != null) {
                    out.println("Warning: The field completenessProductModel in the data set has an illegal value");
                }
            }
        }

        process.setCompleteness(completenessValue);
        List<Review> reviews = this.getReviewInformation(out);
        for (Review review : reviews) {
            process.addReview(review);
        }

        AccessInformation accessInformation = new AccessInformation();
        String copyrightString = this.parserHelper.getStringValue("/ilcd:processDataSet/ilcd:administrativeInformation/ilcd:publicationAndOwnership/common:copyright");
        if (copyrightString != null && copyrightString.equals("true")) {
            accessInformation.setCopyright(true);
        } else {
            accessInformation.setCopyright(false);
        }
        String licenseType = this.parserHelper.getStringValue("/ilcd:processDataSet/ilcd:administrativeInformation/ilcd:publicationAndOwnership/common:licenseType");
        LicenseTypeValue licenseTypeValue = null;
        if (licenseType != null) {
            try {
                licenseTypeValue = LicenseTypeValue.fromValue(licenseType);
                accessInformation.setLicenseType(licenseTypeValue);
            } catch (IllegalArgumentException ex) {
                if (out != null) {
                    out.println("Warning: Licence type {%s} is an illegal license type");
                }
            }
        }

        IMultiLangString useRestrictions = this.parserHelper.getIMultiLanguageString("/ilcd:processDataSet/ilcd:administrativeInformation/ilcd:publicationAndOwnership/common:accessRestrictions");

        accessInformation.setUseRestrictions(useRestrictions);

        process.setAccessInformation(accessInformation);
        // OK, now find all compliance declarations
        List<ComplianceSystem> compliances = this.getCompliances(out);
        for (ComplianceSystem compliance : compliances) {
            process.addComplianceSystem(compliance);
        }

        GlobalReference approvedBy = this.commonReader.getGlobalReference("/ilcd:processDataSet/ilcd:administrativeInformation/ilcd:dataEntryBy/common:referenceToDataSetUseApproval", out);

        process.setApprovedBy(approvedBy);

        for (IGlobalReference r : getDatasetGenerator(GlobalReference.class, out))
            process.getDatasetGenerator().add((GlobalReference) r);

        InternalQuantitativeReference quantitativeRef = this.getQuantitativeReference();

        process.setInternalReference(quantitativeRef);
        // OK, now find all exchanges
        List<Exchange> exchanges = this.getExchanges(out);
        if (exchanges != null) {
            for (Exchange exchange : exchanges) {
                process.addExchange(exchange);
            }
            if (exchanges.size() > 0) {
                process.setExchangesIncluded(true);
            }
        }

        // OK, last we need to get LciaResults
        List<LciaResult> results = this.getLciaResults(out);
        if (CollectionUtils.isNotEmpty(results)) { // null and empty safe
            for (LciaResult result : results) {
                process.addLciaResult(result);
            }
            process.setResultsIncluded(true);
        }

        /* EPD extensions */

        // safety margins
        process.setSafetyMargins(this.getSafetyMargins(out));

        // scenarios
        process.setScenarios(this.getScenarios(out));

        //EPD 1.2 Spec
        String epdFormatVersion = this.parserHelper.getElement("ilcd:processDataSet").getAttributeValue("epd-version", this.epd2Namespace);
        process.setEpdFormatVersion(epdFormatVersion);

        Date publicationDateOfEPD = this.parserHelper.getSimpleDate("ilcd:processDataSet/ilcd:processInformation/ilcd:time/common:other/epd2:publicationDateOfEPD");
        process.setPublicationDateOfEPD(publicationDateOfEPD);

        String registrationNumber = this.parserHelper.getStringValue("/ilcd:processDataSet/ilcd:administrativeInformation/ilcd:publicationAndOwnership/common:registrationNumber");
        process.setRegistrationNumber(registrationNumber);

        GlobalReference referenceToRegistrationAuthority = this.commonReader.getGlobalReference("/ilcd:processDataSet/ilcd:administrativeInformation/ilcd:publicationAndOwnership/common:referenceToRegistrationAuthority", out);
        process.setReferenceToRegistrationAuthority(referenceToRegistrationAuthority);

        List<GlobalReference> referencesToOriginalEPDs = this.commonReader.getGlobalReferences("/ilcd:processDataSet/ilcd:modellingAndValidation/ilcd:dataSourcesTreatmentAndRepresentativeness/common:other/epd2:referenceToOriginalEPD", out);
        process.getReferenceToOriginalEPD().addAll(referencesToOriginalEPDs);

        List<GlobalReference> referencesToPublishers = this.commonReader.getGlobalReferences("/ilcd:processDataSet/ilcd:administrativeInformation/ilcd:publicationAndOwnership/common:other/epd2:referenceToPublisher", out);
        process.getReferenceToPublisher().addAll(referencesToPublishers);

        /* end EPD extensions */

        // metaDataOnly flag
        Boolean metaDataOnly = this.parserHelper.getBooleanValue("ilcd:processDataSet/@metaDataOnly");
        process.setMetaDataOnly(metaDataOnly);

        return process;
    }

    /**
     * Get the safety margins
     *
     * @param out output writer
     * @return
     */
    private SafetyMargins getSafetyMargins(PrintWriter out) {
        SafetyMargins safetyMargins = new SafetyMargins();

        try {
            safetyMargins
                    .setMargins(this.parserHelper.getIntValue("/ilcd:processDataSet/ilcd:processInformation/ilcd:dataSetInformation/common:other/epd:safetyMargins/epd:margins"));
            safetyMargins.setDescription(this.parserHelper
                    .getIMultiLanguageString("/ilcd:processDataSet/ilcd:processInformation/ilcd:dataSetInformation/common:other/epd:safetyMargins/epd:description"));
        } catch (Exception e) {
            // nothing to do
        }

        return safetyMargins;
    }

    private List<Scenario> getScenarios(PrintWriter out) {

        List<Scenario> scenarios = new ArrayList<Scenario>();

        List<Element> scenarioElements = this.parserHelper
                .getElements("/ilcd:processDataSet/ilcd:processInformation/ilcd:dataSetInformation/common:other/epd:scenarios/epd:scenario");

        for (Element scenarioElement : scenarioElements) {
            Scenario s = new Scenario();
            s.setName(scenarioElement.getAttributeValue("name", this.epdNamespace));
            s.setGroup(scenarioElement.getAttributeValue("group", this.epdNamespace));
            if (scenarioElement.getAttributeValue("default", this.epdNamespace) != null && scenarioElement.getAttributeValue("default", this.epdNamespace).equalsIgnoreCase("true")) {
                s.setDefault(true);
            }

            IMultiLangString description = this.parserHelper.getIMultiLanguageString(scenarioElement, "description", this.epdNamespace);

            if (description != null) {
                s.setDescription(description);
            }

            scenarios.add(s);
        }

        if (!scenarios.isEmpty()) {
            return scenarios;
        } else {
            return null;
        }
    }

    private List<Review> getReviewInformation(PrintWriter out) {

        List<Review> reviews = new ArrayList<Review>();

        List<Element> reviewElements = this.parserHelper.getElements("/ilcd:processDataSet/ilcd:modellingAndValidation/ilcd:validation/ilcd:review");

        for (Element reviewElement : reviewElements) {
            Review review = new Review();
            String reviewType = reviewElement.getAttributeValue("type");
            TypeOfReviewValue reviewTypeValue = null;
            try {
                reviewTypeValue = TypeOfReviewValue.fromValue(reviewType);
            } catch (IllegalArgumentException ex) {
                // no value assigned
                if (out != null) {
                    out.println("Warning: One of the review information has an illegal review type value or no type value at all");
                }
            }
            review.setType(reviewTypeValue);
            // logger.debug("found review with type: " + reviewType);
            List<Element> scopeElements = reviewElement.getChildren("scope", this.commonNamespace);

            for (Element scopeElement : scopeElements) {
                String scopeName = scopeElement.getAttributeValue("name");
                // logger.debug("Scope name: " + scopeName);
                ScopeOfReviewValue scopeValue = null;
                try {
                    scopeValue = ScopeOfReviewValue.fromValue(scopeName);
                } catch (IllegalArgumentException ex) {
                    // no value assigned
                    if (out != null) {
                        out.println("Warning: Review section contains scope definition without scope name; will ignore this scope section");
                    }
                    continue;
                }
                ScopeOfReview scope = new ScopeOfReview(scopeValue);
                List<Element> scopeMethodsElements = scopeElement.getChildren("method", this.commonNamespace);

                for (Element scopeMethodElement : scopeMethodsElements) {
                    String methodName = scopeMethodElement.getAttributeValue("name");
                    // logger.debug("scope of review method name: " +
                    // methodName);
                    MethodOfReviewValue methodValue = null;
                    try {
                        methodValue = MethodOfReviewValue.fromValue(methodName);
                    } catch (IllegalArgumentException ex) {
                        // no value assigned
                        if (out != null) {
                            out.println("Warning: The scope section of type " + scopeValue.toString() + " contains an unknown method; will ignore this method");
                        }
                        continue;
                    }
                    scope.addMethod(methodValue);
                }
                review.addScope(scope);
            }

            IMultiLangString reviewDetails = this.parserHelper.getIMultiLanguageString(reviewElement, "reviewDetails", this.commonNamespace);

            if (reviewDetails != null) {
                review.setReviewDetails(reviewDetails);
            }

            IMultiLangString otherReviewDetails = this.parserHelper.getIMultiLanguageString(reviewElement, "otherReviewDetails", this.commonNamespace);

            if (otherReviewDetails != null) {
                review.setOtherReviewDetails(otherReviewDetails);
            }
            List<IGlobalReference> reviewers = this.commonReader.getGlobalReferences(reviewElement, "referenceToNameOfReviewerAndInstitution",
                    this.commonNamespace, out);

            for (IGlobalReference reviewer : reviewers) {
                review.addReferenceToReviewers((GlobalReference) reviewer);
            }

            GlobalReference reviewReport = this.commonReader.getGlobalReference(reviewElement, "referenceToCompleteReviewReport", this.commonNamespace, out);
            review.setReferenceToReport(reviewReport);

            // data quality indicators
            Element dqiElement = reviewElement.getChild("dataQualityIndicators", this.commonNamespace);

            if (dqiElement != null) {
                List<Element> dqis = dqiElement.getChildren("dataQualityIndicator", this.commonNamespace);
                for (Element e : dqis) {
                    String name = e.getAttributeValue("name");
                    String value = e.getAttributeValue("value");
                    DataQualityIndicator dqi = new DataQualityIndicator(DataQualityIndicatorName.fromValue(name),
                            QualityValue.fromValue(value));
                    String numericValue = e.getAttributeValue("numericValue", ext17Namespace);

                    if (StringUtils.isNotBlank(numericValue)) {
                        try {
                            dqi.setNumericValue(Double.valueOf(numericValue));
                        } catch (NumberFormatException nfe) {
                            out.println("Warning: DQI value " + numericValue + " cannot be converted to a number");
                        }
                    }

                    review.addQualityIndicator(dqi);

                }

            }

            reviews.add(review);
        }

        return reviews;

    }

    private List<Exchange> getExchanges(PrintWriter out) {
        List<Exchange> exchanges = new ArrayList<Exchange>();

        List<Element> exchangeElements = this.parserHelper.getElements("/ilcd:processDataSet/ilcd:exchanges/ilcd:exchange");

        try {
            for (Element exchangeElement : exchangeElements) {
                Exchange exchange = new Exchange();

                String internalIdString = exchangeElement.getAttributeValue("dataSetInternalID");

                try {
                    exchange.setInternalId(Integer.parseInt(internalIdString));
                } catch (NumberFormatException e1) {
                }

                GlobalReference flowReference = this.commonReader.getGlobalReference(exchangeElement, "referenceToFlowDataSet", this.processNamespace, out);
                exchange.setFlowReference(flowReference);

                // logger.debug("found exchange " +
                // flowReference.getShortDescription().getDefaultValue() +
                // " mit internal id " + internalIdString);

                String exchangeDirectionString = exchangeElement.getChildText("exchangeDirection", this.processNamespace);

                if (exchangeDirectionString != null) {
                    if (exchangeDirectionString.equals(ExchangeDirectionValues.OUTPUT.value())) {
                        exchange.setExchangeDirection(ExchangeDirectionValues.OUTPUT);
                    } else {
                        exchange.setExchangeDirection(ExchangeDirectionValues.INPUT);
                    }
                }

                String meanAmountString = exchangeElement.getChildText("meanAmount", this.processNamespace);

                if (StringUtils.isNotBlank(meanAmountString)) {
                    try {
                        exchange.setMeanAmount(Double.parseDouble(meanAmountString));

                    } catch (NumberFormatException nfe) {
                        out.println("Warning: Exchange element value " + meanAmountString + " cannot be converted to a number");
                    }
                }

                String resultingAmountString = exchangeElement.getChildText("resultingAmount", this.processNamespace);

                if (StringUtils.isNotBlank(resultingAmountString)) {
                    try {
                        exchange.setResultingAmount(Double.parseDouble(resultingAmountString));

                    } catch (NumberFormatException nfe) {
                        out.println("Warning: Exchange element value " + resultingAmountString + " cannot be converted to a number");
                    }
                }

                String minimumAmountString = exchangeElement.getChildText("minimumAmount", this.processNamespace);

                if (StringUtils.isNotBlank(minimumAmountString)) {
                    try {
                        exchange.setMinimumAmount(Double.parseDouble(minimumAmountString));

                    } catch (NumberFormatException nfe) {
                        out.println("Warning: Exchange element value " + minimumAmountString + " cannot be converted to a number");
                    }

                }

                String maximumAmountString = exchangeElement.getChildText("maximumAmount", this.processNamespace);

                if (StringUtils.isNotBlank(maximumAmountString)) {
                    try {
                        exchange.setMaximumAmount(Double.parseDouble(maximumAmountString));

                    } catch (NumberFormatException nfe) {
                        out.println("Warning: Exchange element value " + maximumAmountString + " cannot be converted to a number");
                    }

                }

                String location = exchangeElement.getChildText("location", this.processNamespace);
                exchange.setLocation(location);
                String functionType = exchangeElement.getChildText("functionType", this.processNamespace);
                exchange.setFunctionType(functionType);
                String referenceToVariable = exchangeElement.getChildText("referenceToVariable", this.processNamespace);

                if (referenceToVariable != null) {
                    exchange.setReferenceToVariable(referenceToVariable);

                }
                String uncertaintyType = exchangeElement.getChildText("uncertaintyDistributionType", this.processNamespace);
                exchange.setUncertaintyDistribution(uncertaintyType);
                String standardDeviation = exchangeElement.getChildText("relativeStandardDeviation95In", this.processNamespace);

                if (standardDeviation != null) {
                    try {
                        exchange.setStandardDeviation(Float.parseFloat(standardDeviation));

                    } catch (NumberFormatException nfe) {
                        out.println("Warning: Exchange element value " + standardDeviation + " cannot be converted to a number");
                    }
                }
                String allocations = exchangeElement.getChildText("allocations", this.processNamespace);
                exchange.setAllocation(allocations);
                String dataSourceType = exchangeElement.getChildText("dataSourceType", this.processNamespace);
                exchange.setDataSource(dataSourceType);
                String derivationType = exchangeElement.getChildText("dataDerivationTypeStatus", this.processNamespace);
                exchange.setDerivationType(derivationType);
                IMultiLangString comment = this.parserHelper.getIMultiLanguageString(exchangeElement, "generalComment", this.processNamespace);
                exchange.setComment(comment);

                /* EPD extensions */
                List<Amount> amounts = this.getAmounts(exchangeElement, out);
                if (!amounts.isEmpty()) {
                    exchange.setAmounts(amounts);
                }

                Element other = exchangeElement.getChild("other", this.commonNamespace);
                if (other != null) {
                    GlobalReference unitGroupReference = this.commonReader.getGlobalReference(other, "referenceToUnitGroupDataSet", this.epdNamespace, out);
                    exchange.setUnitGroupReference(unitGroupReference);
                }
                /* end EPD extensions */

                exchanges.add(exchange);

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }

        return exchanges;

    }

    private List<Amount> getAmounts(Element root, PrintWriter out) {
        List<Amount> amounts = new ArrayList<Amount>();

        Element other = root.getChild("other", this.commonNamespace);

        if (other != null) {
            List<Element> amnts = other.getChildren("amount", this.epdNamespace);

            for (Element amnt : amnts) {
                Amount amount = new Amount();

                String amountModuleString = amnt.getAttributeValue("module", this.epdNamespace);
                amount.setModule(amountModuleString);

                String amountScenarioString = amnt.getAttributeValue("scenario", this.epdNamespace);
                amount.setScenario(amountScenarioString);

                String amountValueString = amnt.getTextTrim();
                if (StringUtils.isNotEmpty(amountValueString)) {
                    try {
                        amount.setValue(Double.parseDouble(amountValueString));

                    } catch (NumberFormatException nfe) {
                        out.println("Warning: Amount value " + amountValueString + " cannot be converted to a number");
                    }
                }
                amounts.add(amount);
            }
        }

        return amounts;
    }

    private List<ComplianceSystem> getCompliances(PrintWriter out) {
        List<ComplianceSystem> compliances = new ArrayList<ComplianceSystem>();
        List<Element> complianceElements = this.parserHelper.getElements("/ilcd:processDataSet/ilcd:modellingAndValidation/ilcd:complianceDeclarations/ilcd:compliance");

        for (Element complianceElement : complianceElements) {
            ComplianceSystem compliance = new ComplianceSystem();
            GlobalReference complianceSystemRef = this.commonReader.getGlobalReference(complianceElement, "referenceToComplianceSystem", this.commonNamespace,
                    out);
            compliance.setSourceReference(complianceSystemRef);
            // logger.debug("create new compliance Entry for: " +
            // complianceSystemRef.getShortDescription().getDefaultValue());
            String overallCompliance = complianceElement.getChildText("approvalOfOverallCompliance", this.commonNamespace);
            ComplianceValue overallComplianceValue;

            try {
                overallComplianceValue = ComplianceValue.fromValue(overallCompliance);

            } catch (Exception e) {
                overallComplianceValue = ComplianceValue.NOT_DEFINED;

            }
            compliance.setOverallCompliance(overallComplianceValue);
            String nomenclatureCompliance = complianceElement.getChildText("nomenclatureCompliance", this.commonNamespace);
            ComplianceValue nomenclatureComplianceValue;

            try {
                nomenclatureComplianceValue = ComplianceValue.fromValue(nomenclatureCompliance);

            } catch (Exception e) {
                nomenclatureComplianceValue = ComplianceValue.NOT_DEFINED;

            }
            compliance.setNomenclatureCompliance(nomenclatureComplianceValue);
            String methodologicalCompliance = complianceElement.getChildText("methodologicalCompliance", this.commonNamespace);
            ComplianceValue methodologicalComplianceValue;

            try {
                methodologicalComplianceValue = ComplianceValue.fromValue(methodologicalCompliance);

            } catch (Exception e) {
                methodologicalComplianceValue = ComplianceValue.NOT_DEFINED;

            }
            compliance.setMethodologicalCompliance(methodologicalComplianceValue);
            String reviewCompliance = complianceElement.getChildText("reviewCompliance", this.commonNamespace);
            ComplianceValue reviewComplianceValue;

            try {
                reviewComplianceValue = ComplianceValue.fromValue(reviewCompliance);

            } catch (Exception e) {
                reviewComplianceValue = ComplianceValue.NOT_DEFINED;

            }
            compliance.setReviewCompliance(reviewComplianceValue);
            String documentationCompliance = complianceElement.getChildText("documentationCompliance", this.commonNamespace);
            ComplianceValue documentationComplianceValue;

            try {
                documentationComplianceValue = ComplianceValue.fromValue(documentationCompliance);

            } catch (Exception e) {
                documentationComplianceValue = ComplianceValue.NOT_DEFINED;

            }
            compliance.setDocumentationCompliance(documentationComplianceValue);
            String qualityCompliance = complianceElement.getChildText("qualityCompliance", this.commonNamespace);
            ComplianceValue qualityComplianceValue;

            try {
                qualityComplianceValue = ComplianceValue.fromValue(qualityCompliance);

            } catch (Exception e) {
                qualityComplianceValue = ComplianceValue.NOT_DEFINED;

            }
            compliance.setQualityCompliance(qualityComplianceValue);

            compliances.add(compliance);

        }

        return compliances;

    }

    private InternalQuantitativeReference getQuantitativeReference() {
        InternalQuantitativeReference quantitativeRef = new InternalQuantitativeReference();

        Element refElement = this.parserHelper.getElement("/ilcd:processDataSet/ilcd:processInformation/ilcd:quantitativeReference");

        if (refElement != null) {
            String type = refElement.getAttributeValue("type");
            if (type != null) {
                TypeOfQuantitativeReferenceValue typeValue = TypeOfQuantitativeReferenceValue.fromValue(type);
                quantitativeRef.setType(typeValue);
            }
            List<Element> flowReferences = refElement.getChildren("referenceToReferenceFlow", this.processNamespace);

            for (Element flowRefElement : flowReferences) {
                String refString = flowRefElement.getText();
                // logger.debug("found reference to reference flow: " +
                // refString);

                if (refString != null) {
                    quantitativeRef.addReferenceId(Integer.parseInt(refString));

                }
            }
        }

        IMultiLangString functionalUnitOrOther = this.parserHelper.getIMultiLanguageString(refElement, "functionalUnitOrOther", this.processNamespace);

        if (functionalUnitOrOther != null) {
            quantitativeRef.setOtherReference(functionalUnitOrOther);

        }

        return quantitativeRef;

    }

    private List<LciaResult> getLciaResults(PrintWriter out) {
        List<LciaResult> results = new ArrayList<LciaResult>();

        List<Element> resultElements = this.parserHelper.getElements("/ilcd:processDataSet/ilcd:LCIAResults/ilcd:LCIAResult");

        for (Element resultElement : resultElements) {
            LciaResult result = new LciaResult();

            GlobalReference methodReference = this.commonReader.getGlobalReference(resultElement, "referenceToLCIAMethodDataSet", this.processNamespace, out);
            result.setMethodReference(methodReference);

            String meanAmountString = resultElement.getChildText("meanAmount", this.processNamespace);

            if (StringUtils.isNotBlank(meanAmountString)) {
                try {
                    result.setMeanAmount(Double.parseDouble(meanAmountString));

                } catch (NumberFormatException nfe) {
                    out.println("Warning: LCIA result value " + meanAmountString + " cannot be converted to a number");
                }
            }

            String uncertaintyType = resultElement.getChildText("uncertaintyDistributionType", this.processNamespace);
            result.setUncertaintyDistribution(uncertaintyType);
            String standardDeviation = resultElement.getChildText("relativeStandardDeviation95In", this.processNamespace);

            if (standardDeviation != null) {
                try {
                    result.setStandardDeviation(Float.parseFloat(standardDeviation));

                } catch (NumberFormatException nfe) {
                    out.println("Warning: LCIA result value " + standardDeviation + " cannot be converted to a number");
                }
            }

            IMultiLangString comment = this.parserHelper.getIMultiLanguageString(resultElement, "generalComment", this.processNamespace);
            result.setComment(comment);

            /* EPD extensions */
            List<Amount> amounts = this.getAmounts(resultElement, out);
            if (!amounts.isEmpty()) {
                result.setAmounts(amounts);
            }

            Element other = resultElement.getChild("other", this.commonNamespace);
            if (other != null) {
                GlobalReference unitGroupReference = this.commonReader.getGlobalReference(other, "referenceToUnitGroupDataSet", this.epdNamespace, out);
                result.setUnitGroupReference(unitGroupReference);
            }
            /* end EPD extensions */

            results.add(result);

        }

        return results;

    }

    public HashSet<IGlobalReference> getDataSources(Class<?> clazz, PrintWriter w) {
        HashSet<IGlobalReference> dataSources = new HashSet<IGlobalReference>();
        Element dataSourcesParent = this.parserHelper.getElement("/ilcd:processDataSet/ilcd:modellingAndValidation/ilcd:dataSourcesTreatmentAndRepresentativeness");

        if (dataSourcesParent == null)
            return dataSources;

        List<IGlobalReference> extractedDataSources = this.commonReader.getGlobalReferences(dataSourcesParent, "referenceToDataSource", this.processNamespace, clazz, w);

        dataSources.addAll(extractedDataSources);

        return dataSources;
    }

    public HashSet<IGlobalReference> getDatasetGenerator(Class<?> clazz, PrintWriter w) {
        HashSet<IGlobalReference> datasetGenerator = new HashSet<IGlobalReference>();
        Element datasetGeneratorParent = this.parserHelper.getElement("/ilcd:processDataSet/ilcd:administrativeInformation/ilcd:dataGenerator");

        if (datasetGeneratorParent == null)
            return datasetGenerator;

        List<IGlobalReference> extractedDatasetGenerator = this.commonReader.getGlobalReferences(datasetGeneratorParent, "referenceToPersonOrEntityGeneratingTheDataSet", this.commonNamespace, clazz, w);

        datasetGenerator.addAll(extractedDatasetGenerator);

        return datasetGenerator;
    }
}
