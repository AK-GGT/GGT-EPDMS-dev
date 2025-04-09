package de.iai.ilcd.webgui.controller.admin;

import de.fzk.iai.ilcd.service.model.process.IReferenceFlow;
import de.iai.ilcd.model.common.ClClass;
import de.iai.ilcd.model.common.Classification;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.dao.ClassificationDao;
import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.dao.ProductFlowDao;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.flow.Flow;
import de.iai.ilcd.model.flow.MaterialProperty;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.model.process.Exchange;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.util.SodaUtil;
import de.iai.ilcd.webgui.controller.ui.AvailableStockHandler;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.util.*;

/**
 * Handler for the selection of processes for quality assurance.
 */
@ViewScoped
@ManagedBean(name = "pqaSelHandler")
public class ProcessQASelectionHandler extends AbstractProcessQAHandler {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -3499207253654899807L;
    /**
     * Choice
     */
    private final ProcessQASelectionEntry choice = new ProcessQASelectionEntry();
    /**
     * Target
     */
    private final ProcessQASelectionEntry target = new ProcessQASelectionEntry();
    /**
     * Available stocks bean
     */
    @ManagedProperty(value = "#{availableStocks}")
    private AvailableStockHandler availableStocks;
    /**
     * Category selections
     */
    private List<CategorySelection> categorySelections = new ArrayList<CategorySelection>();


    /**
     * {@inheritDoc}
     */
    @Override
    public void postConstruct() {
        // set data stock automatically if the list only contains one data stock
        List<IDataStockMetaData> metas = this.getAllStocksMeta();
        if (metas != null && metas.size() == 1) {
            IDataStockMetaData meta = metas.get(0);
            this.choice.setSelectedDsMetaName(meta.getName());
            this.target.setSelectedDsMetaName(meta.getName());
        }
    }

    /**
     * Get all stock meta data
     *
     * @return stock meta data
     */
    public List<IDataStockMetaData> getAllStocksMeta() {
        return this.availableStocks.getAllStocksMeta();
    }

    /**
     * Get the available stocks bean
     *
     * @return available stocks bean
     */
    public AvailableStockHandler getAvailableStocks() {
        return this.availableStocks;
    }

    /**
     * Set available stocks bean
     *
     * @param availableStocks available stocks bean to set
     */
    public void setAvailableStocks(AvailableStockHandler availableStocks) {
        this.availableStocks = availableStocks;
    }

    /**
     * Get the selection entry for the <i>choice</i> side
     *
     * @return selection entry for the <i>choice</i> side
     */
    public ProcessQASelectionEntry getChoice() {
        return this.choice;
    }

    /**
     * Get the selection entry for the <i>target</i> side
     *
     * @return selection entry for the <i>target</i> side
     */
    public ProcessQASelectionEntry getTarget() {
        return this.target;
    }

    /**
     * Callback for selected target Process
     */
    public void targetProcessSelected() {
        if (this.target.getSelectedProcess() != null) {
            this.initCategorySelections(this.target.getSelectedProcess());
            this.updateChoiceClIdFilter();
        }
    }

    /**
     * Update clId filter for choice
     */
    private void updateChoiceClIdFilter() {
        String clFilter = null;
        if (CollectionUtils.isNotEmpty(this.categorySelections)) {
            for (int i = 1; i < this.categorySelections.size(); i++) {
                CategorySelection cSel = this.categorySelections.get(i);
                if (StringUtils.isNotBlank(cSel.getSelected())) {
                    clFilter = cSel.getSelected();
                } else {
                    break;
                }
            }
        }
        if (clFilter != null)
            this.choice.setFilterClId(clFilter);
        else
            this.choice.setFilterClId("");
    }

    /**
     * Callback for selected target data stock
     */
    public void targetDatastockSelected() {
        if (StringUtils.isBlank(this.target.getSelectedDsMetaName())) {
            this.choice.setSelectedProcess(null);
            this.choice.getRefFlowsMeta().clear();
        } else {
            this.choice.loadProcessList();
        }
    }

    /**
     * Get the category selections
     *
     * @return category selections
     */
    public List<CategorySelection> getCategorySelections() {
        return this.categorySelections;
    }

    /**
     * Initialize category selection
     *
     * @param p process to use
     */
    private void initCategorySelections(Process p) {
        ClassificationDao cDao = new ClassificationDao();
        this.categorySelections.clear();

        Classification c = this.target.getSelectedProcess().getClassification();
        if (c == null)
            return;
        int selIdx = 0;

        // first: selection of classification
        CategorySelection selC = new CategorySelection(selIdx++);
        selC.selected = c.getName();
        for (String cName : cDao.getCategorySystemNames()) {
            selC.items.add(new SelectItem(cName, cName));
        }
        this.categorySelections.add(selC);

        List<ClClass> clClasses = c.getClasses();
        Collections.sort(clClasses, new Comparator<ClClass>() {

            @Override
            public int compare(ClClass o1, ClClass o2) {
                // return Integer.compare( o1.getLevel(), o2.getLevel() );
                // as long as we're still deployed on environments with Java 6, use Java 6 compatible variant
                return Integer.valueOf(o1.getLevel()).compareTo(Integer.valueOf(o2.getLevel()));
            }
        });
        for (ClClass clClass : clClasses) {
            this.loadClClassLevel(clClass.getLevel(), clClass.getClId());
        }

    }

    /**
     * Load ClClass
     *
     * @param level     the level to load
     * @param label     the label to apply (temp)
     * @param selection selection to do
     */
    private void loadClClassLevel(int level, String selection) {

        ClassificationDao cDao = new ClassificationDao();
        List<CategorySelection> newCategorySelections = new ArrayList<CategorySelection>();

        CategorySelection selClassficationSystem = this.categorySelections.get(0);
        final String classificationSystem = selClassficationSystem.getSelected();

        newCategorySelections.add(selClassficationSystem);

        if (level == -1) {
            this.categorySelections = newCategorySelections;
            return;
        }

        List<ClClass> classesForLevel = null;

        // top classes of a classification
        if (level == 0) {
            if (StringUtils.isNotBlank(selClassficationSystem.getSelected())) {
                classesForLevel = cDao.getTopClasses(DataSetType.PROCESS, classificationSystem, this.availableStocks.getAllStocksMeta().toArray(new IDataStockMetaData[0]));
            }
        } else {
            try {
                List<String> path = new ArrayList<String>();
                for (int i = 1; i <= level; i++) {
                    CategorySelection c = this.categorySelections.get(i);
                    final String cSelName = c.getSelectedName();
                    if (cSelName != null) {
                        path.add(cSelName);
                        newCategorySelections.add(c);
                    } else {
                        break;
                    }
                }
                // all was ok => size is valid
                if (path.size() == level) {
                    classesForLevel = cDao.getSubClasses(DataSetType.PROCESS, classificationSystem, path, false, this.availableStocks.getAllStocksMeta().toArray(
                            new IDataStockMetaData[0]));
                }
            } catch (IndexOutOfBoundsException e) {
                // if no categories are present, we're done
                return;
            }

        }

        if (CollectionUtils.isNotEmpty(classesForLevel)) {
            CategorySelection catSel = new CategorySelection(level + 1);
            for (ClClass clz : classesForLevel) {
                catSel.items.add(new SelectItem(clz.getClId(), clz.getName()));
            }
            if (selection != null) {
                catSel.selected = selection;
            }
            newCategorySelections.add(catSel);
        }

        this.categorySelections = newCategorySelections;
    }

    /**
     * Start comparison (by redirecting to the compare site with the uuids and versions from {@link #getChoice() choice}
     * and {@link #getTarget() target}
     *
     * @return URL (with faces redirect) to compare site (<code>compareDatasets.xhtml</code>)
     */
    public String startCompare() {
        StringBuilder sb = new StringBuilder("/admin/compareDatasets.xhtml?dataset=");
        final Process selProcTarget = this.getTarget().getSelectedProcess();
        sb.append(selProcTarget.getUuidAsString()).append("_").append(selProcTarget.getDataSetVersion());

        if (this.getChoice().getSelectedProcess() != null) {
            final Process selProcChoice = this.getChoice().getSelectedProcess();
            sb.append("&dataset=").append(selProcChoice.getUuidAsString()).append("_").append(selProcChoice.getDataSetVersion());
        }

        if (this.categorySelections != null) {
            String cat = null;
            for (CategorySelection catSel : this.categorySelections) {
                if (StringUtils.isBlank(catSel.getSelected())) {
                    break;
                }
                cat = catSel.getSelected();
            }
            if (cat != null) {
                sb.append("&category=").append(SodaUtil.encodeURLComponent(cat));
            }
        }

        sb.append("&faces-redirect=true");

        return sb.toString();
    }

    /**
     * Helper method to build key for accessing property file value
     *
     * @param matPropDefName the name
     * @return {@link SodaUtil#getValueFromMatPropKey(String)}
     * @see SodaUtil#getValueFromMatPropKey(String)
     */
    public String getValueFromMatPropKey(String matPropDefName) {
        return SodaUtil.getValueFromMatPropKey(matPropDefName);
    }

    /**
     * Selection entry for process quality assurance
     */
    public class ProcessQASelectionEntry {

        /**
         * Reference flow meta data
         */
        private final List<RefFlowMeta> refFlowsMeta = new ArrayList<RefFlowMeta>();
        /**
         * List of processes
         */
        private List<Process> processes = new ArrayList<Process>();
        /**
         * List of available versions of the selected process
         */
        private List<Process> availableVersions = new ArrayList<Process>();
        /**
         * Selected data stock meta data
         */
        private String selectedDsMetaName;
        /**
         * Selected process
         */
        private Process selectedProcess;
        /**
         * Filter for CLID of process' category classes
         */
        private String filterClId;

        /**
         * Load processes and set them via {@link #setProcesses(List)}
         */
        private void loadProcessList() {
            if (StringUtils.isNotBlank(this.getSelectedDsMetaName())) {
                try {
                    ProcessDao dao = new ProcessDao();
                    if (StringUtils.isBlank(this.filterClId)) {
                        this.processes = dao.lsearch(null, new IDataStockMetaData[]{ProcessQASelectionHandler.this.availableStocks.getStock(this.getSelectedDsMetaName())});
                    } else {
                        this.processes = dao.getByClassClid(this.filterClId, ProcessQASelectionHandler.this.availableStocks.getStock(this.getSelectedDsMetaName()));
                        if (ProcessQASelectionHandler.this.target.getSelectedProcess() != null) {
                            this.processes.remove(ProcessQASelectionHandler.this.target.getSelectedProcess());
                        }
                    }
                    return;
                } catch (Exception e) {
                }
            }
            this.processes = new ArrayList<Process>();
        }

        /**
         * Set the filter for CLID of process' category classes
         *
         * @param filterClId filter for CLID of process' category classes to set
         */
        public void setFilterClId(String filterClId) {
            this.filterClId = filterClId;
            this.loadProcessList();
        }

        /**
         * Get the selected data stock meta name
         *
         * @return selected data stock meta name
         */
        public String getSelectedDsMetaName() {
            return this.selectedDsMetaName;
        }

        /**
         * Set the selected data stock meta
         *
         * @param selectedDsMetaName the data stock name to set
         */
        public void setSelectedDsMetaName(String selectedDsMetaName) {
            if (StringUtils.isNotBlank(selectedDsMetaName)) {

                IDataStockMetaData dsm = ProcessQASelectionHandler.this.availableStocks.getStock(selectedDsMetaName);
                SecurityUtil.assertCanRead(dsm);

                this.selectedDsMetaName = selectedDsMetaName;

                this.selectedProcess = null;
                this.refFlowsMeta.clear();

                this.loadProcessList();
            } else {
                this.selectedProcess = null;
                this.refFlowsMeta.clear();
                this.processes = new ArrayList<Process>();
                this.selectedDsMetaName = null;
            }
        }

        /**
         * Get the list of processes
         *
         * @return list of processes
         */
        public List<Process> getProcesses() {
            return this.processes;
        }

        /**
         * Set the list of processes
         *
         * @param processes list of processes to set
         */
        protected void setProcesses(List<Process> processes) {
            this.processes = processes;
        }

        /**
         * Auto completion for processes
         *
         * @param q query string
         * @return filtered processes
         */
        public List<Process> completeProcesses(String q) {
            List<Process> foo = new ArrayList<Process>();
            for (Process p : this.processes) {
                if (StringUtils.containsIgnoreCase(SodaUtil.getLStringValueWithFallback(ProcessQASelectionHandler.this.getLang(), p.getName()), q)) {
                    foo.add(p);
                }
            }
            return foo;
        }

        /**
         * Get the available versions
         *
         * @return available versions
         */
        public List<Process> getAvailableVersions() {
            return availableVersions;
        }

        /**
         * Get the selected process
         *
         * @return selected process
         */
        public Process getSelectedProcess() {
            return this.selectedProcess;
        }

        /**
         * Set the selected process
         *
         * @param selectedProcess process to set
         */
        public void setSelectedProcess(Process selectedProcess) {
            this.selectedProcess = selectedProcess;
            this.loadReferenceFlowsMetaDataAndOtherVersions(selectedProcess);
        }

        /**
         * Get reference flows meta data
         *
         * @return reference flows meta data
         */
        public List<RefFlowMeta> getRefFlowsMeta() {
            return this.refFlowsMeta;
        }

        /**
         * Load meta data of reference flows
         *
         * @param p process to load for
         */
        private void loadReferenceFlowsMetaDataAndOtherVersions(Process p) {
            this.refFlowsMeta.clear();
            if (p != null) {
                try {
                    // load other versions only if a new data set uuid is selected
                    boolean loadVersions = true;
                    if (CollectionUtils.isNotEmpty(this.availableVersions)) {
                        loadVersions = !StringUtils.equals(this.getAvailableVersions().get(0).getUuidAsString(), p.getUuidAsString());
                    }
                    if (loadVersions) {
                        ProcessDao procDao = new ProcessDao();
                        this.availableVersions.clear();
                        this.availableVersions.add(p);

                        List<Process> otherVersions = procDao.getOtherVersions(p);
                        if (CollectionUtils.isNotEmpty(otherVersions)) {
                            this.availableVersions.addAll(otherVersions);
                        }
                    }

                    ProductFlowDao pFlowDao = new ProductFlowDao();
                    if (p.getQuantitativeReference() != null) {
                        List<IReferenceFlow> refFlows = p.getQuantitativeReference().getReferenceFlows();
                        if (CollectionUtils.isNotEmpty(refFlows)) {
                            for (IReferenceFlow refFlow : refFlows) {
                                RefFlowMeta meta = new RefFlowMeta();
                                meta.refFlow = refFlow;

                                // current implementation uses exchange instances as IRefFlow implementation
                                // => get flow directly
                                if (refFlow instanceof Exchange) {
                                    Exchange ex = (Exchange) refFlow;
                                    Flow exFlow = ex.getFlowWithSoftReference();
                                    if (exFlow instanceof ProductFlow && CollectionUtils.isNotEmpty(((ProductFlow) exFlow).getMaterialProperties())) {
                                        meta.matProps = ((ProductFlow) exFlow).getMaterialProperties();
                                    }
                                }

                                // if not an exchange, get from DB
                                else {
                                    ProductFlow pFlow = pFlowDao.getByUuid(refFlow.getReference().getRefObjectId());
                                    if (CollectionUtils.isNotEmpty(pFlow.getMaterialProperties())) {
                                        meta.matProps = pFlow.getMaterialProperties();
                                    }
                                }


                                this.refFlowsMeta.add(meta);
                            }
                        }
                    }
                } catch (Exception e) {
                }

            }
        }


    }

    /**
     * Reference flow meta data
     */
    public class RefFlowMeta {

        /**
         * Reference flow
         */
        private IReferenceFlow refFlow;

        /**
         * Material properties
         */
        private Set<MaterialProperty> matProps;

        /**
         * Get the reference flow
         *
         * @return reference flow
         */
        public IReferenceFlow getRefFlow() {
            return this.refFlow;
        }

        /**
         * Get the material properties
         *
         * @return material properties
         */
        public Set<MaterialProperty> getMatProps() {
            return this.matProps;
        }

    }

    /**
     * Category selection wrapper
     */
    public class CategorySelection {

        /**
         * Index in list
         */
        private final int index;

        /**
         * The items
         */
        private final List<SelectItem> items = new ArrayList<SelectItem>();

        /**
         * Selected value (clId!)
         */
        private String selected;

        /**
         * Create wrapper
         *
         * @param index index in list
         */
        private CategorySelection(int index) {
            super();
            this.index = index;
        }

        /**
         * Get the items
         *
         * @return items
         */
        public List<SelectItem> getItems() {
            return this.items;
        }

        /**
         * Get selected <code>clId</code>
         *
         * @return selected <code>clId</code>
         */
        public String getSelected() {
            return this.selected;
        }

        /**
         * Set the selected <code>clId</code>
         *
         * @param selected <code>clId</code>
         */
        public void setSelected(String selected) {
            this.selected = selected;
        }

        /**
         * Callback after selection
         */
        public void postSelection() {
            int loadLevel = this.selected != null ? this.index : (this.index - 1);
            ProcessQASelectionHandler.this.loadClClassLevel(loadLevel, this.selected);
            ProcessQASelectionHandler.this.updateChoiceClIdFilter();
        }

        /**
         * Get the index
         *
         * @return index
         */
        public int getIndex() {
            return this.index;
        }

        /**
         * Get name of class of selected <code>clId</code>
         *
         * @return name of class of selected <code>clId</code>
         */
        public String getSelectedName() {
            if (this.selected != null) {
                for (SelectItem i : this.items) {
                    if (this.selected.equals(i.getValue())) {
                        return i.getLabel();
                    }
                }
            }
            return null;
        }
    }

}
