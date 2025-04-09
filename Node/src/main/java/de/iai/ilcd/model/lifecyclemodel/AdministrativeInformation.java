package de.iai.ilcd.model.lifecyclemodel;

import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.AdministrativeInformationType;
import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.DataGeneratorType;
import de.iai.ilcd.model.common.GlobalReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author MK
 * @since soda4LCA 5.7.0
 */

@Entity
@Table(name = "lcm_administrativeinformation")
public class AdministrativeInformation implements Serializable {

    private static final long serialVersionUID = -5959273837657608654L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "administrativeinformation_id")
    protected long id;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "CommissionerAndGoal_id", referencedColumnName = "CommissionerAndGoal_id")
    protected CommissionerAndGoal commissionerAndGoal;
    // === dataGenerator ===//
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "lcm_administrativeinformation_ref_generator", joinColumns = @JoinColumn(name = "administrativeinformation_id"), inverseJoinColumns = @JoinColumn(name = "ref_contact_id"))
    protected List<GlobalReference> referenceToPersonOrEntityGeneratingTheDataSet = new ArrayList<GlobalReference>();

    // === CommissionerAndGoal ===//
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "DataEntryBy_id", referencedColumnName = "DataEntryBy_id")
    protected DataEntryBy dataEntryBy;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PublicationAndOwnership_id", referencedColumnName = "PublicationAndOwnership_id")
    protected PublicationAndOwnership publicationAndOwnership;

    public AdministrativeInformation() {
    }

    public AdministrativeInformation(AdministrativeInformationType adminInfo) {

        this.commissionerAndGoal = Optional.ofNullable(adminInfo)
                .map(AdministrativeInformationType::getCommissionerAndGoal).map(com -> new CommissionerAndGoal(com))
                .orElse(null);

        for (GlobalReferenceType ref : Optional.ofNullable(adminInfo)
                .map(AdministrativeInformationType::getDataGenerator)
                .map(DataGeneratorType::getReferenceToPersonOrEntityGeneratingTheDataSet).orElse(new ArrayList<>()))
            this.addReferenceToPersonOrEntityGeneratingTheDataSet(new GlobalReference(ref));

        this.dataEntryBy = Optional.ofNullable(adminInfo).map(AdministrativeInformationType::getDataEntryBy)
                .map(dataentry -> new DataEntryBy(dataentry)).orElse(null);

        this.publicationAndOwnership = Optional.ofNullable(adminInfo)
                .map(AdministrativeInformationType::getPublicationAndOwnership)
                .map(pub -> new PublicationAndOwnership(pub)).orElse(null);

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CommissionerAndGoal getCommissionerAndGoal() {
        return commissionerAndGoal;
    }

    public void setCommissionerAndGoal(CommissionerAndGoal commissionerAndGoal) {
        this.commissionerAndGoal = commissionerAndGoal;
    }

    public List<GlobalReference> getReferenceToPersonOrEntityGeneratingTheDataSet() {
        return referenceToPersonOrEntityGeneratingTheDataSet;
    }

    public void addReferenceToPersonOrEntityGeneratingTheDataSet(
            GlobalReference referenceToPersonOrEntityGeneratingTheDataSet) {
        this.referenceToPersonOrEntityGeneratingTheDataSet.add(referenceToPersonOrEntityGeneratingTheDataSet);
    }

    public DataEntryBy getDataEntryBy() {
        return dataEntryBy;
    }

    public void setDataEntryBy(DataEntryBy dataEntryBy) {
        this.dataEntryBy = dataEntryBy;
    }

    public PublicationAndOwnership getPublicationAndOwnership() {
        return publicationAndOwnership;
    }

    public void setPublicationAndOwnership(PublicationAndOwnership publicationAndOwnership) {
        this.publicationAndOwnership = publicationAndOwnership;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commissionerAndGoal, dataEntryBy, publicationAndOwnership,
                referenceToPersonOrEntityGeneratingTheDataSet);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AdministrativeInformation other = (AdministrativeInformation) obj;
        return Objects.equals(commissionerAndGoal, other.commissionerAndGoal)
                && Objects.equals(dataEntryBy, other.dataEntryBy)
                && Objects.equals(publicationAndOwnership, other.publicationAndOwnership)
                && Objects.equals(referenceToPersonOrEntityGeneratingTheDataSet,
                other.referenceToPersonOrEntityGeneratingTheDataSet);
    }

    @Override
    public String toString() {
        return "AdministrativeInformation [id=" + id + ", commissionerAndGoal=" + commissionerAndGoal
                + ", referenceToPersonOrEntityGeneratingTheDataSet=" + referenceToPersonOrEntityGeneratingTheDataSet
                + ", dataEntryBy=" + dataEntryBy + ", publicationAndOwnership=" + publicationAndOwnership + "]";
    }
}
