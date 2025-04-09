package de.iai.ilcd.model.process.contentdeclaration;

import de.iai.ilcd.model.process.contentdeclaration.utils.StringUtils;
import de.iai.ilcd.model.process.contentdeclaration.utils.Vocab;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

public class ContentAmount {
    /**
     * For specifying a discrete value: the value.
     */
    public Double value;

    /**
     * For specifying a range of values: the lower value of the range.
     */
    public Double lowerValue;

    /**
     * For specifying a range of values: the upper value of the range. For
     * specifying a value lower than x (e.g. "<42"), only specify the upper
     * value as x.
     */
    public Double upperValue;

    static ContentAmount from(Element e) {
        ContentAmount a = new ContentAmount();
        if (e == null)
            return a;
        try {
            String vStr = e.getAttributeNS(Vocab.NS_EPDv2, "value");
            if (!StringUtils.nullOrEmpty(vStr)) {
                a.value = Double.parseDouble(vStr);
            }
            String lStr = e.getAttributeNS(Vocab.NS_EPDv2, "lowerValue");
            if (!StringUtils.nullOrEmpty(lStr)) {
                a.lowerValue = Double.parseDouble(lStr);
            }
            String uStr = e.getAttributeNS(Vocab.NS_EPDv2, "upperValue");
            if (!StringUtils.nullOrEmpty(uStr)) {
                a.upperValue = Double.parseDouble(uStr);
            }
        } catch (Exception ex) {
            Logger log = LogManager.getLogger(ContentAmount.class);
            log.error("failed to read contect amount", ex);
        }
        return a;
    }

    void write(Element e) {
        if (e == null)
            return;
        if (value != null) {
            e.setAttributeNS(
                    Vocab.NS_EPDv2, "epd2:value", value.toString());
        }
        if (lowerValue != null) {
            e.setAttributeNS(
                    Vocab.NS_EPDv2, "epd2:lowerValue", lowerValue.toString());
        }
        if (upperValue != null) {
            e.setAttributeNS(
                    Vocab.NS_EPDv2, "epd2:upperValue", upperValue.toString());
        }
    }

    @Override
    public String toString() {
        if (value != null)
            return Double.toString(value);
        if (lowerValue != null && upperValue != null) {
            return Double.toString(lowerValue) + " - "
                    + Double.toString(upperValue);
        }
        if (upperValue != null)
            return "< " + Double.toString(upperValue);
        if (lowerValue != null)
            return "> " + Double.toString(lowerValue);
        return "";
    }

    @Override
    public ContentAmount clone() {
        ContentAmount clone = new ContentAmount();
        clone.value = value;
        clone.lowerValue = lowerValue;
        clone.upperValue = upperValue;
        return clone;
    }
}
