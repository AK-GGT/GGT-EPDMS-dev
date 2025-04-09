package de.iai.ilcd.util;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.exception.DatasetNotInSelectedStockException;
import de.iai.ilcd.model.common.exception.InvalidDatasetException;
import de.iai.ilcd.webgui.controller.admin.RandomPassword;
import de.iai.ilcd.webgui.controller.ui.StockSelectionHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.web.util.SavedRequest;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ProjectStage;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 * Handler for faces exceptions
 */
public class FacesExceptionHandler extends ExceptionHandlerWrapper {

    private static final Logger LOGGER = LogManager.getLogger(FacesExceptionHandler.class);

    /**
     * Wrapped exception
     */
    private final ExceptionHandler wrapped;

    /**
     * Create the exception
     *
     * @param exception wrapped exception
     */
    FacesExceptionHandler(ExceptionHandler exception) {
        this.wrapped = exception;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExceptionHandler getWrapped() {
        return this.wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle() throws FacesException {
        FacesContext fc = FacesContext.getCurrentInstance();
        for (Iterator<ExceptionQueuedEvent> i = this.getUnhandledExceptionQueuedEvents().iterator(); i.hasNext(); ) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable t = context.getException();
            if (fc.isProjectStage(ProjectStage.Development)) {
                t.printStackTrace();
            }
            do {
                if (t instanceof AuthorizationException) {
                    this.handleAuthorizationException(fc, (AuthorizationException) t, i);
                    return;
                }
                if (t instanceof ViewExpiredException) {
                    this.handleViewExpiredException(fc);
                    return;
                }
                if (t instanceof InvalidDatasetException) {
                    this.handleInvalidDatasetException(fc, (InvalidDatasetException) t, i);
                    return;
                }
                if (t instanceof DatasetNotInSelectedStockException) {
                    this.handleInvalidDatasetException(fc, (DatasetNotInSelectedStockException) t, i);
                    return;
                }
                t = t.getCause();
            } while (t != null);
            this.handleGenericException(fc);
        }
    }

    /**
     * Handle all exceptions with no special treatment (no message)
     *
     * @param fc current faces context
     */
    private void handleGenericException(FacesContext fc) {
        String msg = "";
        for (ExceptionQueuedEvent event : FacesContext.getCurrentInstance().getExceptionHandler().getUnhandledExceptionQueuedEvents()) {
            Throwable unhandledException = event.getContext().getException();
            LOGGER.error("Exception thrown in front-end!", unhandledException);

            // Let's also display the stacktrace in the UI, if in developerMode
            if (ConfigurationService.INSTANCE.isDeveloperMode()) {
                StringWriter sw = new StringWriter();
                unhandledException.printStackTrace(new PrintWriter(sw));
                msg += sw + System.getProperty("line.separator") + System.getProperty("line.separator");
            }
        }

        msg = msg.trim().isEmpty() ? null : msg; // semantics..
        this.handleExceptionMsgPage(fc, null, "/error.xhtml", msg);

    }

    /**
     * Handle a {@link ViewExpiredException}
     *
     * @param fc current faces context
     */
    private void handleViewExpiredException(FacesContext fc) {
        Object obj = fc.getExternalContext().getRequest();
        if (obj instanceof SavedRequest) {
            SavedRequest savedRequest = (SavedRequest) obj;
            try {
                fc.getExternalContext().redirect(savedRequest.getRequestUrl());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Handle a {@link InvalidDatasetException}
     *
     * @param fc current faces context
     * @param t  exception to get message from
     * @param i  iterator to remove exception from
     */
    private void handleInvalidDatasetException(FacesContext fc, InvalidDatasetException t, Iterator<ExceptionQueuedEvent> i) {
        this.handleExceptionMsgPage(fc, i, "/error.xhtml", t.getMessage());

    }

    /**
     * Handle a {@link DatasetNotInSelectedStockException}
     *
     * @param fc current faces context
     * @param t  exception to get message from
     * @param i  iterator to remove exception from
     */
    private void handleInvalidDatasetException(FacesContext fc, DatasetNotInSelectedStockException t, Iterator<ExceptionQueuedEvent> i) {
        this.handleExceptionMsgPage(fc, i, "/error.xhtml", t.getMessage());

    }

    /**
     * Handle an {@link AuthorizationException}
     *
     * @param fc current faces context
     * @param t  exception to get message from
     * @param i  iterator to remove exception from
     */
    private void handleAuthorizationException(FacesContext fc, AuthorizationException t, Iterator<ExceptionQueuedEvent> i) {
        this.handleExceptionMsgPage(fc, i, "/roleerror.xhtml", t.getMessage());
    }

    /**
     * Handle an exception with just redirects to an error page with a message
     *
     * @param i       iterator to remove from on success
     * @param facelet JSF view to redirect to
     * @param msg     message to display
     */
    private void handleExceptionMsgPage(FacesContext fc, Iterator<ExceptionQueuedEvent> i, String facelet, String msg) {

        NavigationHandler nav = fc.getApplication().getNavigationHandler();
        try {
            final String msgKey = RandomPassword.getPassword(10);
            Object sessionObj = fc.getExternalContext().getSession(true);
            if (sessionObj instanceof HttpSession) {
                ((HttpSession) sessionObj).setAttribute(msgKey, msg);
            }

            FacesContext context = FacesContext.getCurrentInstance();
            StockSelectionHandler stockSelection = context.getApplication().evaluateExpressionGet(context, "#{stockSelection}", StockSelectionHandler.class);
            String stockParam = "";
            if (StringUtils.isNotBlank(stockSelection.getCurrentStockName())) {
                stockParam = "&stock=" + URLEncoder.encode(stockSelection.getCurrentStockName(), "UTF-8");
            }

            nav.handleNavigation(fc, null, facelet + "?msgKey=" + msgKey + stockParam + "&faces-redirect=true");
            fc.renderResponse();
        } catch (Exception eee) {
        } finally {
            if (i != null) {
                i.remove();
            }
        }
    }

}
