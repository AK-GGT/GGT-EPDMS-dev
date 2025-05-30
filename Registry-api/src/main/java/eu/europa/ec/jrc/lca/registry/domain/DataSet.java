package eu.europa.ec.jrc.lca.registry.domain;

import eu.europa.ec.jrc.lca.commons.domain.ILongIdObject;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "t_data_set")
@XmlRootElement(name = "dataset")
@XmlAccessorType(XmlAccessType.FIELD)
@NamedQueries({
        @NamedQuery(name = "updateAssociatedDatasets", query = "UPDATE DataSet ds SET ds.status=eu.europa.ec.jrc.lca.registry.domain.DataSetStatus.NEW_ALTERNATIVE WHERE ds.uuid = :uuid AND ds.status=eu.europa.ec.jrc.lca.registry.domain.DataSetStatus.NEW"),
        @NamedQuery(name = "removeByNode", query = "DELETE FROM DataSet ds WHERE ds.node.nodeId = :nodeId")
})
public class DataSet implements ILongIdObject, Serializable {

    private static final long serialVersionUID = -2059501859283836072L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    private String name;

    private byte[] hash;

    @ElementCollection
    @Column(name = "UUID")
    @CollectionTable(name = "t_data_set_compliance_uuids", joinColumns = @JoinColumn(name = "DS_ID"))
    private List<String> complianceUUIDs = new ArrayList<String>();

    private String owner;

    @Column(name = "USER_")
    private String user;

    @Column(name = "USER_EMAIL")
    private String userEmail;

    private String version;

    @ManyToOne
    @JoinColumn(name = "NODE_ID")
    private Node node;

    @Enumerated(EnumType.STRING)
    private DataSetStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = Arrays.copyOf(hash, hash.length);
    }

    public List<String> getComplianceUUIDs() {
        return complianceUUIDs;
    }

    public void setComplianceUUIDs(List<String> complianceUUIDs) {
        this.complianceUUIDs = complianceUUIDs;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public DataSetStatus getStatus() {
        return status;
    }

    public void setStatus(DataSetStatus status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isAccepted() {
        return DataSetStatus.ACCEPTED.equals(status);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
