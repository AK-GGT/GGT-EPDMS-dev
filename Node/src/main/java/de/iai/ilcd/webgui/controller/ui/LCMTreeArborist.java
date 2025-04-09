package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;

import java.io.Serializable;
import java.util.Objects;

/**
 * Dedicated class for constructing nested tables in PrimeFaces
 * <p>
 * Specifically tailored for LifeCycleModel
 *
 * @see LifeCycleModel
 * @see LifeCycleModelHandler
 */

public class LCMTreeArborist implements Serializable {

    private static final long serialVersionUID = 6892452608709604676L;

    public String title, flowUUID, id, location, parameters;
    public Object obj;

    public LCMTreeArborist(String title, String flowUUID, String id, String location, String parameters, Object obj) {
        this.title = title;
        this.flowUUID = flowUUID;
        this.id = id;
        this.location = location;
        this.parameters = parameters;
        this.obj = obj;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFlowUUID() {
        return flowUUID;
    }

    public void setFlowUUID(String flowUUID) {
        this.flowUUID = flowUUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowUUID, id, location);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LCMTreeArborist other = (LCMTreeArborist) obj;
        return Objects.equals(flowUUID, other.flowUUID) && Objects.equals(id, other.id)
                && Objects.equals(location, other.location);
    }

    @Override
    public String toString() {
        return "TreeArborist [flowUUID=" + flowUUID + ", id=" + id + ", location=" + location + "]";
    }

}