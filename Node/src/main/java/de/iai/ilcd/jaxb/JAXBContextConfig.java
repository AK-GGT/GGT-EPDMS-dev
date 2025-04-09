package de.iai.ilcd.jaxb;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

@Configuration
public class JAXBContextConfig {

    @Bean
    @Primary
    @Scope("singleton")
    public JAXBContext jaxbContext() {
        try {
            return JAXBContext.newInstance(
                    String.join(":", "edu.kit.iai.epd._2013",
                            "generated",
                            "network.indata.epd._2019",
                            "org.matml",
                            "it.jrc.lca.ilcd.categories",
                            "it.jrc.lca.ilcd.common",
                            "it.jrc.lca.ilcd.contact",
                            "it.jrc.lca.ilcd.flow",
                            "it.jrc.lca.ilcd.flowproperty",
                            "it.jrc.lca.ilcd.lciamethod",
                            "it.jrc.lca.ilcd.locations",
                            "it.jrc.lca.ilcd.process",
                            "it.jrc.lca.ilcd.source",
                            "it.jrc.lca.ilcd.unitgroup",
                            "it.jrc.lca.ilcd.wrapper"
                    ));

        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }

    }
}
