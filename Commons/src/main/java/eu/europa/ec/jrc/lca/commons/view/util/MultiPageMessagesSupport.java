package eu.europa.ec.jrc.lca.commons.view.util;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MultiPageMessagesSupport implements PhaseListener {

    private static final long serialVersionUID = 4286585609182075813L;

    private static final String sessionToken = "MULTI_PAGE_MESSAGES_SUPPORT";

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    public void beforePhase(final PhaseEvent event) {
        FacesContext facesContext = event.getFacesContext();
        this.saveMessages(facesContext);

        if (PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
            if (!facesContext.getResponseComplete()) {
                this.restoreMessages(facesContext);
            }
        }
    }

    public void afterPhase(final PhaseEvent event) {
        if (!PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
            FacesContext facesContext = event.getFacesContext();
            this.saveMessages(facesContext);
        }
    }

    @SuppressWarnings("unchecked")
    private int saveMessages(final FacesContext facesContext) {
        List<FacesMessage> messages = new ArrayList<FacesMessage>();
        for (Iterator<FacesMessage> iter = facesContext.getMessages(null); iter.hasNext(); ) {
            messages.add(iter.next());
            iter.remove();
        }

        if (messages.size() == 0) {
            return 0;
        }

        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
        List<FacesMessage> existingMessages = (List<FacesMessage>) sessionMap.get(sessionToken);
        if (existingMessages != null) {
            existingMessages.addAll(messages);
        } else {
            sessionMap.put(sessionToken, messages);
        }
        return messages.size();
    }

    @SuppressWarnings("unchecked")
    private int restoreMessages(final FacesContext facesContext) {
        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
        List<FacesMessage> messages = (List<FacesMessage>) sessionMap.remove(sessionToken);

        if (messages == null) {
            return 0;
        }

        int restoredCount = messages.size();
        for (Object element : messages) {
            facesContext.addMessage(null, (FacesMessage) element);
        }
        return restoredCount;
    }
}