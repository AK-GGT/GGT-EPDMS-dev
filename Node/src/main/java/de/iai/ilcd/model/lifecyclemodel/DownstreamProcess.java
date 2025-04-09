package de.iai.ilcd.model.lifecyclemodel;

import de.fzk.iai.ilcd.api.binding.generated.lifecyclemodel.DownstreamProcessType;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "downstreamprocess")
public class DownstreamProcess implements Serializable {

    private static final long serialVersionUID = 2443451606483286990L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "downstreamprocess_key")
    protected long key;
    @Column(name = "internaldownstreamprocess_id")
    protected long internaldownstreamprocess_id;
    @Column(name = "flowUUID", nullable = true)
    protected String flow;
    @Column(name = "location")
    protected String location;
    @Column(name = "dominant")
    protected boolean dominant;

    public DownstreamProcess() {
    }

    public DownstreamProcess(DownstreamProcessType d) {
        this.internaldownstreamprocess_id = Optional.ofNullable(d).map(DownstreamProcessType::getId)
                .map(BigInteger::longValue).orElse(null);

        this.flow = Optional.ofNullable(d).map(DownstreamProcessType::getFlowUUID).orElse(null);

        this.location = Optional.ofNullable(d).map(DownstreamProcessType::getLocation).orElse(null);
        this.dominant = Optional.ofNullable(d).map(DownstreamProcessType::isDominant).orElse(false);
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public long getInternaldownstreamprocess_id() {
        return internaldownstreamprocess_id;
    }

    public void setInternaldownstreamprocess_id(long internaldownstreamprocess_id) {
        this.internaldownstreamprocess_id = internaldownstreamprocess_id;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isDominant() {
        return dominant;
    }

    public void setDominant(boolean dominant) {
        this.dominant = dominant;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dominant, flow, location);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DownstreamProcess other = (DownstreamProcess) obj;
        return dominant == other.dominant && Objects.equals(flow, other.flow)
                && Objects.equals(location, other.location);
    }

}