package de.iai.ilcd.util.url;

import de.iai.ilcd.model.common.DataSetType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;

class LocalURLGenerator {

    private static final String AND = "&amp;";

    /**
     * Resolves (local) URL for a single digital file of a given source data set.
     *
     * @param uuid    of the source data set
     * @param version of the source data set
     * @return url to (local) resource file
     */
    URL composeResourceFileURL(@Nonnull URL baseURL, @Nonnull final String uuid, @Nullable final String version,
                               @Nonnull final String fileName) {
        StringBuilder sb = new StringBuilder(baseURL.toString());
        sb.append("resource/");
        sb.append(DataSetType.SOURCE.getStandardFolderName()).append("/");
        sb.append(uuid).append("/");
        sb.append(fileName);
        if (version != null)
            sb.append("?version=").append(version);

        URL url;
        try {
            url = new URL(sb.toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    /**
     * Resolves the (local) URL for the details of a referenced data set.
     *
     * @param baseURL        base url
     * @param typeFolderName The standard folder name that will be used as path parameter (This depends on the type of the data set, see {@link DataSetType#getStandardFolderName()}).
     * @param uuid           The uuid of the required data set
     * @param version        version of the required data set.
     * @param formatKey      determines the type of result, e.g. html, json, xml (see values of {@link LocalURLs.FORMAT_KEY})
     * @return (local) URL to data set resource
     * @throws IllegalArgumentException if UUID is blank. (Data set type and uuid are mandatory.)
     */
    URL composeDataSetDetailsURL(@Nonnull URL baseURL, @Nonnull String typeFolderName, @Nonnull String uuid, @Nullable String version, @Nonnull String formatKey) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder(baseURL.toString());
        sb.append("resource/");
        sb.append(typeFolderName).append("/");
        sb.append(uuid).append("?");
        if (version != null) {
            sb.append("version=").append(version).append(AND);
        }
        sb.append("format=").append(formatKey);

        try {
            return new URL(sb.toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
