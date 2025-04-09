package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.dao.MergeException;
import de.iai.ilcd.model.dao.TagDao;
import de.iai.ilcd.model.tag.Tag;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import eu.europa.ec.jrc.lca.commons.view.util.FacesUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.xml.bind.JAXBException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ViewScoped
@ManagedBean(name = "tagHandler")
public class TagHandler extends AbstractHandler implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -3788672879147396652L;

    public static Logger logger = LogManager.getLogger(TagHandler.class);

    private TagDao tDao;

    private Tag selectedTag;

    private int selectedRow = -1;

    private String selectedColumn;

    private boolean dirty = false;

    private List<Tag> tags;

    private List<Tag> tagsToDelete = new ArrayList<Tag>();

    public TagHandler() {
        tDao = new TagDao();
        tags = tDao.getTagsAsc();
    }

    public List<Tag> getAllTags() {
        return this.tags;
    }

    // delete later if not needed
    public boolean addTags(List<Tag> tags) {
        try {
            tDao.merge(tags);
            return true;
        } catch (MergeException e) {
            return false;
        }
    }

    // delete later if not needed
    public void addTag(Tag tag) {
        tags.add(tag);
        this.dirty = true;
    }


    public void removeTag(Tag tag) {
        // tag is empty row
        // selectedRow is -1 means no entries are found in the table
        if (tag == null && selectedRow != -1) {
            if (tags.get(selectedRow).getName().equals("")) {
                if (tags.get(selectedRow).getDescription().equals("")) {
                    tags.remove(selectedRow);
                }
            }
        } else if (selectedRow != -1) {
            // check if tag is persisted tag
            Map<String, String> tDaoStrings = new HashMap<String, String>();
            for (Tag t : tDao.getAll()) {
                tDaoStrings.put(t.getName(), t.getDescription());
            }
            if (tDaoStrings.get(tag.getName()) != null) {
                if (tDaoStrings.get(tag.getName()).equals(tag.getDescription())) {
                    tagsToDelete.add(tag);
                    this.dirty = true;
                }
            }
            tags.remove(selectedRow);
        }
        // needed if tags were added and then deleted without saving
        if (tags.isEmpty()) {
            selectedRow = -1;
        }
        // tags don't contain any changes       
        if (!containsEmptyTag() && !containsModifiedTag()) {
            this.dirty = false;
        }

    }

    /**
     * @return true if tags contain modified tags
     */
    private boolean containsModifiedTag() {
        //List<Tag> tagsModified = tags;
        if (!tagsToDelete.isEmpty() || tags.size() != tDao.getAll().size())
            return true;
        // if no persisted tags got deleted and local tag list is the same size as DAO list, then check for edited tags
        Map<String, String> tDaoStrings = new HashMap<String, String>();
        Map<String, String> tStrings = new HashMap<String, String>();
        for (Tag t : tDao.getAll()) {
            tDaoStrings.put(t.getName(), t.getDescription() + t.isVisible());
        }
        for (Tag t : tags) {
            tStrings.put(t.getName(), t.getDescription() + t.isVisible());
        }
        if (!tDaoStrings.equals(tStrings))
            return true;
        return false;
    }

    /**
     * @return true if tags contain empty tags
     */
    private boolean containsEmptyTag() {
        for (Tag t : tags) {
            if (t.getName().equals(""))
                return true;
        }
        return false;
    }

    public void onCellEdit(CellEditEvent event) {
        String oldValue = event.getOldValue().toString();
        String newValue = event.getNewValue().toString();
        selectedColumn = event.getColumn().getClientId();
        selectedRow = event.getRowIndex();

        // check if input is valid
        if (!oldValue.equals(newValue)) {
            if (newValue.equals("") || newValue.equals("\\s")) {
                // input is not valid, rollback changes
                if (selectedColumn.contains("names")) {
                    tags.get(event.getRowIndex()).setName(oldValue);
                } else {
                    tags.get(event.getRowIndex()).setDescription(oldValue);
                }
            }
        }

        if (selectedColumn.contains("names")) {
            for (Tag t : tags) {
                if (newValue.equals(t.getName()) && tags.indexOf(t) != selectedRow) {
                    tags.get(selectedRow).setName(oldValue);
                    // TODO: faces msg
                }
            }
        } else {
            for (Tag t : tags) {
                if (newValue.equals(t.getDescription()) && tags.indexOf(t) != selectedRow) {
                    tags.get(selectedRow).setDescription(oldValue);
                    // TODO: faces msg
                }
            }
        }

        if (selectedColumn.contains("names")) {
            for (Tag t : tags) {
                if (newValue.equals(t.getName()) && tags.indexOf(t) != selectedRow) {
                    tags.get(selectedRow).setName(oldValue);
                    // TODO: faces msg duplicate name
                }
            }
        } else {
            for (Tag t : tags) {
                if (newValue.equals(t.getDescription()) && tags.indexOf(t) != selectedRow) {
                    tags.get(selectedRow).setDescription(oldValue);
                    // TODO: faces msg duplicate desc
                }
            }
        }

        if (containsModifiedTag())
            this.dirty = true;
    }

    public void onContextMenu(SelectEvent event) {
        this.selectedRow = tags.indexOf(event.getObject());
    }

    public void onAddNew() {
        Tag newTag = new Tag();
        newTag.setName("");
        newTag.setDescription("");
        tags.add(newTag);
        this.dirty = true;
    }

    public void save() {
        if (!this.dirty) {
            if (logger.isDebugEnabled())
                logger.debug("no dirty state, nothing to do");
            return;
        }

        try {
            if (!containsEmptyTag()) {
                if (!tagsToDelete.isEmpty()) {
                    for (Tag t : tagsToDelete) {
                        tDao.remove(t);
                    }
                }
                tDao.merge(tags);

                this.dirty = false;

                this.addI18NFacesMessage("facesMsg.tags.saveSuccess", FacesMessage.SEVERITY_INFO);
                FacesUtils.forwardToPage("manageTags");
            } else {
                this.addI18NFacesMessage("facesMsg.tags.saveFail.emptyRows", FacesMessage.SEVERITY_ERROR);
            }

        } catch (JAXBException e) {
            this.addI18NFacesMessage("facesMsg.tags.saveFail", FacesMessage.SEVERITY_ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            this.addI18NFacesMessage("facesMsg.tags.saveFail", FacesMessage.SEVERITY_ERROR);
            e.printStackTrace();
        }

    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public List<String> getTagNames() {
        List<String> tagNames = new ArrayList<String>();
        for (Tag t : tags) {
            tagNames.add(t.getName());
        }
        return tagNames;
    }

    public List<String> getTagDescriptions(List<Tag> tagList) {
        List<String> tagDescriptions = new ArrayList<String>();
        for (Tag t : tags) {
            tagDescriptions.add(t.getDescription());
        }
        return tagDescriptions;
    }

    public Tag getSelectedTag() {
        return selectedTag;
    }

    public void setSelectedTag(Tag selectedTag) {
        this.selectedTag = selectedTag;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public void setSelectedRow(int selectedRow) {
        this.selectedRow = selectedRow;
    }

    public String getSelectedColumn() {
        return selectedColumn;
    }

    public void setSelectedColumn(String selectedColumn) {
        this.selectedColumn = selectedColumn;
    }

}
