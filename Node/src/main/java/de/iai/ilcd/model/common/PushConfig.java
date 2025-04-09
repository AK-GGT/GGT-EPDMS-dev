package de.iai.ilcd.model.common;

import de.iai.ilcd.model.dao.DependenciesMode;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.service.util.JobState;
import eu.europa.ec.jrc.lca.commons.domain.ILongIdObject;

import javax.persistence.*;
import java.util.Date;

/**
 * The push configuration. This manages the data table entry containing name,
 * source {@link AbstractDataStock} and {@link PushTarget} configuration.
 *
 * @author sarai
 */
@Entity
@Table(name = "pushconfig")
public class PushConfig implements ILongIdObject {
    /*
     * The unique id of push config entry
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * The unique name of push cinfig entry
     */
    @Column(unique = true)
    private String name;

    /*
     * The source data stock
     */
    @ManyToOne
    @JoinColumn(name = "source_datastock_id")
    private AbstractDataStock source;

    /*
     * The push target entry containing target node and target stock
     */
    @ManyToOne
    @JoinColumn(name = "target_pushtarget_id")
    private PushTarget target;

    /*
     * The dependencies mode for pushing data sets
     */
    @Basic
    @Column(name = "dependencies_mode")
    private DependenciesMode dependenciesMode;

    /*
     * The date of last push operation
     */
    @Basic
    @Column(name = "lastpushdate")
    private Date lastPushDate;

    /*
     * The state of last push operation
     */
    @Basic
    @Column(name = "lastjobstate")
    private JobState lastJobState;

    @Basic
    @Column(name = "favourite")
    private boolean favourite;

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Gets the name of this Push configuration entry.
     *
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this Push Config entry.
     *
     * @param name name of push config entry
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the source data stock
     *
     * @return A data stock that is used as source
     */
    public AbstractDataStock getSource() {
        return this.source;
    }

    /**
     * Sets the source data stock.
     *
     * @param source The data stock used as source
     */
    public void setSource(AbstractDataStock source) {
        this.source = source;
    }

    /**
     * Gets Push target entry.
     *
     * @return a Push target entry
     */
    public PushTarget getTarget() {
        return this.target;
    }

    /**
     * Sets the Push target entry
     *
     * @param target
     */
    public void setTarget(PushTarget target) {
        this.target = target;
    }

    /**
     * gets the dependencies mode for pushing data sets
     *
     * @return the dependencies mode
     */
    public DependenciesMode getDependenciesMode() {
        return this.dependenciesMode;
    }

    /**
     * Sets dependencies mode for pushing data sets
     *
     * @param dependenciesMode
     */
    public void setDependenciesMode(DependenciesMode dependenciesMode) {
        this.dependenciesMode = dependenciesMode;
    }

    /**
     * Gets date of last push call
     *
     * @return The date of last push call
     */
    public Date getLastPushDate() {
        return lastPushDate;
    }

    /**
     * Sets date of last push call.
     *
     * @param lastPushDate Date of last push call
     */
    public void setLastPushDate(Date lastPushDate) {
        this.lastPushDate = lastPushDate;
    }

    /**
     * Gets last known job state.
     *
     * @return Last known job state
     */
    public JobState getLastJobState() {
        return this.lastJobState;
    }

    /**
     * Sets last known job state.
     *
     * @param lastJobState Last known job state
     */
    public void setLastJobState(JobState lastJobState) {
        this.lastJobState = lastJobState;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }
}
