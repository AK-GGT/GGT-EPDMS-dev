package de.iai.ilcd.webgui.util;

import de.fzk.iai.ilcd.api.app.categories.Categories;
import de.fzk.iai.ilcd.api.app.categories.Category;
import de.fzk.iai.ilcd.api.app.categories.CategorySystem;
import de.fzk.iai.ilcd.api.binding.generated.categories.CategoriesType;
import de.fzk.iai.ilcd.api.binding.generated.categories.CategoryType;
import de.fzk.iai.ilcd.api.binding.generated.categories.DataSetType;
import de.fzk.iai.ilcd.api.binding.helper.CategoriesHelper;
import de.iai.ilcd.model.common.CategoryDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CategoryDefinitionsUtil {

    public static Logger logger = LogManager.getLogger(CategoryDefinitionsUtil.class);

    public de.fzk.iai.ilcd.api.app.categories.CategorySystem convertDefinitionToCategorySystem(CategoryDefinition catDef)
            throws UnsupportedEncodingException, JAXBException {

        InputStream bais = new ByteArrayInputStream(catDef.getXmlFile().getContent().getBytes("UTF-8"));

        CategoriesHelper cHelper = new CategoriesHelper();

        return cHelper.unmarshal(bais);
    }


    public de.fzk.iai.ilcd.api.app.categories.Categories convertDefinitionToCategories(CategoryDefinition catDef)
            throws UnsupportedEncodingException, JAXBException {

        CategorySystem catSystem = convertDefinitionToCategorySystem(catDef);

        return (Categories) catSystem.getCategories();
    }

    public String fromModel(de.fzk.iai.ilcd.api.app.categories.CategorySystem catSystem)
            throws JAXBException, IllegalArgumentException {
        CategoriesHelper cHelper = new CategoriesHelper();

        if (catSystem == null)
            throw new IllegalArgumentException("Provided CategorySystem was null.");

        // We're going to override the flow categories with the product categories, if
        // both process and flow categories exist.
        List<CategoriesType> categories = catSystem.getCategories();
        Categories processCats = null;
        for (CategoriesType cat : categories) {
            if (cat != null && DataSetType.PROCESS.equals(cat.getDataType())) {
                processCats = (Categories) cat;
                break;
            }
        }

        if (processCats != null) {
            // If flow categories are present..
            for (CategoriesType cat : categories) {
                if (cat != null && DataSetType.FLOW.equals(cat.getDataType())) {

                    // ..create duplicate of processCats with Type Flow
                    Categories flowCats = new Categories();
                    flowCats.setDataType(DataSetType.FLOW);
                    flowCats.getCategory().addAll(processCats.getCategory());

                    // ..override flow categories
                    int i = categories.indexOf(cat);
                    categories.set(i, flowCats);
                    break;
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cHelper.marshal(catSystem, baos);

        return baos.toString();
    }

    public List<Integer> convertRowKey(String rowKey) {
        List<Integer> rowKeys = new ArrayList<Integer>();
        StringTokenizer tokenizer = new StringTokenizer(rowKey, "_");

        while (tokenizer.hasMoreTokens())
            rowKeys.add(Integer.parseInt(tokenizer.nextToken()));

        return rowKeys;
    }

    public void updateCategories(String rowKey, Categories categories, String name, String id) {
        List<Integer> rowKeys = convertRowKey(rowKey);

        if (rowKeys.size() == 0) {
            if (logger.isDebugEnabled())
                logger.debug("update category: empty rowKey given, nothing to do");
            return;
        } else if (rowKeys.size() > 0) {
            Integer currentIndex = rowKeys.get(0);
            rowKeys.remove(0);
            updateCategory(rowKeys, (Category) categories.getCategory().get(currentIndex), name, id);
        }
    }

    public void updateCategory(List<Integer> rowKeys, Category category, String name, String id) {
        if (rowKeys.size() == 0) {
            category.setName(name);
            category.setId(id);
        } else {
            Integer currentIndex = rowKeys.get(0);
            rowKeys.remove(0);
            updateCategory(rowKeys, (Category) category.getCategory().get(currentIndex), name, id);
        }
    }

    public void removeCategory(String rowKey, Categories categories) {
        List<Integer> rowKeys = convertRowKey(rowKey);

        if (rowKeys.size() == 0) {
            if (logger.isDebugEnabled())
                logger.debug("remove category: empty rowKey given, nothing to do");
            return;
        } else if (rowKeys.size() == 1) {
            Integer currentIndex = rowKeys.get(0);
            if (logger.isDebugEnabled())
                logger.debug("removing category with row key " + rowKey + ": " + categories.getCategory().get(rowKeys.get(currentIndex)).getName());
            categories.getCategory().remove(categories.getCategory().get(currentIndex));
        } else if (rowKeys.size() > 1) {
            Integer currentIndex = rowKeys.get(0);
            rowKeys.remove(0);
            if (logger.isDebugEnabled())
                logger.debug("removing category with row key " + rowKey + ": " + categories.getCategory().get(currentIndex).getName());
            removeCategory(rowKeys, (Category) categories.getCategory().get(currentIndex));
        }
    }

    private void removeCategory(List<Integer> rowKeys, Category category) {
        Integer currentIndex = rowKeys.get(0);

        if (logger.isTraceEnabled())
            logger.trace("  removing category - next row key is " + currentIndex);

        rowKeys.remove(0);

        if (rowKeys.size() == 0) {
            if (logger.isDebugEnabled())
                logger.debug("  removing category " + category.getCategory().get(currentIndex).getName());
            category.getCategory().remove(category.getCategory().get(currentIndex));
        } else {
            removeCategory(rowKeys, (Category) category.getCategory().get(currentIndex));
        }
    }

    public void addCategory(String rowKey, Categories cats) {
        List<Integer> rowKeys = convertRowKey(rowKey);

        if (rowKeys.size() == 0) {
            if (logger.isDebugEnabled())
                logger.debug("add category: empty rowKey given, nothing to do");
            return;
        } else if (rowKeys.size() == 1) {
            Integer currentIndex = rowKeys.get(0);
            cats.getCategory().add(currentIndex + 1, new Category());
        } else if (rowKeys.size() > 1) {
            Integer currentIndex = rowKeys.get(0);
            rowKeys.remove(0);
            if (logger.isTraceEnabled())
                logger.trace("add category: rowKey is " + rowKey + ", currentIndex is " + currentIndex);

            addCategory(rowKeys, (Category) cats.getCategory().get(currentIndex));
        }
    }

    private void addCategory(List<Integer> rowKeys, Category category) {
        Integer currentIndex = rowKeys.get(0);
        rowKeys.remove(0);

        if (logger.isTraceEnabled())
            logger.trace("  adding category - next row key is " + currentIndex);

        if (rowKeys.size() == 0) {
            if (logger.isDebugEnabled())
                logger.debug("  adding category at pos " + (currentIndex + 1));
            category.getCategory().add(currentIndex + 1, new Category());
        } else {
            addCategory(rowKeys, (Category) category.getCategory().get(currentIndex));
        }
    }

    public void addChildCategory(String rowKey, Categories cats) {
        List<Integer> rowKeys = convertRowKey(rowKey);

        if (rowKeys.size() == 0) {
            if (logger.isDebugEnabled())
                logger.debug("add child category: empty rowKey given, nothing to do");
            return;
        } else if (rowKeys.size() == 1) {
            Integer currentIndex = rowKeys.get(0);
            cats.getCategory().get(currentIndex).getCategory().add(new Category());
        } else if (rowKeys.size() > 1) {
            Integer currentIndex = rowKeys.get(0);
            rowKeys.remove(0);
            addChildCategory(rowKeys, (Category) cats.getCategory().get(currentIndex));
        }
    }

    private void addChildCategory(List<Integer> rowKeys, Category category) {
        Integer currentIndex = rowKeys.get(0);
        rowKeys.remove(0);

        if (logger.isTraceEnabled())
            logger.trace("  adding child category - next row key is " + currentIndex);

        if (rowKeys.size() == 0) {
            if (logger.isDebugEnabled())
                logger.debug("  adding child category at pos " + currentIndex);
            category.getCategory().get(currentIndex).getCategory().add(new Category());
        } else {
            addChildCategory(rowKeys, (Category) category.getCategory().get(currentIndex));
        }
    }

    public TreeNode catsToTree(de.fzk.iai.ilcd.api.app.categories.CategorySystem apiCatSystem) {

        Categories cats = (Categories) apiCatSystem.getCategories().get(0);

        return catsToTree(cats);
    }

    public TreeNode catsToTree(de.fzk.iai.ilcd.api.app.categories.Categories cats) {
        TreeNode root = new DefaultTreeNode();
        root.setExpanded(true);

        for (CategoryType cat : cats.getCategory()) {
            TreeNode node = new DefaultTreeNode(cat, root);
            addChildren(node, cat);
            node.setExpanded(true);
        }

        return root;
    }

    public TreeNode catsToTree(CategoryDefinition catDef) {

        de.fzk.iai.ilcd.api.app.categories.CategorySystem apiCatSystem = null;

        try {
            apiCatSystem = convertDefinitionToCategorySystem(catDef);
        } catch (UnsupportedEncodingException | JAXBException e) {
            // TODO Auto-generated catch block
            logger.error("could not convert category definitions to TreeNode");
            e.printStackTrace();
            return null;
        }

        return catsToTree(apiCatSystem);
    }

    private void addChildren(TreeNode treeNode, CategoryType cat) {
        if (cat == null)
            return;

        for (CategoryType subCat : cat.getCategory()) {
            TreeNode node = new DefaultTreeNode(subCat, treeNode);
            addChildren(node, subCat);
        }
    }

}
