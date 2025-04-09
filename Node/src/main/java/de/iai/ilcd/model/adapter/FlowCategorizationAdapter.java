package de.iai.ilcd.model.adapter;

import de.fzk.iai.ilcd.service.client.impl.vo.types.common.CategoryType;
import de.fzk.iai.ilcd.service.client.impl.vo.types.flow.FlowCategorizationType;
import de.fzk.iai.ilcd.service.model.common.IClass;
import de.fzk.iai.ilcd.service.model.common.IClassification;

public class FlowCategorizationAdapter extends FlowCategorizationType {

    public FlowCategorizationAdapter(IClassification classification) throws Exception {
        if (classification == null || classification.getIClassList().size() == 0) {
            throw new Exception("no categorization present");
        }

        this.setName(classification.getName());

        for (IClass cat : classification.getIClassList()) {
            this.getCategories().add(new CategoryType(cat.getLevel(), cat.getName()));
        }
    }
}
