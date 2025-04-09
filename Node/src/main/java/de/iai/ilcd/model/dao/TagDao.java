package de.iai.ilcd.model.dao;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.tag.Tag;
import de.iai.ilcd.persistence.PersistenceUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;
import java.util.stream.Collectors;

public class TagDao extends AbstractLongIdObjectDao<Tag> {

    Logger logger = LogManager.getLogger(TagDao.class);

    public TagDao() {
        super(Tag.class.getSimpleName(), Tag.class);
    }

    public List<Tag> getTagsAsc() {
        EntityManager em = PersistenceUtil.getEntityManager();
        return em.createQuery("SELECT a FROM " + this.getJpaName() + " a ORDER BY a.name ASC").getResultList();
    }

    public Tag getTagByName(String name) {
        for (Tag t : this.getAll()) {
            if (t.getName().equals(name)) {
                return t;
            }
        }
        return null;
    }

    public List<Tag> findExistingByNames(Collection<String> names) {
        if (names == null)
            return null;
        if (names.size() == 0)
            return new ArrayList<>();

        List<Tag> result = new ArrayList<>();
        Set<String> nameSet = new HashSet<>(names);
        List<Tag> knownTags = getAll();

        for (Tag t : knownTags) {
            if (nameSet.contains(t.getName())) {
                result.add(t);
                nameSet.remove(t.getName());
            }
        }

        return result;
    }

    public void persistUnknownsByNames(Collection<String> names) throws PersistException {
        if (names == null)
            throw new NullPointerException("Cannot persist null.");
        Set<String> trimmedNames = names.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        if (trimmedNames.isEmpty())
            logger.warn("Attempted to persist tags by name collection, which was actually empty.");

        List<Tag> knownTags = findExistingByNames(trimmedNames);
        Set<Tag> tagsToBePersisted = trimmedNames.stream().map(Tag::new).collect(Collectors.toSet());
        knownTags.forEach(tagsToBePersisted::remove);
        persist(tagsToBePersisted);
    }

    public List<Tag> getPersistedImportTags() {
        Set<String> importTagNames = ConfigurationService.INSTANCE.getImportTagNames();
        if (importTagNames.isEmpty())
            return new ArrayList<>();

        List<Tag> persistedImportTags = findExistingByNames(importTagNames);
        if (importTagNames.size() > persistedImportTags.size())
            logger.warn("Less import tags in db than referenced in config. Either the config doesn't adhere to" +
                    " tag equality standards, or this method is called before persisting.");
        if (importTagNames.size() < persistedImportTags.size())
            logger.error("There are duplicates in the tag table!");

        return persistedImportTags;
    }

    /**
     * Tags the referenced process by configured import tags
     * Manually (=bypassing JPA) inserting ID pairs into the corresponding crossreference table.<br/>
     *
     * @param processIds set of ids referencing processes in the processes table
     */
    public void batchTagProcessesByProcessIds(Set<Long> processIds, Set<Tag> tags) {
        if (processIds == null) {
            throw new IllegalArgumentException("Collection of data set ids can't be null," +
                    " when attempting to tag data sets");
        }
        if (tags == null) {
            throw new IllegalArgumentException("Collection of tags can't be null," +
                    " when attempting to tag data sets");
        }

        final List<Long> processIdsFinal = new ArrayList<>(processIds);
        final List<Tag> tagsFinal = new ArrayList<>(tags);

        if (processIdsFinal.isEmpty()) {
            logger.warn("No processes tagged: No process ids provided.");
            return;
        }
        if (tagsFinal.isEmpty()) {
            logger.warn("No processes tagged: No tags provided.");
            return;
        }

        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        List<Tag> attachedTags = tagsFinal.stream()
                .map(t -> em.find(Tag.class, t.getId()))
                .collect(Collectors.toList());
        for (Long id : processIdsFinal) {
            Process p = em.find(Process.class, id);
            p.addAll(attachedTags);
        }
        tx.commit();
    }
}
