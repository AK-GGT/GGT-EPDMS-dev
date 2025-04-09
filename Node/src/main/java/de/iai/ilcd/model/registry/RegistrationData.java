package de.iai.ilcd.model.registry;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "registration_data")
public class RegistrationData implements Serializable {

    private static final long serialVersionUID = 2168582558731195218L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nodeId;

    @Column(name = "NODE_BASEURL")
    private String nodeBaseUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeBaseUrl() {
        return nodeBaseUrl;
    }

    public void setNodeBaseUrl(String nodeBaseUrl) {
        this.nodeBaseUrl = nodeBaseUrl;
    }
}
