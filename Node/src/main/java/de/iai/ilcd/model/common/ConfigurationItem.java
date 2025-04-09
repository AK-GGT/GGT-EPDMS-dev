package de.iai.ilcd.model.common;

import javax.persistence.*;

@Entity
@Table(name = "configuration")
public class ConfigurationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    @Column(name = "property")
    private String property;

    @Basic
    @Column(name = "intvalue")
    private Long intvalue;

    @Basic
    @Column(name = "stringvalue")
    private String stringvalue;


    public Long getId() {
        return this.id;
    }

    public String getProperty() {
        return property;
    }


    public void setPoperty(String property) {
        this.property = property;
    }

    public Long getIntvalue() {
        return this.intvalue;
    }

    public void setIntvalue(Long intvalue) {
        this.intvalue = intvalue;
    }

    public String getStringvalue() {
        return this.stringvalue;
    }

    public void setStringvalue(String stringvalue) {
        this.stringvalue = stringvalue;
    }
}
