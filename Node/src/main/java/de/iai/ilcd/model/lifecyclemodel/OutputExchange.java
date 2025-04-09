package de.iai.ilcd.model.lifecyclemodel;

import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.DownstreamProcessType;
import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.OutputExchangeType;

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
@Table(name = "outputexchange")
public class OutputExchange implements Serializable {

    private static final long serialVersionUID = -7580249384273294380L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outputexchange_key")
    protected long key;
    @Column(name = "internaloutputexchange_id")
    protected long internaloutputexchange_id;
    @Column(name = "dominant")
    protected boolean dominant;
    @Column(name = "flowUUID")
    protected String flow;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "outputexchange_downstreamprocess", joinColumns = @JoinColumn(name = "outputexchange_key"), inverseJoinColumns = @JoinColumn(name = "downstreamprocess_key"))
    protected List<DownstreamProcess> downstreamProcesses = new ArrayList<DownstreamProcess>();

    public OutputExchange() {
    }

    public OutputExchange(OutputExchangeType c) {
        this.flow = Optional.ofNullable(c).map(OutputExchangeType::getFlowUUID).orElse(null);

        for (DownstreamProcessType d : Optional.ofNullable(c).map(OutputExchangeType::getDownstreamProcess)
                .orElse(new ArrayList<>()))
            this.addDownstreamProcesses(new DownstreamProcess(d));

        this.dominant = Optional.ofNullable(c).map(OutputExchangeType::isDominant).orElse(false);
    }

    public long getInternaloutputexchange_id() {
        return internaloutputexchange_id;
    }

    public void setInternaloutputexchange_id(long internaloutputexchange_id) {
        this.internaloutputexchange_id = internaloutputexchange_id;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public boolean isDominant() {
        return dominant;
    }

    public void setDominant(boolean dominant) {
        this.dominant = dominant;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public List<DownstreamProcess> getDownstreamProcesses() {
        return downstreamProcesses;
    }

    public void addDownstreamProcesses(DownstreamProcess downstreamProcess) {
        this.downstreamProcesses.add(downstreamProcess);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dominant, downstreamProcesses, flow);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OutputExchange other = (OutputExchange) obj;
        return dominant == other.dominant && Objects.equals(downstreamProcesses, other.downstreamProcesses)
                && Objects.equals(flow, other.flow);
    }

    @Override
    public String toString() {
        return "OutputExchange [key=" + key + ", internaloutputexchange_id=" + internaloutputexchange_id + ", dominant="
                + dominant + ", flow=" + flow + ", downstreamProcesses=" + downstreamProcesses + "]";
    }

}
