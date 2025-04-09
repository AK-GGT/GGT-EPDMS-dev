package de.iai.ilcd.model.common;

import de.fzk.iai.ilcd.service.model.common.ILString;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.configuration.ConfigurationService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author clemens.duepmeier
 * @deprecated
 */
// @Embeddable
public class MultiLanguageString implements Serializable, IMultiLangString {

    /**
     *
     */
    private static final long serialVersionUID = -1317700324445317497L;

    private String defaultValue = "";

    // @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    // @MapKey( name = "lang" )
    private Map<String, LString> lStringMap = new HashMap<String, LString>();

    public MultiLanguageString() {

    }

    public MultiLanguageString(MultiLanguageText text) {
        this.defaultValue = text.getDefaultValue();
        for (String key : text.getLStringMap().keySet()) {
            LText lText = text.getLStringMap().get(key);
            lStringMap.put(key, new LString(key, lText.getValue()));
        }
    }

    public MultiLanguageString(IMultiLangString other) {
        // this.defaultValue=other.getValue();
        for (ILString lString : other.getLStrings()) {
            if (lString.getLang() != null && lString.getLang().equals(ConfigurationService.INSTANCE.getDefaultLanguage()))
                this.defaultValue = lString.getValue();
            else
                lStringMap.put(lString.getLang(), new LString(lString.getLang(), lString.getValue()));
        }
    }

    public MultiLanguageString(String value) {
        if (value.length() <= 255)
            this.defaultValue = value;
        else
            this.defaultValue = value.substring(0, 254);
    }

    public String getDefaultValue() {
        if (defaultValue != null && !defaultValue.equals(""))
            return defaultValue;
        if (lStringMap.isEmpty())
            return null;
        String textValue = null;
        for (LString lValue : lStringMap.values()) {
            textValue = lValue.getValue();
            break;
        }
        return textValue;
    }

    public void setDefaultValue(String value) {
        if (value.length() <= 255)
            this.defaultValue = value;
        else
            this.defaultValue = value.substring(0, 254);
    }

    public Map<String, LString> getLStringMap() {
        return lStringMap;
    }

    protected void setLStringMap(Map<String, LString> multiLangValues) {
        this.lStringMap = multiLangValues;
    }

    public void addLString(String language, String value) {
        if (language != null && value != null)
            lStringMap.put(language, new LString(language, value));

    }

    @Override
    public List<ILString> getLStrings() {
        List<ILString> lStrings = new ArrayList<ILString>();
        for (ILString lValue : lStringMap.values()) {
            lStrings.add(lValue);
        }
        return lStrings;
    }

    @Override
    public String getValue() {
        return this.getDefaultValue();
    }

    @Override
    public void setValue(String value) {
        this.setDefaultValue(value);
    }

    @Override
    public String getValue(String lang) {
        if (lStringMap == null || lang == null)
            return null;
        LString lString = lStringMap.get(lang);
        if (lString == null)
            return null;
        return lString.getValue();
    }

    @Override
    public void setValue(String lang, String value) {
        lStringMap.put(lang, new LString(lang, value));
    }

    public String getValueWithFallback(String lang) {
        //get requested language
        String result = getValue(lang);
        if (result != null)
            return result;

        // get whatever is there in the priority of the preferredlanguages application property
        for (String confLang : ConfigurationService.INSTANCE.getPreferredLanguages()) {
            if (this.lStringMap.containsKey(confLang))
                return this.lStringMap.get(confLang).getValue();
        }

        // get default language
        return getValue();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MultiLanguageString other = (MultiLanguageString) obj;
        if ((this.getDefaultValue() == null) ? (other.getDefaultValue() != null) : !this.getDefaultValue().equals(other.getDefaultValue())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.getDefaultValue() != null ? this.getDefaultValue().hashCode() : 0);
        return hash;
    }

}
