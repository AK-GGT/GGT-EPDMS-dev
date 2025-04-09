package de.iai.ilcd.model.process.contentdeclaration;


import de.fzk.iai.ilcd.service.client.impl.vo.types.common.LString;
import de.fzk.iai.ilcd.service.client.impl.vo.types.common.MultiLangString;
import de.iai.ilcd.model.process.contentdeclaration.utils.Dom;
import de.iai.ilcd.model.process.contentdeclaration.utils.Vocab;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class ContentElement {
    /**
     * Name of the component, material, or substance.
     */
    public final MultiLangString name = new MultiLangString();
    /**
     * Some comment about the component, material, or substance.
     */
    public final MultiLangString comment = new MultiLangString();
    /**
     * Mass percentage: either a discrete value or a range of values has to be
     * specified.
     */
    public ContentAmount massPerc;
    /**
     * Absolute mass of the fraction in kg. Either a discrete value or a range
     * of values has to be specified.
     */
    public ContentAmount mass;

    ContentElement read(Element e) {
        if (e == null)
            return this;
        List<LString> lNames = Dom.getChildren(e, "name", Vocab.NS_EPDv2).stream()
                .map(Dom::getLString)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (LString lName : lNames)
            name.addLString(lName);

        List<LString> lComments = Dom.getChildren(e, "comment", Vocab.NS_EPDv2).stream()
                .map(Dom::getLString)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (LString lComment : lComments)
            comment.addLString(lComment);

        Element massPercElem = Dom.getChild(e, "weightPerc", Vocab.NS_EPDv2);
        if (massPercElem != null) {
            massPerc = ContentAmount.from(massPercElem);
        }
        Element massElem = Dom.getChild(e, "mass", Vocab.NS_EPDv2);
        if (massElem != null) {
            mass = ContentAmount.from(massElem);
        }
        return this;
    }

    public ContentAmount getMassPerc() {
        return massPerc;
    }

    public void setMassPerc(ContentAmount massPerc) {
        this.massPerc = massPerc;
    }

    public ContentAmount getMass() {
        return mass;
    }

    public void setMass(ContentAmount mass) {
        this.mass = mass;
    }

    public MultiLangString getName() {
        return name;
    }

    public MultiLangString getComment() {
        return comment;
    }
}
