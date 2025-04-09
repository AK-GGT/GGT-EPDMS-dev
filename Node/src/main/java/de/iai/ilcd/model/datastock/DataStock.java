package de.iai.ilcd.model.datastock;

import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import de.iai.ilcd.model.lciamethod.LCIAMethod;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.model.unitgroup.UnitGroup;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Stock
 */
@Entity
@DiscriminatorValue("ds")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class DataStock extends AbstractDataStock {

    @ManyToMany
    protected Set<Process> processes = new HashSet<Process>();

    @ManyToMany
    protected Set<ElementaryFlow> elementaryFlows = new HashSet<ElementaryFlow>();

    @ManyToMany
    protected Set<ProductFlow> productFlows = new HashSet<ProductFlow>();

    @ManyToMany
    protected Set<FlowProperty> flowProperties = new HashSet<FlowProperty>();

    @ManyToMany
    protected Set<UnitGroup> unitGroups = new HashSet<UnitGroup>();

    @ManyToMany
    protected Set<Source> sources = new HashSet<Source>();

    @ManyToMany
    protected Set<Contact> contacts = new HashSet<Contact>();

    @ManyToMany
    protected Set<LCIAMethod> lciaMethods = new HashSet<LCIAMethod>();

    @ManyToMany
    protected Set<LifeCycleModel> lifeCycleModels = new HashSet<LifeCycleModel>();

    public DataStock() {
        super();
    }

    /**
     * {@inheritDoc}
     *
     * @return <code>false</code>
     */
    @Override
    public boolean isRoot() {
        return false;
    }

    /**
     * Get all contacts of this data stock
     *
     * @return contacts of this data stock
     */
    public Set<Contact> getContacts() {
        return this.contacts;
    }

    /**
     * Set the contacts of this data stock
     *
     * @param contacts contacts to set
     */
    protected void setContacts(Set<Contact> contacts) {
        this.contacts = contacts;
    }

    /**
     * Get all flow properties of this data stock
     *
     * @return flow properties of this data stock
     */
    public Set<FlowProperty> getFlowProperties() {
        return this.flowProperties;
    }

    /**
     * Set the flow properties of this data stock
     *
     * @param flowProperties flow properties to set
     */
    protected void setFlowProperties(Set<FlowProperty> flowProperties) {
        this.flowProperties = flowProperties;
    }

    /**
     * Get all processes of this data stock
     *
     * @return processes of this data stock
     */
    public Set<Process> getProcesses() {
        return this.processes;
    }

    /**
     * Set the processes of this data stock
     *
     * @param processes processes to set
     */
    protected void setProcesses(Set<Process> processes) {
        this.processes = processes;
    }

    /**
     * Get all sources of this data stock
     *
     * @return sources of this data stock
     */
    public Set<Source> getSources() {
        return this.sources;
    }

    /**
     * Set the sources of this data stock
     *
     * @param sources sources to set
     */
    protected void setSources(Set<Source> sources) {
        this.sources = sources;
    }

    /**
     * Get all unit groups of this data stock
     *
     * @return unit groups of this data stock
     */
    public Set<UnitGroup> getUnitGroups() {
        return this.unitGroups;
    }

    /**
     * Set the unit groups of this data stock
     *
     * @param unitGroups unit groups to set
     */
    protected void setUnitGroups(Set<UnitGroup> unitGroups) {
        this.unitGroups = unitGroups;
    }

    /**
     * Get all LCIA methods of this data stock
     *
     * @return LCIA methods of this data stock
     */
    public Set<LCIAMethod> getLciaMethods() {
        return this.lciaMethods;
    }

    /**
     * Set the LCIA methods of this data stock
     *
     * @param lciaMethods LCIA methods to set
     */
    protected void setLciaMethods(Set<LCIAMethod> lciaMethods) {
        this.lciaMethods = lciaMethods;
    }

    /**
     * Get all elementary flows of this data stock
     *
     * @return elementary flows of this data stock
     */
    public Set<ElementaryFlow> getElementaryFlows() {
        return this.elementaryFlows;
    }

    /**
     * Set the elementary flows of this data stock
     *
     * @param elementaryFlows elementary flows to set
     */
    protected void setElementaryFlows(Set<ElementaryFlow> elementaryFlows) {
        this.elementaryFlows = elementaryFlows;
    }

    /**
     * Get all product flows of this data stock
     *
     * @return product flows of this data stock
     */
    public Set<ProductFlow> getProductFlows() {
        return this.productFlows;
    }

    /**
     * Set the product flows of this data stock
     *
     * @param productFlows product flows to set
     */
    protected void setProductFlows(Set<ProductFlow> productFlows) {
        this.productFlows = productFlows;
    }

    /**
     * Get all life-cycle models of this data stock
     *
     * @return life-cycle models of this data stock
     */
    public Set<LifeCycleModel> getLifeCycleModels() {
        return lifeCycleModels;
    }

    /**
     * Set the life-cycle models of this data stock
     *
     * @param lifeCycleModels life-cycle models to set
     */
    public void setLifeCycleModels(Set<LifeCycleModel> lifeCycleModels) {
        this.lifeCycleModels = lifeCycleModels;
    }

    /**
     * Add process to this data stock
     *
     * @param p process to add
     */
    public void addProcess(Process p) {
        this.addDataset(p, this.processes);
    }

    /**
     * Remove process from this data stock
     *
     * @param p process to remove
     */
    public void removeProcess(Process p) {
        this.removeDataset(p, this.processes);
    }

    /**
     * Add LCIA method to this data stock
     *
     * @param m LCIA method to add
     */
    public void addLCIAMethod(LCIAMethod m) {
        this.addDataset(m, this.lciaMethods);
    }

    /**
     * Remove LCIA method from this data stock
     *
     * @param m LCIA method to remove
     */
    public void removeLCIAMethod(LCIAMethod m) {
        this.removeDataset(m, this.lciaMethods);
    }

    /**
     * Add elementary flow to this data stock
     *
     * @param f elementary flow to add
     */
    public void addElementaryFlow(ElementaryFlow f) {
        this.addDataset(f, this.elementaryFlows);
    }

    /**
     * Remove elementary flow from this data stock
     *
     * @param f elementary flow to remove
     */
    public void removeElementaryFlow(ElementaryFlow f) {
        this.removeDataset(f, this.elementaryFlows);
    }

    /**
     * Add product flow to this data stock
     *
     * @param f product flow to add
     */
    public void addProductFlow(ProductFlow f) {
        this.addDataset(f, this.productFlows);
    }

    /**
     * Remove product flow from this data stock
     *
     * @param f product flow to remove
     */
    public void removeProductFlow(ProductFlow f) {
        this.removeDataset(f, this.productFlows);
    }

    /**
     * Add flow property to this data stock
     *
     * @param p flow property to add
     */
    public void addFlowProperty(FlowProperty p) {
        this.addDataset(p, this.flowProperties);
    }

    /**
     * Remove flow property from this data stock
     *
     * @param p flow property to remove
     */
    public void removeFlowProperty(FlowProperty p) {
        this.removeDataset(p, this.flowProperties);
    }

    /**
     * Add unit group to this data stock
     *
     * @param ug unit group to add
     */
    public void addUnitGroup(UnitGroup ug) {
        this.addDataset(ug, this.unitGroups);
    }

    /**
     * Remove unit group from this data stock
     *
     * @param ug unit group to remove
     */
    public void removeUnitGroup(UnitGroup ug) {
        this.removeDataset(ug, this.unitGroups);
    }

    /**
     * Add source to this data stock
     *
     * @param s source to add
     */
    public void addSource(Source s) {
        this.addDataset(s, this.sources);
    }

    /**
     * Remove source from this data stock
     *
     * @param s source to remove
     */
    public void removeSource(Source s) {
        this.removeDataset(s, this.sources);
    }

    /**
     * Add contact to this data stock
     *
     * @param c contact to add
     */
    public void addContact(Contact c) {
        this.addDataset(c, this.contacts);
    }

    /**
     * Remove contact from this data stock
     *
     * @param c contact to remove
     */
    public void removeContact(Contact c) {
        this.removeDataset(c, this.contacts);
    }

    /**
     * Add lifecyclemodel to this data stock
     *
     * @param l lifecyclemodel to add
     */

    public void addLifeCycleModel(LifeCycleModel l) {
        this.addDataset(l, this.lifeCycleModels);
    }

    /**
     * Remove lifecyclemodel from this data stock
     *
     * @param l lifecyclemodel to remove
     */
    public void removeLifeCycleModel(LifeCycleModel l) {
        this.removeDataset(l, this.lifeCycleModels);
    }

    /**
     * Remove a data set from given list (and handle bi-directonal assignment)
     *
     * @param dataset dataset to remove
     * @param list    list to remove from
     */
    private <T extends DataSet> void removeDataset(T dataset, Set<T> list) {
        if (list.contains(dataset)) {
            list.remove(dataset);
            dataset.removeFromDataStock(this);
        }
    }

    /**
     * Add a data set from given list (and handle bi-directonal assignment)
     *
     * @param dataset dataset to add
     * @param list    list to add from
     */
    private <T extends DataSet> void addDataset(T dataset, Set<T> list) {
        if (!list.contains(dataset)) {
            list.add(dataset);
            dataset.addToDataStock(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataStock) {
            return super.equals(obj);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "de.iai.ilcd.model.datastock.DataStock[name=" + this.getName() + "]";
    }

}
