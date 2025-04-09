package de.iai.ilcd.webgui.controller.ui;

import de.fzk.iai.ilcd.api.binding.generated.unitgroup.UnitGroupDataSetType;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.IUnitGroupVO;
import de.iai.ilcd.model.dao.UnitGroupDao;
import de.iai.ilcd.model.unitgroup.UnitGroup;

import javax.faces.bean.ManagedBean;

/**
 * Backing bean for unit group detail view
 */
@ManagedBean
public class UnitgroupHandler extends AbstractDataSetHandler<IUnitGroupVO, UnitGroup, UnitGroupDao, UnitGroupDataSetType> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -4782844752690903955L;

    /**
     * Initialize handler
     */
    public UnitgroupHandler() {
        super(new UnitGroupDao(), DatasetTypes.UNITGROUPS.getValue(), ILCDTypes.UNITGROUP);
    }

    /**
     * Convenience method, delegates to {@link #getDataSet()}
     *
     * @return represented unit group
     */
    public IUnitGroupVO getUnitgroup() {
        return this.getDataSet();
    }

}
