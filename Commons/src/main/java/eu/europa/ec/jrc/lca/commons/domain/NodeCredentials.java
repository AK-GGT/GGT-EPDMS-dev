package eu.europa.ec.jrc.lca.commons.domain;

import eu.europa.ec.jrc.lca.commons.security.Credentials;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;

@Entity
@Table(name = "t_node_credentials")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeCredentials extends Credentials<NodeCredentials> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nodeId;

    private String accessAccount;

    @Column(name = "ACCESSPASSWORD")
    private byte[] encryptedPassword;

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

    public String getAccessAccount() {
        return accessAccount;
    }

    public void setAccessAccount(String accessAccount) {
        this.accessAccount = accessAccount;
    }

    @Override
    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }

    @Override
    public void setEncryptedPassword(byte[] encrypted) {
        this.encryptedPassword = Arrays.copyOf(encrypted, encrypted.length);

    }

    @Override
    public NodeCredentials getCopy() {
        NodeCredentials copy = new NodeCredentials();
        copy.setAccessAccount(this.accessAccount);
        copy.setAccessPassword(getAccessPassword());
        copy.setNodeId(this.nodeId);
        return copy;
    }

    @Override
    public String getUniqueId() {
        return nodeId;
    }

}
