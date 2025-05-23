package de.iai.ilcd.model.flowproperty;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.IFlowPropertyVO;
import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.fzk.iai.ilcd.service.model.flowproperty.IUnitGroupType;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.FlowPropertyDao;
import de.iai.ilcd.model.datastock.DataStock;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import de.iai.ilcd.util.lstring.IStringMapProvider;
import de.iai.ilcd.util.lstring.MultiLangStringMapAdapter;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.iai.ilcd.model.flowproperty.FlowProperty.TABLE_NAME;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = TABLE_NAME, uniqueConstraints = @UniqueConstraint(columnNames = {"UUID", "MAJORVERSION", "MINORVERSION", "SUBMINORVERSION"}))
@AssociationOverrides({
        @AssociationOverride(name = "classifications", joinTable = @JoinTable(name = "flowproperty_classifications"), joinColumns = @JoinColumn(name = "flowproperty_id")),
        @AssociationOverride(name = "description", joinTable = @JoinTable(name = "flowproperty_description"), joinColumns = @JoinColumn(name = "flowproperty_id")),
        @AssociationOverride(name = "name", joinTable = @JoinTable(name = "flowproperty_name"), joinColumns = @JoinColumn(name = "flowproperty_id"))})
public class FlowProperty extends DataSet implements Serializable, IFlowPropertyVO {

public static final String TABLE_NAME = "flowproperty";

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 7473119545025737038L;

    @ElementCollection
    @Column(name = "value", columnDefinition = "TEXT")
    @CollectionTable(name = "flowproperty_synonyms", joinColumns = @JoinColumn(name = "flowproperty_id"))
    @MapKeyColumn(name = "lang")
    protected Map<String, String> synonyms = new HashMap<String, String>();

    /**
     * Adapter for API backwards compatibility.
     */
    @Transient
    private final MultiLangStringMapAdapter synonymsAdapter = new MultiLangStringMapAdapter(new IStringMapProvider() {

        @Override
        public Map<String, String> getMap() {
            return FlowProperty.this.synonyms;
        }
    });

    @ManyToOne(cascade = CascadeType.ALL)
    protected GlobalReference referenceToUnitGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    protected UnitGroup unitGroup;

    /**
     * The data stocks this flow properties is contained in
     */
    @ManyToMany(mappedBy = "flowProperties", fetch = FetchType.LAZY)
    protected Set<DataStock> containingDataStocks = new HashSet<DataStock>();

    /**
     * Cache for the default unit group.
     * 20 character limit should be sufficient
     */
    // only for query efficiency
    @Basic
    @Column(name = "defaultUnitGroup_cache", length = 20)
    private String defaultUnitGroupCache;

    /**
     * Cache for the default unit.
     * 20 character limit should be sufficient
     */
    // only for query efficiency
    @Basic
    @Column(name = "defaultUnit_cache", length = 10)
    private String defaultUnitCache;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addSelfToDataStock(DataStock stock) {
        stock.addFlowProperty(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeSelfFromDataStock(DataStock stock) {
        stock.removeFlowProperty(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<DataStock> getContainingDataStocks() {
        return this.containingDataStocks;
    }

    @Override
    public IMultiLangString getSynonyms() {
        return this.synonymsAdapter;
    }

    public void setSynonyms(IMultiLangString synonyms) {
        this.synonymsAdapter.overrideValues(synonyms);
    }

    public GlobalReference getReferenceToUnitGroup() {
        return this.referenceToUnitGroup;
    }

    public void setReferenceToUnitGroup(GlobalReference referenceToUnitGroup) {
        this.referenceToUnitGroup = referenceToUnitGroup;
    }

    public UnitGroup getUnitGroup() {
        return this.unitGroup;
    }

    public void setUnitGroup(UnitGroup unitGroup) {
        this.unitGroup = unitGroup;
    }

    public IMultiLangString getUnitGroupName() {
        IMultiLangString unitGroupName = null;

        if (this.getUnitGroup() != null) {
            unitGroupName = this.getUnitGroup().getName();
        } else if (this.getReferenceToUnitGroup() != null) {
            unitGroupName = this.getReferenceToUnitGroup().getShortDescription();
        }

        return unitGroupName;
    }

    public String getDefaultUnit() {
        if (this.getUnitGroup() != null) {
            return this.getUnitGroup().getReferenceUnit().getName();
        }
        // OK, we can't find the unit group, let's use the GlobalReference
        if (this.referenceToUnitGroup != null) {
            return this.referenceToUnitGroup.getShortDescription().getDefaultValue();
        }
        // No chance, for meaningful information
        return null;
    }

    @Override
    public IUnitGroupType getUnitGroupDetails() {
        return new UnitGroupDetails(this.unitGroup, this.referenceToUnitGroup);
    }

    /**
     * Apply cache fields for flow property, those are:
     * <ul>
     * <li>{@link #getDefaultUnit()}</li>
     * <li>{@link #getUnitGroupName()}</li>
     * </ul>
     */
    @Override
    @PrePersist
    protected void applyDataSetCache() {
        super.applyDataSetCache();
        IMultiLangString tmp = this.getUnitGroupName();
        if (tmp != null) {
            this.defaultUnitGroupCache = StringUtils.substring(tmp.getDefaultValue(), 0, 20);
        } else {
            this.defaultUnitGroupCache = null;
        }
        this.defaultUnitCache = StringUtils.substring(this.getDefaultUnit(), 0, 10);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetType getDataSetType() {
        return DataSetType.FLOWPROPERTY;
    }

    @Override
    public String getDirPathInZip() {
        return "ILCD/" + DatasetTypes.FLOWPROPERTIES.getValue();
    }

    @Override
    public DataSetDao<?, ?, ?> getCorrespondingDSDao() {
        return new FlowPropertyDao();
    }
}
