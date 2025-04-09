package eu.europa.ec.jrc.lca.registry.dao;

import eu.europa.ec.jrc.lca.commons.dao.SearchParameters;

import java.io.Serializable;
import java.util.List;

public interface GenericDAO<T, ID extends Serializable> {

    T findById(ID id);

    List<T> findAll();

    T saveOrUpdate(T entity);

    List<T> find(SearchParameters sp);

    Long count(SearchParameters sp);

    void remove(ID id);

    void remove(T entity);

    void detach(T entity);

    void flush();
}
