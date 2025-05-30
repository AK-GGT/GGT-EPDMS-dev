package eu.europa.ec.jrc.lca.registry.domain;

import eu.europa.ec.jrc.lca.commons.domain.ILongIdObject;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "t_user")
public class User implements ILongIdObject, Serializable {
    private static final long serialVersionUID = 69695394619550586L;

    @Id
    private Long id;

    private String login;

    private String password;

    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
