package de.iai.ilcd.webgui.controller;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.io.Serializable;

@ManagedBean(name = "dirty")
@ApplicationScoped
public class DirtyFlagBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7282679061288102510L;

    /**
     * Time stamp of last data stock modification
     */
    private long dataStockDirtyStamp = DirtyFlagBean.getNow();

    /**
     * Get the current time stamp / milliseconds
     *
     * @return current time stamp / milliseconds
     */
    public final static long getNow() {
        return System.currentTimeMillis();
    }

    /**
     * Determine if provided time stamp indicates that a stock was modified since
     *
     * @param ts time stamp to check
     * @return <code>true</code> if modification was performed, <code>false</code> otherwise
     */
    public boolean isStockModificationDirty(long ts) {
        return this.dataStockDirtyStamp > ts;
    }

    /**
     * Set stock modification to now
     */
    public void stockModified() {
        this.dataStockDirtyStamp = DirtyFlagBean.getNow();
    }

}
