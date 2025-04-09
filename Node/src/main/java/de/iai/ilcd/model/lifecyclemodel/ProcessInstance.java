package de.iai.ilcd.model.lifecyclemodel;

import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.ConnectionsType;
import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.GroupsType;
import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.ParametersType;
import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.ProcessInstanceType;
import de.iai.ilcd.model.common.GlobalReference;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/**
 * @author MK
 * @since soda4LCA 5.7.0
 */

@Entity
@Table(name = "lifecyclemodel_processinstance")
public class ProcessInstance implements Serializable, Comparable<ProcessInstance> {

    private static final long serialVersionUID = -2226477255823943016L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "processinstance_key")
    protected long key;
    @Column(name = "datasetinternal_id")
    protected long dataSetInternalID;
    @Column(name = "multiplicationfactor", nullable = true)
    protected Double multiplicationFactor;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "referenceToProcess")
    protected GlobalReference referenceToProcess;
    @Column(name = "scalingfactor")
    protected Double scalingFactor;
    @ElementCollection
    @Column(name = "parameter_value", columnDefinition = "TEXT")
    @CollectionTable(name = "processinstance_parameters", joinColumns = @JoinColumn(name = "processinstance_key"))
    @MapKeyColumn(name = "parameter_MatV")
    protected Map<String, Double> parameters = new HashMap<String, Double>();
    @ElementCollection
    @Column(name = "memberof_group_id")
    @CollectionTable(name = "processinstance_memberofgroup", joinColumns = @JoinColumn(name = "processinstance_key"))
    protected List<Integer> memberOf = new ArrayList<Integer>();
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "processinstance_connections", joinColumns = @JoinColumn(name = "processinstanceid"), inverseJoinColumns = @JoinColumn(name = "outputexchange_id"))
    protected List<OutputExchange> connections = new ArrayList<OutputExchange>();

    public ProcessInstance() {
    }

    public ProcessInstance(ProcessInstanceType p) {
        this.dataSetInternalID = Optional.ofNullable(p).map(ProcessInstanceType::getDataSetInternalID)
                .map(BigInteger::longValue).orElse(null);

        this.multiplicationFactor = Optional.ofNullable(p).map(ProcessInstanceType::getMultiplicationFactor)
                .orElse(null);

        this.referenceToProcess = Optional.ofNullable(p).map(ProcessInstanceType::getReferenceToProcess)
                .map(g -> new GlobalReference(g)).orElse(null);

        this.scalingFactor = Optional.ofNullable(p).map(ProcessInstanceType::getScalingFactor).orElse(null);

        Optional.ofNullable(p).map(ProcessInstanceType::getParameters).map(ParametersType::getParameter)
                .orElse(new ArrayList<>())
                .forEach(param -> this.getParameters().put(param.getName(), param.getValue()));

        Optional.ofNullable(p).map(ProcessInstanceType::getGroups).map(GroupsType::getMemberOf)
                .orElse(new ArrayList<>()).forEach(m -> this.getMemberOf().add(m.getGroupId().intValue()));

        Optional.ofNullable(p).map(ProcessInstanceType::getConnections).map(ConnectionsType::getOutputExchange)
                .orElse(new ArrayList<>()).forEach(c -> this.addConnections(new OutputExchange(c)));

    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public long getDataSetInternalID() {
        return dataSetInternalID;
    }

    public void setDataSetInternalID(long dataSetInternalID) {
        this.dataSetInternalID = dataSetInternalID;
    }

    public Double getMultiplicationFactor() {
        return multiplicationFactor;
    }

    public void setMultiplicationFactor(Double multiplicationFactor) {
        this.multiplicationFactor = multiplicationFactor;
    }

    public GlobalReference getReferenceToProcess() {
        return referenceToProcess;
    }

    public void setReferenceToProcess(GlobalReference referenceToProcess) {
        this.referenceToProcess = referenceToProcess;
    }

    public Double getScalingFactor() {
        return scalingFactor;
    }

    public void setScalingFactor(Double scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    public Map<String, Double> getParameters() {
        return parameters;
    }

    public List<Integer> getMemberOf() {
        return memberOf;
    }

    public List<OutputExchange> getConnections() {
        return connections;
    }

    public void addConnections(OutputExchange connection) {
        this.connections.add(connection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connections, dataSetInternalID, memberOf, multiplicationFactor, parameters,
                referenceToProcess, scalingFactor);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProcessInstance other = (ProcessInstance) obj;
        return Objects.equals(connections, other.connections) && dataSetInternalID == other.dataSetInternalID
                && Objects.equals(memberOf, other.memberOf)
                && Double.doubleToLongBits(multiplicationFactor) == Double.doubleToLongBits(other.multiplicationFactor)
                && Objects.equals(parameters, other.parameters)
                && Objects.equals(referenceToProcess, other.referenceToProcess)
                && Double.doubleToLongBits(scalingFactor) == Double.doubleToLongBits(other.scalingFactor);
    }

    @Override
    public String toString() {
        return "ProcessInstance [dataSetInternalID=" + dataSetInternalID + ", multiplicationFactor="
                + multiplicationFactor + ", referenceToProcess=" + referenceToProcess + ", scalingFactor="
                + scalingFactor + ", parameters=" + parameters + ", memberOf=" + memberOf + ", connections="
                + connections + "]";
    }

    @Override
    public int compareTo(ProcessInstance o) {
        return this.getReferenceToProcess().getShortDescription().getDefaultValue().compareToIgnoreCase(
                o.getReferenceToProcess().getShortDescription().getDefaultValue());
    }

}
