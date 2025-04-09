package de.iai.ilcd.model.common;

import eu.europa.ec.jrc.lca.commons.domain.ILongIdObject;

import javax.persistence.*;

/**
 * The target for pushing a data stock. This manages the data table entry containing name, target node and target data stock.
 *
 * @author sarai
 */
@Entity
@Table(name = "pushtarget")
public class PushTarget implements ILongIdObject {

    /*
     * The unique id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * The target node nem
     */
    @Basic
    @Column(name = "network_node_name")
    private String targetName;

    /*
     * The target node ID
     */
    @Basic
    @Column(name = "network_node_id")
    private String targetID;

    /*
     * The URL of target node
     */
    @Basic
    @Column(name = "network_node_url")
    private String targetURL;

    /*
     * The target data stock name in target node
     */
    @Basic
    @Column(name = "target_ds_name")
    private String targetDsName;

    /*
     * The UUID of target stock
     */
    @Basic
    @Column(name = "target_ds_uuid")
    private String targetDsUuid;

    /*
     * The name of push target entry
     */
    @Column(unique = true)
    private String name;

    /*
     * The login name of target node
     */
    private String login;

    /**
     * Gates name of push target entry.
     *
     * @return Name of push target entry
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of push target entry.
     *
     * @param name The name of push target entry
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the Id of push target entry.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the Id of push target entry.
     *
     * @param id Id of push target entry
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets name of target node.
     *
     * @return The name of target node
     */
    public String getTargetName() {
        return this.targetName;
    }

    /**
     * Sets name of target node with given name.
     *
     * @param targetName The name the target node shall have
     */
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    /**
     * Gets ID of target node.
     *
     * @return The ID of target node
     */
    public String getTargetID() {
        return this.targetID;
    }

    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }

    /**
     * Gets the target node of push target entry.
     *
     * @return The target node of push target entry
     */
    public String getTargetURL() {
        return this.targetURL;
    }

    /**
     * Sets the node of push target entry.
     *
     * @param node The node of push target entry
     */
    public void setTargetURL(String targetURL) {
        this.targetURL = targetURL;
    }

    /**
     * Sets Name of target data stock.
     *
     * @return Name of target data stock
     */
    public String getTargetDsName() {
        return this.targetDsName;
    }

    /**
     * Sets the name of target stock
     *
     * @param targetDsName Name of target stock
     */
    public void setTargetDsName(String targetDsName) {
        this.targetDsName = targetDsName;
    }

    /**
     * Gets UUID of target stock.
     *
     * @return UUID of target stock
     */
    public String getTargetDsUuid() {
        return this.targetDsUuid;
    }

    /**
     * Sets UUID of target stock.
     *
     * @param targetDsUuid UUID of target stock
     */
    public void setTargetDsUuid(String targetDsUuid) {
        this.targetDsUuid = targetDsUuid;
    }

    /**
     * Gets login name of target node.
     *
     * @return login name of target node
     */
    public String getLogin() {
        return this.login;
    }

    /**
     * Sets login name of target node.
     *
     * @param login Login name of target node
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PushTarget)) {
            return false;
        }
        PushTarget other = (PushTarget) obj;
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
        return true;
    }

}
