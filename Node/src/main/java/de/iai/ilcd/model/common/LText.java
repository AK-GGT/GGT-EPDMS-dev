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
public class LText implements Serializable, ILString {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lang")
    private String lang;

    @Column(name = "text", length = 10000)
    private String value;

    public LText() {

    }

    public LText(String language, String value) {
        this.lang = language;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String language) {
        this.lang = language;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String text) {
        this.value = text;
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
        if (!(object instanceof LText)) {
            return false;
        }
        LText other = (LText) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.common.LString[id=" + id + "]";
    }

}
