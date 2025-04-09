package de.iai.ilcd.service.glad;

import de.fzk.iai.ilcd.api.app.process.ProcessDataSet;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceTypeValues;
import de.fzk.iai.ilcd.api.binding.generated.common.STMultiLang;
import de.fzk.iai.ilcd.service.model.common.IClass;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.enums.CompletenessValue;
import de.fzk.iai.ilcd.service.model.enums.LCIMethodApproachesValue;
import de.fzk.iai.ilcd.service.model.enums.LicenseTypeValue;
import de.fzk.iai.ilcd.service.model.process.IComplianceSystem;
import de.fzk.iai.ilcd.service.model.process.IReview;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.ConfigurationItem;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.dao.ConfigurationItemDao;
import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.security.ProtectionType;
import de.iai.ilcd.security.StockAccessRight;
import de.iai.ilcd.security.StockAccessRight.AccessRightType;
import de.iai.ilcd.security.StockAccessRightDao;
import de.iai.ilcd.service.glad.model.*;
import de.iai.ilcd.util.SodaUtil;
import de.iai.ilcd.util.UnmarshalHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The Object for converting data sets to GLAD metadata (needed for GLAD registration).
 *
 * @author sarai
 */
public class GLADMetaDataUtil {

    private final Logger log = LogManager.getLogger(GLADMetaDataUtil.class);

    private GLADMetaData gmd;

    public GLADMetaData getMetaData() {
        return this.gmd;
    }

    public void setGLADMetaData(GLADMetaData gmd) {
        this.gmd = gmd;
    }

    /**
     * Gets a GLADMetaData object with all relevant information from given data
     * set object.
     *
     * @param dataset The data set which shall be converted into GLADMetaData
     * @return A GLADMetaData containing all relevant information given from
     * data set
     */
    public GLADMetaData convertToGLADMetaData(final DataSet dataset) {
        if (log.isDebugEnabled())
            log.debug("converting process metadata to GLAD metadata for " + dataset.getUuidAsString() + " " + dataset.getDataSetVersion());

        ProcessDao processDao = new ProcessDao();
        final Process process = processDao.getByUuid(dataset.getUuid().getUuid());
        GLADDatabasePropertiesDao gladPropsDao = new GLADDatabasePropertiesDao();
        GLADDatabaseProperties props = gladPropsDao.getById(1);

        setRefId(process);
        setName(process);
        setDatasetUrl(process);
        setFormat(process);
        setCategories(process);
        setProcessType(process);
        setRepresentativenessType(props);
        setCompleteness(process);
        setValidFrom(process);
        setValidUntil(process);
        setValidFromYear(process);
        setValidUntilYear(process);
        setLocation(process);
        setDataprovider(process);
        setSupportedNomenclatures(process);
        setModelingType(process);
        setMultifunctionalModeling(process);
        setBiogenicCarbonModeling(props);
        setEndOfLifeModeling(props);
        setWaterModeling(props);
        setInfrastructureModeling(props);
        setEmmissionModeling(props);
        setCarbonStorageModeling(props);
        setAggregationType(process);
        setReview(process);
        setContact(process);
        setCopyrightProtected(process);
        setCopyrightHolder(process);
        setFree(process);
        setPubliclyAccessible(process);
        setDescription(process);
        setLicense(process);

        //Amount deviation not supported yet
//		setAmountDeviation(process);

        try {
            ProcessDataSet xmlDataset = (ProcessDataSet) new UnmarshalHelper()
                    .unmarshal(process);
            setTechnology(xmlDataset);
            setLciaMethods(xmlDataset);
            setRepresentativenessValue(xmlDataset);
            setLatitudeAndLongitude(xmlDataset);
        } catch (Exception e) {
            log.error("error unmarshalling dataset");
            e.printStackTrace();
        }

        return gmd;
    }

    /**
     * Replace tabs with spaces
     *
     * @param input
     * @return the converted result
     */
    private String replaceSpecialCharacters(String input) {
        if (input == null)
            return null;
        String result = input.replace("\t", "    ");
        return result;
    }

    private void setRefId(Process process) {
        try {
            gmd.setRefId(process.getUuid().getUuid());
            if (log.isTraceEnabled())
                log.trace("Set ref id: " + process.getUuid().getUuid());
        } catch (NullPointerException npe) {
        }
    }

    private void setName(Process process) {
        String name = "";
        String baseName = "";
        String nameRoute = "";
        String nameLocation = "";
        String nameUnit = "";

        try {
            baseName = process.getBaseName().getValue();
            if (log.isTraceEnabled())
                log.trace("Setting name-basename: " + process.getBaseName().getValue());
        } catch (NullPointerException npe) {
        }
        try {
            nameRoute = process.getNameRoute().getValue();
            if (log.isTraceEnabled())
                log.trace("Setting name-standardstreatmentsroutes: " + process.getNameRoute().getValue());
        } catch (NullPointerException npe) {
        }
        try {
            nameLocation = process.getNameLocation().getValue();
            if (log.isTraceEnabled())
                log.trace("Setting name-mixtypeandlocation: " + process.getNameLocation().getValue());
        } catch (NullPointerException npe) {
        }
        try {
            nameUnit = process.getNameUnit().getValue();
            if (log.isTraceEnabled())
                log.trace("Setting name-f.unit-flowproperties: " + process.getNameUnit().getValue());
        } catch (NullPointerException npe) {
        }

        name = baseName;

        if (StringUtils.isNotBlank(nameRoute))
            name += "; " + nameRoute;
        if (StringUtils.isNotBlank(nameLocation))
            name += "; " + nameLocation;
        if (StringUtils.isNotBlank(nameUnit))
            name += "; " + nameUnit;

        gmd.setName(replaceSpecialCharacters(name));
    }

    private void setDatasetUrl(Process process) {
        try {
            gmd.setDataSetUrl(buildDataSetUrl(process.getUuid().getUuid(), process.getVersion().getVersionString()));
            if (log.isTraceEnabled())
                log.trace("Set dataset URL: "
                        + buildDataSetUrl(process.getUuid().getUuid(), process.getVersion().getVersionString()));
        } catch (NullPointerException npe) {
        }
    }

    private void setFormat(Process process) {
        try {
            gmd.setFormat(DataFormat.ILCD);
            if (log.isTraceEnabled())
                log.trace("Set format: " + DataFormat.ILCD);
        } catch (NullPointerException npe) {
        }
    }

    private void setCategories(Process process) {
        try {
            List<String> classList = new ArrayList<String>();
            for (IClass c : process.getClassifications().get(0).getIClassList()) {
                classList.add(c.getName());
            }
            gmd.setCategories(classList);
            log.trace("Set categories: " + classList.toString());
        } catch (NullPointerException npe) {
        } catch (ArrayIndexOutOfBoundsException aoobe) {
        }
    }

    private void setProcessType(Process process) {
        try {
            gmd.setProcessType(ProcessType.fromValue(process.getType()));
            if (log.isTraceEnabled())
                log.trace("Set process type: " + ProcessType.fromValue(process.getType()));
        } catch (NullPointerException npe) {
        }
    }

    private void setRepresentativenessType(GLADDatabaseProperties props) {
        try {
            gmd.setRepresentativenessType(props.getRepresentativenessType());
            if (log.isTraceEnabled())
                log.trace("Set representativeness type: " + props.getRepresentativenessType());
        } catch (NullPointerException npe) {

        }
    }

    private void setCompleteness(Process process) {
        try {
            if (process.getCompleteness().equals(CompletenessValue.ALL_RELEVANT_FLOWS_QUANTIFIED)) {
                gmd.setCompleteness(Double.valueOf(100));
                if (log.isTraceEnabled())
                    log.trace("Set completeness: " + process.getCompleteness().getValue());
            }
        } catch (NullPointerException npe) {
        }
    }

    private void setValidFrom(Process process) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String referenceYear = process.getTimeInformation().getReferenceYear().toString();
            Date date = sdf.parse(referenceYear);
            gmd.setValidFrom(date.getTime());
            if (log.isTraceEnabled())
                log.trace("Set validFrom: " + date.getTime());
        } catch (NullPointerException npe) {
        } catch (ParseException e) {
        }
    }

    private void setValidUntil(Process process) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String validUntilStr = process.getTimeInformation().getValidUntil().toString();
            Date date = sdf.parse(validUntilStr);
            gmd.setValidUntil(date.getTime());
            if (log.isTraceEnabled())
                log.trace("Set validUntil: " + date.getTime());
        } catch (NullPointerException npe) {
        } catch (ParseException e) {
        }
    }

    private void setValidFromYear(Process process) {
        try {
            gmd.setValidFromYear(process.getTimeInformation().getReferenceYear());
            if (log.isTraceEnabled())
                log.trace("Set validFromYear: " + process.getTimeInformation().getReferenceYear());
        } catch (NullPointerException npe) {
        }
    }

    private void setValidUntilYear(Process process) {
        try {
            gmd.setValidUntilYear(process.getTimeInformation().getValidUntil());
            if (log.isTraceEnabled())
                log.trace("Set validUntilYear: " + process.getTimeInformation().getValidUntil());
        } catch (NullPointerException npe) {
        }
    }

    private void setLocation(Process process) {
        try {
            gmd.setLocation(process.getGeography().getLocation());
            if (log.isTraceEnabled())
                log.trace("Set location: " + process.getGeography().getLocation());
        } catch (NullPointerException npe) {
        }
    }

    private void setDataprovider(Process process) {
        try {
            ConfigurationItemDao dao = new ConfigurationItemDao();
            ConfigurationItem configItem = dao.getConfigurationItem("glad.dataprovider");
            gmd.setDataprovider(configItem.getStringvalue());
            if (log.isTraceEnabled())
                log.trace("Set dataprovider: " + configItem.getStringvalue());
        } catch (NullPointerException npe) {
        }
    }

    private void setSupportedNomenclatures(Process process) {
        List<String> result = new ArrayList<String>();
        try {
            Set<IComplianceSystem> complianceSet = process.getComplianceSystems();
            Iterator<IComplianceSystem> iterator = complianceSet.iterator();

            if (log.isDebugEnabled())
                log.debug(complianceSet.size() + " compliance declarations found");

            // first check EF
            while (iterator.hasNext()) {
                IComplianceSystem compliance = iterator.next();
                if ("Environmental Footprint 2.0".equalsIgnoreCase(compliance.getName()) || "c2633e08-f120-4def-b761-a63acbd75d8c".equalsIgnoreCase(compliance.getReference().getRefObjectId())) {
                    result.add("EF2.0");
                } else if ("Environmental Footprint 3.0".equalsIgnoreCase(compliance.getName()) || "3f5b0b56-60e6-4df7-869d-a811830386d9".equalsIgnoreCase(compliance.getReference().getRefObjectId())) {
                    result.add("EF3.0");
                } else if ("Environmental Footprint 3.1".equalsIgnoreCase(compliance.getName()) || "0eb2b770-2cb1-43cf-9fbc-215df36fe9f0".equalsIgnoreCase(compliance.getReference().getRefObjectId())) {
                    result.add("EF3.1");
                } else if ("Environmental Footprint 4.0".equalsIgnoreCase(compliance.getName()) || "d6a16320-0d7e-4b53-8248-1a74413ec87a".equalsIgnoreCase(compliance.getReference().getRefObjectId())) {
                    result.add("EF4.0");
                }
            }

            // only if none of the above have been picked up, we'll check for ILCD EL and set it if present
            if (result.isEmpty()) {
                while (iterator.hasNext()) {
                    IComplianceSystem compliance = iterator.next();
                    if ("ILCD Data Network - Entry-level".equalsIgnoreCase(compliance.getName()) || "d92a1a12-2545-49e2-a585-55c259997756".equalsIgnoreCase(compliance.getReference().getRefObjectId())) {
                        result.add("ILCD");
                    }
                }
            }

            gmd.setSupportedNomenclatures(result);
            if (log.isTraceEnabled())
                log.trace("Set supportedNomenclatures: " + result.toString());
        } catch (NullPointerException npe) {
        }
    }

    private void setModelingType(Process process) {
        try {
            gmd.setModelingType(ModelingType.fromValue(process.getLCIMethodInformation().getMethodPrinciple()));
            if (log.isTraceEnabled())
                log.trace("Set modelingType: "
                        + ModelingType.fromValue(process.getLCIMethodInformation().getMethodPrinciple()).toString());
        } catch (NullPointerException npe) {
        }
    }

    private void setMultifunctionalModeling(Process process) {
        try {
            Set<LCIMethodApproachesValue> modelingSet = process.getLCIMethodInformation().getApproaches();
            if (modelingSet == null || modelingSet.isEmpty()) {
                gmd.setMultifunctionalModeling(MultifunctionalModeling.NONE);
            } else {
                Iterator<LCIMethodApproachesValue> iterator = modelingSet.iterator();
                MultifunctionalModeling last = null;
                MultifunctionalModeling current = null;
                MultifunctionalModeling set = null;
                while (iterator.hasNext()) {
                    current = MultifunctionalModeling.fromValue(iterator.next());
                    if (last != null && !current.equals(last)) {
                        set = MultifunctionalModeling.UNKNOWN;
                        break;
                    }
                    last = current;
                }
                if (set == null) {
                    set = current;
                }
                gmd.setMultifunctionalModeling(set);
                if (log.isTraceEnabled())
                    log.trace("Set multifunctional modeling: " + set);
            }
        } catch (NullPointerException npe) {
        }
    }

    private void setAggregationType(Process process) {
        try {
            gmd.setAggregationType(AggregationType.fromValue(process.getType()));
            if (log.isTraceEnabled())
                log.trace("Set aggregation type: " + AggregationType.fromValue(process.getType()));
        } catch (NullPointerException npe) {
        }
    }

    private void setReview(Process process) {
        try {
            List<String> reviewers = new ArrayList<String>();
            log.trace("Getting reviewers.");
            List<IReview> reviews = process.getReviews();
            ReviewType best = ReviewType.UNKNOWN;
            ReviewSystem system = ReviewSystem.ILCD;
            List<IReview> bestReviews = new ArrayList<IReview>();
            log.trace("Starting to find best review type.");
            for (IReview review : reviews) {
                ReviewType tmp = ReviewType.fromValue(review.getType());
                if (tmp.compareTo(best) <= 0) {
                    if (tmp.compareTo(best) < 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("current reviewType is: " + tmp.toString());
                            log.debug("current best review type is: " + best.toString());
                        }
                        best = tmp;
                        bestReviews.clear();
                    }
                    bestReviews.add(review);
                }
            }
            try {
                for (IReview review : bestReviews) {
                    for (IGlobalReference ref : review.getReferencesToReviewers())
                        reviewers.add(ref.getShortDescription().getValue());
                }
                gmd.setReviewers(reviewers);
                if (log.isTraceEnabled())
                    log.trace("Set reviewers: " + reviewers.toString());
            } catch (NullPointerException npe) {
            }

            try {
                gmd.setReviewType(best);
                if (log.isTraceEnabled())
                    log.trace("Set review type: " + best);
            } catch (NullPointerException npe) {
            }
            try {
                gmd.setReviewSystem(system);
                if (log.isTraceEnabled())
                    log.trace("Set review system: " + system);
            } catch (NullPointerException npe) {
            }
        } catch (NullPointerException npe) {
        }
    }

    private void setContact(Process process) {
        try {
            gmd.setContact(process.getOwnerReference().getShortDescription().getValue());
            if (log.isTraceEnabled())
                log.trace("Set contact: " + process.getOwnerReference().getShortDescription().getValue());
        } catch (NullPointerException npe) {
        }
    }

    private void setCopyrightProtected(Process process) {
        try {
            gmd.setCopyrightProtected(process.getAccessInformation().isCopyright());
            if (log.isTraceEnabled())
                log.trace("Set copyrightProtected: "
                        + process.getAccessInformation().isCopyright());
        } catch (NullPointerException npe) {
        }
    }

    private void setCopyrightHolder(Process process) {
        try {
            if (process.getAccessInformation().isCopyright())
                gmd.setCopyrightHolder(process.getOwnerReference().getShortDescription().getValue());
            if (log.isTraceEnabled())
                log.trace("Set copyright holder: "
                        + process.getOwnerReference().getShortDescription().getValue());
        } catch (NullPointerException npe) {
        }
    }

    private void setFree(Process process) {
        try {
            if (process.getAccessInformation().getLicenseType() == LicenseTypeValue.FREE_OF_CHARGE_FOR_ALL_USERS_AND_USES ||
                    process.getAccessInformation().getLicenseType() == LicenseTypeValue.FREE_OF_CHARGE_FOR_MEMBERS_ONLY ||
                    process.getAccessInformation().getLicenseType() == LicenseTypeValue.FREE_OF_CHARGE_FOR_SOME_USER_TYPES_OR_USE_TYPES ||
                    process.getAccessInformation().getLicenseType() == LicenseTypeValue.OTHER) {
                log.trace("Set free to: {}", true);
                gmd.setFree(true);
            } else if (process.getAccessInformation().getLicenseType() == LicenseTypeValue.LICENSE_FEE) {
                log.trace("Set free to: {}", false);
                gmd.setFree(false);
            } else {
                log.trace("license type is {}, setting free to true", process.getAccessInformation().getLicenseType().getValue());
                gmd.setFree(true);
            }
        } catch (NullPointerException npe) {
            log.trace("no license type is present, setting free to true");
            gmd.setFree(true);
        }
    }

    private void setPubliclyAccessible(Process process) {
        // as long as there is an EXPORT permission somewhere, we'll consider this publicly accessible
        try {
            RootDataStock root = process.getRootDataStock();
            StockAccessRightDao dao = new StockAccessRightDao();
            List<StockAccessRight> sarListRoot = dao.get(root, AccessRightType.User);
            int value = 0;
            for (StockAccessRight sar : sarListRoot) {
                log.debug("root: current Ug Id: " + sar.getUgId());
                if (sar.getUgId() == 0) {
                    value = sar.getValue();
                    log.debug("root: value: " + sar.getValue());
                }
            }

            int value_div_8 = value / ProtectionType.EXPORT.getNumerical();
            if ((value_div_8 % 2) == 1) {
                gmd.setPubliclyAccessible(true);
                if (log.isTraceEnabled()) {
                    log.trace("Set publicly accessible: true");
                }
                return;
            }
            Set<DataStock> stocks = process.getContainingDataStocks();
            Iterator<DataStock> iterator = stocks.iterator();
            log.trace("got all containing stocks.");
            if (log.isDebugEnabled()) {
                log.debug("stock size: " + stocks.size());
            }
            while (iterator.hasNext()) {
                DataStock stock = iterator.next();
                if (log.isDebugEnabled()) {
                    log.debug("current stock: " + stock.getName());
                }


                List<StockAccessRight> sarList = dao.get(stock, AccessRightType.User);
                value = 0;
                for (StockAccessRight sar : sarList) {
                    if (log.isDebugEnabled()) {
                        log.debug("current Ug Id: " + sar.getUgId());
                    }
                    if (sar.getUgId() == 0) {
                        value = sar.getValue();
                        if (log.isDebugEnabled()) {
                            log.debug("value: " + sar.getValue());
                        }
                    }
                }

                value_div_8 = value / ProtectionType.EXPORT.getNumerical();
                if ((value_div_8 % 2) == 1) {
                    gmd.setPubliclyAccessible(true);
                    if (log.isTraceEnabled())
                        log.trace("Set publiclyAccessible: " + true);
                    return;
                }
            }

            gmd.setPubliclyAccessible(false);
            if (log.isTraceEnabled())
                log.trace("Set publiclyAccessible: " + false);

        } catch (NullPointerException npe) {
        }
    }

    private void setDescription(Process process) {
        try {
            StringBuilder buffer = new StringBuilder();
            String description = process.getDescription().getValue();
            buffer.append(description).append(" ").append(process.getUseAdvice().getValue());

            if (StringUtils.isNotBlank(buffer.toString()))
                gmd.setDescription(replaceSpecialCharacters(buffer.toString()));
            if (log.isTraceEnabled()) {
                log.trace("Set description: " + description);
                log.trace("Set new description: " + replaceSpecialCharacters(buffer.toString()));
            }
        } catch (NullPointerException npe) {
        }
    }

    private void setLicense(Process process) {
        try {
            gmd.setLicense(replaceSpecialCharacters(process.getAccessInformation().getUseRestrictions().getValue()));
            if (log.isTraceEnabled())
                log.trace("Set license: " + replaceSpecialCharacters(process.getAccessInformation().getUseRestrictions().getValue()));
        } catch (NullPointerException npe) {
        }
    }

    private void setTechnology(ProcessDataSet xmlDataset) {
        try {
            List<de.fzk.iai.ilcd.api.binding.generated.common.FTMultiLang> tech = xmlDataset.getProcessInformation()
                    .getTechnology().getTechnologyDescriptionAndIncludedProcesses();
            gmd.setTechnology(replaceSpecialCharacters(SodaUtil.getMultilangWithFallback("en", tech).getValue()));
            if (log.isTraceEnabled())
                log.trace("Set technology: "
                        + replaceSpecialCharacters(SodaUtil.getMultilangWithFallback("en", tech).getValue()));
        } catch (NullPointerException npe) {
        }
    }

    private void setLciaMethods(ProcessDataSet xmlDataset) {
        try {

            List<String> lciaMethods = new ArrayList<String>();
            List<GlobalReferenceType> references = xmlDataset.getModellingAndValidation().getCompleteness().getReferenceToSupportedImpactAssessmentMethods();
            for (GlobalReferenceType reference : references) {
                if (reference.getType().equals(GlobalReferenceTypeValues.LCIA_METHOD_DATA_SET)) {
                    List<STMultiLang> shortDescription = reference.getShortDescription();
                    String descr = SodaUtil.getMultilangWithFallback("en", shortDescription).getValue();
                    lciaMethods.add(descr);
                }
            }
            gmd.setLciaMethods(lciaMethods);
            if (log.isTraceEnabled()) {
                log.trace("Set lcia methods: " + lciaMethods.toString());
            }
        } catch (NullPointerException npe) {

        }
    }

    private void setBiogenicCarbonModeling(GLADDatabaseProperties props) {
        try {
            gmd.setBiogenicCarbonModeling(props.getBiogenicCarbonModeling());
            if (log.isTraceEnabled()) {
                log.trace("Set biogenic carbon modeling: " + props.getBiogenicCarbonModeling());
            }
        } catch (NullPointerException npe) {
        }
    }

    private void setEndOfLifeModeling(GLADDatabaseProperties props) {
        try {
            gmd.setEndOfLifeModeling(props.getEndOfLifeModeling());
            if (log.isTraceEnabled()) {
                log.trace("Set end of life modeling: " + props.getEndOfLifeModeling());
            }
        } catch (NullPointerException npe) {
        }
    }

    private void setWaterModeling(GLADDatabaseProperties props) {
        try {
            gmd.setWaterModeling(props.getWaterModeling());
            if (log.isTraceEnabled()) {
                log.trace("Set water modeling: " + props.getWaterModeling());
            }
        } catch (NullPointerException npe) {
        }
    }

    private void setInfrastructureModeling(GLADDatabaseProperties props) {
        try {
            gmd.setInfraStructureModeling(props.getInfrastructureModeling());
            if (log.isTraceEnabled()) {
                log.trace("Set infrastructure modeling: " + props.getInfrastructureModeling());
            }
        } catch (NullPointerException npe) {
        }
    }

    private void setEmmissionModeling(GLADDatabaseProperties props) {
        try {
            gmd.setEmissionModeling(props.getEmissionModeling());
            if (log.isTraceEnabled()) {
                log.trace("Set emmission modeling: " + props.getEmissionModeling());
            }
        } catch (NullPointerException npe) {
        }
    }

    private void setCarbonStorageModeling(GLADDatabaseProperties props) {
        try {
            gmd.setCarbonStorageModeling(props.getCarbonStorageModeling());
            if (log.isTraceEnabled()) {
                log.trace("Set carbon storage modeling: " + props.getCarbonStorageModeling());
            }
        } catch (NullPointerException npe) {
        }
    }

    //currently not supported
//	private void setSourceReliability(Process process) {
//		try {} catch (NullPointerException npe) {}
//		// set source reliability
//	}

    private void setLatitudeAndLongitude(ProcessDataSet xmlDataset) {
        try {
            String latitudeAndLongitude = xmlDataset.getProcessInformation().getGeography().getLocationOfOperationSupplyOrProduction().getLatitudeAndLongitude();
            String[] location = latitudeAndLongitude.split(";");
            gmd.setLatitude(Double.parseDouble(location[0]));
            if (log.isTraceEnabled()) {
                log.trace("Set latitude: " + location[0]);
            }
            gmd.setLongitude(Double.parseDouble(location[1]));
            if (log.isTraceEnabled()) {
                log.trace("Set longitude: " + location[1]);
            }
        } catch (NullPointerException npe) {
        }
    }

    //currently not supported
//	private void setAmountDeviation(Process process) {
//		try {} catch (NullPointerException npe) {}
//		// set amount deviation
//	}

    private void setRepresentativenessValue(ProcessDataSet xmlDataset) {
        try {
            BigDecimal representBD = xmlDataset.getModellingAndValidation().getDataSourcesTreatmentAndRepresentativeness().getPercentageSupplyOrProductionCovered();
            Double represent = representBD.doubleValue();
            gmd.setRepresentativenessValue(represent);
            if (log.isTraceEnabled()) {
                log.trace("Set representativeness value: " + represent);
            }
        } catch (NullPointerException npe) {
        }


    }

    /**
     * Gets the data set URL from given data set UUID and data set version.
     *
     * @param uuid    The UUID of data set
     * @param version The Version of data set
     * @return The URL of data set specified by UUID and version
     */
    private String buildDataSetUrl(String uuid, String version) {
        StringBuffer dataSetUrl = new StringBuffer();
        String baseURL = ConfigurationService.INSTANCE.getNodeInfo().getBaseURL();
        dataSetUrl.append(baseURL);
        if (!baseURL.endsWith("/")) {
            dataSetUrl.append("/");
        }
        if (!baseURL.endsWith("/resource/")) {
            dataSetUrl.append("resource/");
        }
        dataSetUrl.append("processes/");
        dataSetUrl.append(uuid);
        dataSetUrl.append("?format=xml&version="); //
        dataSetUrl.append(version);
        if (log.isDebugEnabled())
            log.debug("url is: " + dataSetUrl.toString());
        return dataSetUrl.toString();
    }

}
