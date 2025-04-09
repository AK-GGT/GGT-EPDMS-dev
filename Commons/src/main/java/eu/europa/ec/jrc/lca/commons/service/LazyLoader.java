package eu.europa.ec.jrc.lca.commons.service;

import eu.europa.ec.jrc.lca.commons.dao.SearchParameters;

import java.util.List;

public interface LazyLoader<T> {
    List<T> loadLazy(SearchParameters sp);

    Long count(SearchParameters sp);
}
