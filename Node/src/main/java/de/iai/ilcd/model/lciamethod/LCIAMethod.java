package de.iai.ilcd.model.lciamethod;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.ILCIAMethodVO;
import de.fzk.iai.ilcd.service.model.enums.AreaOfProtectionValue;
import de.fzk.iai.ilcd.service.model.enums.LCIAImpactCategoryValue;
import de.fzk.iai.ilcd.service.model.enums.TypeOfLCIAMethodValue;
import de.fzk.iai.ilcd.service.model.lciamethod.ITimeInformation;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.LCIAMethodDao;
import de.iai.ilcd.model.datastock.DataStock;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.iai.ilcd.model.lciamethod.LCIAMethod.TABLE_NAME;

/**
 * <p>
 * Class that represents LCIAMethod entity
 * </p>
 * <p>
 * Please note that this class is no thread safe!
 * </p>
 */
@Entity
@Table(name = TABLE_NAME, uniqueConstraints = @UniqueConstraint(columnNames = {"UUID", "MAJORVERSION", "MINORVERSION", "SUBMINORVERSION"}))
@AssociationOverrides({
        @AssociationOverride(name = "classifications", joinTable = @JoinTable(name = "lciamethod_classifications"), joinColumns = @JoinColumn(name = "lciamethod_id")),
        @AssociationOverride(name = "description", joinTable = @JoinTable(name = "lciamethod_description"), joinColumns = @JoinColumn(name = "lciamethod_id")),
        @AssociationOverride(name = "name", joinTable = @JoinTable(name = "lciamethod_name"), joinColumns = @JoinColumn(name = "lciamethod_id"))})
public class LCIAMethod extends DataSet implements ILCIAMethodVO {

    public static final String TABLE_NAME = "lciamethod";

    /**
     * Serialization ID
     */
    @Transient
    private static final long serialVersionUID = 1365429475652486423L;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "referencequantity_ID")
    protected GlobalReference referenceQuantity = null;
    /**
     * The data stocks this LCIA Method is contained in
     */
    @ManyToMany(mappedBy = "lciaMethods", fetch = FetchType.LAZY)
    protected Set<DataStock> containingDataStocks = new HashSet<DataStock>();
    /**
     * The time information
     */
    @Embedded
    private TimeInformation timeInformation = new TimeInformation();
    /**
     * The type of this LCIA method
     */
    @Enumerated(EnumType.STRING)
    private TypeOfLCIAMethodValue type;
    /**
     * The areas of protection (singular name to comply with bean conventions)
     */
    @ElementCollection
    @CollectionTable(name = "lciamethod_areaofprotection", joinColumns = @JoinColumn(name = "lciamethod_id"))
    @Enumerated(EnumType.STRING)
    private List<AreaOfProtectionValue> areaOfProtection = new ArrayList<AreaOfProtectionValue>();
    /**
     * The impact categories (singular name to comply with bean conventions)
     */
    @ElementCollection
    @CollectionTable(name = "lciamethod_impactcategory", joinColumns = @JoinColumn(name = "lciamethod_id"))
    @Enumerated(EnumType.STRING)
    private List<LCIAImpactCategoryValue> impactCategory = new ArrayList<LCIAImpactCategoryValue>();
    /**
     * The impact indicator
     */
    @Basic
    @Column(length = 500)
    private String impactIndicator;
    /**
     * The methodologies (singular name to comply with bean conventions)
     */
    @ElementCollection
    @CollectionTable(name = "lciamethod_methodology", joinColumns = @JoinColumn(name = "lciamethod_id"))
    private List<String> methodology = new ArrayList<String>();
    /**
     * The characterisation factors
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LCIAMethodCharacterisationFactor> characterisationFactors = new ArrayList<LCIAMethodCharacterisationFactor>();

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
        stock.addLCIAMethod(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeSelfFromDataStock(DataStock stock) {
        stock.removeLCIAMethod(this);
    }

    /**
     * Get the time information
     *
     * @return time information
     */
    @Override
    public ITimeInformation getTimeInformation() {
        return this.timeInformation;
    }

    /**
     * Set the new time information
     *
     * @param timeInformation the new time information
     */
    public void setTimeInformation(TimeInformation timeInformation) {
        this.timeInformation = timeInformation;
    }

    /**
     * Get the type of this LCIA method
     *
     * @return type of this LCIA method
     */
    @Override
    public TypeOfLCIAMethodValue getType() {
        return this.type;
    }

    /**
     * Set the type of the LCIA method
     *
     * @param type new type of the LCIA method
     */
    public void setType(TypeOfLCIAMethodValue type) {
        this.type = type;
    }

    /**
     * Get the areas of protection
     *
     * @return areas of protection
     */
    @Override
    public List<AreaOfProtectionValue> getAreaOfProtection() {
        return this.areaOfProtection;
    }

    /**
     * Set the areas of protection
     *
     * @param areasOfProtection areas of protection
     */
    protected void setAreasOfProtection(List<AreaOfProtectionValue> areasOfProtection) {
        this.areaOfProtection = areasOfProtection;
    }

    /**
     * Add an area of protection
     *
     * @param areaOfProtection area of protection to add
     */
    public void addAreaOfProtection(AreaOfProtectionValue areaOfProtection) {
        if (!this.areaOfProtection.contains(areaOfProtection)) {
            this.areaOfProtection.add(areaOfProtection);
        }
    }

    /**
     * Get the impact categories
     *
     * @return impact categories
     */
    @Override
    public List<LCIAImpactCategoryValue> getImpactCategory() {
        return this.impactCategory;
    }

    /**
     * Set the impact categories
     *
     * @param impactCategory new impact categories
     */
    protected void setImpactCategory(List<LCIAImpactCategoryValue> impactCategory) {
        this.impactCategory = impactCategory;
    }

    /**
     * Add an impact category
     *
     * @param impactCategory impact category to add
     */
    public void addImpactCategory(LCIAImpactCategoryValue impactCategory) {
        if (!this.impactCategory.contains(impactCategory)) {
            this.impactCategory.add(impactCategory);
        }
    }

    /**
     * Get the impact indicator
     *
     * @return impact indicator
     */
    @Override
    public String getImpactIndicator() {
        return this.impactIndicator;
    }

    /**
     * Set the new impact indicator
     *
     * @param impactIndicator the new impact indicator
     */
    public void setImpactIndicator(String impactIndicator) {
        if (impactIndicator != null && impactIndicator.length() > 500) {
            throw new IllegalArgumentException("Impact indicator field must not be longer than 500 characters!");
        }
        this.impactIndicator = impactIndicator;
    }

    /**
     * Get the methodologies
     *
     * @return methodologies
     */
    @Override
    public List<String> getMethodology() {
        return this.methodology;
    }

    /**
     * Set the new methodologies
     *
     * @param methodology the new methodologies
     */
    protected void setMethodology(List<String> methodology) {
        this.methodology = methodology;
    }

    /**
     * Add a methodology
     *
     * @param methodology methodology to add
     */
    public void addMethodology(String methodology) {
        if (!this.methodology.contains(methodology)) {
            this.methodology.add(methodology);
        }
    }

    /**
     * Get the characterisation factors
     *
     * @return characterisation factors
     */
    public List<LCIAMethodCharacterisationFactor> getCharactarisationFactors() {
        return this.characterisationFactors;
    }

    /**
     * Set the new characterisation factors
     *
     * @param characterisationFactors the new characterisation factors
     */
    protected void setCharactarisationFactors(List<LCIAMethodCharacterisationFactor> characterisationFactors) {
        this.characterisationFactors = characterisationFactors;
    }

    /**
     * Add a characterisation factor
     *
     * @param characterisationFactor characterisation factor to add
     */
    public void addCharacterisationFactor(LCIAMethodCharacterisationFactor characterisationFactor) {
        if (!this.characterisationFactors.contains(characterisationFactor)) {
            this.characterisationFactors.add(characterisationFactor);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return LCIAMethod.class.getName() + "[id=" + this.getId() + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetType getDataSetType() {
        return DataSetType.LCIAMETHOD;
    }

    public GlobalReference getReferenceQuantity() {
        return referenceQuantity;
    }

    public void setReferenceQuantity(GlobalReference referenceQuantity) {
        this.referenceQuantity = referenceQuantity;
    }

    @Override
    public String getDirPathInZip() {
        return "ILCD/" + DatasetTypes.LCIAMETHODS.getValue();
    }

    @Override
    public DataSetDao<?, ?, ?> getCorrespondingDSDao() {
        return new LCIAMethodDao();
    }
}
