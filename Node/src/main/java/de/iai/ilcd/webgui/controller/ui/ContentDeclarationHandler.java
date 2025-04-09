package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.process.contentdeclaration.*;
import de.iai.ilcd.webgui.controller.AbstractHandler;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name = "contentDeclarationHandler")
@ViewScoped
public class ContentDeclarationHandler extends AbstractHandler {

    /**
     * Serialisation..
     */
    private static final long serialVersionUID = -2375439336364839456L;

    private String lang;

    private ContentDeclaration contentDeclaration;

    private boolean holdsData = false;

    private TreeNode root;

    public ContentDeclarationHandler() {
        super();
    }

    public ContentDeclarationHandler(ContentDeclaration cd) {
        super();
        this.contentDeclaration = cd;
        if (cd != null)
            this.holdsData = true;
    }

    private static void expandOrCollapse(TreeNode treeNode, boolean option) {
        if (treeNode != null) {
            if (treeNode.getChildCount() == 0)
                treeNode.setSelected(false);
            else {
                for (TreeNode t : treeNode.getChildren())
                    expandOrCollapse(t, option);

                treeNode.setExpanded(option);
                treeNode.setSelected(false);
            }
        }
    }

    public TreeNode getRootNode(String lang) {
        // We will only do the labour, if it hasn't been done yet or the language changes.
        if (this.root != null && StringUtils.equalsIgnoreCase(this.lang, lang))
            return this.root;

        // Nothing to be done if there's no data..
        if (this.holdsData) {
            this.lang = lang;
            generateRootnode();
        }
        return this.root;
    }

    private void generateRootnode() {
        TreeNode root = new DefaultTreeNode(new CDRowInfo("Content declaration"), null);

        // We want to separate product and packaging, therefore we introduce an artificial layer
        TreeNode productNode = new DefaultTreeNode(new CDRowInfo("Product"), root);
        TreeNode packagingNode = new DefaultTreeNode(new CDRowInfo("Packaging"), root);
        boolean packagingRegistered = false;

        for (ContentElement element : this.contentDeclaration.getContent()) {
            if (element instanceof Component) {
                TreeNodeFromComponent(element, productNode); // Components can't be packaging..
            } else if (element instanceof Material) {
                Material material = (Material) element;
                if (material.isPackaging()) {
                    TreeNodeFromMaterial(material, packagingNode);
                    packagingRegistered = true;
                } else {
                    TreeNodeFromMaterial(material, productNode);
                }
            } else if (element instanceof Substance) {
                Substance substance = (Substance) element;
                if (substance.isPackaging()) {
                    TreeNodeFromSubstance(substance, packagingNode);
                    packagingRegistered = true;
                } else {
                    TreeNodeFromSubstance(substance, productNode);
                }
            }
        }

        if (!packagingRegistered)
            removeFrom(root, packagingNode);

        for (TreeNode artificialChild : root.getChildren())
            artificialChild.setExpanded(true);

        this.root = root;
    }

    private void removeFrom(TreeNode root, TreeNode nodeToBeRemoved) {
        root.getChildren().remove(nodeToBeRemoved);
    }

    private TreeNode TreeNodeFromComponent(ContentElement component, TreeNode entryPoint) {
        TreeNode componentNode = new DefaultTreeNode(new CDRowInfo(component, this.lang), entryPoint);

        for (ContentElement element : ((Component) component).getContent()) {
            if (element instanceof Material) {
                TreeNodeFromMaterial((Material) element, componentNode);
            } else if (element instanceof Substance) {
                TreeNodeFromSubstance((Substance) element, componentNode);
            }
        }
        return componentNode;
    }

    private TreeNode TreeNodeFromMaterial(Material material, TreeNode entryPoint) {
        TreeNode materialNode = new DefaultTreeNode(new CDRowInfo((Substance) material, this.lang), entryPoint);

        for (Substance element : material.getSubstances()) {
            TreeNodeFromSubstance(element, materialNode);
        }
        return materialNode;
    }

    private TreeNode TreeNodeFromSubstance(Substance element, TreeNode entryPoint) {
        return new DefaultTreeNode(new CDRowInfo(element, this.lang), entryPoint);
    }

    public void setContentDeclaration(ContentDeclaration contentDeclaration) {
        this.contentDeclaration = contentDeclaration;
        if (contentDeclaration != null)
            this.holdsData = true;
    }

    public boolean hasData() {
        return this.holdsData;
    }

    public void expandAll() {
        expandOrCollapse(root, true);
    }

    public void collapseAll() {
        expandOrCollapse(root, false);
    }

    /**
     * This innerclass is just a suitable wrapper for all types of
     * content elements to model them as rows in a tree table.
     * <p>
     * The getters standardise results for blank strings.
     */
    public class CDRowInfo {


        private final String EMPTY = "";

        private final String UNMODELLED = "";
        boolean isPackaging, isSubstance;
        boolean isSimpleNameRow = false;
        //Fields defining ContentElement
        private String name, comment, massPercentage, mass;
        //Fields defining Substance
        private String ecNumber, casNumber, renewable, recycled, recyclable;

        CDRowInfo(String name) {
            this.name = name;
            this.isSimpleNameRow = true;
        }

        CDRowInfo(ContentElement cElement, String lang) {
            this.initContentElementFields(cElement, lang);
            this.isSubstance = false;
        }

        CDRowInfo(Substance substance, String lang) {
            this.initContentElementFields(substance, lang);
            this.isSubstance = true;

            this.casNumber = substance.getCasNumber();
            this.ecNumber = substance.getEcNumber();
            if (substance.getRenewable() != null)
                this.renewable = substance.getRenewable().toString();
            if (substance.getRecycled() != null)
                this.recycled = substance.getRecycled().toString();
            if (substance.getRecyclable() != null)
                this.recyclable = substance.getRecyclable().toString();
            this.isPackaging = substance.isPackaging();
        }

        private void initContentElementFields(ContentElement cElement, String lang) {
            try {
                this.name = cElement.getName().getValueWithFallback(lang);
            } catch (NullPointerException e) {
            }
            try {
                this.comment = cElement.getComment().getValueWithFallback(lang);
            } catch (NullPointerException e) {
            }
            try {
                this.massPercentage = cElement.getMassPerc().toString();
            } catch (NullPointerException e) {
            }
            try {
                this.mass = cElement.getMass().toString();
            } catch (NullPointerException e) {
            }
        }

        private String smartReturn(String stringValue, boolean available) {
            if (StringUtils.isBlank(stringValue))
                return available ? this.EMPTY : this.UNMODELLED;
            return stringValue;
        }

        public String getName() {
            return this.name;
        }

        public String getComment() {
            if (this.isSimpleNameRow)
                return "";
            return smartReturn(comment, true);
        }

        public String getMassPercentage() {
            if (this.isSimpleNameRow)
                return "";
            return smartReturn(massPercentage, true);
        }

        public String getMass() {
            if (this.isSimpleNameRow)
                return "";
            return smartReturn(mass, true);
        }

        public String getEcNumber() {
            if (this.isSimpleNameRow)
                return "";
            return smartReturn(ecNumber, isSubstance);
        }

        public String getCasNumber() {
            if (this.isSimpleNameRow)
                return "";
            return smartReturn(casNumber, isSubstance);
        }

        public String getRenewable() {
            if (this.isSimpleNameRow)
                return "";
            return smartReturn(renewable, isSubstance);
        }

        public String getRecycled() {
            if (this.isSimpleNameRow)
                return "";
            return smartReturn(recycled, isSubstance);
        }

        public String getRecyclable() {
            if (this.isSimpleNameRow)
                return "";
            return smartReturn(recyclable, isSubstance);
        }

        public boolean isPackaging() {
            return this.isPackaging;
        }
    }
}
