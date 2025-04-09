package de.iai.ilcd.model.process.contentdeclaration;

import de.fzk.iai.ilcd.api.binding.generated.common.Other;
import de.iai.ilcd.model.process.contentdeclaration.utils.Dom;
import de.iai.ilcd.model.process.contentdeclaration.utils.Vocab;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Content declaration according to EN 15804/ISO 219301. The content declaration
 * may contain component, material and/or substance elements, which may (but do
 * not have to) be nested.
 */
public class ContentDeclaration {

    /**
     * A content declaration can contain components, materials, and substances.
     * Components can in turn contain materials and substances; materials can
     * contain substances.
     */
    public final List<ContentElement> content = new ArrayList<>();

    /**
     * Read a content declaration from the given extension element. May return
     * null when the extension is null or when it has no content declaration.
     */
    public static ContentDeclaration read(Other other) {
        if (other == null)
            return null;

        // find the root element
        Element root = null;
        for (Object any : other.getAny()) {
            if (!(any instanceof Element))
                continue;
            Element e = (Element) any;
            if (Objects.equals(Vocab.NS_EPDv2, e.getNamespaceURI())
                    && Objects.equals("contentDeclaration", e.getLocalName())) {
                root = e;
                break;
            }
        }
        if (root == null)
            return null;

        // add the content elements
        ContentDeclaration decl = new ContentDeclaration();
        Dom.eachChild(root, e -> {
            ContentElement ce = readElement(e);
            if (ce == null)
                return;
            decl.content.add(ce);
        });
        return decl;
    }

    static ContentElement readElement(Element elem) {
        if (elem == null)
            return null;
        if (!Objects.equals(Vocab.NS_EPDv2, elem.getNamespaceURI()))
            return null;
        switch (elem.getLocalName()) {
            case "component":
                return new Component().read(elem);
            case "material":
                return new Material().read(elem);
            case "substance":
                return new Substance().read(elem);
            default:
                return null;
        }
    }

    public List<ContentElement> getContent() {
        return content;
    }
}
