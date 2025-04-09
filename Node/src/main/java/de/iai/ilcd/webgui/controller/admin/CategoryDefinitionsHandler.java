package de.iai.ilcd.webgui.controller.admin;

import de.fzk.iai.ilcd.api.binding.helper.CategoriesHelper;
import de.iai.ilcd.model.common.CategoryDefinition;
import de.iai.ilcd.model.common.CategorySystem;
import de.iai.ilcd.model.common.XmlFile;
import de.iai.ilcd.model.dao.CategorySystemDao;
import de.iai.ilcd.model.dao.PersistException;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AbortProcessingException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

@ManagedBean(name = "categoryDefinitionsHandler")
@ViewScoped
public class CategoryDefinitionsHandler extends AbstractHandler implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7152522840736563200L;

    public static Logger logger = LogManager.getLogger(CategoryDefinitionsHandler.class);

    private Collection<CategorySystem> categorySystems = null;

    private CategorySystem selectedItem = null;

    private CategorySystemDao catSystemDao = new CategorySystemDao();

    @PostConstruct
    public void init() {
        this.categorySystems = this.catSystemDao.getAll();
    }

    public synchronized void handleFileUpload(FileUploadEvent event) throws AbortProcessingException {
        logger.debug("File upload handler routine called");
        UploadedFile file = event.getFile();
        if (file != null) { // i.e. we have success
            try {
                if (logger.isDebugEnabled())
                    logger.debug("Uploaded file: " + file.getFileName());

                String catDefinition = IOUtils.toString(file.getInputStream(), "UTF-8");

                CategoriesHelper ch = new CategoriesHelper();
                de.fzk.iai.ilcd.api.app.categories.CategorySystem cdImported = ch.unmarshal(file.getInputStream());
                String csImportedName = cdImported.getName();

                CategorySystem csExisting = null;

                for (CategorySystem cs : this.catSystemDao.getAll()) {
                    if (cs.getName().equalsIgnoreCase(csImportedName)) {
                        csExisting = cs;
                        break;
                    }
                }

                if (csExisting == null)
                    csExisting = new CategorySystem(csImportedName);

                XmlFile xmlFile = new XmlFile();
                xmlFile.setContent(catDefinition);

                CategoryDefinition catDef = new CategoryDefinition();
                catDef.setXmlFile(xmlFile);
                catDef.setImportDate(new Date());

                csExisting.getCategoryDefinitions().add(catDef);

                catSystemDao.persist(csExisting);

                this.categorySystems = this.catSystemDao.getAll();

                if (logger.isDebugEnabled())
                    this.addI18NFacesMessage("facesMsg.import.profileuploadSuccess", FacesMessage.SEVERITY_INFO);
            } catch (IOException e) {
                logger.error(e.getMessage());
                this.addI18NFacesMessage("facesMsg.import.fileuploadError1", FacesMessage.SEVERITY_ERROR);
            } catch (JAXBException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (PersistException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            this.addI18NFacesMessage("facesMsg.import.fileuploadError2", FacesMessage.SEVERITY_ERROR);
        }
    }

    public void deleteItem() {
        if (this.selectedItem == null)
            return;

        try {
            this.catSystemDao.remove(selectedItem);
        } catch (Exception e) {
            logger.error("failed to delete category system " + selectedItem.getName(), e);
            return;
        }
        this.categorySystems.remove(selectedItem);
        this.selectedItem = null;
    }

    public Collection<CategorySystem> getCategorySystems() {
        return categorySystems;
    }

    public void setCategorySystems(Collection<CategorySystem> categorySystems) {
        this.categorySystems = categorySystems;
    }

    public CategorySystem getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(CategorySystem selectedItem) {
        this.selectedItem = selectedItem;
    }

}
