package de.iai.ilcd.model.lifecyclemodel;

import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.DataEntryByType;
import de.iai.ilcd.model.common.GlobalReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author MK
 * @since soda4LCA 5.7.0
 */

@Entity
@Table(name = "lcm_administrativeinformation_dataentryby")
public class DataEntryBy implements Serializable {

    private static final long serialVersionUID = -9066067693840888143L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dataentryby_id")
    protected long id;
    @Column(name = "timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date timeStamp;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "lcm_dataentry_ref_datasetformat", joinColumns = @JoinColumn(name = "dataentryby_id"), inverseJoinColumns = @JoinColumn(name = "ref_source_id"))
    protected List<GlobalReference> referenceToDataSetFormat = new ArrayList<GlobalReference>();
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "lcm_dataentry_ref_entity_entr_data", joinColumns = @JoinColumn(name = "dataentryby_id"), inverseJoinColumns = @JoinColumn(name = "ref_contact_id"))
    protected List<GlobalReference> referenceToPersonOrEntityEnteringTheData = new ArrayList<GlobalReference>();

    public DataEntryBy() {
    }

    public DataEntryBy(DataEntryByType entry) {

        this.timeStamp = Optional.ofNullable(entry).map(DataEntryByType::getTimeStamp)
                .map(c -> c.toGregorianCalendar().getTime()).orElse(null);
        for (GlobalReferenceType ref : Optional.ofNullable(entry).map(DataEntryByType::getReferenceToDataSetFormat)
                .orElse(new ArrayList<GlobalReferenceType>()))
            this.addReferenceToDataSetFormat(new GlobalReference(ref));

        Optional.ofNullable(entry).map(DataEntryByType::getDataEntryByGroup2)
                .orElse(new ArrayList<GlobalReferenceType>())
                .forEach(ref -> this.getReferenceToPersonOrEntityEnteringTheData().add(new GlobalReference(ref)));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public List<GlobalReference> getReferenceToDataSetFormat() {
        return referenceToDataSetFormat;
    }

    public void addReferenceToDataSetFormat(GlobalReference referenceToDataSetFormat) {
        this.referenceToDataSetFormat.add(referenceToDataSetFormat);
    }

    public List<GlobalReference> getReferenceToPersonOrEntityEnteringTheData() {
        return referenceToPersonOrEntityEnteringTheData;
    }

    @Override
    public int hashCode() {
        return Objects.hash(referenceToDataSetFormat, referenceToPersonOrEntityEnteringTheData, timeStamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataEntryBy other = (DataEntryBy) obj;
        return Objects.equals(referenceToDataSetFormat, other.referenceToDataSetFormat) && Objects
                .equals(referenceToPersonOrEntityEnteringTheData, other.referenceToPersonOrEntityEnteringTheData)
                && Objects.equals(timeStamp, other.timeStamp);
    }

    @Override
    public String toString() {
        return "DataEntryBy [id=" + id + ", timeStamp=" + timeStamp + ", referenceToDataSetFormat="
                + referenceToDataSetFormat + ", referenceToPersonOrEntityEnteringTheData="
                + referenceToPersonOrEntityEnteringTheData + "]";
    }

}