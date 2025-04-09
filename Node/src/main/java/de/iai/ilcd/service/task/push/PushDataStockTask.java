package de.iai.ilcd.service.task.push;

import de.fzk.iai.ilcd.service.client.ILCDServiceClientException;
import de.fzk.iai.ilcd.service.client.impl.FileParam;
import de.fzk.iai.ilcd.service.client.impl.ILCDClientResponse;
import de.fzk.iai.ilcd.service.client.impl.ILCDNetworkClient;
import de.iai.ilcd.model.common.*;
import de.iai.ilcd.model.dao.DataSetDaoType;
import de.iai.ilcd.model.dao.DependenciesMode;
import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.PushConfigDao;
import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.security.User;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.rest.util.NotAuthorizedException;
import de.iai.ilcd.service.queue.TaskProcessingStrategy;
import de.iai.ilcd.service.task.TaskResult;
import de.iai.ilcd.service.task.TaskRunException;
import de.iai.ilcd.service.task.TaskStatus;
import de.iai.ilcd.service.task.abstractTask.AbstractDataSetLoggingTask;
import de.iai.ilcd.service.util.JobState;
import de.iai.ilcd.service.util.JobType;
import de.iai.ilcd.util.DependenciesUtil;
import de.iai.ilcd.util.result.DataSetResult;
import de.iai.ilcd.util.result.ResultType;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class PushDataStockTask extends AbstractDataSetLoggingTask<PushDataStockTaskConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushDataStockTask.class);

    /**
     * Prefix for REST endpoint urls
     */
    private static final String REST_SERVLET_PREFIX = "resource/";

    /**
     * The network client for network connection to target node
     */
    protected static ILCDNetworkClient client = null;
    /**
     * Uuids of data sets which are already known to be pushed or scheduled to be pushed.
     * These will e.g. be ignored when (iteratively) fetching dependencies.
     */
    private final Set<String> knownReferences = new HashSet<>();
    private Instant startProcessingInstant;

    public PushDataStockTask(PushDataStockTaskConfig data, User signee) {
        super(data, signee);
    }

    @Override
    protected String getDescription(PushDataStockTaskConfig data) {
        PushConfig pushConfig = data.getPushConfig();
        AbstractDataStock source = pushConfig.getSource();
        PushTarget target = pushConfig.getTarget();

        return "Pushing from data stock '" +
                source.getName() +
                "' (" +
                source.getUuidAsString() +
                ") to data stock '" +
                target.getTargetDsName() +
                "' (" +
                target.getTargetDsUuid() +
                ") in " +
                target.getTargetID();
    }

    @Override
    protected TaskProcessingStrategy determineProcessingStrategy(PushDataStockTaskConfig data) {
        return TaskProcessingStrategy.BACKGROUND_QUEUE;
    }

    /**
     * Make sure the involved entities are detached to begin with.
     *
     * @param data   the task data
     * @param signee the user who ordered the task done
     */
    @Override
    protected void preProcess(PushDataStockTaskConfig data, User signee) {
        super.preProcess(data, signee);
        EntityManager em = PersistenceUtil.getEntityManager();
        AbstractDataStock sourceStock = data.getPushConfig().getSource();
        em.detach(sourceStock);
    }

    @Override
    protected TaskResult process(PushDataStockTaskConfig data) throws TaskRunException {
        startProcessingInstant = Instant.now();
        PushConfig pushConfig = data.getPushConfig();
        String password = data.getPassword();
        TaskStatus taskStatus;

        // The set up
        String baseUrl = pushConfig.getTarget().getTargetURL();
        if (!pushConfig.getTarget().getTargetURL().endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        if (!baseUrl.endsWith(REST_SERVLET_PREFIX))
            baseUrl += REST_SERVLET_PREFIX;

        try {
            client = new ILCDNetworkClient(baseUrl, pushConfig.getTarget().getLogin(), password);

            if (!client.getAuthenticationStatus().isAuthenticated()) {
                LOGGER.warn("Is not authenticated.");
                throw new NotAuthorizedException();
            }

            // Now we can push the stock
            pushDataStock(pushConfig, password);
            taskStatus = TaskStatus.COMPLETE;

        } catch (ILCDServiceClientException | NotAuthorizedException e) {
            throw new TaskRunException(this.getClass(), e);
        }

        return new TaskResult(taskStatus);
    }

    private void pushDataStock(PushConfig pushConfig, String password) {
        final var sourceStock = pushConfig.getSource();

        final var datasetsToBePushed = new HashSet<DataSetReference>();
        for (final var ilcdType : DataSetDaoType.values()) {
            final var dao = ilcdType.getDao();
            datasetsToBePushed.addAll(dao.get(sourceStock).stream()
                    .filter(Objects::nonNull)
                    .map(DataSetReference::new)
                    .collect(Collectors.toSet()));
        }

        // push each reference
        DependenciesMode depMode = pushConfig.getDependenciesMode();
        String targetStockUuid = pushConfig.getTarget().getTargetDsUuid();
        for (DataSetReference ref : datasetsToBePushed) {
            DataSetResult result = pushDataSetReference(ref, depMode, targetStockUuid);
            knownReferences.add(ref.getUuidAsString());
            log(result);
        }
    }

    /**
     * Pushes a data set and (according to the provided dependency mode) its dependencies.
     *
     * @param ref             data set reference
     * @param depMode         dependency mode
     * @param targetStockUuid uuid of target stock
     * @return the data set result (including dependency results)
     */
    private DataSetResult pushDataSetReference(DataSetReference ref, DependenciesMode depMode, String targetStockUuid) {
        DataSetResult result = new DataSetResult(ref, null);

        // Collect dependency references
        Set<DataSetReference> dependencies = DependenciesUtil
                .getDependencyCluster(ref.getDaoType().getDao().getByDataSetId(ref.getId()), depMode,
                        true, knownReferences)
                .stream()
                .map(DataSetReference::new)
                .collect(Collectors.toSet());

        // Push dependencies
        ResultType resultType;
        for (DataSetReference dep : dependencies) {
            resultType = pushDataSetReference(dep, targetStockUuid, true);
            result.addDependencyResult(dep, resultType);
            knownReferences.add(dep.getUuidAsString());
        }

        // Push 'root' data set
        resultType = pushDataSetReference(ref, targetStockUuid, false);
        if (resultType != null &&
                (result.getResultType() == null || resultType.getSeverity() >= result.getResultType().getSeverity()))
            result.setResultType(resultType);
        knownReferences.add(ref.getUuidAsString());

        return result;
    }

    /**
     * Pushes a single data set.
     *
     * @param ref             reference for the data set to be pushed
     * @param targetStockUuid uuid of the target stock
     * @param isDependency    dependency flag, solely for logging purposes
     * @return result type
     */
    private ResultType pushDataSetReference(DataSetReference ref, String targetStockUuid, boolean isDependency) {
        String dataSetUuid = ref.getUuidAsString();
        String dataSetVersion = ref.getVersion();

        // Get the entity
        DataSet dataSet;
        EntityManager em = PersistenceUtil.getEntityManager();
        dataSet = em.find(ref.getDataSetClass(), ref.getId());
        em.detach(dataSet);

        // Check whether data set exists
        Class clazs = dataSet.getDataSetType().getILCDClass();
        boolean exists = client.existsDataSet(clazs, dataSetUuid, dataSetVersion);
        LOGGER.debug((isDependency ? "Dependency " : "Data set ") +
                ref.toLogString() +
                " already exists in target? " + exists);

        // Push if necessary
        if (exists) {
            return ResultType.REDUNDANT;

        } else {
            ILCDClientResponse response;

            // Obtain input stream
            InputStream is;
            try {
                is = IOUtils.toInputStream(dataSet.getXmlFile().getContent(), "UTF-8");
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Obtained input stream.");
                }
            } catch (IOException ioe) {
                LOGGER.error("Could not obtain input stream for xml file of " + ref.toLogString());
                return ResultType.ERROR;
            }

            // Send PUT request for data set
            boolean check = dataSet instanceof Source;
            if (check) {
                response = pushSource(dataSet, is, targetStockUuid);
            } else {
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("Sending PUT request via service API...");
                response = client.putDataSetAsStream(clazs, is, targetStockUuid);
            }

            // interpret and trace respose
            boolean success = response != null && response.getStatus() == 200;
            if (!success &&
                    (!isDependency || LOGGER.isTraceEnabled())) {
                final var msgB = new StringBuilder("ERROR: Failed to push dataset to stock '")
                        .append(targetStockUuid)
                        .append("': ")
                        .append(ref.toLogString());

                if (response != null) {
                    msgB.append(System.lineSeparator())
                            .append("Response: ")
                            .append(response.getClientResponse());
                } else {
                    msgB.append(" (Response is null)");
                }
                LOGGER.error(msgB.toString());
            } else if (success && !isDependency && LOGGER.isDebugEnabled())
                LOGGER.debug("SUCCESS: Successfully pushed " + ref.toLogString());
            else if (success && isDependency && LOGGER.isTraceEnabled())
                LOGGER.trace("SUCCESS: Successfully pushed " + ref.toLogString());

            return success ? ResultType.SUCCESS : ResultType.ERROR;
        }
    }

    /**
     * Collections digital files associated with the (source) data set
     *
     * @param dataSet         the whole data set
     * @param is              input stream of xml data set
     * @param targetStockUuid uuid of target stock
     * @return the client response
     */
    private ILCDClientResponse pushSource(DataSet dataSet, InputStream is, String targetStockUuid) {
        if (!(dataSet instanceof Source))
            throw new IllegalArgumentException("Expected a data set of data set type source.");

        Source source = (Source) dataSet;
        List<DigitalFile> files;
        files = source.getFilesAsList();

        List<FileParam> fileParams = new ArrayList<>();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Data set is source file with " + files.size() + " digital files.");
        }
        for (DigitalFile file : files) {
            LOGGER.trace("Checking each file");
            try {
                FileParam fileParam = new FileParam(file.getFileName(),
                        new FileInputStream(file.getAbsoluteFileName()));
                fileParams.add(fileParam);
            } catch (FileNotFoundException fnfe) {
                LOGGER.warn("File was not found: " + file.getFileName());
                if (LOGGER.isDebugEnabled()) {
                    fnfe.printStackTrace();
                }
            }
        }
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Sending PUT request via service API...");
        return client.putSourceDataSetAsStream(is, targetStockUuid, fileParams);
    }

    @Override
    protected TaskResult postProcessFinally(PushDataStockTaskConfig data, User signee, TaskResult taskResult) {
        // At last, we need to update the push config
        PushConfigDao pushConfigDao = new PushConfigDao();
        PushConfig pushConfigLatest = pushConfigDao.getById(data.getPushConfig().getId());
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("lastPushDate to be persisted in push config: " + Date.from(startProcessingInstant));
        pushConfigLatest.setLastPushDate(Date.from(startProcessingInstant));
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("job state to be persisted in push config: " + JobState.fromTaskStatus(taskResult.getStatus()));
        pushConfigLatest.setLastJobState(JobState.fromTaskStatus(taskResult.getStatus()));
        try {
            pushConfigDao.merge(pushConfigLatest);

        } catch (MergeException e) {
            getRunLog().append("Push has gone through.");
            getRunLog().append(System.getProperty("line.separator"));
            getRunLog().append("There has, however, been a problem updating the push config.");
        }

        publishLog(true);

        return taskResult;
    }

    @Override
    protected JobType determineJobType(PushDataStockTaskConfig data) {
        return JobType.PUSH;
    }

}
