package de.iai.ilcd.util.url;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.*;
import de.iai.ilcd.model.source.Source;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class that gives static access to the LocaLURLGenerator class.<br/>
 * This class purpose is overloading the methods of LocalURLGenerator and handling input validtion + defaults.
 *
 * @see LocalURLGenerator
 */
public final class LocalURLs {

    private static final Logger logger = LogManager.getLogger(LocalURLs.class);
    private static final LocalURLGenerator urlGenerator = new LocalURLGenerator();
    private static URL baseURLCache = null;

    /**
     * Resolves the (local) URL for the details of a data set.
     *
     * @param dataSet   the required data set
     * @param formatKey required format, see {@link FORMAT_KEY}
     * @return (local) URL to data set details resource
     * @throws IllegalArgumentException if uuid or data set type are not initialized or can't be extracted.
     */
    public static URL getDataSetDetailsURL(@Nonnull DataSet dataSet, @Nonnull FORMAT_KEY formatKey) throws IllegalArgumentException {
        return getDateSetDetailsURL(
                dataSet::getDataSetType,
                () -> UUID.fromString(dataSet.getUuid().getUuid()),
                dataSet::getVersion,
                formatKey);
    }

    /**
     * Resolves the (local) URL for the details of a referenced data set.
     *
     * @param ref       Reference to the required data set
     * @param formatKey required format, see {@link FORMAT_KEY}
     * @return (local) URL to the referenced data set details resource
     * @throws IllegalArgumentException if uuid or data set type are not initialized or can't be extracted.
     */
    public static URL getDataSetDetailsURL(@Nonnull GlobalReference ref, @Nonnull FORMAT_KEY formatKey) throws IllegalArgumentException {
        return getDateSetDetailsURL(
                () -> DataSetType.fromValue(ref.getType().getValue()),
                () -> UUID.fromString(ref.getRefObjectId()),
                ref::getVersion,
                formatKey);
    }

    /**
     * Calls the provided suppliers while dealing with illegal arguments and exceptions (setting defaults
     * where available) before resolving a (local) url to the data set details resource.
     *
     * @param dataSetType functional providing the data set type. E.g. '<code>dataSetInstance::getDataSetType</code>'
     * @param uuid        functional providing the uuid of the required data set. E.g. '<code>() -> UUID.fromString(dataSetInstance.getUuid().getUuid())</code>'
     * @param version     functional providing the version of the required data set. E.g. <code>'dataSetInstance::getVersion'</code>
     * @param formatKey   required format, see {@link FORMAT_KEY}
     * @return (local) URL to data set details resource
     * @see LocalURLGenerator#composeDataSetDetailsURL(URL, String, String, String, String) LocalURLs::composeDataSetDetailsURL
     */
    public static URL getDateSetDetailsURL(@Nonnull Supplier<DataSetType> dataSetType, @Nonnull Supplier<UUID> uuid,
                                           @Nullable Supplier<DataSetVersion> version, @Nonnull FORMAT_KEY formatKey) {
        return getDateSetDetailsURL(null, dataSetType, uuid, version, formatKey);
    }

    /**
     * Calls the provided suppliers while dealing with illegal arguments and exceptions (setting defaults
     * where available) before resolving a (local) url to the data set details resource.
     *
     * @param baseURL     base url
     * @param dataSetType functional providing the data set type. E.g. '<code>dataSetInstance::getDataSetType</code>'
     * @param uuid        functional providing the uuid of the required data set. E.g. '<code>() -> UUID.fromString(dataSetInstance.getUuid().getUuid())</code>'
     * @param version     functional providing the version of the required data set. E.g. <code>'dataSetInstance::getVersion'</code>
     * @param formatKey   required format, see {@link FORMAT_KEY}
     * @return (local) URL to data set details resource
     * @see LocalURLGenerator#composeDataSetDetailsURL(URL, String, String, String, String) LocalURLs::composeDataSetDetailsURL
     */
    public static URL getDateSetDetailsURL(@Nullable URL baseURL, @Nonnull Supplier<DataSetType> dataSetType, @Nonnull Supplier<UUID> uuid,
                                           @Nullable Supplier<DataSetVersion> version, @Nonnull FORMAT_KEY formatKey) {
        String uuidString = null;
        DataSetType dsType;
        String versionString = null;
        String formatKeyString;

        try {
            UUID obtained = uuid.get();
            if (obtained != null)
                uuidString = obtained.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to obtain uuid", e);
        }
        if (uuidString == null || uuidString.isBlank()) {
            throw new IllegalArgumentException(String.format("Uuid can't be %s", uuidString == null ? "null" : "blank"));
        }

        if (version != null) {
            try {
                versionString = version.get().getVersionString();
            } catch (Exception e) {
                if (logger.isTraceEnabled())
                    logger.warn(String.format("Failed to obtain version for data set '%s'. Proceeding without version ...", uuidString), e);
            }
        }

        try {
            dsType = dataSetType.get();
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to obtain data set type (data set {uuid='%s', version='%s'})", uuidString, versionString), e);
        }
        if (dsType == null) {
            throw new IllegalArgumentException(String.format("Data set type can't be null (data set {uuid='%s', version='%s'})", uuidString, versionString));
        }

        try {
            formatKeyString = formatKey.getValue();
        } catch (NullPointerException npe) {
            throw new IllegalArgumentException(String.format("Format key can't be null (affected data set {uuid='%s', version='%s'})", uuidString, versionString));
        }

        if (baseURL == null)
            baseURL = getBaseURL();
        else
            baseURL = ensureHasTrailingSlash(baseURL);

        return urlGenerator.composeDataSetDetailsURL(baseURL, dsType.getStandardFolderName(), uuidString, versionString, formatKeyString);
    }

    /**
     * Resolves the (local) LocalURLs for the digital file resources of a source data set.
     * <p><i>
     * Note: The result list will contain null entries if and only if <code>DigitalFile</code> instances
     * hold by the source data set had insufficient or corrupted data.
     * </i></p>
     *
     * @param source the source whose digital file resources should be resolved.
     * @return list of (local) urls pointing to digital files
     * @see LocalURLGenerator#composeResourceFileURL(URL, String, String, String) LocalURLs::composeResourceFileURL
     */
    public static List<URL> getResourceFileURLs(@Nonnull final Source source) {
        Function<DigitalFile, URL> obtainUrlForFileResourceOrNull = df -> {
            try {
                return getResourceFileURL(
                        () -> UUID.fromString(source.getUuid().getUuid()),
                        source::getVersion,
                        df::getFileName);
            } catch (Exception e) {
                logger.warn("Failed to map resourceFile to URL", e);
            }
            return null;
        };

        return source.getFiles().stream()
                .map(obtainUrlForFileResourceOrNull)
                .collect(Collectors.toList());
    }

    /**
     * Calls the provided suppliers while dealing with illegal arguments and exceptions (setting defaults
     * where available) before resolving a (local) url to the file resource of a source data set.
     *
     * @param sourceDataSetUuid    functional providing the uuid of the source data set owning the resource file
     * @param sourceDataSetVersion functional providing the version of the source data set owning the resource file
     * @param filename             functional providing the name of the file resource
     * @return (local) url to resource file data set
     * @throws IllegalArgumentException if uuid or filename fail to provide non-null/non-blank values.
     * @see LocalURLGenerator#composeResourceFileURL(URL, String, String, String) LocalURLs::composeResourceFileURL
     */
    public static URL getResourceFileURL(@Nonnull Supplier<UUID> sourceDataSetUuid, @Nullable Supplier<DataSetVersion> sourceDataSetVersion,
                                         @Nonnull Supplier<String> filename) throws IllegalArgumentException {
        return getResourceFileURL(null, sourceDataSetUuid, sourceDataSetVersion, filename);
    }

    /**
     * Calls the provided suppliers while dealing with illegal arguments and exceptions (setting defaults
     * where available) before resolving a (local) url to the file resource of a source data set.
     *
     * @param baseURL              the base url
     * @param sourceDataSetUuid    functional providing the uuid of the source data set owning the resource file
     * @param sourceDataSetVersion functional providing the version of the source data set owning the resource file
     * @param filename             functional providing the name of the file resource
     * @return (local) url to resource file data set
     * @throws IllegalArgumentException if uuid or filename fail to provide non-null/non-blank values.
     * @see LocalURLGenerator#composeResourceFileURL(URL, String, String, String) LocalURLs::composeResourceFileURL
     */
    public static URL getResourceFileURL(@Nullable URL baseURL, @Nonnull Supplier<UUID> sourceDataSetUuid, @Nullable Supplier<DataSetVersion> sourceDataSetVersion,
                                         @Nonnull Supplier<String> filename) throws IllegalArgumentException {
        String uuidString;
        String versionString = null;
        String fileNameString;

        try {
            uuidString = sourceDataSetUuid.get().toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to obtain uuid", e);
        }
        if (uuidString == null || uuidString.isBlank()) {
            throw new IllegalArgumentException(String.format("Uuid can't be %s", uuidString == null ? "null" : "blank"));
        }

        if (sourceDataSetVersion != null) {
            try {
                DataSetVersion dsVersion = sourceDataSetVersion.get();
                if (dsVersion != null)
                    versionString = dsVersion.getVersionString();

            } catch (Exception e) {
                if (logger.isTraceEnabled())
                    logger.warn(String.format("Failed to obtain version. Proceeding with null as default. (Source data set uuid='%s')", uuidString), e);
            }
        }

        try {
            fileNameString = filename.get();
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to obtain filename (Source data set {uuid='%s', version='%s})", uuidString, versionString), e);
        }
        if (fileNameString == null || fileNameString.isBlank()) {
            throw new IllegalArgumentException(String.format("Filename can't be %s (Source {uuid='%s', version='%s})", fileNameString == null ? "null" : "blank", uuidString, versionString));
        }

        if (baseURL == null)
            baseURL = getBaseURL();
        else
            baseURL = ensureHasTrailingSlash(baseURL);

        return urlGenerator.composeResourceFileURL(baseURL, uuidString, versionString, fileNameString);
    }

    private static URL getBaseURL() {
        if (baseURLCache != null)
            return baseURLCache;

        try {
            baseURLCache = ensureHasTrailingSlash(ConfigurationService.INSTANCE.getBaseURI().toURL());

        } catch (MalformedURLException mfe) {
            throw new RuntimeException(mfe);
        }
        return baseURLCache;
    }

    /////////////////////
    /// PRIVATE UTILS ///
    ////////////////////

    private static URL ensureHasTrailingSlash(URL url) {
        String urlString = url.toString();
        if (!urlString.endsWith("/"))
            urlString += "/";

        try {
            return new URL(urlString);
        } catch (MalformedURLException mfe) {
            throw new RuntimeException(mfe);
        }

    }

    public enum FORMAT_KEY {
        JSON("json"),
        HTML("html"),
        XML("xml");

        private final String value;

        FORMAT_KEY(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
