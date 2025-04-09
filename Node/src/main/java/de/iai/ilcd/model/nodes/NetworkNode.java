package de.iai.ilcd.model.nodes;

import de.iai.ilcd.model.registry.Registry;
import eu.europa.ec.jrc.lca.commons.domain.ILongIdObject;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "networknode")
public class NetworkNode implements ILongIdObject, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nodeId;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    private String operator;

    @Column(unique = true, nullable = false)
    private String baseUrl;

    private String adminName;

    private String adminEmailAddress;

    private String adminWwwAddress;

    private String adminPhone;

    private String accessAccount;

    private String accessPassword;

    @Column(name = "datastockID")
    private String dataStockID;

    @ManyToOne
    @JoinColumn(name = "REGISTRY_ID")
    private Registry registry;

    public NetworkNode() {
    }

    public NetworkNode(String nodeId, String name) {
        this.nodeId = nodeId;
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdminName() {
        return this.adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAdminEmailAddress() {
        return this.adminEmailAddress;
    }

    public void setAdminEmailAddress(String adminEmailAddress) {
        this.adminEmailAddress = adminEmailAddress;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getAccessAccount() {
        return accessAccount;
    }

    public void setAccessAccount(String accessAccount) {
        this.accessAccount = accessAccount;
    }

    public String getAccessPassword() {
        return accessPassword;
    }

    public void setAccessPassword(String accessPassword) {
        this.accessPassword = accessPassword;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.nodeId == null) ? 0 : this.nodeId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NetworkNode)) {
            return false;
        }
        NetworkNode other = (NetworkNode) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.nodeId == null) {
            if (other.nodeId != null) {
                return false;
            }
        } else if (!this.nodeId.equals(other.nodeId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.nodes.NetworkNode[nodeId=" + this.nodeId + "]";
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the operator
     */
    public String getOperator() {
        return this.operator;
    }

    /**
     * @param operator the operator to set
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * @return the adminWwwAddress
     */
    public String getAdminWwwAddress() {
        return this.adminWwwAddress;
    }

    /**
     * @param adminWwwAddress the adminWwwAddress to set
     */
    public void setAdminWwwAddress(String adminWwwAddress) {
        this.adminWwwAddress = adminWwwAddress;
    }

    /**
     * @return the adminPhone
     */
    public String getAdminPhone() {
        return this.adminPhone;
    }

    /**
     * @param adminPhone the adminPhone to set
     */
    public void setAdminPhone(String adminPhone) {
        this.adminPhone = adminPhone;
    }

    public Registry getRegistry() {
        return this.registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public String getDataStockID() {
        return dataStockID;
    }

    public void setDataStockID(String dataStockID) {
        this.dataStockID = dataStockID;
    }


}
