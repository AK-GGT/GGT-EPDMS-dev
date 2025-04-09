package eu.europa.ec.jrc.lca.registry.view.filters;

import eu.europa.ec.jrc.lca.registry.view.util.Consts;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component(value = "accessFilter")
public class AccessFilter implements Filter {
    private String noAccessPage;

    private List<String> excludesPatternsToken = new ArrayList<>();

    private final PathMatcher pm = new AntPathMatcher();

    public void destroy() {

    }

    public void init(FilterConfig config) {
        this.noAccessPage = config.getInitParameter("noAccessPage");
        String excludesPatterns = config.getInitParameter("excludesPatterns");

        if (excludesPatterns != null && !excludesPatterns.trim().equals("")) {
            excludesPatternsToken = Arrays.asList(excludesPatterns.split(","));
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String path = httpReq.getServletPath();

        // if this path is excluded, do not apply this filter and continue
        // processing the filter chain
        if (matchExcludedPaths(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = ((HttpServletRequest) request).getSession(true);

        if (session.getAttribute(Consts.AUTHENTICATED_USER) == null) {
            httpRes.sendRedirect(httpReq.getContextPath() + noAccessPage);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean matchExcludedPaths(String url) {
        for (String token : excludesPatternsToken) {
            if (pm.match(token.trim(), url)) {
                return true;
            }
        }
        return false;
    }
}
