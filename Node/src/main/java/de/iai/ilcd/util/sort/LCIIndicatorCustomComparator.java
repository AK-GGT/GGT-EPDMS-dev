package de.iai.ilcd.util.sort;

import de.iai.ilcd.model.process.Exchange;
import org.apache.commons.collections4.comparators.FixedOrderComparator;

import java.util.Comparator;

public class LCIIndicatorCustomComparator implements Comparator<Exchange> {

    public static final String[] order = {"20f32be5-0398-4288-9b6d-accddd195317", "fb3ec0de-548d-4508-aea5-00b73bf6f702", "53f97275-fa8a-4cdd-9024-65936002acd0", "ac857178-2b45-46ec-892a-a9a4332f0372", "1421caa0-679d-4bf4-b282-0eb850ccae27", "06159210-646b-4c8d-8583-da9b3b95a6c1", "c6a1f35f-2d09-4f54-8dfb-97e502e1ce92", "64333088-a55f-4aa2-9a31-c10b07816787", "89def144-d39a-4287-b86f-efde453ddcb2", "3cf952c8-f3a4-461d-8c96-96456ca62246", "430f9e0f-59b2-46a0-8e0d-55e0e84948fc", "b29ef66b-e286-4afa-949f-62f1a7b4d7fa", "3449546e-52ad-4b39-b809-9fb77cea8ff6", "a2b32f97-3fc7-4af2-b209-525bc6426f33", "d7fe48a5-4103-49c8-9aae-b0b5dfdbd6ae", "59a9181c-3aaf-46ee-8b13-2b3723b6e447", "4da0c987-2b76-40d6-9e9e-82a017aaaf29", "98daf38a-7a79-46d3-9a37-2b7bd0955810"};

    public LCIIndicatorCustomComparator() {
        this.comp = new FixedOrderComparator<>(order);
        this.comp.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.AFTER);
    }

    private final FixedOrderComparator<String> comp;

    @Override
    public int compare(Exchange o1, Exchange o2) {

        String uuid1 = o1.getFlowReference().getRefObjectId().trim().toLowerCase();
        String uuid2 = o2.getFlowReference().getRefObjectId().trim().toLowerCase();

        return comp.compare(uuid1, uuid2);
    }
}
