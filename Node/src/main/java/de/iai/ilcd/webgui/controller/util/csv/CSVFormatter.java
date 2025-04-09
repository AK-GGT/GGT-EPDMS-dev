package de.iai.ilcd.webgui.controller.util.csv;

import de.fzk.iai.ilcd.service.model.common.IClass;
import de.fzk.iai.ilcd.service.model.process.IComplianceSystem;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.flow.FlowPropertyDescription;
import de.iai.ilcd.model.flow.MaterialProperty;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.process.Scenario;
import de.iai.ilcd.model.utils.DataSetUUIDVersionComparator;
import de.iai.ilcd.util.CategoryTranslator;
import de.iai.ilcd.util.sort.ModuleOrderComparator;
import de.iai.ilcd.webgui.controller.util.csv.header.CustomIndicatorsHeaderEnum;
import de.iai.ilcd.webgui.controller.util.csv.header.HeaderEnum;
import de.iai.ilcd.webgui.controller.util.csv.header.IndicatorsHeaderEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Writes data from a data stock as CSV.
 *
 * IMPORTANT NOTES for developers, PLEASE READ CAREFULLY:
 *
 * - All textual information that is printed to the result has to go through cleanString() before in order to
 *   remove and/or replace any possible newlines and semicolons.
 * - All non-integer numbers that are printed must go through format() in order to honor the requested decimal
 *   separator which may be a dot (.) or comma (,).
 *
 */
public class CSVFormatter {

    private static final Logger LOGGER = LogManager.getLogger(CSVFormatter.class);
    private static final String entrySeparator = ";";
    private final DecimalSeparator decimalSeparator;

    private List<String> profiles;

    public CSVFormatter(DecimalSeparator decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
        this.profiles = ConfigurationService.INSTANCE.getCsvProfiles();
    }

    private String format(Double d) {
        return format(d, decimalSeparator);
    }

    public static String format(Double d, DecimalSeparator decimalSeparator) {
        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
        decimalSymbols.setDecimalSeparator(decimalSeparator.separator);
        return new DecimalFormat("#.####################", decimalSymbols).format(d);
    }

    public void writeHeader(Writer w) throws IOException {

        Function<HeaderEnum, String> langMapper;
        if ("de".equals(ConfigurationService.INSTANCE.getDefaultLanguage()))
            langMapper = h -> h.title_de;
        else
            langMapper = h -> h.title_en;

        for (HeaderEnum h : HeaderEnum.values()) {
            if (HeaderEnum.Name.equals(h)) {
                // We want to export the name in all preferred languages, with headers like: "Name (en)".
                for (String lang : ConfigurationService.INSTANCE.getPreferredLanguages()) {
                    String s = langMapper.apply(h) + " (" + lang + ")";
                    w.write(s + entrySeparator);
                }
            } else if (HeaderEnum.Category.equals(h)) {
                // Standard
                String std = langMapper.apply(h) + " (original)";
                w.write(std + entrySeparator);

                // Translated
                ConfigurationService.INSTANCE.getTranslateClassificationCSVlanguages().stream()
                        .map(lang -> langMapper.apply(h) + " (" + lang + ")")
                        .forEach(header -> {
                            try {
                                w.write(header + entrySeparator);
                            } catch (IOException e) {
                                LOGGER.error("Failed to write header for name (preferred language).", e);
                            }
                        });
            } else {
                String s = langMapper.apply(h);
                w.write(s + entrySeparator);
            }
        }

        for (IndicatorsHeaderEnum h : IndicatorsHeaderEnum.values())
            w.write(h.toString() + entrySeparator);

        // write custom indicators depending on activated profile(s)
        if (profiles != null && !profiles.isEmpty()) {
            for (CustomIndicatorsHeaderEnum ch : CustomIndicatorsHeaderEnum.valuesForProfiles(profiles))
                w.write(ch.toString() + entrySeparator);
        }

        w.write("\n");
    }

    private String cleanString(String s) {
        s = StringUtils.trimToEmpty(s); // also takes care of ( null -> "" )
        s = s.replaceAll(";", ",");
        s = s.replaceAll("\\R+", "");
        s = s.trim();
        return s;
    }

    private <T> String newCellString(T p, Function<T, String> f) {
        return newCellString(p, f, () -> "");
    }

    private <T> String newCellString(T p, Function<T, String> f, Supplier<String> fallback) {
        String s = "";
        try {
            s += cleanString(f.apply(p));
        } catch (Exception e) {
            // log exception
            if (LOGGER.isTraceEnabled()) {
                String uuid = null;
                try {
                    DataSet ds = (DataSet) p;
                    uuid = ds.getUuidAsString();
                } catch (Exception ex) {
                    // ignore
                }
                LOGGER.error("Failed to parse cellstring for uuid: " + uuid, e);
            }

            s += fallback.get();
        }
        s += entrySeparator;
        return s;
    }

    public void formatCSV(List<Process> processes, Map<String, ProductFlow> flowProperties, Writer w)
            throws IOException {

        CategoryTranslator categoryTranslator = null;
        try {
            categoryTranslator = new CategoryTranslator(DataSetType.PROCESS, ConfigurationService.INSTANCE.getDefaultClassificationSystem());
        } catch (Exception e) {
            LOGGER.debug("Failed to initialise category translator.", e);
        }

        final CategoryTranslator ct = categoryTranslator;

        for (Process process : processes) {

            StringBuilder sb = new StringBuilder();
            sb.append(process.getUuidAsString());
            sb.append(entrySeparator);
            sb.append(process.getDataSetVersion());
            sb.append(entrySeparator);

            // Append names for all preferred languages
            ConfigurationService.INSTANCE.getPreferredLanguages().stream()
                    .map(lang -> newCellString(process, p -> p.getBaseName().getValue(lang)))
                    .forEach(sb::append);

            sb.append(newCellString(process, p -> p.getClassification().getIClassList().stream().map(IClass::getName)
                    .collect(Collectors.joining("' / '", "'", "'"))));

            if (ct != null && !ConfigurationService.INSTANCE.getTranslateClassificationCSVlanguages().isEmpty()) {
                ConfigurationService.INSTANCE.getTranslateClassificationCSVlanguages().stream()
                        .map(lang -> newCellString(process,
                                p -> p.getClassification().getIClassList().stream()
                                        .map(c -> ct.translateTo(c.getClId(), lang))
                                        .filter(c -> c != null && !c.trim().isEmpty())
                                        .map(c -> "'" + c + "'")
                                        .collect(Collectors.joining(" / "))))
                        .forEach(sb::append);
            }

            sb.append(newCellString(process, p -> p.getComplianceSystems().stream().map(IComplianceSystem::getName)
                    .collect(Collectors.joining("' / '", "'", "'"))));

            // Location
            sb.append(newCellString(process, Process::getLocation));

            // Sub-type
            sb.append(newCellString(process, p -> p.getSubType().getValue()));

            // Reference year
            sb.append(newCellString(process, p -> p.getTimeInformation().getReferenceYear().toString()));

            // Valid until
            sb.append(newCellString(process, p -> p.getTimeInformation().getValidUntil().toString()));

            // URL
            sb.append(newCellString(process, p -> ConfigurationService.INSTANCE.getNodeInfo().getBaseURL()
                    + "processes/" + p.getUuidAsString() + "?version=" + p.getVersion().getVersionString()));

            // Declaration Owner
            sb.append(newCellString(process, p -> p.getOwnerReference().getShortDescription().getDefaultValue()));

            // Publication date
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            sb.append(newCellString(process, p -> dateFormatter.format(p.getPublicationDateOfEPD())));

            // Registration number
            sb.append(newCellString(process, Process::getRegistrationNumber));

            // Registration authority
            sb.append(newCellString(process, p -> p.getReferenceToRegistrationAuthority().getShortDescription().getDefaultValue()));

            // Add predecessor data
            try {
                Comparator<? super GlobalReference> comparator = new DataSetUUIDVersionComparator();
                GlobalReference predecessorReference = process.getPrecedingDataSetVersions().stream()
                        .sorted(comparator)
                        .collect(Collectors.toList())
                        .get(0); // Throws IndexOutOfBoundsException if no predecessor found.

//				ProcessDao pDao = new ProcessDao();
//				Process predecessor = pDao.getByUuid(predecessorReference.getRefObjectId());

                // Predecessor UUID,
                // using the reference in case the predecessor hasn't been found.
                sb.append(newCellString(predecessorReference, GlobalReference::getRefObjectId));

                // Predecessor Version
                sb.append(newCellString(predecessorReference, GlobalReference::getVersionAsString));

                // Predecessor URL
                sb.append(newCellString(predecessorReference, GlobalReference::getHref));

            } catch (IndexOutOfBoundsException iobe) {
                // No Predecessor, so we simply skip the corresponding columns
                sb.append(entrySeparator + entrySeparator + entrySeparator);
            }

            // Declared unit - value
            sb.append(newCellString(process,
                    p -> format(p.getReferenceExchanges().get(0).getMeanAmount()
                            * p.getReferenceExchanges().get(0).getFlowWithSoftReference().getReferencePropertyDescription().getMeanValue()),
                    () -> "not available")); // fallback

            // Declared unit - unit
            sb.append(newCellString(process, p -> p.getReferenceExchanges().get(0).getReferenceUnit(),
                    () -> "not available")); // fallback

            ProductFlow productFlow = flowProperties.get(process.getUuid().getUuid());
            if (productFlow != null) {
                // Product flow UUID
                sb.append(newCellString(productFlow, DataSet::getUuidAsString));

                // Product flow name
                sb.append(newCellString(productFlow, pf -> pf.getName().getDefaultValue()));

                Map<String, Double> matPropMap = new HashMap<>();
                for (MaterialProperty matProp : productFlow.getMaterialProperties()) {
                    matPropMap.put(matProp.getDefinition().getName(), matProp.getValue());
                }

                if (matPropMap.containsKey("bulk density")) {
                    sb.append(format(matPropMap.get("bulk density")));
                }
                sb.append(entrySeparator);

                if (matPropMap.containsKey("grammage")) {
                    sb.append(format(matPropMap.get("grammage")));
                }
                sb.append(entrySeparator);

                if (matPropMap.containsKey("gross density")) {
                    sb.append(format(matPropMap.get("gross density")));
                }
                sb.append(entrySeparator);

                if (matPropMap.containsKey("layer thickness")) {
                    sb.append(format(matPropMap.get("layer thickness")));
                }
                sb.append(entrySeparator);

                if (matPropMap.containsKey("productiveness")) {
                    sb.append(format(matPropMap.get("productiveness")));
                }
                sb.append(entrySeparator);

                if (matPropMap.containsKey("linear density")) {
                    sb.append(format(matPropMap.get("linear density")));
                }
                sb.append(entrySeparator);

                if (matPropMap.containsKey("weight per piece")) {
                    sb.append(format(matPropMap.get("weight per piece")));
                }
                sb.append(entrySeparator);

                if (matPropMap.containsKey("conversion factor to 1 kg")) {
                    sb.append(format(matPropMap.get("conversion factor to 1 kg")));
                }
                sb.append(entrySeparator);

                List<FlowPropertyDescription> cc = process.getCarbogenics();
                Map<String, Double> flowPropDesMap = new HashMap<>();
                for (var c : cc) {
                    var uuid = c.getFlowPropertyRef().getUuid().getUuid();
                    var meanValue = c.getMeanValue();
                    flowPropDesMap.put(uuid, meanValue);
                }

                if (flowPropDesMap.containsKey("62e503ce-544a-4599-b2ad-bcea15a7bf20"))
                    sb.append(format(flowPropDesMap.get("62e503ce-544a-4599-b2ad-bcea15a7bf20")));
                sb.append(entrySeparator);
                if (flowPropDesMap.containsKey("262a541b-209e-44cc-a426-33bce30de7b1"))
                    sb.append(format(flowPropDesMap.get("262a541b-209e-44cc-a426-33bce30de7b1")));
                sb.append(entrySeparator);

            } else {
                // we skipped 11 fields
                sb.append(entrySeparator.repeat(11));
            }


            String metaData = sb.toString();

            // convert String[] to list<String>
            List<List<String>> modSceEx = new ArrayList<>();
            for (String[] modSec : process.getDeclaredModulesScenariosForExchanges())
                modSceEx.add(Arrays.asList(modSec));

            List<List<String>> modSceRes = new ArrayList<>();
            for (String[] modSec : process.getDeclaredModulesScenariosForLciaResults())
                modSceRes.add(Arrays.asList(modSec));

            // make union from both list and remove duplicates.
            List<List<String>> union = new ArrayList<>();
            union.addAll(modSceEx);
            union.addAll(modSceRes);
            union = union.stream().distinct().collect(Collectors.toList());
            union.sort(new ModuleOrderComparator());
            
            // fetch descriptions of each scenario.
            List<Scenario> scenarios = process.getScenarios();
            Map<String, String> sceDesc = new HashMap<>();
            for (Scenario sce : scenarios)
                sceDesc.put(sce.getName(), (sce.getDescription() != null ? sce.getDescription().getDefaultValue() : ""));

            for (List<String> modSec : union) {
                w.write(metaData);

                String module = modSec.get(0);
                String scenario = modSec.get(1);
                w.write(module);
                w.write(entrySeparator);
                if (scenario == null) {
                    w.write(entrySeparator);
                } else {
                    w.write(cleanString(scenario));
                    w.write(entrySeparator);
                    if (sceDesc.get(scenario) != null)
                        w.write(cleanString(sceDesc.get(scenario)));
                }
                w.write(entrySeparator);

                for (IndicatorsHeaderEnum h : IndicatorsHeaderEnum.values())
                    w.write(h.getResultValue(process, module, scenario, decimalSeparator) + entrySeparator);

                // write custom indicators depending on activated profile(s)
                if (profiles != null && !profiles.isEmpty()) {
                    LOGGER.debug("custom indicator profiles {} are active, writing values", profiles);
                    for (CustomIndicatorsHeaderEnum ch : CustomIndicatorsHeaderEnum.valuesForProfiles(profiles)) {
                        w.write(ch.getResultValue(process, module, scenario, decimalSeparator) + entrySeparator);
                    }
                }

                w.write("\n");
            }
        }
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

}
