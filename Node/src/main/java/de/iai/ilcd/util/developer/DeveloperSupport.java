package de.iai.ilcd.util.developer;

import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.dao.*;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.model.flow.ElementaryFlow;
import de.iai.ilcd.model.flow.ProductFlow;
import de.iai.ilcd.model.flowproperty.FlowProperty;
import de.iai.ilcd.model.lciamethod.LCIAMethod;
import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import de.iai.ilcd.model.process.Process;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import de.iai.ilcd.xml.read.DataSetImporter;
import de.iai.ilcd.xml.zip.exceptions.ZipException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * A class containing several supporting methods for developers.
 *
 * @author sarai
 */
@ManagedBean
@ViewScoped
public class DeveloperSupport {

    /**
     * Deletes all data from database.
     *
     * @throws Exception
     */
    public void deleteAllData() throws Exception {
        ProcessDao processDao = new ProcessDao();
        List<Process> processList = processDao.getAll();
        if (!processList.isEmpty())
            processDao.remove(processList);
        LCIAMethodDao lciaMDao = new LCIAMethodDao();
        List<LCIAMethod> lciaMList = lciaMDao.getAll();
        if (!lciaMList.isEmpty())
            lciaMDao.remove(lciaMList);
        ElementaryFlowDao elemFDao = new ElementaryFlowDao();
        List<ElementaryFlow> elemFList = elemFDao.getAll();
        if (!elemFList.isEmpty())
            elemFDao.remove(elemFList);
        ProductFlowDao prodFDao = new ProductFlowDao();
        List<ProductFlow> prodFList = prodFDao.getAll();
        if (!prodFList.isEmpty())
            prodFDao.remove(prodFList);
        FlowPropertyDao flowPDao = new FlowPropertyDao();
        List<FlowProperty> flowPList = flowPDao.getAll();
        if (!flowPList.isEmpty())
            flowPDao.remove(flowPList);
        UnitGroupDao unitGDao = new UnitGroupDao();
        List<UnitGroup> unitGList = unitGDao.getAll();
        if (!unitGList.isEmpty())
            unitGDao.remove(unitGList);
        SourceDao sourceDao = new SourceDao();
        List<Source> sourceList = sourceDao.getAll();
        if (!sourceList.isEmpty())
            sourceDao.remove(sourceList);
        ContactDao contactDao = new ContactDao();
        List<Contact> contactList = contactDao.getAll();
        if (!contactList.isEmpty())
            contactDao.remove(contactList);
        LifeCycleModelDao lcmDao = new LifeCycleModelDao();
        List<LifeCycleModel> lcmList = lcmDao.getAll();
        if (!lcmList.isEmpty())
            lcmDao.remove(lcmList);
    }

    public void importData() throws IOException, ZipException {
        final String path = System.getProperty("user.home") + "/git/soda4LCA/Node/src/test/resources/sample_data_med.zip";

        CommonDataStockDao dsDao = new CommonDataStockDao();
        RootDataStock rds = dsDao.getRootDataStockByName("default");

        DataSetImporter xmlReader = new DataSetImporter();
        xmlReader.importZipFile(path, new PrintWriter(System.out), rds);
    }

    public void gc() {
        System.gc();
    }

}
