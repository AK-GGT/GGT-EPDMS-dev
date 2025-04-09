package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.api.app.source.SourceDataSet;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.ISourceListVO;
import de.fzk.iai.ilcd.service.model.ISourceVO;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.enums.PublicationTypeValue;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.common.DigitalFile;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.persistence.PersistenceUtil;
import de.iai.ilcd.util.UnmarshalHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.tools.generic.ValueParser;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data access object for {@link Source sources}
 */
public class SourceDao extends DataSetDao<Source, ISourceListVO, ISourceVO> {

    /**
     * Logger
     */
    private static final Logger logger = LogManager.getLogger(SourceDao.class);

    /**
     * Create the source DAO
     */
    public SourceDao() {
        super("Source", Source.class, ISourceListVO.class, ISourceVO.class, DataSetType.SOURCE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDataStockField() {
        return DatasetTypes.SOURCES.getValue();
    }

    public List<Source> getComplianceSystems() {
        return getSources("s.classificationCache='Compliance systems'");
    }

    public List<Source> getDatabases() {
        return getSources("s.classificationCache='Databases'");
    }

    public HashSet<String> getDatabasesUuids() {
        String queryString = "SELECT DISTINCT s.uuid.uuid FROM Source s WHERE s.classificationCache='Databases' ORDER BY s.nameCache";

        EntityManager em = PersistenceUtil.getEntityManager();
        Query q = em.createQuery(queryString, String.class);

        HashSet<String> result = new HashSet<String>();

        result.addAll(q.getResultList());
        return result;
    }

    private List<Source> getSources(String whereClause) {
        List<String> wheres = new ArrayList<>();
        wheres.add(whereClause);

        StringBuilder queryString = new StringBuilder();

        queryString.append("SELECT DISTINCT s FROM ");
        queryString.append(this.getJpaName());
        queryString.append(" s WHERE ");
        queryString.append(whereClause);
        queryString.append(" AND ");
        queryString.append(buildMostRecentVersionsOnlySubQuery("s", this.getJpaName(), null, null, wheres));
        queryString.append(" ORDER BY s.nameCache");

        EntityManager em = PersistenceUtil.getEntityManager();
        Query q = em.createQuery(queryString.toString(), Source.class);

        return q.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preCheckAndPersist(Source source) {
        // nothing to do
    }

    /**
     * Concrete implementation required for saving of digital files
     */
    @Override
    public boolean checkAndPersist(Source source, PersistType pType, PrintWriter out) {
        if (CollectionUtils.isNotEmpty(source.getFiles())) {
            throw new IllegalArgumentException("Source data set contains digial file references, provider required");
        }
        return this.checkAndPersist(source, pType, null, out);
    }

    /**
     * Concrete implementation required for saving of digital files
     *
     * @param source            source data set to persist
     * @param pType             persistence type
     * @param digitFileProvider provider for digital files
     * @param out               output writer
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    public boolean checkAndPersist(Source source, PersistType pType, AbstractDigitalFileProvider digitFileProvider, PrintWriter out) {
        EntityManager em = PersistenceUtil.getEntityManager();
        Source existingSource = this.getByUuidAndVersion(source.getUuid().getUuid(), source.getVersion());
        if (existingSource != null) {
            if (pType == PersistType.ONLYNEW) {
                out.println("Warning: source data set with this uuid already exists in database; will ignore this data set");
                return false;
            }
        }

        EntityTransaction t = em.getTransaction();
        try {
            t.begin();
            if (existingSource != null && (pType == PersistType.MERGE)) {
                // delete first the existing one, we will use the new one
                if (out != null) {
                    out.println("Notice: source data set with this uuid already exists in database; will merge this data set");
                }
                em.remove(existingSource);
                this.deleteDigitalFiles(source);
            }

            em.persist(source);

            t.commit();

            if (!super.setMostRecentVersionFlags(source.getUuidAsString())) {
                return false;
            }

            if (source != null && source.getId() != null && digitFileProvider != null) {
                if (!this.saveDigitalFiles(source, digitFileProvider, out)) {
                    if (out != null) {
                        out.println("Warning: couldn't save all files of this source data set into database file directory: see messages above");
                    }
                }
            }
            return true;

        } catch (Exception e) {
            if (out != null) {
                out.println("Can't save source data file to database because of: " + e.getMessage());
            }
            t.rollback();
            return false;
        }
    }

    /**
     * Save the digital files
     *
     * @param source            source data set
     * @param digitFileProvider provider for digital files
     * @param out               output writer
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    private boolean saveDigitalFiles(Source source, AbstractDigitalFileProvider digitFileProvider, PrintWriter out) {
        EntityManager em = PersistenceUtil.getEntityManager();
        try {
            EntityTransaction t = em.getTransaction();

            try {
                // OK, now let's handle the files if
                // we have files and the source has a valid id
                if (CollectionUtils.isNotEmpty(source.getFiles()) && source.getId() != null && source.getId() > 0) {
                    // first let's check if the source has already a file directory to save binary files
                    Path dir = Paths.get(source.getFilesDirectory()).toAbsolutePath();

                    if (!Files.exists(dir)) {
                        Files.createDirectories(dir); // OK, create the directory and all parents
                    }

                    // OK, now that we have verified that the directory exists,
                    // we copy the files to the directory
                    logger.debug("source dataset has {} attachments", source.getFiles().size());

                    for (DigitalFile digitalFile : source.getFiles()) {
                        t.begin();
                        digitalFile = em.merge(digitalFile);

                        String sourcePath = digitalFile.getFileName();
                        logger.info("have to save digital file {}", sourcePath);
                        logger.debug("saving to {}", FilenameUtils.concat(dir.toString(), digitFileProvider.getBasename(sourcePath)));

                        if (digitFileProvider.hasDigitalFile(sourcePath)) {
                            try (
                                    FileOutputStream outStream = new FileOutputStream(FilenameUtils.concat(dir.toString(), digitFileProvider.getBasename(sourcePath)));
                                    // only valid for files < 2GB, but should be OK for the time being
                                    InputStream inStream = digitFileProvider.getInputStream(sourcePath)) {

                                if (IOUtils.copy(inStream, outStream) > -1) {
                                    logger.debug("setting filename to {}", digitFileProvider.getBasename(sourcePath));
                                    // replace name in digitalFile with just the name of the file
                                    digitalFile.setFileName(digitFileProvider.getBasename(sourcePath));
                                } else {
                                    if (out != null) {
                                        out.println("cannot copy digital file reference '" + sourcePath + "' of source data set '" + source.getName().getDefaultValue() + "' to database file firectory");
                                    }
                                    logger.error("cannot copy digital file reference '{}' to source directory {}", sourcePath, dir);
                                }
                            }
                        } else {
                            // maybe better use org.apache.commons.validator.UrlValidator (1.3.1) or even
                            // org.apache.commons.validator.routines.UrlValidator (1.4.1)
                            if (!sourcePath.startsWith("http:") && !sourcePath.startsWith("https:")) {
                                // there are sometimes URL refs in source which don't have http:// prepended
                                if (!sourcePath.matches(".+\\....") && sourcePath.contains(".")) {
                                    // looks like a URL with no http:// in front; try to fix that
                                    digitalFile.setFileName(ConfigurationService.INSTANCE.getHttpPrefix() + sourcePath);
                                } else {
                                    // we have a file which we cannot find
                                    String fileName = FilenameUtils.getName(sourcePath);
                                    digitalFile.setFileName(fileName);
                                    if (out != null)
                                        out.println("Warning: digital file '" + fileName + "' of source data set '" + source.getName().getDefaultValue() + "' cannot be found");
                                    logger.warn("Warning: digital file reference '{}' of source data set {} cannot be found; will be ignored", sourcePath, source.getName().getDefaultValue());
                                }
                            }
                        }
                        t.commit();
                    }

                    // delete empty (unnecessary) directory
                    boolean directoryEmpty;
                    try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir)) {
                        directoryEmpty = !paths.iterator().hasNext();
                    }
                    if (directoryEmpty) {
                        Files.delete(dir);
                        logger.info("### Directory '" +
                                dir +
                                "' was empty and has been deleted.");
                    }
                }
            } catch (Exception e) {
                // OK, let's delete the digital files and rollback the whole transaction to remove database items
                logger.error("cannot save digital file", e);
                if (t.isActive()) {
                    t.rollback();
                }
                this.deleteDigitalFiles(source);
                return false;
            }
        } finally {
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Source remove(Source source) throws DeleteDataSetException {
        if (source == null || source.getId() == null) {
            return null;
        }
        Source tmp = super.remove(source);
        this.deleteDigitalFiles(tmp);
        return tmp;
    }

    private void deleteDigitalFiles(Source source) {
        if (source != null && (source.getFiles().size() > 0) && (source.getId() > 0)) { // we have files and the source has a valid id
            // first let's check if the source
            // already has a file directory to save binary files
            String directoryPath = source.getFilesDirectory();
            Path directory = Paths.get("/").resolve(directoryPath);

            if (Files.exists(directory)) {
                try (Stream<Path> paths = Files.walk(directory)) {

                    paths.sorted(Comparator.reverseOrder()) // Go from bottom to top
                            .forEach(p -> {
                                try {
                                    Files.delete(p);
                                    logger.info("Deleted " + p);
                                } catch (Exception e) {
                                    logger.info("Failed to delete " + p);
                                }
                            });
                } catch (IOException ex) {
                    // we will ignore this because we tried the best to delete everything
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addWhereClausesAndNamedParamesForQueryStringJpql(String typeAlias, ValueParser params,
                                                                    List<String> whereClauses,
                                                                    Map<String, Object> whereParamValues) {
        String type = params.getString("type");
        if (type != null && (type.length() > 3) && (!type.equals("select option"))) {
            PublicationTypeValue typeValue = null;
            try {
                typeValue = PublicationTypeValue.valueOf(type);
            } catch (Exception e) {
                // ignore it as we do not have a parsable value
            }
            if (typeValue != null) {
                whereClauses.add(typeAlias + ".type=:typeOfSrc");
                whereParamValues.put("typeOfSrc", typeValue);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getQueryStringOrderJpql(String typeAlias, String sortString, boolean sortOrder) {
        if ("publicationType.value".equals(sortString)) {
            return buildOrderBy(typeAlias, "publicationType", typeAlias, "nameCache", sortOrder);
        } else {
            return super.getQueryStringOrderJpql(typeAlias, sortString, sortOrder);
        }
    }

    /* (non-Javadoc)
     * @see de.iai.ilcd.model.dao.DataSetDao#getDependencies(de.iai.ilcd.model.common.DataSet, de.iai.ilcd.model.dao.DependenciesMode)
     */
    @Override
    public Set<DataSet> getDependencies(DataSet dataset, DependenciesMode mode) {
        return getDependencies(dataset, mode, null);
    }

    @Override
    public Set<DataSet> getDependencies(DataSet dataSet, DependenciesMode depMode, Collection<String> ignoreList) {
        Set<DataSet> dependencies = new HashSet<>();
        Source source = (Source) dataSet;
        RootDataStock stock = source.getRootDataStock();
        final List<String> ignoreListFinal = ignoreList != null ? new ArrayList<>(ignoreList) : new ArrayList<>();

        //contacts
        for (IGlobalReference ref : source.getContacts())
            if (!ignoreListFinal.contains(ref.getRefObjectId()))
                addDependency(null, ref, stock, dependencies);

        SourceDataSet xmlDataset = (SourceDataSet) new UnmarshalHelper().unmarshal(source);

        try {
            List<GlobalReferenceType> refs = xmlDataset.getAdministrativeInformation().getDataEntryBy()
                    .getReferenceToDataSetFormat()
                    .stream()
                    .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                    .collect(Collectors.toList());
            addDependencies(refs, stock, dependencies);
        } catch (Exception e) {
        }

        try {
            GlobalReferenceType ref = xmlDataset.getAdministrativeInformation().getPublicationAndOwnership()
                    .getReferenceToOwnershipOfDataSet();
            if (ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                addDependency(ref, stock, dependencies);
        } catch (Exception e) {
        }

        return dependencies;
    }

    @Override
    public Source getSupersedingDataSetVersion(String uuid) {
        // TODO Auto-generated method stub
        return null;
    }

}
