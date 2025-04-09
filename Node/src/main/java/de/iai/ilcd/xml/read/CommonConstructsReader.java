package de.iai.ilcd.xml.read;

import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.common.ILString;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.iai.ilcd.model.common.*;
import de.iai.ilcd.model.common.exception.FormatException;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author clemens.duepmeier
 */
public class CommonConstructsReader {

    private final Namespace commonNamespace = Namespace.getNamespace("common", "http://lca.jrc.it/ILCD/Common");
    private DataSetParsingHelper parsingHelper = null;

    public CommonConstructsReader(DataSetParsingHelper parsingHelper) {
        this.parsingHelper = parsingHelper;
    }

    public Uuid getUuid() {
        String xpath = "/*/*/*[local-name()='dataSetInformation']/common:UUID";

        String uuidString = this.parsingHelper.getStringValue(xpath);

        return new Uuid(uuidString);
    }

    public List<Classification> getClassifications(DataSetType dataSetType) {
        List<Element> classificationSections = this.parsingHelper.getElements("/*/*/*[local-name()='dataSetInformation']/*[local-name()='classificationInformation']/common:classification");

        List<Classification> classifications = new LinkedList<Classification>();
        for (Element classificationSection : classificationSections) {
            String classificationName = classificationSection.getAttributeValue("name");
            if (classificationName == null) {
                classificationName = "ilcd";
            }
            Classification classification = new Classification(classificationName);
            // logger.debug("found classification with name " + classificationName);

            // OK, now we have to read in the classification classes

            List classTags = classificationSection.getChildren("class", this.commonNamespace);
            for (Object classTag : classTags) {
                Element classElement = (Element) classTag;
                String level = classElement.getAttributeValue("level");
                String uuidStr = classElement.getAttributeValue("uuid");
                String id = classElement.getAttributeValue("classId");
                String className = classElement.getText();
                ClClass clClass = new ClClass(className, Integer.parseInt(level));
                clClass.setDataSetType(dataSetType);
                if (uuidStr != null) {
                    Uuid uuid = new Uuid(uuidStr);
                    clClass.setUuid(uuid);
                }
                if (id != null) {
                    clClass.setClId(id);
                }
                // logger.debug("found class with name=%s, level=%s\n",className,level);
                classification.addClass(clClass);
            }
            classifications.add(classification);
        }

        return classifications;
    }

    public GlobalReference getGlobalReference(String xpath, PrintWriter out) {

        Element referenceElement = this.parsingHelper.getElement(xpath);
        if (referenceElement == null) {
            return null;
        }
        GlobalReference globalRef = this.createGlobalReference(referenceElement, out);

        return globalRef;
    }

    public GlobalReference getGlobalReference(Element element, String subElementName, Namespace nameSpace, PrintWriter out) {
        Element referenceElement = element.getChild(subElementName, nameSpace);
        if (referenceElement == null) {
            return null;
        }
        GlobalReference globalRef = this.createGlobalReference(referenceElement, out);

        return globalRef;
    }

    public GlobalReference getGlobalReference(Element referenceElement, PrintWriter out) {
        return this.createGlobalReference(referenceElement, out);
    }

    public IGlobalReference getGlobalReference(Element element, String subElementName, Namespace nameSpace, Class<?> clazz, PrintWriter out) {
        Element referenceElements = element.getChild(subElementName, nameSpace);

        IGlobalReference reference = this.createGlobalReference(referenceElements, clazz, out);

        return reference;
    }

    public List<GlobalReference> getGlobalReferences(String xpath, PrintWriter out) {
        List<GlobalReference> references = new ArrayList<GlobalReference>();

        List<Element> referenceElements = this.parsingHelper.getElements(xpath);

        for (Element referenceElement : referenceElements) {
            GlobalReference reference = this.createGlobalReference(referenceElement, out);
            references.add(reference);
        }
        return references;
    }

    public List<IGlobalReference> getGlobalReferences(Element element, String subElementName, Namespace nameSpace, PrintWriter out) {
        return getGlobalReferences(element, subElementName, nameSpace, GlobalReference.class, out);
    }

    @SuppressWarnings("unchecked")
    public List<IGlobalReference> getGlobalReferences(Element element, String subElementName, Namespace nameSpace, Class<?> clazz, PrintWriter out) {
        List<IGlobalReference> references = new ArrayList<IGlobalReference>();

        List<Element> referenceElements = element.getChildren(subElementName, nameSpace);

        for (Element referenceElement : referenceElements) {
            IGlobalReference reference = this.createGlobalReference(referenceElement, clazz, out);
            references.add(reference);
        }

        return references;
    }

    private GlobalReference createGlobalReference(Element referenceElement, PrintWriter out) {
        return (GlobalReference) createGlobalReference(referenceElement, GlobalReference.class, out);
    }

    @SuppressWarnings("unchecked")
    private <T extends IGlobalReference> IGlobalReference createGlobalReference(Element referenceElement, Class<?> clazz, PrintWriter out) {

        T globalRef;

        try {
            globalRef = (T) clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | NoSuchMethodException | SecurityException e1) {
            e1.printStackTrace();
            return null;
        }

        String uri = referenceElement.getAttributeValue("uri");
        String type = referenceElement.getAttributeValue("type");
        String versionString = referenceElement.getAttributeValue("version");
        String refObjectId = referenceElement.getAttributeValue("refObjectId");

        IMultiLangString shortDescription;

        if (clazz.equals(GlobalReference.class))
            shortDescription = this.parsingHelper.getIMultiLanguageString(referenceElement, "shortDescription", this.commonNamespace);
        else
            shortDescription = this.parsingHelper.getSimpleIMultiLanguageString(referenceElement, "shortDescription", this.commonNamespace);

        globalRef.setUri(uri);
        if (out != null && uri == null) {
            // an empty @uri attribute is totally okay
        }

        GlobalReferenceTypeValue dataSetType = null;
        try {
            dataSetType = GlobalReferenceTypeValue.fromValue(type);
        } catch (Exception e) {
            if (out != null) {
                out.println("Warning: global reference for " + referenceElement.getName() + " has no type attribute value");
            }
        }
        globalRef.setType(dataSetType);

        if (clazz.equals(GlobalReference.class)) {
            DataSetVersion version = null;
            try {
                if (versionString != null) {
                    version = DataSetVersion.parse(versionString);
                }
            } catch (FormatException ex) {
                if (out != null) {
                    out.println("Warning: global reference for " + referenceElement.getName() + " has version attribute value which isn't a version number");
                }
            }

            ((GlobalReference) globalRef).setVersion(version);
        } else {
            globalRef.setVersion(versionString);
        }


        if (refObjectId != null) {
            if (clazz.equals(GlobalReference.class))
                ((GlobalReference) globalRef).setUuid(new Uuid(refObjectId));
            else
                globalRef.setRefObjectId(refObjectId);
        } else {
            // refObjectId does not deliver valid UUID, let' try to extract it from uri
            if (out != null) {
                out.println("Warning: global reference for " + referenceElement.getName() + "has no valid refObjectId set; will try to extract it from uri");
            }
            if (uri != null) {
                GlobalRefUriAnalyzer uriAnalyzer = new GlobalRefUriAnalyzer(uri);
                if (uriAnalyzer.getUuid() != null) {
                    if (clazz.equals(GlobalReference.class))
                        ((GlobalReference) globalRef).setUuid(uriAnalyzer.getUuid());
                    else
                        globalRef.setRefObjectId(uriAnalyzer.getUuidAsString());
                }
            }
        }

        for (ILString l : shortDescription.getLStrings())
            globalRef.getShortDescription().setValue(l.getLang(), l.getValue());

        //TODO: subreferences currently only supported for GlobalReference (not GlobalReferenceType)
        if (clazz.equals(GlobalReference.class) && globalRef.getType().equals(GlobalReferenceTypeValue.SOURCE_DATA_SET)) {
            List<String> subRefs = this.parsingHelper.getStringValues(referenceElement, "subReference", this.commonNamespace);
            if (subRefs != null)
                ((GlobalReference) globalRef).getSubReferences().addAll(subRefs);
        }

        return globalRef;
    }
}
