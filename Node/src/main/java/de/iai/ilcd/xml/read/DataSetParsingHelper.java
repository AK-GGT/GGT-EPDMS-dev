package de.iai.ilcd.xml.read;

import de.fzk.iai.ilcd.service.client.impl.vo.types.common.LString;
import de.fzk.iai.ilcd.service.client.impl.vo.types.common.MultiLangString;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * @author clemens.duepmeier
 */
public class DataSetParsingHelper {

    public static Logger logger = LogManager.getLogger(de.iai.ilcd.xml.read.DataSetParsingHelper.class);
    private final Namespace xmlNamespace = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");
    private JXPathContext context = null;

    public DataSetParsingHelper(JXPathContext context) {
        this.context = context;
    }

    public IMultiLangString getIMultiLanguageString(String xpath) {

        Map<String, String> languageStrings = this.getChildLanguageStrings(xpath);

        return new MultiLangStringMapAdapter(languageStrings);
    }

    public IMultiLangString getIMultiLanguageString(Element element, String xpath) {
        Map<String, String> languageStrings = this.getChildLanguageStrings(element, xpath, null);

        return new MultiLangStringMapAdapter(languageStrings);
    }

    public IMultiLangString getIMultiLanguageString(Element element, String xpath, Namespace ns) {
        Map<String, String> languageStrings = this.getChildLanguageStrings(element, xpath, ns);

        if (languageStrings == null) {
            return null;
        }
        return new MultiLangStringMapAdapter(languageStrings);
    }

    public IMultiLangString getSimpleIMultiLanguageString(Element element, String xpath, Namespace ns) {
        Map<String, String> languageStrings = this.getSimpleChildLanguageStrings(element, xpath, ns);

        if (languageStrings == null) {
            return null;
        }

        List<LString> lStrings = new ArrayList<LString>();
        for (String lang : languageStrings.keySet())
            lStrings.add(new LString(lang, languageStrings.get(lang)));

        return new MultiLangString(lStrings);
    }


    // public MultiLanguageString getMultiLanguageString( String xpath ) {
    //
    // Map<String, String> languageStrings = getChildLanguageStrings( xpath );
    //
    // return createLanguageStringFromMap( languageStrings );
    // }
    //
    // public MultiLanguageString getMultiLanguageString( Element element, String xpath ) {
    // Map<String, String> languageStrings = getChildLanguageStrings( element, xpath, null );
    //
    // return createLanguageStringFromMap( languageStrings );
    // }
    //
    // public MultiLanguageString getMultiLanguageString( Element element, String xpath, Namespace ns ) {
    // Map<String, String> languageStrings = getChildLanguageStrings( element, xpath, ns );
    //
    // if ( languageStrings == null )
    // return null;
    // return createLanguageStringFromMap( languageStrings );
    // }

    // public MultiLanguageText getMultiLanguageText( String xpath ) {
    //
    // Map<String, String> languageStrings = getChildLanguageStrings( xpath );
    //
    // return createLanguageTextFromMap( languageStrings );
    // }
    //
    // public MultiLanguageText getMultiLanguageText( Element element, String xpath ) {
    // Map<String, String> languageStrings = getChildLanguageStrings( element, xpath, null );
    //
    // return createLanguageTextFromMap( languageStrings );
    // }
    //
    // public MultiLanguageText getMultiLanguageText( Element element, String xpath, Namespace ns ) {
    // Map<String, String> languageStrings = getChildLanguageStrings( element, xpath, ns );
    //
    // return createLanguageTextFromMap( languageStrings );
    // }

    public String getStringValue(String xpath) {
        String returnValue = (String) this.context.getValue(xpath);
        // logger.trace(xpath + ": " + returnValue);
        return returnValue;
    }

    public int getIntValue(String xpath) {
        int returnValue = 0;
        String value = (String) this.context.getValue(xpath);
        logger.trace(value);
        returnValue = Integer.parseInt(value);
        // logger.trace(returnValue);
        return returnValue;
    }

    public List<String> getStringValues(String xpath) {
        List<String> returnList = new ArrayList<String>();

        List<Element> nodes = this.context.selectNodes(xpath);
        Iterator<Element> i = nodes.iterator();
        while (i.hasNext()) {
            Element elem = i.next();
            // logger.trace("Element: " + elem.getName());
            String value = (String) elem.getText();
            // logger.trace("Digital file value: " + value);
            if (value != null) {
                returnList.add(value);
            }
        }

        return returnList;
    }

    public List<String> getStringValues(String xpath, String attribute) {
        List<String> returnList = new ArrayList<String>();

        List<Element> nodes = this.context.selectNodes(xpath);
        Iterator<Element> i = nodes.iterator();
        while (i.hasNext()) {
            Element elem = i.next();
            // logger.trace("Element: " + elem.getName());
            String value = (String) elem.getAttributeValue(attribute);
            // logger.trace("Digital file value: " + value);
            if (value != null) {
                returnList.add(value);
            }
        }

        return returnList;
    }

    public List<String> getStringValues(Element element, String xpath, Namespace ns) {

        List<Element> referenceElements = element.getChildren(xpath, ns);
        if (referenceElements == null) {
            return null;
        }

        List<String> result = new ArrayList<String>();

        for (Element refElement : referenceElements) {
            String text = (String) refElement.getText();
            result.add(text);
        }

        return result;
    }

    public String[][] getStringValues(Element element, String xpath, String attribute, String attribute2, Namespace ns) {

        List<Element> referenceElements = element.getChildren(xpath, ns);
        if (referenceElements == null) {
            return null;
        }

        String[][] returnArray = new String[referenceElements.size()][2];

        int i = 0;

        for (Element refElement : referenceElements) {
            String name = (String) refElement.getAttributeValue(attribute);
            String value = (String) refElement.getAttributeValue(attribute2);
            returnArray[i][0] = name;
            returnArray[i][1] = value;
            i++;
        }

        return returnArray;
    }

    public Element getElement(String xpath) {
        return (Element) this.context.selectSingleNode(xpath);
    }

    public List<Element> getElements(String xpath) {
        List<Element> nodes = this.context.selectNodes(xpath);
        List<Element> elements = new ArrayList<Element>();

        Iterator<Element> i = nodes.iterator();
        while (i.hasNext()) {
            Element elem = i.next();
            if (elem != null) {
                elements.add(elem);
            }
        }

        return nodes;
    }

    private Map<String, String> getChildLanguageStrings(Element element, String xpath, Namespace ns) {
        return getChildLanguageStrings(element, xpath, ns, false);
    }

    private Map<String, String> getSimpleChildLanguageStrings(Element element, String xpath, Namespace ns) {
        return getChildLanguageStrings(element, xpath, ns, true);
    }

    private Map<String, String> getChildLanguageStrings(Element element, String xpath, Namespace ns, boolean simple) {
        Map<String, String> languageStrings = new HashMap<String, String>();

        if (element == null) {
            return null;
        }
        Iterator<Element> i = null;
        if (ns == null) {
            if (element.getChildren(xpath) == null) {
                return null;
            }
            i = element.getChildren(xpath).iterator();
        } else {
            if (element.getChildren(xpath, ns) == null) {
                return null;
            }
            i = element.getChildren(xpath, ns).iterator();
        }
        while (i.hasNext()) {
            Element childElement = i.next();
            String lang;

            if (!simple)
                lang = ConfigurationService.INSTANCE.getDefaultLanguage(); // set default language
            else
                lang = "en";
            if (childElement.getAttributeValue("lang", this.xmlNamespace) != null) {
                lang = childElement.getAttributeValue("lang", this.xmlNamespace); // OK,
            }
            // set
            // for
            // this
            // language
            String stringValue = childElement.getText();
            languageStrings.put(lang, stringValue);
        }

        return languageStrings;
    }

    private Map<String, String> getChildLanguageStrings(String xpath) {
        Map<String, String> languageStrings = new HashMap<String, String>();

        List<Element> nodeList = this.context.selectNodes(xpath);
        for (Object object : nodeList) {
            Element element = (Element) object;
            String lang = ConfigurationService.INSTANCE.getDefaultLanguage(); // set default value
            if (element.getAttributeValue("lang", this.xmlNamespace) != null) {
                lang = element.getAttributeValue("lang", this.xmlNamespace); // OK,
            }
            // language
            // explicitly
            // stated
            String stringValue = element.getText();
            languageStrings.put(lang, stringValue);
        }

        return languageStrings;
    }

    public boolean getBooleanValue(String xpath) {

        String value = this.getStringValue(xpath);

        if (value == null) {
            return false;
        }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equals("1")) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Parses a Date from 'yyyy-mm-dd' string entry.
     *
     * @param xpath
     * @return
     */
    public Date getSimpleDate(String xpath) {
        String stringValue = (String) this.context.getValue(xpath);
        Date simpleDate = null;

        if (!StringUtils.isBlank(stringValue)) {
            try {
                LocalDate date = LocalDate.parse(stringValue);
                simpleDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException | NullPointerException | IllegalArgumentException e) {
                logger.error(e.getMessage());
            }
        }
        return simpleDate;
    }

    // private MultiLanguageString createLanguageStringFromMap( Map<String, String> languageStrings ) {
    //
    // MultiLanguageString langString = new MultiLanguageString();
    //
    // if ( languageStrings == null )
    // return null;
    // for ( String key : languageStrings.keySet() ) {
    // if ( key.equals( "en" ) )
    // langString.setDefaultValue( languageStrings.get( key ) );
    // else
    // langString.addLString( key, languageStrings.get( key ) );
    // }
    // return langString;
    // }
    //
    // private MultiLanguageText createLanguageTextFromMap( Map<String, String> languageStrings ) {
    //
    // MultiLanguageText langText = new MultiLanguageText();
    //
    // for ( String key : languageStrings.keySet() ) {
    // if ( key.equals( "en" ) )
    // langText.setDefaultValue( languageStrings.get( key ) );
    // else
    // langText.addLString( key, languageStrings.get( key ) );
    // }
    // return langText;
    // }
}
