package de.iai.ilcd.model.unitgroup;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.IUnitGroupVO;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.UnitGroupDao;
import de.iai.ilcd.model.datastock.DataStock;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.io.File;
import java.io.Serializable;
import java.util.*;

import static de.iai.ilcd.model.unitgroup.UnitGroup.TABLE_NAME;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = TABLE_NAME, uniqueConstraints = @UniqueConstraint(columnNames = {"UUID", "MAJORVERSION", "MINORVERSION", "SUBMINORVERSION"}))
@AssociationOverrides({
        @AssociationOverride(name = "classifications", joinTable = @JoinTable(name = "unitgroup_classifications"), joinColumns = @JoinColumn(name = "unitgroup_id")),
        @AssociationOverride(name = "description", joinTable = @JoinTable(name = "unitgroup_description"), joinColumns = @JoinColumn(name = "unitgroup_id")),
        @AssociationOverride(name = "name", joinTable = @JoinTable(name = "unitgroup_name"), joinColumns = @JoinColumn(name = "unitgroup_id"))})
public class UnitGroup extends DataSet implements Serializable, IUnitGroupVO {

    public static final String TABLE_NAME = "unitgroup";

    /**
     *
     */
    private static final long serialVersionUID = -7670192301518075529L;
    /**
     * The data stocks this unit group is contained in
     */
    @ManyToMany(mappedBy = "unitGroups", fetch = FetchType.LAZY)
    protected Set<DataStock> containingDataStocks = new HashSet<DataStock>();
    @ManyToOne
    Unit referenceUnit;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("internalId")
    Set<Unit> units = new HashSet<Unit>();
    /**
     * Cache for the default unit.
     * 20 character limit should be sufficient
     */
    // only for query efficiency
    @Basic
    @Column(name = "referenceUnit_cache", length = 10)
    private String referenceUnitCache;

    public UnitGroup() {

    }

    public UnitGroup(String name) {
        this.getName().setValue(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<DataStock> getContainingDataStocks() {
        return this.containingDataStocks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addSelfToDataStock(DataStock stock) {
        stock.addUnitGroup(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeSelfFromDataStock(DataStock stock) {
        stock.removeUnitGroup(this);
    }

    public Set<Unit> getUnits() {
        return this.units;
    }

    protected void setUnits(Set<Unit> units) {
        this.units = units;
    }

    /**
     * Convenience method for returning units as List in order to user p:dataList (primefaces)
     *
     * @return List of units
     */
    public List<Unit> getUnitsAsList() {
        return new ArrayList<Unit>(this.getUnits());
    }

    public void addUnit(Unit unit) {
        this.units.add(unit);
    }

    public Unit getReferenceUnit() {
        return this.referenceUnit;
    }

    public void setReferenceUnit(Unit referenceUnit) {
        this.referenceUnit = referenceUnit;
    }

    @Override
    public String getDefaultUnit() {
        // DefaultUnit can be null (dimensionless)
        // return this.referenceUnit.getName();
        return Optional.ofNullable(this).map(UnitGroup::getReferenceUnit).map(Unit::getName).orElse(null);
    }

    /**
     * Apply cache fields for unit group, those are:
     * <ul>
     * <li>{@link #getDefaultUnit()}</li>
     * </ul>
     */
    @Override
    @PrePersist
    protected void applyDataSetCache() {
        super.applyDataSetCache();
        if (this.referenceUnit != null) {
            this.referenceUnitCache = StringUtils.substring(this.referenceUnit.getName(), 0, 10);
        } else {
            this.referenceUnitCache = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetType getDataSetType() {
        return DataSetType.UNITGROUP;
    }

    protected void collectDependencies() {
    }


    @Override
    public String getDirPathInZip() {
        return "ILCD/" + DatasetTypes.UNITGROUPS.getValue();
    }

    @Override
    public DataSetDao<?, ?, ?> getCorrespondingDSDao() {
        return new UnitGroupDao();
    }
}
