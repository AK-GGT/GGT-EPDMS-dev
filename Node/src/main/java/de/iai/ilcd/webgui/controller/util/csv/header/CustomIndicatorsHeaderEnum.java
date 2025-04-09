package de.iai.ilcd.webgui.controller.util.csv.header;

import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.webgui.controller.util.csv.CSVFormatter;
import de.iai.ilcd.webgui.controller.util.csv.DecimalSeparator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public enum CustomIndicatorsHeaderEnum {
    GWP_IOBC_GHG("nordic", "GWP-IOBC/GHG", "fb774615-0575-45de-9a89-1ded92f19770", IndicatorsValueType.LCIAResult);

    final String profile;
    final String name;
    final String UUID;
    final IndicatorsValueType indicatorsValueType;

    private static final Logger LOGGER = LogManager.getLogger(CustomIndicatorsHeaderEnum.class);

    CustomIndicatorsHeaderEnum(String profile, String name, String UUID, IndicatorsValueType indicatorsValueType) {
        this.profile = profile;
        this.name = name;
        this.UUID = UUID;
        this.indicatorsValueType = indicatorsValueType;
    }

    public static List<CustomIndicatorsHeaderEnum> valuesForProfiles(List<String> profiles) {

        List<CustomIndicatorsHeaderEnum> result = new ArrayList<>();

        for (CustomIndicatorsHeaderEnum v : values()) {
            if (profiles.contains(v.profile))
                result.add(v);
        }

        return result;
    }


    public String getResultValue(Process process, String module, String scenario, DecimalSeparator decimalSeparator) {
        switch (indicatorsValueType) {
            case LCIAResult:
                return getLciaResultValue(process, UUID, module, scenario, decimalSeparator);
            case Exchange:
                return getExchangeValue(process, UUID, module, scenario, decimalSeparator);
            default:
                return "MISSING IndicatorsValueType";
        }

    }

    private String getExchangeValue(Process process, String indicatorUUID, String module, String scenario, DecimalSeparator decimalSeparator) {
        try {
            if (scenario == null)
                return CSVFormatter.format(
                        process.getExchangesByIndicator(indicatorUUID).get(0).getAmountByModule(module).getValue(), decimalSeparator);
            else
                return CSVFormatter.format(
                        process.getExchangesByIndicator(indicatorUUID).get(0).getAmountByModuleScenario(module, scenario).getValue(), decimalSeparator);
        } catch (Exception e) {
            return "";
        }
    }

    private String getLciaResultValue(Process process, String indicatorUUID, String module, String scenario, DecimalSeparator decimalSeparator) {
        try {
            if (scenario == null)
                return CSVFormatter.format(
                        process.getLciaResultsByIndicator(indicatorUUID).get(0).getAmountByModule(module).getValue(), decimalSeparator);
            else
                return CSVFormatter.format(
                        process.getLciaResultsByIndicator(indicatorUUID).get(0).getAmountByModuleScenario(module, scenario).getValue(), decimalSeparator);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String toString() {
        return this.name();
    }

}

