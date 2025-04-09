package de.iai.ilcd.util;

import javax.servlet.*;
import java.io.IOException;

/**
 * Fixes Problem with PF 3.x Character Encoding
 */
public class CharsetFilter implements Filter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        chain.doFilter(req, resp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig conf) throws ServletException {
    }

}
