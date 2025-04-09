package de.iai.ilcd.model.datastock;

import javax.persistence.*;
import java.io.Serializable;

/**
 * This class is used to manage the display (hide/unhide and sort order) of data stocks.
 */
@Entity
@Table(name = "datastock_display_props")
public class DisplayProperties implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3829209708907773608L;
    /**
     * whether the datastock is hidden or not
     */
    @Basic
    @Column(name = "hidden")
    protected boolean hidden = false;
    /**
     * Database ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * path of the cached file
     */
    @Basic
    @Column(name = "ordinal")
    private Integer ordinal = null;

    public DisplayProperties() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

}
