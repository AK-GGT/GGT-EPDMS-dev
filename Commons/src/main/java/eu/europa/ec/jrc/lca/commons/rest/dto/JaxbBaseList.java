package eu.europa.ec.jrc.lca.commons.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "list")
public class JaxbBaseList<T> {

    @XmlElement(name = "item")
    protected List<T> list;

    public JaxbBaseList() {
    }

    public JaxbBaseList(List<T> list) {
        this.list = list;
    }

    public List<T> getList() {
        return list;
    }
}
