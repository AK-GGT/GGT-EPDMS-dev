package edu.kit.soda4lca.test.categories;

import de.fzk.iai.ilcd.api.app.categories.Categories;
import de.fzk.iai.ilcd.api.app.categories.Category;
import de.fzk.iai.ilcd.api.app.categories.CategorySystem;
import de.fzk.iai.ilcd.api.binding.generated.categories.CategoriesType;
import de.fzk.iai.ilcd.api.binding.generated.categories.CategoryType;
import de.fzk.iai.ilcd.api.binding.generated.categories.DataSetType;
import de.fzk.iai.ilcd.api.binding.helper.CategoriesHelper;
import de.iai.ilcd.model.common.CategoryDefinition;
import de.iai.ilcd.model.common.XmlFile;
import de.iai.ilcd.webgui.util.CategoryDefinitionsUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.primefaces.model.TreeNode;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CategoriesTest {

    private static final String ILCD_CATEGORIES_FILE = "src/test/resources/categories/ILCDClassification.xml";

    private static final String CUSTOM_CATEGORIES_FILE = "src/test/resources/categories/CustomClassification.xml";

    public static Logger logger = LogManager.getLogger(CategoriesTest.class);

    private CategoryDefinitionsUtil cdu = new CategoryDefinitionsUtil();

    @Test
    public void testUnmarshal() throws UnsupportedEncodingException, FileNotFoundException, JAXBException {
        CategoriesHelper ch = new CategoriesHelper();
        CategorySystem csILCD = ch.unmarshal(new FileInputStream(new File(ILCD_CATEGORIES_FILE)));

        assertEquals("ILCD", csILCD.getName());
        assertEquals(DataSetType.PROCESS, csILCD.getCategories().get(0).getDataType());
        assertEquals("Energy carriers and technologies", csILCD.getCategories().get(0).getCategory().get(0).getName());

        CategorySystem csOBD = ch.unmarshal(new FileInputStream(new File(CUSTOM_CATEGORIES_FILE)));

        assertEquals("OEKOBAU.DAT", csOBD.getName());
        assertEquals(DataSetType.PROCESS, csOBD.getCategories().get(0).getDataType());
        assertEquals("Mineralische Baustoffe", csOBD.getCategories().get(0).getCategory().get(0).getName());
        assertEquals("1", csOBD.getCategories().get(0).getCategory().get(0).getId());

    }

    @Test
    public void testConvertCatsFromFileToAPI() throws UnsupportedEncodingException, JAXBException {

        CategoryDefinition catDef = loadCategoryDefinition(CUSTOM_CATEGORIES_FILE);

        de.fzk.iai.ilcd.api.app.categories.CategorySystem result = cdu.convertDefinitionToCategorySystem(catDef);

        assertEquals(1, result.getCategories().size());

        CategoriesType categories = result.getCategories().get(0);

        assertEquals(DataSetType.PROCESS, categories.getDataType());

        assertEquals(9, categories.getCategory().size());

        Category top1 = (Category) categories.getCategory().get(0);

        assertEquals("Mineralische Baustoffe", top1.getName());
        assertEquals("1", top1.getId());

        Category sub3 = (Category) top1.getCategory().get(2);
        assertEquals("Steine und Elemente", sub3.getName());
        assertEquals("1.3", sub3.getId());

        Category subsub6 = (Category) sub3.getCategory().get(5);
        assertEquals("Steinzeug", subsub6.getName());
        assertEquals("1.3.06", subsub6.getId());

    }

    @Test
    public void testConvertCatsToTree() throws UnsupportedEncodingException, JAXBException {

        CategoryDefinition catDef = loadCategoryDefinition(CUSTOM_CATEGORIES_FILE);

        TreeNode root = cdu.catsToTree(catDef);
        printTree(root, 0);

        assertEquals(9, root.getChildCount());

        TreeNode top1 = root.getChildren().get(0);

        assertEquals("Mineralische Baustoffe", ((Category) top1.getData()).getName());
        assertEquals("1", ((Category) top1.getData()).getId());

        TreeNode sub3 = top1.getChildren().get(2);

        assertEquals("Steine und Elemente", ((Category) sub3.getData()).getName());
        assertEquals("1.3", ((Category) sub3.getData()).getId());

        TreeNode subsub6 = sub3.getChildren().get(5);

        assertEquals("Steinzeug", ((Category) subsub6.getData()).getName());
        assertEquals("1.3.06", ((Category) subsub6.getData()).getId());

    }

    @Test
    public void testConvertRowKey() {
        String rowKey1 = "1";
        List<Integer> rowKeys1 = cdu.convertRowKey(rowKey1);
        assertEquals(1, rowKeys1.size());
        assertEquals((Integer) 1, rowKeys1.get(0));

        String rowKey2 = "0_2";
        List<Integer> rowKeys2 = cdu.convertRowKey(rowKey2);
        assertEquals(2, rowKeys2.size());
        assertEquals((Integer) 0, rowKeys2.get(0));
        assertEquals((Integer) 2, rowKeys2.get(1));

        String rowKey3 = "0_3_1";
        List<Integer> rowKeys3 = cdu.convertRowKey(rowKey3);
        assertEquals(3, rowKeys3.size());
        assertEquals((Integer) 0, rowKeys3.get(0));
        assertEquals((Integer) 3, rowKeys3.get(1));
        assertEquals((Integer) 1, rowKeys3.get(2));

        String rowKey4 = "0_4_5_1";
        List<Integer> rowKeys4 = cdu.convertRowKey(rowKey4);
        assertEquals(4, rowKeys4.size());
        assertEquals((Integer) 0, rowKeys4.get(0));
        assertEquals((Integer) 4, rowKeys4.get(1));
        assertEquals((Integer) 5, rowKeys4.get(2));
        assertEquals((Integer) 1, rowKeys4.get(3));
    }

    private void testUpdateModel(CategorySystem catSystem, String rowKey, String newName, String newId) {
        Categories cats = (Categories) catSystem.getCategories().get(0);
        cdu.updateCategories(rowKey, cats, newName, newId);
    }

    @Test
    public void testUpdateModel() throws UnsupportedEncodingException, FileNotFoundException, JAXBException {
        CategoriesHelper ch = new CategoriesHelper();
        CategorySystem csOBD = ch.unmarshal(new FileInputStream(new File(CUSTOM_CATEGORIES_FILE)));

        testUpdateModel(csOBD, "0_0_0", "FooBar", "0825");

        ch.marshal(csOBD, System.out);

        assertEquals("FooBar", csOBD.getCategories().get(0).getCategory().get(0).getCategory().get(0).getCategory().get(0).getName());
        assertEquals("0825", csOBD.getCategories().get(0).getCategory().get(0).getCategory().get(0).getCategory().get(0).getId());
    }

    @Test
    public void testUpdateCategory() throws UnsupportedEncodingException, JAXBException {
        CategoryDefinition catDef = loadCategoryDefinition(CUSTOM_CATEGORIES_FILE);

        CategorySystem catSystem = cdu.convertDefinitionToCategorySystem(catDef);

        Categories cats = (Categories) catSystem.getCategories().get(0);

        cdu.updateCategory(cdu.convertRowKey("0_0_0"), (Category) cats.getCategory().get(0), "Foo", "0.0.0");

        assertEquals("Foo", cats.getCategory().get(0).getCategory().get(0).getCategory().get(0).getName());
        assertEquals("0.0.0", cats.getCategory().get(0).getCategory().get(0).getCategory().get(0).getId());
    }

    @Test
    public void testDeleteNode() throws UnsupportedEncodingException, FileNotFoundException, JAXBException {
        CategoriesHelper ch = new CategoriesHelper();
        CategorySystem csOBD = ch.unmarshal(new FileInputStream(new File(CUSTOM_CATEGORIES_FILE)));

        Categories processCats = (Categories) csOBD.getCategories().get(0);

        cdu.removeCategory("0_0_0", processCats);

        ch.marshal(csOBD, System.out);

        assertEquals("Kalk", processCats.getCategory().get(0).getCategory().get(0).getCategory().get(0).getName());
        assertEquals("1.1.02", processCats.getCategory().get(0).getCategory().get(0).getCategory().get(0).getId());
    }

    @Test
    public void testAddCategory() throws UnsupportedEncodingException, FileNotFoundException, JAXBException {
        CategoriesHelper ch = new CategoriesHelper();
        CategorySystem csOBD = ch.unmarshal(new FileInputStream(new File(CUSTOM_CATEGORIES_FILE)));

        Categories processCats = (Categories) csOBD.getCategories().get(0);

        cdu.addCategory("0_0_1", processCats);

        ch.marshal(csOBD, System.out);

        assertEquals(5, processCats.getCategory().get(0).getCategory().get(0).getCategory().size());
        assertEquals(null, processCats.getCategory().get(0).getCategory().get(0).getCategory().get(1).getName());
    }

    @Test
    public void testAddChildCategory() throws UnsupportedEncodingException, FileNotFoundException, JAXBException {
        CategoriesHelper ch = new CategoriesHelper();
        CategorySystem csOBD = ch.unmarshal(new FileInputStream(new File(CUSTOM_CATEGORIES_FILE)));

        Categories processCats = (Categories) csOBD.getCategories().get(0);

        cdu.addChildCategory("0_0_1", processCats);

        ch.marshal(csOBD, System.out);

        assertEquals(4, processCats.getCategory().get(0).getCategory().get(0).getCategory().size());
        assertEquals(null, processCats.getCategory().get(0).getCategory().get(0).getCategory().get(1).getCategory().get(0).getName());
    }

    private void printTree(TreeNode node, int level) {
        CategoryType cat = (CategoryType) node.getData();
        if (cat != null)
            logger.info(StringUtils.repeat("  ", level) + cat.getName() + "(" + cat.getId() + ")");
        else
            logger.info("(empty node)");
        for (TreeNode child : node.getChildren()) {
            printTree(child, level + 1);
        }
    }

    private CategoryDefinition loadCategoryDefinition(String path) {
        CategoryDefinition catDef = new CategoryDefinition();
        XmlFile file = new XmlFile();
        String content = null;

        try {
            InputStream is = new FileInputStream(new File(CUSTOM_CATEGORIES_FILE));
            content = IOUtils.toString(is, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        file.setContent(content);
        catDef.setXmlFile(file);
        return catDef;
    }
}
