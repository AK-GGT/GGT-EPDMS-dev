package de.iai.ilcd.model.dao;

import de.iai.ilcd.model.registry.Registry;

import java.util.List;

public interface RegistryDao extends GenericDAO<Registry, Long> {

    Registry findByIdentity(String identity);

    Registry findByUUID(String uuid);

    Registry findByUrl(String url);

    List<Registry> getRegistriesOrderedByName();

    List<Registry> getNonVirtualRegistriesInWhichRegistered();

    List<Registry> getRegistriesInWhichRegistered();

    List<Registry> getRegistriesToCheckAcceptance();
}
