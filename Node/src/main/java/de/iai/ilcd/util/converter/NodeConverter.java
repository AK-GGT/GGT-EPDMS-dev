package de.iai.ilcd.util.converter;

import de.iai.ilcd.model.dao.NetworkNodeDao;
import de.iai.ilcd.model.nodes.NetworkNode;
import eu.europa.ec.jrc.lca.registry.domain.Node;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * This is the class for converting {@link Node} objects into printable Strings.
 *
 * @author sarai
 */
@FacesConverter(value = "nodeConverter")
public class NodeConverter implements Converter {


    /**
     * Gets the NetworkNode ID String.
     *
     * @param obj The NetworkNode
     * @return The ID of given NetowrkNode as String
     */
    protected String getId(NetworkNode obj) {
        return obj.getNodeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAsObject(FacesContext contect, UIComponent comp, String objStr) {
        try {
            NetworkNodeDao dao = new NetworkNodeDao();
            return dao.getNetworkNode(objStr);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsString(FacesContext contect, UIComponent comp, Object obj) {
        if (NetworkNode.class.isInstance(obj)) {
            return this.getId((NetworkNode) obj);
        }
        return "";
    }

}
