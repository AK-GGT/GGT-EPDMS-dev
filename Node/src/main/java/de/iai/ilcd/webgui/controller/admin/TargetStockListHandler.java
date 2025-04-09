package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.common.TargetStock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.SelectableDataModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler for managing several target stocks.
 *
 * @author sarai
 */
@ViewScoped
@ManagedBean(name = "targetStockListHandler")
public class TargetStockListHandler implements SelectableDataModel<TargetStock> {
    /*
     * The logger for loggong data.
     */
    protected final Logger log = LogManager.getLogger(this.getClass());

    /*
     * The target stock
     */
    private TargetStock selectedTargetStock = new TargetStock();

    /*
     * List of available target stocks
     */
    private List<TargetStock> targetStockList = new ArrayList<TargetStock>();

    /**
     * The constructor.
     */
    public TargetStockListHandler() {

    }

    /**
     * Gets selected target stock.
     *
     * @return Selected taget stock
     */
    public TargetStock getSelectedTargetStock() {
        return this.selectedTargetStock;
    }

    /**
     * Sets the selected target stock.
     *
     * @param selectedTargetStock The selected target stock
     */
    public void setSelectedTargetStock(TargetStock selectedTargetStock) {
        this.selectedTargetStock = selectedTargetStock;
    }

    /**
     * Gets list of available target stocks.
     *
     * @return List of available target stocks
     */
    public List<TargetStock> getTargetStockList() {
        return targetStockList;
    }

    /**
     * Sets the list of available target stocks.
     *
     * @param targetStockList List of available target stocks
     */
    public void setTargetStockList(List<TargetStock> targetStockList) {
        this.targetStockList = targetStockList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getRowKey(TargetStock object) {
        return object.getDsUuid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TargetStock getRowData(String rowKey) {
        for (TargetStock object : targetStockList) {
            if (object.getDsUuid().equals(rowKey)) {
                return object;
            }
        }
        return null;
    }

}