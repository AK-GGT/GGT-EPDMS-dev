package de.iai.ilcd.model.process.contentdeclaration;

import de.iai.ilcd.model.process.contentdeclaration.utils.Dom;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class Component extends ContentElement {

    /**
     * A component can contain materials and substances.
     */
    public final List<ContentElement> content = new ArrayList<>();

    @Override
    Component read(Element e) {
        if (e == null)
            return this;
        super.read(e);
        Dom.eachChild(e, child -> {
            ContentElement ce = ContentDeclaration.readElement(child);
            if ((ce instanceof Material) || (ce instanceof Substance)) {
                content.add(ce);
            }
        });
        return this;
    }

    public List<ContentElement> getContent() {
        return content;
    }
}
