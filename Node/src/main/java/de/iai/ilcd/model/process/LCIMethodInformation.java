package de.iai.ilcd.model.process;

import de.fzk.iai.ilcd.service.model.enums.LCIMethodApproachesValue;
import de.fzk.iai.ilcd.service.model.enums.LCIMethodPrincipleValue;
import de.fzk.iai.ilcd.service.model.process.ILCIMethodInformation;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author clemens.duepmeier
 */
@Entity
@Table(name = "lcimethodinformation")
public class LCIMethodInformation implements Serializable, ILCIMethodInformation {

    private static final long serialVersionUID = 1L;
    @Enumerated(EnumType.STRING)
    protected LCIMethodPrincipleValue methodPrinciple;
    @ElementCollection()
    @CollectionTable(name = "process_lcimethodapproaches", joinColumns = @JoinColumn(name = "processId"))
    @Column(name = "approach")
    @Enumerated(EnumType.STRING)
    protected Set<LCIMethodApproachesValue> approaches = new HashSet<LCIMethodApproachesValue>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Set<LCIMethodApproachesValue> getApproaches() {
        return approaches;
    }

    protected void setApproaches(Set<LCIMethodApproachesValue> allocationApproaches) {
        this.approaches = allocationApproaches;
    }

    /**
     * Convenience method for returning LCI method approaches as List in order to user p:dataList (primefaces)
     *
     * @return List of LCI method approaches
     */
    public List<LCIMethodApproachesValue> getApproachesAsList() {
        return new ArrayList<LCIMethodApproachesValue>(this.getApproaches());
    }

    public void addApproach(LCIMethodApproachesValue allocationApproach) {
        if (!approaches.contains(allocationApproach))
            approaches.add(allocationApproach);
    }

    @Override
    public LCIMethodPrincipleValue getMethodPrinciple() {
        return methodPrinciple;
    }

    public void setMethodPrinciple(LCIMethodPrincipleValue methodPrinciple) {
        this.methodPrinciple = methodPrinciple;
    }

}
