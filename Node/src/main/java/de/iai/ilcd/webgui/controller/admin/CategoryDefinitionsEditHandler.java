package de.iai.ilcd.webgui.controller.admin;

import de.fzk.iai.ilcd.api.app.categories.Categories;
import de.fzk.iai.ilcd.api.app.categories.Category;
import de.iai.ilcd.model.common.CategoryDefinition;
import de.iai.ilcd.model.common.CategorySystem;
import de.iai.ilcd.model.common.XmlFile;
import de.iai.ilcd.model.dao.CategoriesDefinitionDao;
import de.iai.ilcd.model.dao.CategorySystemDao;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import de.iai.ilcd.webgui.util.CategoryDefinitionsUtil;
import eu.europa.ec.jrc.lca.commons.view.util.FacesUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.TreeNode;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.xml.bind.JAXBException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;

@ManagedBean(name = "categoryDefinitionsEditHandler")
@ViewScoped
public class CategoryDefinitionsEditHandler extends AbstractHandler implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7152522840736563200L;

    public static Logger logger = LogManager.getLogger(CategoryDefinitionsEditHandler.class);

    private TreeNode definitionsView = null;

    private CategoryDefinition categoryDefinition = null;

    private CategorySystem categorySystem = null;

    private de.fzk.iai.ilcd.api.app.categories.Categories categoriesModel = null;

    private de.fzk.iai.ilcd.api.app.categories.CategorySystem apiCategorySystem = null;

    private TreeNode selectedNode = null;

    private String selectionRowKey = null;

    private boolean dirty = false;

    private CategoryDefinitionsUtil cdu = new CategoryDefinitionsUtil();

    private CategorySystemDao csDao = new CategorySystemDao();

    @PostConstruct
    public void init() {

        String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");

        this.categorySystem = csDao.getById(id);

        this.categoryDefinition = this.categorySystem.getCurrentCategoryDefinition();

        try {
            this.apiCategorySystem = cdu.convertDefinitionToCategorySystem(this.categoryDefinition);
        } catch (UnsupportedEncodingException | JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.categoriesModel = (Categories) this.apiCategorySystem.getCategories().get(0);

        updateView();
    }

    public void updateView() {
        this.definitionsView = cdu.catsToTree(this.categoriesModel);
        expandAll();
        PrimeFaces.current().ajax().update(":catDefsEditor:btnSave");
    }

    public void save() {
        if (!this.dirty) {
            if (logger.isDebugEnabled())
                logger.debug("no dirty state, nothing to do");
            return;
        }

        String catDef;
        try {
            catDef = cdu.fromModel(this.apiCategorySystem);

            // if (logger.isDebugEnabled())
            // logger.debug(catDef);

            CategoryDefinition newCatDef = new CategoryDefinition();
            XmlFile xmlFile = new XmlFile();
            xmlFile.setContent(catDef);
            newCatDef.setMostRecentVersion(true);
            newCatDef.setXmlFile(xmlFile);
            newCatDef.setImportDate(new Date());

            this.categorySystem.getCategoryDefinitions().add(newCatDef);

            CategoriesDefinitionDao cdDao = new CategoriesDefinitionDao();
            cdDao.persist(newCatDef);

            CategorySystemDao dao = new CategorySystemDao();
            dao.merge(this.categorySystem);

            this.dirty = false;

        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        FacesUtils.forwardToPage("manageCategoryDefinitions");
    }

    public void editNode(ActionEvent event) {
        String cmd = "jQuery(\"tr[id='catDefsEditor:catsTree_node_" + selectionRowKey
                + "']\").find('span.ui-icon-pencil').each(function(){jQuery(this).click()});";

        PrimeFaces.current().executeScript(cmd);
    }

    public void deleteNode() {
        if (this.selectedNode == null) {
            logger.debug("nothing selected");
            return;
        }

        if (logger.isDebugEnabled())
            logger.debug("deleting node " + ((Category) this.selectedNode.getData()).getName());

        cdu.removeCategory(selectionRowKey, (Categories) this.categoriesModel);

        this.dirty = true;
        updateView();
    }

    public void addNode(ActionEvent event) {
        if (selectedNode == null) {
            return;
        }
        cdu.addCategory(selectionRowKey, (Categories) this.categoriesModel);

        if (selectionRowKey.contains("_"))
            selectionRowKey = StringUtils.substringBeforeLast(selectionRowKey, "_") + "_"
                    + Integer.parseInt(StringUtils.substringAfterLast(selectionRowKey, "_")) + 1;
        else
            selectionRowKey = Integer.toString(Integer.parseInt(selectionRowKey) + 1);

        this.dirty = true;
        updateView();

        editNode(null);
    }

    public void addChildNode(ActionEvent event) {
        if (selectedNode == null) {
            return;
        }
        cdu.addChildCategory(selectionRowKey, (Categories) this.categoriesModel);

        if (selectionRowKey.contains("_"))
            selectionRowKey = StringUtils.substringBeforeLast(selectionRowKey, "_") + "_"
                    + Integer.parseInt(StringUtils.substringAfterLast(selectionRowKey, "_")) + 1;
        else
            selectionRowKey = Integer.toString(Integer.parseInt(selectionRowKey) + 1);

        this.dirty = true;
        updateView();

        editNode(null);
    }

    public void onNodeSelect(NodeSelectEvent event) {
        this.selectedNode = event.getTreeNode();
        this.selectionRowKey = this.selectedNode.getRowKey();

        if (logger.isTraceEnabled())
            logger.trace("selecting node " + ((Category) this.selectedNode.getData()).getName() + " - " + this.selectionRowKey);
    }

    public void onNodeUnSelect(NodeSelectEvent event) {
        this.selectedNode = null;

        if (logger.isTraceEnabled())
            logger.trace("unselecting node " + ((Category) this.selectedNode.getData()).getName() + " - " + this.selectionRowKey);
    }

    public void onRowEdit(RowEditEvent event) throws UnsupportedEncodingException, JAXBException {
        TreeNode node = (TreeNode) event.getObject();

        if (logger.isDebugEnabled())
            logger.debug("edited node " + ((Category) node.getData()).getName() + " - " + node.getRowKey());

        cdu.updateCategories(node.getRowKey(), (Categories) this.categoriesModel, ((Category) node.getData()).getName(), ((Category) node.getData()).getId());

        node.setExpanded(true);
        node.getParent().setExpanded(true);

        this.dirty = true;
        updateView();
    }

    public void collapseAll() {
        setExpandedRecursively(this.definitionsView, false);
    }

    public void expandAll() {
        setExpandedRecursively(this.definitionsView, true);
    }

    private void setExpandedRecursively(final TreeNode node, final boolean expanded) {
        for (final TreeNode child : node.getChildren()) {
            setExpandedRecursively(child, expanded);
        }
        node.setExpanded(expanded);
    }

    public TreeNode getDefinitions() {
        return this.definitionsView;
    }

    public CategoryDefinition getCategoryDefinition() {
        return categoryDefinition;
    }

    public void setCategoryDefinition(CategoryDefinition categoryDefinition) {
        this.categoryDefinition = categoryDefinition;
    }

    public CategorySystem getCategorySystem() {
        return categorySystem;
    }

    public void setCategorySystem(CategorySystem categorySystem) {
        this.categorySystem = categorySystem;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;

        if (selectedNode != null)
            this.selectionRowKey = selectedNode.getRowKey();

        if (logger.isTraceEnabled()) {
            if (selectedNode == null)
                logger.trace("setting selected node to null");
            else
                logger.trace("setting selected node to " + ((Category) this.selectedNode.getData()).getName());
        }
    }

    public String getSelectedNodeCategoryName() {
        if (this.selectedNode == null)
            return null;

        Category cat = (Category) this.selectedNode.getData();
        return cat.getName();
    }

    public boolean isSelectedNodeHasChildren() {
        if (this.selectedNode == null)
            return false;

        return (this.selectedNode.getChildCount() > 0);
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public String getSelectionRowKey() {
        return selectionRowKey;
    }

    public void setSelectionRowKey(String selectionRowKey) {
        this.selectionRowKey = selectionRowKey;
    }
}
