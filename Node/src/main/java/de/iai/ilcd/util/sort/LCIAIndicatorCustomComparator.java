package de.iai.ilcd.util.sort;

import de.iai.ilcd.model.process.LciaResult;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

public class LCIAIndicatorCustomComparator implements Comparator<LciaResult> {

    public static final String[] order = {
            "6a37f984-a4b3-458a-a20a-64418c145fa2", // EN 15804+A2 EF 3.0 : GWP-total
            "2356e1ab-0185-4db5-86e5-16de51c7485c", // EN 15804+A2 EF 3.0 : GWP-biogenic
            "5f635281-343e-44fb-83df-1971b155e6b6", // EN 15804+A2 EF 3.0 : GWP-fossil
            "4331bbdb-978a-490d-8707-eeb047f01a55", // EN 15804+A2 EF 3.0 : GWP-luluc
            "a7ea142a-9749-11ed-a8fc-0242ac120002", // EN 15804+A2 EF 3.1 : GWP-total
            "a7ea186c-9749-11ed-a8fc-0242ac120002", // EN 15804+A2 EF 3.1 : GWP-biogenic
            "a7ea19c0-9749-11ed-a8fc-0242ac120002", // EN 15804+A2 EF 3.1 : GWP-fossil
            "a7ea1ae2-9749-11ed-a8fc-0242ac120002", // EN 15804+A2 EF 3.1 : GWP-luluc
            "b2ad6d9a-c78d-11e6-9d9d-cec0c932ce01", // EF 3.0 : Climate change
            "2105d3ac-c7c7-4c80-b202-7328c14c66e8", // EF 3.0 : Climate change-Fossil
            "0db6bc32-3f72-48b9-bdb3-617849c2752f", // EF 3.0 : Climate change-Biogenic
            "6f1b7d2a-eb2d-4b86-9b4d-2301b3186400", // EF 3.0 : Climate change-Land use and land use change
            "6209b35f-9447-40b5-b68c-a1099e3674a0", // EF 3.1 : Climate change
            "7fce5b3a-66b8-4ce1-91e8-a925aee1f186", // EF 3.1 : Climate change-Fossil
            "706261af-a357-4cc0-a50a-f3033fcbd556", // EF 3.1 : Climate change-Biogenic
            "14af9ca7-aa1d-4832-b1d9-ab05a06dcb12", // EF 3.1 : Climate change-Land use and land use chang
            "b5c629d6-def3-11e6-bf01-fe55135034f3", // EF 3.0 : Ozone depletion
            "2299222a-bbd8-474f-9d4f-4dd1f18aea7c", // EF 3.0 : Human toxicity, cancer
            "3af763a5-b7a1-48c9-9cee-1f223481fcef", // EF 3.0 : Human toxicity, non-cancer
            "503699e0-eca9-4089-8bf8-e0f49c93e578", // EF 3.0 : Human toxicity, cancer_organics
            "6c7a9d7d-4888-41ae-84bb-0d460ec52b80", // EF 3.0 : Human toxicity, cancer_inorganics
            "01500b74-7ffb-463e-9bd4-72f17c2263ff", // EF 3.1 : Human toxicity, cancer_inorganics
            "6453d675-cfd8-42c8-93e9-4ea34ab1b78e", // EF 3.0 : Human toxicity, cancer_metals
            "216596d1-9539-4237-80ff-5eef712b0a71", // EF 3.0 : Human toxicity, non-cancer_organics
            "b92648c3-e996-41ad-9dca-78c69932b779", // EF 3.0 : Human toxicity, non-cancer_inorganics
            "dea1149d-d918-4b48-a3af-838b0a1e4ca1", // EF 3.0 : Human toxicity, non-cancer_metals
            "7cfdcfcf-b222-4b26-888a-a55f9fbf7ac8", // EF 3.1 : Human toxicity, non-cancer
            "9d1d43a2-e1aa-4c16-acd4-3dd8a6a2fb85", // EF 3.1 : Human toxicity, non-cancer_inorganics
            "8c3141e9-1f15-43b5-bff2-182e49893a46", // EF 3.1 : Human toxicity, non-cancer_organics
            "b5c602c6-def3-11e6-bf01-fe55135034f3", // EF 3.0 : EF-particulate Matter
            "b5c632be-def3-11e6-bf01-fe55135034f3", // EF 3.0 : Ionising radiation, human health
            "b5c610fe-def3-11e6-bf01-fe55135034f3", // EF 3.0 : Photochemical ozone formation - human health
            "b5c611c6-def3-11e6-bf01-fe55135034f3", // EF 3.0 : Acidification
            "b5c614d2-def3-11e6-bf01-fe55135034f3", // EF 3.0 : Eutrophication, terrestrial
            "b53ec18f-7377-4ad3-86eb-cc3f4f276b2b", // EF 3.0 : Eutrophication, freshwater
            "b5c619fa-def3-11e6-bf01-fe55135034f3", // EF 3.0 : Eutrophication marine
            "ee1082d1-b0f7-43ca-a1f0-21e2a4a74511", // EF 3.0 : Ecotoxicity, freshwater
            "6800f5d3-7284-4ba3-8eb6-e3ab3ade7995", // EF 3.0 : Ecotoxicity, freshwater_organics
            "9ee9886e-1430-4afe-ae1b-5580b76def07", // EF 3.0 : Ecotoxicity, freshwater_inorganics
            "7fe353ed-2aac-4c81-9045-2f0ed4ef855e", // EF 3.0 : Ecotoxicity, freshwater_metals
            "05316e7a-b254-4bea-9cf0-6bf33eb5c630", // EF 3.1 : Ecotoxicity, freshwater
            "dacd48b5-4da5-49aa-aff4-cd5f5495c037", // EF 3.1 : Ecotoxicity, freshwater_inorganics
            "fd530f00-9325-424a-92ef-aaac67922fd9", // EF 3.1 : Ecotoxicity, freshwater_organics
            "b2ad6890-c78d-11e6-9d9d-cec0c932ce01", // EF 3.0 : Land use
            "b2ad66ce-c78d-11e6-9d9d-cec0c932ce01", // EF 3.0 : Water use
            "b2ad6494-c78d-11e6-9d9d-cec0c932ce01", // EF 3.0 : Resource use, minerals and metals
            "b2ad6110-c78d-11e6-9d9d-cec0c932ce01", // EF 3.0 : Resource use, fossils
            "77e416eb-a363-4258-a04e-171d843a6460", // EN 15804+A1 : GWP
            "06dcd26f-025f-401a-a7c1-5e457eb54637", // EN 15804+A1 : ODP
            "1e84a202-dae6-42aa-9e9d-71ea48b8be00", // EN 15804+A1 : POCP
            "b4274add-93b7-4905-a5e4-2e878c4e4216", // EN 15804+A1 : AP
            "f58827d0-b407-4ec6-be75-8b69efb98a0f", // EN 15804+A1 : EP
            "f7c73bb9-ab1a-4249-9c6d-379a0de6f67e", // EN 15804+A1 : ADPE
            "804ebcdf-309d-4098-8ed8-fdaf2f389981", // EN 15804+A1 : ADPF

            "1cf37565-0154-4f01-94e4-b4dcbf63b519", // RMI fossile
            "755a42f4-bce4-4aaf-af9b-f6ffad4dabb8", // RMI metals
            "eb2ad53a-874a-4c97-8017-fa2c06803b9f", // RMI minerals
            "03ee44a1-0e8a-471e-a30d-6e779361dddf", // RMI forestry
            "505188c3-77bb-4bcd-bb49-2f5f328aa372", // RMI agriculture
            "f88e175e-0cf6-49dc-9219-539d023a03d3", // RMI fisheries
            "32200a68-5488-49d7-9e81-0e93c0f19cc1", // TMR fossile
            "4be326ff-416b-400f-b835-472f33b4cf16", // TMR metals
            "a9b02237-8034-49ee-850d-876dbe6e148d", // TMR  minerals
            "89c8b6c5-c525-4541-8dbd-211b8db1d3f0", // TMR forestry
            "25359720-f52a-4597-980d-3ff571bc2164", // TMR agriculture
            "5b6a0c3a-d0d3-49ff-811e-9816aa8c71f5" // TMR fisheries
    };

    public LCIAIndicatorCustomComparator() {
        this.comp = new FixedOrderComparator<>(order);
        this.comp.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.AFTER);
    }
    private final FixedOrderComparator<String> comp;

    @Override
    public int compare(LciaResult o1, LciaResult o2) {

        String uuid1 = o1.getMethodReference().getRefObjectId().trim().toLowerCase();
        String uuid2 = o2.getMethodReference().getRefObjectId().trim().toLowerCase();

        try {
            return comp.compare(uuid1, uuid2);
        } catch (IllegalArgumentException e) {
            return StringUtils.compare(uuid1, uuid2);
        }
    }
}
