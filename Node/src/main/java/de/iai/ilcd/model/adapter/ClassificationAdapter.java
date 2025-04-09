package de.iai.ilcd.model.adapter;

import de.fzk.iai.ilcd.service.client.impl.vo.types.common.ClassType;
import de.fzk.iai.ilcd.service.client.impl.vo.types.common.ClassificationType;
import de.fzk.iai.ilcd.service.model.common.IClass;
import de.fzk.iai.ilcd.service.model.common.IClassification;

public class ClassificationAdapter extends ClassificationType {

    public ClassificationAdapter(IClassification classification) throws Exception {
        if (classification == null || classification.getIClassList().size() == 0)
            throw new Exception("no classification present");

        this.setName(classification.getName());

        for (IClass clazz : classification.getIClassList()) {
            this.getClasses().add(new ClassType(clazz.getLevel(), clazz.getName(), clazz.getClId()));
        }
    }
}
