package de.iai.ilcd.model.common;

import de.fzk.iai.ilcd.service.model.common.ILString;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author clemens.duepmeier
 * @deprecated
 */
// @Entity
public class LString implements Serializable, ILString {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lang")
    private String lang;

    @Column(name = "strValue")
    private String value;

    public LString() {

    }

    public LString(String language, String value) {
        this.lang = language;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public void setLang(String language) {
        this.lang = language;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String strValue) {
        this.value = strValue;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof LString)) {
            return false;
        }
        LString other = (LString) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

}
