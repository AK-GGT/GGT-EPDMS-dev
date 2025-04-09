package de.iai.ilcd.webgui.controller.admin;

import com.okworx.ilcd.validation.util.IUpdateEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

@ManagedBean(name = "statusProgress")
@SessionScoped
public class StatusProgressBarEventListener implements Serializable, IUpdateEventListener {

    /**
     *
     */
    private static final long serialVersionUID = 8279539963388010424L;

    protected final Logger log = LogManager.getLogger(this.getClass());

    private Integer progress;

    private boolean completed = false;

    private String messageSummary = null;

    private String messageDetail = null;

    private boolean success = false;

    public Integer getProgress() {
        log.debug("returning progress value " + progress);
        return progress;
    }

    public void setProgress(Integer progress) {
        log.debug("setting progress to " + progress);
        this.progress = progress;
    }

    public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage((success ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR), this.messageSummary, this.messageDetail));
    }

    public void cancel() {
        progress = 0;
    }

    @Override
    public void updateProgress(double percentFinished) {
        progress = (int) (percentFinished * 100);
        log.debug("progress updated to " + progress + ", " + percentFinished + "%");
    }

    @Override
    public void updateStatus(String statusMessage) {
        // TODO Auto-generated method stub
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getMessageSummary() {
        return messageSummary;
    }

    public void setMessageSummary(String messageSummary) {
        this.messageSummary = messageSummary;
    }

    public String getMessageDetail() {
        return messageDetail;
    }

    public void setMessageDetail(String messageDetail) {
        this.messageDetail = messageDetail;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
