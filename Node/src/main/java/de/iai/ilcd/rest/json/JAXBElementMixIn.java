package de.iai.ilcd.rest.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.namespace.QName;

/**
 * Jackson doesn't play nice with XMLEnums and XMLGregorianCalendar.
 * They are rendered as raw JAXBElement.
 * <p>
 * This mixin ignore the field we don't want from them.
 * <p>
 * https://stackoverflow.com/a/64923605/
 *
 * @param <T>
 * @author MK
 * @see QNameSerializer
 * @since soda4LCA 6.8.1
 */
@JsonIgnoreProperties(value = {"globalScope", "typeSubstituted", "nil", "declaredType", "scope"})
public abstract class JAXBElementMixIn<T> {

    @JsonCreator
    public JAXBElementMixIn(
            @JsonProperty("name") QName name,
//            @JsonProperty("declaredType") Class<T> declaredType,
//            @JsonProperty("scope") Class scope,
            @JsonProperty("value") T value) {
    }
}