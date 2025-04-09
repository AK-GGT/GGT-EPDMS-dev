package de.iai.ilcd.model.common;

import de.iai.ilcd.model.security.User;
import de.iai.ilcd.service.util.JobState;
import de.iai.ilcd.service.util.JobType;
import eu.europa.ec.jrc.lca.commons.domain.ILongIdObject;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "jobmetadata")
public class JobMetaData implements ILongIdObject {

    /*
     * The generated id of jobmetadata entry
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * The unique id of each job
     */
    @Column(name = "jobid", unique = true, updatable = false)
    private String jobId;

    /*
     * The user who has put job into queue
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User User;

    /*
     * The date the job has been fired
     */
    @Basic
    @Column(name = "firingtime")
    private Date jobFireTime;

    /*
     * The date the job has been completed
     */
    @Basic
    @Column(name = "completiontime")
    private Date jobCompletionTime;

    /*
     * The job run time
     */
    @Basic
    @Column(name = "runtime")
    private Long jobRunTime;

    /*
     * The description of job
     */
    @Basic
    @Column(name = "jobname")
    private String jobName;

    /*
     * The job type
     */
    @Basic
    @Column(name = "jobtype")
    private JobType jobType;

    /*
     * The state the job currently has
     */
    @Basic
    @Column(name = "jobstate")
    private JobState jobState;

    /**
     * The message log of executed job
     */
    @Basic
    @Column(name = "log")
    private String log;

    /**
     * The number of occurred errors during job execution
     */
    @Column(name = "infoscount")
    private Long infosCount;

    /**
     * The number of occurred warnings during job execution
     */
    @Column(name = "errorscount")
    private Long errorsCount;

    /**
     * The number of successes during job jexecution
     */
    @Column(name = "successescount")
    private Long successesCount;

    public JobMetaData() {

    }

    /**
     * Creates a JobMetaData entry.
     *
     * @param jobFireTime       The date the job has been fired; is null if job has not been
     *                          fired yet
     * @param jobCompletionTime The date the job has been completed; is null if job has not
     *                          been completed yet
     * @param jobType           The type of job
     * @param jobState          The state the job currently has
     * @param user              The user having put job into queue
     * @param jobName           The job description
     */
    public JobMetaData(Date jobFireTime, Date jobCompletionTime, JobType jobType, JobState jobState, User user,
                       String jobName) {
        this.User = user;
        this.jobCompletionTime = jobCompletionTime;
        this.jobFireTime = jobFireTime;
        this.jobType = jobType;
        this.jobState = jobState;
        this.jobName = jobName;
    }

    /**
     * Gets the fire date
     *
     * @return null if job has not been fired yet, else the firing date
     */
    public Date getJobFireTime() {
        return jobFireTime;
    }

    /**
     * Sets the firing date
     *
     * @param jobFireTime The firing date
     */
    public void setJobFireTime(Date jobFireTime) {
        this.jobFireTime = jobFireTime;
    }

    /**
     * Gets the date the job has been completed
     *
     * @return null if job has not been completed yet, else the completion date
     * date
     */
    public Date getJobCompletionTime() {
        return this.jobCompletionTime;
    }

    /**
     * Sets the completion date.
     *
     * @param jobCompletionTime The date the job was completed
     */
    public void setJobCompletionTime(Date jobCompletionTime) {
        this.jobCompletionTime = jobCompletionTime;

    }

    /**
     * Gets the job run time
     *
     * @return The job run time
     */
    public Long getJobRunTime() {
        return this.jobRunTime;
    }

    /**
     * Gets the job run time.
     *
     * @param jobRunTime The job run time
     */
    public void setJobRunTime(Long jobRunTime) {
        this.jobRunTime = jobRunTime;
    }

    /**
     * Gets the job description.
     *
     * @return the job description
     */
    public String getJobName() {
        return this.jobName;
    }

    /**
     * Sets the job description.
     *
     * @param jobName The job description
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Gets the job type.
     *
     * @return The job type
     */
    public JobType getJobType() {
        return this.jobType;
    }

    /**
     * Sets the job type.
     *
     * @param jobType The job type
     */
    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    /**
     * Gets the current job state.
     *
     * @return The current job state
     */
    public JobState getJobState() {
        return this.jobState;
    }

    /**
     * Sets the current job state.
     *
     * @param jobState The current job state
     */
    public void setJobState(JobState jobState) {
        this.jobState = jobState;
    }

    /**
     * Gets the user having queued job.
     *
     * @return The user having queued job
     */
    public User getUser() {
        return this.User;
    }

    /**
     * Sets the user having queued job.
     *
     * @param user The user having queued job
     */
    public void setUser(User user) {
        this.User = user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return id;
    }

    /**
     * Gets the job id.
     *
     * @return The unique job id
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Sets the job id.
     *
     * @param jobId The unique job id
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    /**
     * Gets the log.
     *
     * @return The log
     */
    public String getLog() {
        return this.log;
    }

    /**
     * Sets the log with given new log
     *
     * @param log The log
     */
    public void setLog(String log) {
        this.log = log;
    }

    /**
     * Appends the new log to existing log.
     *
     * @param logToAppend The log that shall be appended
     */
    public void appendLog(String logToAppend) {
        this.log = this.log.concat(logToAppend);
        this.log = this.log.concat("\n");
    }

    /**
     * Gets the number of infos that occurred during job execution
     */
    public Long getInfosCount() {
        return this.infosCount;
    }

    /**
     * Sets the number of infos that occured during job execution
     */
    public void setInfosCount(Long infosCount) {
        if (this.infosCount == null)
            initialiseCounters();
        this.infosCount = infosCount;
    }

    /**
     * Increases the number of infos by 1
     */
    public void addInfosCount() {
        if (this.infosCount == null)
            initialiseCounters();

        this.infosCount++;
    }

    /**
     * Gets the number of errors that occurred during job execution
     */
    public Long getErrorsCount() {
        if (this.errorsCount == null)
            initialiseCounters();
        return this.errorsCount;
    }

    /**
     * Sets the number of errors that occured during job execution.
     */
    public void setErrorsCount(Long errorsCount) {
        if (this.successesCount == null)
            initialiseCounters();
        this.errorsCount = errorsCount;
    }

    /**
     * Increases the number of errors by 1.
     */
    public void addErrorsCount() {
        if (this.errorsCount == null)
            initialiseCounters();

        this.errorsCount++;
    }

    /**
     * Gets the number of successes that occurred during job execution.
     */
    public Long getSuccessesCount() {
        return this.successesCount;
    }

    /**
     * Sets The number of successes that occurres during job execution.
     */
    public void setSuccessesCount(Long successesCount) {
        if (this.successesCount == null)
            initialiseCounters();
        this.successesCount = successesCount;
    }

    /**
     * Increases the number of successes by 1.
     */
    public void addSuccessesCount() {
        if (this.successesCount == null)
            initialiseCounters();

        this.successesCount++;
    }

    private void initialiseCounters() {
        if (successesCount == null)
            successesCount = 0L;
        if (infosCount == null)
            infosCount = 0L;
        if (errorsCount == null)
            errorsCount = 0L;
    }

}
