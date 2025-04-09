package eu.europa.ec.jrc.lca.registry.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "t_compliance")
public class Compliance implements Serializable {
    private static final long serialVersionUID = -4467216545150487007L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
