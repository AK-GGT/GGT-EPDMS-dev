package de.iai.ilcd.jaxb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

@Configuration
public class JAXBUnmarshallerConfig {

    private JAXBContext jaxbContext;
//    private PrefixMapper prefixMapper;

    @Autowired
    JAXBUnmarshallerConfig(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
//        this.prefixMapper = prefixMapper;
    }

    @Bean
    @Scope("prototype")
    public Unmarshaller jaxbMarshaller() {
        Unmarshaller m = null;
        try {
            m = jaxbContext.createUnmarshaller();
            // TODO: set xml header? no
            // TODO: set schema location? no

//            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            m.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);


        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }

        return m;
    }
}