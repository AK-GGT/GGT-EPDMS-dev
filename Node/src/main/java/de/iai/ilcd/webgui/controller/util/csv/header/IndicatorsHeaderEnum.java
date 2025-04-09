package de.iai.ilcd.webgui.controller.util.csv.header;

import com.google.common.collect.Lists;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.webgui.controller.util.csv.CSVFormatter;
import de.iai.ilcd.webgui.controller.util.csv.DecimalSeparator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public enum IndicatorsHeaderEnum {
    GWP("GWP", List.of("77e416eb-a363-4258-a04e-171d843a6460"), IndicatorsValueType.LCIAResult),
    ODP("ODP", List.of("06dcd26f-025f-401a-a7c1-5e457eb54637"), IndicatorsValueType.LCIAResult),
    POCP("POCP", List.of("1e84a202-dae6-42aa-9e9d-71ea48b8be00"), IndicatorsValueType.LCIAResult),
    AP("AP", List.of("b4274add-93b7-4905-a5e4-2e878c4e4216"), IndicatorsValueType.LCIAResult),
    EP("EP", List.of("f58827d0-b407-4ec6-be75-8b69efb98a0f"), IndicatorsValueType.LCIAResult),
    ADPE("ADPE", List.of("f7c73bb9-ab1a-4249-9c6d-379a0de6f67e"), IndicatorsValueType.LCIAResult),
    ADPF("ADPF", List.of("804ebcdf-309d-4098-8ed8-fdaf2f389981"), IndicatorsValueType.LCIAResult),

    PERE("PERE", List.of("20f32be5-0398-4288-9b6d-accddd195317"), IndicatorsValueType.Exchange),
    PERM("PERM", List.of("fb3ec0de-548d-4508-aea5-00b73bf6f702"), IndicatorsValueType.Exchange),
    PERT("PERT", List.of("53f97275-fa8a-4cdd-9024-65936002acd0"), IndicatorsValueType.Exchange),
    PENRE("PENRE", List.of("ac857178-2b45-46ec-892a-a9a4332f0372"), IndicatorsValueType.Exchange),
    PENRM("PENRM", List.of("1421caa0-679d-4bf4-b282-0eb850ccae27"), IndicatorsValueType.Exchange),
    PENRT("PENRT", List.of("06159210-646b-4c8d-8583-da9b3b95a6c1"), IndicatorsValueType.Exchange),
    SM("SM", List.of("c6a1f35f-2d09-4f54-8dfb-97e502e1ce92"), IndicatorsValueType.Exchange),
    RSF("RSF", List.of("64333088-a55f-4aa2-9a31-c10b07816787"), IndicatorsValueType.Exchange),
    NRSF("NRSF", List.of("89def144-d39a-4287-b86f-efde453ddcb2"), IndicatorsValueType.Exchange),
    FW("FW", List.of("3cf952c8-f3a4-461d-8c96-96456ca62246"), IndicatorsValueType.Exchange),
    HWD("HWD", List.of("430f9e0f-59b2-46a0-8e0d-55e0e84948fc"), IndicatorsValueType.Exchange),
    NHWD("NHWD", List.of("b29ef66b-e286-4afa-949f-62f1a7b4d7fa"), IndicatorsValueType.Exchange),
    RWD("RWD", List.of("3449546e-52ad-4b39-b809-9fb77cea8ff6"), IndicatorsValueType.Exchange),
    CRU("CRU", List.of("a2b32f97-3fc7-4af2-b209-525bc6426f33"), IndicatorsValueType.Exchange),
    MFR("MFR", List.of("d7fe48a5-4103-49c8-9aae-b0b5dfdbd6ae"), IndicatorsValueType.Exchange),
    MER("MER", List.of("59a9181c-3aaf-46ee-8b13-2b3723b6e447"), IndicatorsValueType.Exchange),
    EEE("EEE", List.of("4da0c987-2b76-40d6-9e9e-82a017aaaf29"), IndicatorsValueType.Exchange),
    EET("EET", List.of("98daf38a-7a79-46d3-9a37-2b7bd0955810"), IndicatorsValueType.Exchange),

    A2AP("AP (A2)", List.of("b5c611c6-def3-11e6-bf01-fe55135034f3"), IndicatorsValueType.LCIAResult),
    A2GWPtotal("GWPtotal (A2)", List.of("6a37f984-a4b3-458a-a20a-64418c145fa2", "a7ea142a-9749-11ed-a8fc-0242ac120002"), IndicatorsValueType.LCIAResult),
    A2GWPbiogenic("GWPbiogenic (A2)", List.of("2356e1ab-0185-4db5-86e5-16de51c7485c", "a7ea186c-9749-11ed-a8fc-0242ac120002"), IndicatorsValueType.LCIAResult),
    A2GWPfossil("GWPfossil (A2)", List.of("5f635281-343e-44fb-83df-1971b155e6b6", "a7ea19c0-9749-11ed-a8fc-0242ac120002"), IndicatorsValueType.LCIAResult),
    A2GWPluluc("GWPluluc (A2)", List.of("4331bbdb-978a-490d-8707-eeb047f01a55", "a7ea1ae2-9749-11ed-a8fc-0242ac120002"), IndicatorsValueType.LCIAResult),
    A2ETPfw("ETPfw (A2)", List.of("ee1082d1-b0f7-43ca-a1f0-21e2a4a74511", "05316e7a-b254-4bea-9cf0-6bf33eb5c630"), IndicatorsValueType.LCIAResult),
    A2PM("PM (A2)", List.of("b5c602c6-def3-11e6-bf01-fe55135034f3"), IndicatorsValueType.LCIAResult),
    A2EPmarine("EPmarine (A2)", List.of("b5c619fa-def3-11e6-bf01-fe55135034f3"), IndicatorsValueType.LCIAResult),
    A2EPfreshwater("EPfreshwater (A2)", List.of("b53ec18f-7377-4ad3-86eb-cc3f4f276b2b"), IndicatorsValueType.LCIAResult),
    A2EPterrestrial("EPterrestrial (A2)", List.of("b5c614d2-def3-11e6-bf01-fe55135034f3"), IndicatorsValueType.LCIAResult),
    A2HTPc("HTPc (A2)", List.of("2299222a-bbd8-474f-9d4f-4dd1f18aea7c"), IndicatorsValueType.LCIAResult),
    A2HTPnc("HTPnc (A2)", List.of("3af763a5-b7a1-48c9-9cee-1f223481fcef", "7cfdcfcf-b222-4b26-888a-a55f9fbf7ac8"), IndicatorsValueType.LCIAResult),
    A2IRP("IRP (A2)", List.of("b5c632be-def3-11e6-bf01-fe55135034f3"), IndicatorsValueType.LCIAResult),
    A2SOP("SOP (A2)", List.of("b2ad6890-c78d-11e6-9d9d-cec0c932ce01"), IndicatorsValueType.LCIAResult),
    A2ODP("ODP (A2)", List.of("b5c629d6-def3-11e6-bf01-fe55135034f3"), IndicatorsValueType.LCIAResult),
    A2POCP("POCP (A2)", List.of("b5c610fe-def3-11e6-bf01-fe55135034f3"), IndicatorsValueType.LCIAResult),
    A2ADPF("ADPF (A2)", List.of("b2ad6110-c78d-11e6-9d9d-cec0c932ce01"), IndicatorsValueType.LCIAResult),
    A2ADPE("ADPE (A2)", List.of("b2ad6494-c78d-11e6-9d9d-cec0c932ce01"), IndicatorsValueType.LCIAResult),
    A2WDP("WDP (A2)", List.of("b2ad66ce-c78d-11e6-9d9d-cec0c932ce01"), IndicatorsValueType.LCIAResult);

    private static final Logger LOGGER = LogManager.getLogger(IndicatorsHeaderEnum.class);

    final List<String> UUIDs;
    final String name;
    final IndicatorsValueType indicatorsValueType;

    IndicatorsHeaderEnum(String name, List<String> UUIDs, IndicatorsValueType indicatorsValueType) {
        this.name = name;
        this.UUIDs = UUIDs;
        this.indicatorsValueType = indicatorsValueType;
    }

    public String getResultValue(Process process, String module, String scenario, DecimalSeparator separator) {
        switch (indicatorsValueType) {
            case LCIAResult:
                return getLciaResultValue(process, module, scenario, separator);
            case Exchange:
                return getExchangeValue(process, module, scenario, separator);
            default:
                return "MISSING IndicatorsValueType";
        }
    }

    private String getExchangeValue(Process process, String module, String scenario, DecimalSeparator separator) {
        for (String UUID : Lists.reverse(UUIDs)) {
            Double d = null;
            try {
                d = process.getExchangesByIndicator(UUID).get(0).getAmountByModuleScenario(module, scenario).getValue();
            } catch (IndexOutOfBoundsException | NullPointerException e) {
            }
            if (d != null)
                return CSVFormatter.format(d, separator);
        }
        LOGGER.trace("no result found for getExchangeValue on process {} for indicators {} module {} scenario {} ", process.getUuidAsString(), UUIDs, module, scenario);
        return "";
    }

    private String getLciaResultValue(Process process, String module, String scenario, DecimalSeparator separator) {
         for (String UUID : Lists.reverse(UUIDs)) {
            Double d = null;
            try {
                d = process.getLciaResultsByIndicator(UUID).get(0).getAmountByModuleScenario(module, scenario).getValue();
            } catch (IndexOutOfBoundsException | NullPointerException e) {
            }
            if (d != null)
                return CSVFormatter.format(d, separator);
        }
        LOGGER.trace("no result found for getLciaResultValue on process {} for indicators {} module {} scenario {} ", process.getUuidAsString(), UUIDs, module, scenario);
        return "";
    }

    @Override
    public String toString() {
        return this.name;
    }

}

