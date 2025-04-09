package de.iai.ilcd.model.process;

import de.fzk.iai.ilcd.service.model.enums.DataQualityIndicatorName;
import de.fzk.iai.ilcd.service.model.enums.QualityValue;
import de.fzk.iai.ilcd.service.model.process.IDataQualityIndicator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "dataqualityindicator")
public class DataQualityIndicator implements Serializable, IDataQualityIndicator {

    private static final long serialVersionUID = 1L;
    @Column(name = "indicatorName")
    @Enumerated(EnumType.STRING)
    protected DataQualityIndicatorName name;
    @Column(name = "indicatorValue")
    @Enumerated(EnumType.STRING)
    protected QualityValue value;
    @Column(name = "indicatorNumericValue")
    protected Double numericValue;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected DataQualityIndicator() {

    }

    public DataQualityIndicator(DataQualityIndicatorName name, QualityValue value) {
        this.name = name;
        this.value = value;
    }

    public Double getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(Double numericValue) {
        this.numericValue = numericValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public DataQualityIndicatorName getName() {
        return name;
    }

    public void setName(DataQualityIndicatorName name) {
        this.name = name;
    }

    @Override
    public QualityValue getValue() {
        return value;
    }

    public void setValue(QualityValue value) {
        this.value = value;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((numericValue == null) ? 0 : numericValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataQualityIndicator other = (DataQualityIndicator) obj;
        if (name != other.name)
            return false;
        if (value != other.value)
            return false;
        if (numericValue != other.numericValue)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.process.DataQualityIndicator[id=" + id + "]";
    }

}
