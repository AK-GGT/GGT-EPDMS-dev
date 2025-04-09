package de.iai.ilcd.util.lstring;

import de.fzk.iai.ilcd.service.model.common.ILString;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.configuration.ConfigurationService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for API backwards compatibility for MultiLangString fields that
 * are now realized with a Map of strings directly.
 */
public class MultiLangStringMapAdapter implements IMultiLangString {

    /**
     * The provider for the map with the actual data
     */
    private final IStringMapProvider provider;

    /**
     * Adapter for list
     */
    private LStringListAdapter listAdapter = null;

    private String defaultLanguage = ConfigurationService.INSTANCE.getDefaultLanguage();

    /**
     * Create adapter instance
     *
     * @param provider data provider
     */
    public MultiLangStringMapAdapter(IStringMapProvider provider) {
        super();
        this.provider = provider;
    }

    public MultiLangStringMapAdapter(String value) {
        this(MultiLangStringMapAdapter.createProvider(value));
    }

    public MultiLangStringMapAdapter(Map<String, String> map) {
        this(MultiLangStringMapAdapter.createProvider(map));
    }

    public MultiLangStringMapAdapter(String value, String lang) {
        this(MultiLangStringMapAdapter.createProvider(value, lang));
    }

    private static IStringMapProvider createProvider(String value, String lang) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(lang, value);
        return new IStringMapProvider() {

            @Override
            public Map<String, String> getMap() {
                return map;
            }
        };
    }

    private static IStringMapProvider createProvider(String value) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(ConfigurationService.INSTANCE.getDefaultLanguage(), value);
        return new IStringMapProvider() {

            @Override
            public Map<String, String> getMap() {
                return map;
            }
        };
    }

    private static IStringMapProvider createProvider(final Map<String, String> map) {
        return new IStringMapProvider() {

            @Override
            public Map<String, String> getMap() {
                return map;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ILString> getLStrings() {
        if (this.listAdapter == null) {
            this.listAdapter = new LStringListAdapter(this.provider);
        }
        return this.listAdapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue() {
        String defaultValue = this.getValue(this.defaultLanguage);
        if (defaultValue != null) {
            return defaultValue;
        } else if (MapUtils.isEmpty(this.provider.getMap())) {
            return null;
        } else {
            // return whatever is there in the priority of the preferredlanguages application property
            for (String lang : ConfigurationService.INSTANCE.getPreferredLanguages()) {
                if (this.provider.getMap().containsKey(lang))
                    return this.provider.getMap().get(lang);
            }
            return this.provider.getMap().values().toArray(new String[0])[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(String value) {
        this.setValue(this.defaultLanguage, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue(String key) {
        if (MapUtils.isNotEmpty(this.provider.getMap())) {
            return this.provider.getMap().get(key);
        }
        return null;
    }

    /**
     * Get value for provided language and {@link #getValue() default value} as fall back if not available.
     *
     * @param key preferred language
     * @return value
     */
    public String getValueWithFallback(String key) {
        final String val = this.getValue(key);
        if (StringUtils.isNotBlank(val)) {
            return val;
        }
        return this.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(String key, String value) {
        if (this.provider.getMap() != null) {
            this.provider.getMap().put(key, value);
        }
    }

    /**
     * Override all values in the map with those of the provided multi language string
     *
     * @param mls multi language string to use
     */
    public void overrideValues(IMultiLangString mls) {
        final Map<String, String> map = this.provider.getMap();
        if (map != null) {
            map.clear();
            if (mls != null) {
                for (ILString lstr : mls.getLStrings()) {
                    map.put(lstr.getLang(), lstr.getValue());
                }
            }
        }
    }

    @Override
    public String getDefaultValue() {
        return getValue();
    }

}
