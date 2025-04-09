package de.iai.ilcd.util;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.DependenciesMode;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import de.iai.ilcd.model.process.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DependenciesUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependenciesUtil.class);

    private static final Predicate<DataSet> noProcesses = ds -> !(ds instanceof Process); // to filter out processes

    private static final Predicate<DataSet> allDataSetsOk = ds -> true; // the alternative: all data set (types) are fine

    private static final Function<DataSet, Integer> guessDepth = dataSet ->
            (dataSet instanceof Process || dataSet instanceof LifeCycleModel) ? 3 : 2;

    /**
     * Given a data set we build a set of dependencies up to a given depth.
     * Note that the data set itself is considered a dependency, too.
     * <br/>
     * Example depth 2 means: we include data set a, its dependencies b,c and
     * their dependencies d,e and f,g respectively.
     * <pre>
     *
     * 		   a
     * 		    ├── b
     * 		    │   ├── d
     * 		    │   └── e
     * 		    └── c
     * 		        ├── f
     * 		        └── g
     * </pre>
     *
     * @param dataSet       the data set to fetch dependencies for
     * @param depMode       the dependencies mode, according to which dependencies are fetched
     * @param depth         the number of iterations
     * @param uuidBlacklist Set of uuids which will be used to exclude dependencies from the iteration and the result.
     */
    public static Set<DataSet> getDependencyCluster(final DataSet dataSet, final DependenciesMode depMode,
                                                    final int depth, final Set<String> uuidBlacklist) {
        Instant start = null;
        if (LOGGER.isTraceEnabled())
            start = Instant.now();

        Set<DataSet> result = new HashSet<>();
        final Set<String> uuidBlacklistFinal = uuidBlacklist != null ? new HashSet<>(uuidBlacklist) : new HashSet<>();

//		Given the dataSet 'a' we fetch its dependencies b and c.
//		a
//		┣━━━ b
//		┗━━━ c
//		The difference between 'a','b' and 'c' is now, that for 'a' we have already fetched dependencies.
//		That's why we will collect them in two different sets.
//		Note:
//		In the next iteration step we fetch dependencies for the "peripheral" data sets. Some references, however,
//		may point towards the 'interior' of the cluster, e.g. 'b' points to 'a'.
//		┏━━━┓
//		a	┃
//		├── b
//		│   ┣━━━ d
//		│   ┗━━━ e
//		└── c
//			┣━━━ f
//			┗━━━ g
//		While, in these constellations we can't easily keep the Daos from resolving references that are already
//		known to us, we must at least make sure to only fetch dependencies for "new" data sets, here, for instance,
//		only for 'd','e','f' and 'g'.

        Set<DataSet> crawledDataSets = new HashSet<>(); // data sets for which we already have fetched dependencies
        Set<DataSet> uncrawledDataSets = new HashSet<>(); // data sets for which we haven't fetched dependencies (yet)
        uncrawledDataSets.add(dataSet); // We haven't crawled our "root" data set yet.

        if (DependenciesMode.NONE.equals(depMode))
            return uncrawledDataSets; // Just the 'root' data set.

        // We may need a process-filter, depending on the config
        boolean isGloballyIgnoreProcessDependencies = ConfigurationService.INSTANCE.isDependenciesIgnoreProcesses();
        Predicate<DataSet> typeFilter = isGloballyIgnoreProcessDependencies ? noProcesses : allDataSetsOk;
        Predicate<DataSet> blacklistFilter = ds -> !uuidBlacklistFinal.contains(ds.getUuidAsString()); // true: data set is
        // not blacklisted

        for (int i = 0; i < depth; i++) {
            final int currentDepth = i;
            if (LOGGER.isTraceEnabled())
                LOGGER.trace("For " + dataSet.getUuidAsString() +
                        ", step " + (currentDepth + 1) +
                        ", Crawling " + uncrawledDataSets.size() +
                        " data sets.");

            crawledDataSets.addAll(uncrawledDataSets); // the new inner cluster
            uncrawledDataSets = uncrawledDataSets.stream()
                    .map(ds -> ds.getCorrespondingDSDao().getDependencies(ds, depMode, uuidBlacklistFinal).stream()
                            .filter(currentDepth == 0 && DataSetType.LIFECYCLEMODEL.equals(ds.getDataSetType()) ?
                                    allDataSetsOk : typeFilter) // LifeCycleModels always need process deps in depth 1
                            .filter(blacklistFilter)
                            .collect(Collectors.toSet()))
                    .flatMap(Set::stream) // merge all found dependency sets into one collection (actually stream)
                    .distinct() // the next filter is expensive, so let's fix that our stream may contain duplicates
                    .filter(ds -> !crawledDataSets.contains(ds)) // leave only the 'new' data sets as periphery
                    .collect(Collectors.toSet());

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Finished. Now counting " + (crawledDataSets.size() + uncrawledDataSets.size()) + " data sets.");
            }
        }

        // The result cluster consists of crawled and uncrawled data sets
        result.addAll(crawledDataSets);
        result.addAll(uncrawledDataSets);
        if (start != null)
            LOGGER.trace("Spent " +
                    Duration.between(start, Instant.now()).getSeconds() +
                    " seconds fetching " +
                    (result.size() - 1) +
                    " dependencies.");
        return result;
    }

    /////////////////
    // Overloading //
    /////////////////

    public static Set<DataSet> getDependencyCluster(final DataSet dataSet, final DependenciesMode depMode,
                                                    final Set<String> uuidBlacklist) {
        return getDependencyCluster(dataSet, depMode, guessDepth.apply(dataSet), uuidBlacklist);
    }

    public static Set<DataSet> getDependencyCluster(final DataSet dataSet, final DependenciesMode depMode,
                                                    final int depth, final boolean excludeRootDataSet,
                                                    final Set<String> uuidBlacklist) {
        Set<DataSet> deps = getDependencyCluster(dataSet, depMode, depth, uuidBlacklist);
        if (excludeRootDataSet)
            deps.remove(dataSet);

        return deps;
    }

    public static Set<DataSet> getDependencyCluster(final DataSet dataSet, final DependenciesMode depMode,
                                                    final boolean excludeRootDataSet, final Set<String> uuidBlacklist) {
        return getDependencyCluster(dataSet, depMode, guessDepth.apply(dataSet), excludeRootDataSet, uuidBlacklist);
    }

    public static Set<DataSet> getDependencyCluster(final DataSet dataSet, final DependenciesMode depMode) {
        return getDependencyCluster(dataSet, depMode, guessDepth.apply(dataSet), null);
    }

    public static Set<DataSet> getDependencyCluster(final DataSet dataSet, final DependenciesMode depMode,
                                                    final int depth, final boolean excludeRootDataSet) {
        return getDependencyCluster(dataSet, depMode, depth, excludeRootDataSet, null);
    }

    public static Set<DataSet> getDependencyCluster(final DataSet dataSet, final DependenciesMode depMode,
                                                    final boolean excludeRootDataSet) {
        return getDependencyCluster(dataSet, depMode, excludeRootDataSet, null);
    }

}
