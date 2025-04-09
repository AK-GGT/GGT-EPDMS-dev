package de.iai.ilcd.service.task.dataSetAssignment;

import de.iai.ilcd.model.common.DataSetReference;
import de.iai.ilcd.service.task.TaskRunException;
import de.iai.ilcd.util.result.DataSetResult;
import de.iai.ilcd.util.result.ResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class BatchProcessor {

    private static final int PROCESSING_BATCH_SIZE = 50;
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessor.class);
    private final Consumer<Set<DataSetResult>> publisher;

    //////////////////////////
    // Pre-processing state //
    //////////////////////////
    /**
     * Caching root data sets that are to be processed
     */
    private final Set<DataSetReference> rootReferences = new HashSet<>();

    /**
     * Caching dependencies that are to be processed
     */
    private final Map<DataSetReference, Set<DataSetReference>> dependenciesByRootReference = new HashMap<>();

    ///////////////////////////
    // Post-processing state //
    ///////////////////////////

    /**
     * Caching data set results, indexed by uuid of the root data set.
     * This is necessary since completing each result may be achieved in multiple steps.
     */
    private final Map<DataSetReference, DataSetResult> resultsByRootReference = new HashMap<>();

    /**
     * Data set results concerning these keys are ready to be published.
     */
    private final Set<DataSetReference> publishableResultKeys = new HashSet<>();

    /////////////////////////////////////////
    // Blacklist for fetching dependencies //
    /////////////////////////////////////////

    /**
     * The references of all root data set references that have at some point been in the cache are kept here.
     * This helps to avoid redundant processing, e.g. when fetching dependencies.
     */
    private final Set<DataSetReference> knownReferences = new HashSet<>();

    private final AssignUnassignBatchMethod batchMethod;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    BatchProcessor(AssignUnassignBatchMethod batchMethod, Consumer<Set<DataSetResult>> publisher) {
        super();
        this.publisher = publisher;
        this.batchMethod = batchMethod;
    }

    /////////////// METHODS ///////////////

    Set<DataSetReference> getKnownReferences() {
        return knownReferences;
    }

    /////////////////
    // Consumption //
    /////////////////

    void consumeEverything() throws TaskRunException {
        while (hasNextBatch())
            consumeBatch(collectBatch());
    }

    /**
     * This method collects batches of data sets, processes them and publishes publishable results.*
     */
    private void consumeFullBatches() throws TaskRunException {
        while (hasNextFullBatch()) {
            consumeBatch(collectBatch());
        }
    }

    /**
     * Processes list of references and publishes results
     *
     * @param batch list of references
     */
    void consumeBatch(List<RootedDataSetReference> batch) throws TaskRunException {
        if (Thread.currentThread().isInterrupted())
            throw new TaskRunException("Interrupted.");

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Processing Batch...");

        // We need extract the references.
        // Assignment doesn't care about root data sets and such.
        Set<DataSetReference> simpleRefs = batch.stream()
                .map(RootedDataSetReference::getDataSetReference)
                .collect(Collectors.toSet());

        // Do things
        ResultType resultType = batchMethod.applyTo(simpleRefs);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("..." + resultType.name());

        // store results
        storeResults(batch, resultType);

        // Some results may be ready to publish
        publish();
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Publishing " + publishableResultKeys.size() + " result(s).");
    }

    /**
     * Iterates over all cached data set references and collects a batch of (rooted)
     * data set references.<br/>
     * Note: collecting here means 'moving', i.e. the reference are removed from the cache.
     *
     * @return batch of rooted data set references
     */
    private List<RootedDataSetReference> collectBatch() {
        Iterator<DataSetReference> rootIterator = rootReferences.iterator();
        List<RootedDataSetReference> batch = new ArrayList<>();

        while (rootIterator.hasNext() && batch.size() < PROCESSING_BATCH_SIZE) {
            DataSetReference rootRef = rootIterator.next();

            // Dependencies first. We will only process data sets after
            // all their dependencies have been processed - to avoid redundancies
            boolean allDependenciesCollected = collectCachedDependenciesForBatch(batch, rootRef);
            if (allDependenciesCollected && batch.size() <= PROCESSING_BATCH_SIZE - 1) {
                batch.add(new RootedDataSetReference(rootRef, false, rootRef));
                // all references for the given root uuid are used, after processing the result will be publishable
                publishableResultKeys.add(rootRef);
            }
        }

        return batch;
    }

    /**
     * Adds to a given collection all dependencies associated with the provided uuid until either the dependencies
     * are depleted or the batch-size is reached.<br/>
     *
     * @param collectingBatch      the collection to which we will add dependencies
     * @param rootDataSetReference the data set reference for which we fetch dependencies
     * @return true if all dependencies associated with the provided data set reference are depleted.
     */
    private boolean collectCachedDependenciesForBatch(Collection<RootedDataSetReference> collectingBatch, DataSetReference rootDataSetReference) {
        Iterator<DataSetReference> itr = dependenciesByRootReference.get(rootDataSetReference).iterator();

        while (collectingBatch.size() <= PROCESSING_BATCH_SIZE && itr.hasNext()) {
            collectingBatch.add(new RootedDataSetReference(rootDataSetReference, true, itr.next()));
            itr.remove();
        }

        return !itr.hasNext();
    }

    boolean hasNextFullBatch() {
        return getCountOfUnprocessedReferences() >= PROCESSING_BATCH_SIZE;
    }

    boolean hasNextBatch() {
        return getCountOfUnprocessedReferences() > 0;
    }

    private int getCountOfUnprocessedReferences() {
        int rootDataSetCount = rootReferences.size();
        int dependencyCount = 0;
        for (Set<DataSetReference> deps : dependenciesByRootReference.values())
            dependencyCount += deps.size();
        int cacheCount = rootDataSetCount + dependencyCount;
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Counting: preProcessed: " + cacheCount + " (roots=" + rootDataSetCount + " + deps=" + dependencyCount +
                    ") -- processed: " + (knownReferences.size() - cacheCount));
        return cacheCount;
    }

    /////////////////////
    // Post-processing //
    /////////////////////

    /**
     * All data set references are assigned the provided resultType in the corresponding DataSetResult object.
     *
     * @param rootedDataSetReferences the rooted data set references
     * @param resultType              the result type
     */
    private void storeResults(Collection<RootedDataSetReference> rootedDataSetReferences, ResultType resultType) {
        // Helper map to only look up a result twice per dependency cluster instead of once per dependency
        // (batches are relatively small, the cache may be huge.)
        Map<DataSetReference, Set<DataSetReference>> tmp = new HashMap<>();
        for (RootedDataSetReference rr : rootedDataSetReferences) {
            DataSetReference rrRootRef = rr.getRootDataSetReference();

            if (rr.isDependency()) {
                tmp.computeIfAbsent(rrRootRef, k -> new HashSet<>());
                tmp.get(rr.getRootDataSetReference()).add(rr.getDataSetReference());
            } else {
                // We can already store the root result
                DataSetResult result = resultsByRootReference.get(rr.getDataSetReference());
                result.setResultType(resultType);
            }
        }

        for (Map.Entry<DataSetReference, Set<DataSetReference>> e : tmp.entrySet()) {
            Collection<DataSetResult> depResults = e.getValue().stream()
                    .map(ref -> new DataSetResult(ref, resultType))
                    .collect(Collectors.toList());
            resultsByRootReference.get(e.getKey()).addAllDependencyResults(depResults);
        }

        // And finally, references for publishable results (i.e. clusters that
        // have been completely processed already) shouldn't be tracked any longer.
        rootReferences.removeAll(publishableResultKeys);
        publishableResultKeys.forEach(dependenciesByRootReference::remove);
    }

    /**
     * Publishes, according to the injected publisher, all DataSetResult objects that are considered publishable.
     */
    void publish() {
        Set<DataSetResult> publishableResults = publishableResultKeys.stream()
                .map(resultsByRootReference::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        publisher.accept(publishableResults);
        publishableResultKeys.clear();
    }

    ////////////////////
    // Pre-processing //
    ////////////////////

    /**
     * Register the rootReference, and it's dependencies to be processed.
     *
     * @param rootReference        the reference for the root data set of the dependency cluster (resutls increment counters)
     * @param dependencyReferences references for the dependencies (results don't increment counters)
     * @throws TaskRunException if the current thread has been interrupted.
     */
    void addDependencyCluster(DataSetReference rootReference, Set<DataSetReference> dependencyReferences) throws TaskRunException {
        // Nonsense handling
        if (rootReference == null)
            throw new IllegalArgumentException("Root reference can't be null");
        dependencyReferences = dependencyReferences != null ? dependencyReferences : new HashSet<>();

        // cache references
        rootReferences.add(rootReference);
        dependenciesByRootReference.put(rootReference, dependencyReferences);


        // initialize result object (will receive and hold results until publishable (i.e. all associated references
        // have been processed)
        resultsByRootReference.put(rootReference, new DataSetResult(rootReference, null));

        // We will blacklist the retrieved data sets s.t they will be ignored when fetching further dependencies.
        knownReferences.add(rootReference);
        // Warning: Technically one loses depth when black-listing dependencies
        // To avoid this, one would have to black-list (and retrieve!) dependencies together with
        // the depth they were retrieved at -- This would introduce some more code complexity, which
        // at the time of writing seems unnecessary. Furthermore, it may easily slow things down by
        // factor 10 or more.
        // If it should, however, at some point become an issue an alternative solution would be to stop blacklisting
        // dependencies i.e. commenting out the next line of code...
        // (Please, too, note that, of course, blacklisting root data sets is fine i.e. no depth is lost!)
        knownReferences.addAll(dependencyReferences);

        consumeFullBatches();
    }

}
