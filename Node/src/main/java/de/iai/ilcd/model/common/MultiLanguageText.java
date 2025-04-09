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
public class MultiLanguageText implements Serializable, IMultiLangString {

    /**
     *
     */
    private static final long serialVersionUID = -4223951519476771333L;

    // @Column( columnDefinition = "text", length = 10000 )
    private String defaultValue = "";

    // @OneToMany( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    // @MapKey( name = "lang" )
    private Map<String, LText> lStringMap = new HashMap<String, LText>();

    public MultiLanguageText() {

    }

    public MultiLanguageText(String value) {
        this.defaultValue = value;
    }

    public MultiLanguageText(IMultiLangString other) {
        // this.defaultValue=multiLangString.;
        for (ILString lString : other.getLStrings()) {
            if (lString.getValue() != null && lString.getValue().equals(ConfigurationService.INSTANCE.getDefaultLanguage()))
                this.defaultValue = lString.getValue();
            else {
                if (lString.getLang() != null && lString.getValue() != null)
                    lStringMap.put(lString.getLang(), new LText(lString.getLang(), lString.getValue()));
            }
        }
    }

    public String getDefaultValue() {
        if (defaultValue != null && !defaultValue.equals(""))
            return defaultValue;
        if (lStringMap.isEmpty())
            return null;
        String textValue = null;
        for (LText lValue : lStringMap.values()) {
            textValue = lValue.getValue();
            break;
        }
        return textValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Map<String, LText> getLStringMap() {
        return lStringMap;
    }

    protected void setLStringMap(Map<String, LText> multiLangValues) {
        this.lStringMap = multiLangValues;
    }

    public void addLString(String language, String value) {
        if (language != null && value != null)
            lStringMap.put(language, new LText(language, value));

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
        return lStringMap.get(lang).getValue();
    }

    @Override
    public void setValue(String lang, String value) {
        lStringMap.put(lang, new LText(lang, value));
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
        final MultiLanguageText other = (MultiLanguageText) obj;
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
