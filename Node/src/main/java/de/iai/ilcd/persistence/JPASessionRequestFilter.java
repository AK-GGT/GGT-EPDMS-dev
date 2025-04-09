/*
 * HibernateSessionRequestFilter.java
 * Created on 15. Mai 2006, 10:18
 */

package de.iai.ilcd.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author clemens.duepmeier
 */

public class JPASessionRequestFilter implements javax.servlet.Filter {

    final static Logger log = LogManager.getLogger(JPASessionRequestFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {
            // sf.getCurrentSession().beginTransaction();
            // we don't need beginTransaction, on every request. Let the dao objects handle this

            // Call the next filter (continue request processing)
            chain.doFilter(request, response);

            // Commit and cleanup
            log.trace("Committing the database transaction");

        } catch (Throwable ex) {
            // Rollback only
            // log.error("Could not commit transaction",ex);
            try {
            } catch (Throwable rbEx) {
                log.error("Could not rollback transaction after exception!", rbEx);
            }

            // Let others handle it... maybe another interceptor for exceptions?
            throw new ServletException(ex);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // log.debug("Initializing filter...");
    }

    @Override
    public void destroy() {
    }

}
