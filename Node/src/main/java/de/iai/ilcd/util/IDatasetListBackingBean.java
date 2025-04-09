package de.iai.ilcd.util;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.webgui.controller.admin.StockHandler;

public interface IDatasetListBackingBean<E extends DataSet> {

    public StockHandler getStockHandler();

    public DependenciesOptions getDependenciesOptions();

    public IDataStockMetaData getMoveTarget();

    public void setMoveTarget(IDataStockMetaData moveTarget);

}