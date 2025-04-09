package de.iai.ilcd.model.dao;

import eu.europa.ec.jrc.lca.commons.dao.NonceDao;
import eu.europa.ec.jrc.lca.commons.domain.Nonce;
import eu.europa.ec.jrc.lca.commons.domain.Nonce_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.Date;

@Repository(value = "nonceDao")
public class NonceDaoImpl extends GenericDAOImpl<Nonce, Long> implements NonceDao {

    private EntityManagerFactory emf;

    @Autowired
    public NonceDaoImpl(@Qualifier("soda4lcaEntityManagerFactory") EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public boolean exists(String nonceValue) {
        EntityManager em = emf.createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Nonce> c = cb.createQuery(Nonce.class);
        Root<Nonce> nonceR = c.from(Nonce.class);
        Path<byte[]> value = nonceR.get(Nonce_.value);
        c.where(cb.and(cb.equal(value, nonceValue.getBytes())));

        TypedQuery<Nonce> q = em.createQuery(c);
        return q.getResultList().size() != 0;
    }

    @Override
    public void save(String nonceValue) {
        Nonce n = new Nonce();
        n.setUseDate(new Date());
        n.setValue(nonceValue.getBytes());
        saveOrUpdate(n);
    }

    @Override
    public void clearNonces(Date date) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.createNamedQuery("removeOldNonces").setParameter("clearDate", date).executeUpdate();
        tx.commit();
    }
}
