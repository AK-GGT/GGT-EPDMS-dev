package de.iai.ilcd.model.process.contentdeclaration.utils;

import de.fzk.iai.ilcd.api.binding.generated.common.Other;
import de.fzk.iai.ilcd.service.client.impl.vo.types.common.LString;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;


/**
 * Utility methods for reading and writing data using the W3C DOM API.
 */
public final class Dom {
    private Dom() {
    }

    /**
     * Get all child elements with the given name and namespace from the parent
     * element.
     */
    public static List<Element> getChildren(Element parent, String name,
                                            String ns) {
        if (parent == null || name == null)
            return Collections.emptyList();
        List<Element> elems = new ArrayList<>();
        eachChild(parent, e -> {
            if (!Objects.equals(ns, e.getNamespaceURI()))
                return;
            if (Objects.equals(name, e.getLocalName())) {
                elems.add(e);
            }
        });
        return elems;
    }

    public static String getAttribute(Element elem, String name) {
        if (elem == null)
            return null;
        return elem.getAttribute(name);
    }

    public static LString getLString(Element elem) {
        String text = getText(elem);
        if (text == null)
            return null;
        String lang = getAttribute(elem, "xml:lang");
        if (StringUtils.nullOrEmpty(lang)) {
            lang = "en";
        }
        return new LString(lang, text);
    }

    /**
     * Get the first child element with the given name and namespace from the
     * parent element.
     */
    public static Element getChild(Element parent, String name, String ns) {
        if (parent == null || name == null)
            return null;
        AtomicReference<Element> ar = new AtomicReference<Element>();
        eachChild(parent, e -> {
            if (ar.get() != null)
                return;
            if (!Objects.equals(ns, e.getNamespaceURI()))
                return;
            if (Objects.equals(name, e.getLocalName())) {
                ar.set(e);
            }
        });
        return ar.get();
    }


    public static String getText(Element e) {
        if (e == null)
            return null;
        NodeList nl = e.getChildNodes();
        if (nl == null || nl.getLength() == 0)
            return null;
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n == null)
                continue;
            s.append(n.getTextContent());
        }
        return s.toString();
    }

    /**
     * Removes all elements with the given tag-name from the extensions.
     */
    public static void clear(Other extension, String tagName) {
        if (extension == null || tagName == null)
            return;
        List<Element> matches = new ArrayList<>();
        for (Object any : extension.getAny()) {
            if (!(any instanceof Element))
                continue;
            Element e = (Element) any;
            if (Objects.equals(tagName, e.getLocalName()))
                matches.add(e);
        }
        extension.getAny().removeAll(matches);
    }

    public static void eachChild(Element parent, Consumer<Element> fn) {
        if (parent == null || fn == null)
            return;
        NodeList childs = parent.getChildNodes();
        if (childs == null || childs.getLength() == 0)
            return;
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            if (!(node instanceof Element))
                continue;
            fn.accept((Element) node);
        }
    }
}
