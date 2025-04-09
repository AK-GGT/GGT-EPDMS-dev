package de.iai.ilcd.webgui;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.webgui.controller.ConfigurationBean;
import org.apache.commons.configuration.Configuration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author clemens.duepmeier
 */
public class Welcome extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = -1742067961355131346L;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Configuration conf = ConfigurationService.INSTANCE.getProperties();
        String jumpPage = conf.getString("welcomePage");

        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/index.xhtml");
        if (jumpPage != null) {
            ConfigurationBean confBean = new ConfigurationBean(request);
            // if the jump page is external, redirect instead
            if (jumpPage.toLowerCase().startsWith("http://") || jumpPage.toLowerCase().startsWith("https://")) {
                response.sendRedirect(jumpPage);
                return;
            } else {
                String jumpPagePath = confBean.getTemplatePath() + "/" + jumpPage;
                dispatcher = this.getServletContext().getRequestDispatcher(jumpPagePath);
            }
        }

        // OK, now forward to the welcome page: either "index.xhtml" or the jump page
        dispatcher.forward(request, response);
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
