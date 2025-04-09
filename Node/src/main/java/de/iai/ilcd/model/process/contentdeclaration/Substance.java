package de.iai.ilcd.model.process.contentdeclaration;

import de.iai.ilcd.model.process.contentdeclaration.utils.Fn;
import de.iai.ilcd.model.process.contentdeclaration.utils.StringUtils;
import de.iai.ilcd.model.process.contentdeclaration.utils.Vocab;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import java.util.function.Function;

public class Substance extends ContentElement {

    /**
     * The optional data dictionary GUID (whatever this is).
     */
    public String guid;

    /**
     * CAS Number of the material or substance.
     */
    public String casNumber;

    /**
     * EC Number of the material or substance.
     */
    public String ecNumber;

    /**
     * The percentage of renewable resources contained.
     */
    public Double renewable;

    /**
     * The percentage of recycled materials contained.
     */
    public Double recycled;

    /**
     * The percentage of recyclable materials contained.
     */
    public Double recyclable;

    public Boolean packaging;

    @Override
    Substance read(Element e) {
        if (e == null)
            return this;
        super.read(e);
        guid = e.getAttributeNS(Vocab.NS_EPDv2, "ddGUID");
        casNumber = e.getAttributeNS(Vocab.NS_EPDv2, "CASNumber");
        ecNumber = e.getAttributeNS(Vocab.NS_EPDv2, "ECNumber");

        Function<String, Double> dfn = attr -> {
            try {
                String s = e.getAttributeNS(Vocab.NS_EPDv2, attr);
                if (StringUtils.nullOrEmpty(s))
                    return null;
                return Double.parseDouble(s);
            } catch (Exception ex) {
                Logger log = LogManager.getLogger(getClass());
                log.error("failed to read attr " + attr, ex);
                return null;
            }
        };
        renewable = dfn.apply("renewable");
        recycled = dfn.apply("recycled");
        recyclable = dfn.apply("recyclable");

        packaging = null;
        Fn.with(e.getAttributeNS(Vocab.NS_EPDv2, "packaging"), s -> {
            if (!StringUtils.nullOrEmpty(s)) {
                s = s.trim().toLowerCase();
                packaging = "true".equals(s) || "1".equals(s);
            }
        });
        return this;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getCasNumber() {
        return casNumber;
    }

    public void setCasNumber(String casNumber) {
        this.casNumber = casNumber;
    }

    public String getEcNumber() {
        return ecNumber;
    }

    public void setEcNumber(String ecNumber) {
        this.ecNumber = ecNumber;
    }

    public Double getRenewable() {
        return renewable;
    }

    public void setRenewable(Double renewable) {
        this.renewable = renewable;
    }

    public Double getRecycled() {
        return recycled;
    }

    public void setRecycled(Double recycled) {
        this.recycled = recycled;
    }

    public Double getRecyclable() {
        return recyclable;
    }

    public void setRecyclable(Double recyclable) {
        this.recyclable = recyclable;
    }

    public Boolean isPackaging() {
        return packaging;
    }

    public void setPackaging(Boolean packaging) {
        this.packaging = packaging;
    }
}
