package de.iai.ilcd.model.datastock;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.io.Serializable;

/**
 * <p>
 * Implementation of a root data stock.
 * </p>
 */
@Entity
@DiscriminatorValue("rds")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class RootDataStock extends AbstractDataStock implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8470787872313541110L;

    public RootDataStock() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RootDataStock) {
            return super.equals(obj);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return <code>true</code>
     */
    @Override
    public boolean isRoot() {
        return true;
    }

}
