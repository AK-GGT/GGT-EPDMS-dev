package de.iai.ilcd.rest.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import javax.xml.namespace.QName;
import java.io.IOException;

/**
 * Jackson some times render elements as QName.
 * This custom serializer removes the namespaceURI from the value field of name;
 *
 * @author MK
 * @see JAXBElementMixIn
 * @since soda4LCA 6.8.1
 */
public class QNameSerializer extends JsonSerializer<QName> {

    @Override
    public void serialize(QName value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getLocalPart());
    }

}
