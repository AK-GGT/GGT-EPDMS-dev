package de.iai.ilcd.webgui.util;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.registry.Registry;
import de.iai.ilcd.rest.RegistryService;
import eu.europa.ec.jrc.lca.commons.view.util.SelectItemsProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Scope("request")
public class RegistriesSelectItems extends SelectItemsProducer<Registry> {

    @Autowired
    private RegistryService registryService;

    @PostConstruct
    public void init() {
        setEntities(registryService.getNonVirtualRegistriesInWhichRegistered());
        addNotSelectedItem();
        if (ConfigurationService.INSTANCE.isGladEnabled()) {
            addGLADSelectedItem();
        }
    }

    @Override
    public Object getValue(Registry entity) {
        return entity.getUuid();
    }

    @Override
    public String getLabel(Registry entity) {
        return entity.getName();
    }

}
