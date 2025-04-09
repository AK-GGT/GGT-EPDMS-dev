package de.iai.ilcd.model.adapter;

import de.fzk.iai.ilcd.service.client.impl.vo.types.common.ClassType;
import de.fzk.iai.ilcd.service.model.common.IClass;

public class ClClassAdapter extends ClassType {

    public ClClassAdapter(IClass clazz) {
        super(clazz.getLevel(), clazz.getName(), clazz.getClId());
    }
}
