package de.iai.ilcd.model.flowproperty;

import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.flowproperty.IUnitGroupType;
import de.iai.ilcd.model.unitgroup.UnitGroup;

/**
 * @author clemens.duepmeier
 */
public class UnitGroupDetails implements IUnitGroupType {

    private UnitGroup unitGroup;

    private IGlobalReference unitGroupReference;

    public UnitGroupDetails(UnitGroup unitGroup, IGlobalReference unitGroupReference) {
        this.unitGroup = unitGroup;
        this.unitGroupReference = unitGroupReference;
    }

    @Override
    public String getHref() {
        return null;
    }

    @Override
    public IMultiLangString getName() {
        return (unitGroup != null ? unitGroup.getName() : null);
    }

    @Override
    public String getDefaultUnit() {
        if (this.unitGroup != null) {
            return unitGroup.getDefaultUnit();
        } else {
            return "";
        }

    }

    @Override
    public IGlobalReference getReference() {
        return unitGroupReference;
    }

}
