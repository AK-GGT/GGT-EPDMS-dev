package de.iai.ilcd.model.dao;

import de.fzk.iai.ilcd.api.app.contact.ContactDataSet;
import de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.IContactListVO;
import de.fzk.iai.ilcd.service.model.IContactVO;
import de.iai.ilcd.model.common.DataSet;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.datastock.RootDataStock;
import de.iai.ilcd.util.UnmarshalHelper;
import org.apache.velocity.tools.generic.ValueParser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Data access object for {@link Contact contacts}
 */
public class ContactDao extends DataSetDao<Contact, IContactListVO, IContactVO> {

    /**
     * Create the data access object for {@link Contact contacts}
     */
    public ContactDao() {
        super("Contact", Contact.class, IContactListVO.class, IContactVO.class, DataSetType.CONTACT);
    }

    @Override
    protected String getDataStockField() {
        return DatasetTypes.CONTACTS.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preCheckAndPersist(Contact dataSet) {
        // Nothing to do for contacts :)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getQueryStringOrderJpql(String typeAlias, String sortString, boolean sortOrder) {
        if ("email".equals(sortString)) {
            return buildOrderBy(typeAlias, "email", typeAlias, "nameCache", sortOrder);
        } else if ("www".equals(sortString)) {
            return buildOrderBy(typeAlias, "www", typeAlias, "nameCache", sortOrder);
        } else {
            return super.getQueryStringOrderJpql(typeAlias, sortString, sortOrder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addWhereClausesAndNamedParamesForQueryStringJpql(String typeAlias, ValueParser params, List<String> whereClauses,
                                                                    Map<String, Object> whereParamValues) {
        // nothing to do beside the defaults
    }

    /* (non-Javadoc)
     * @see de.iai.ilcd.model.dao.DataSetDao#getDependencies(de.iai.ilcd.model.common.DataSet, de.iai.ilcd.model.dao.DependenciesMode)
     */
    @Override
    public Set<DataSet> getDependencies(DataSet dataset, DependenciesMode mode) {
        return getDependencies(dataset, mode, null);
    }

    @Override
    public Set<DataSet> getDependencies(DataSet dataSet, DependenciesMode depMode, Collection<String> ignoreList) {
        Set<DataSet> dependencies = new HashSet<>();
        Contact contact = (Contact) dataSet;
        RootDataStock stock = contact.getRootDataStock();
        final List<String> ignoreListFinal = ignoreList != null ? new ArrayList<>(ignoreList) : new ArrayList<>();

        ContactDataSet xmlDataset = (ContactDataSet) new UnmarshalHelper().unmarshal(contact);

        GlobalReferenceType tmp;
        List<GlobalReferenceType> tmpList;
        try {
            tmpList = xmlDataset.getAdministrativeInformation().getDataEntryBy().getReferenceToDataSetFormat().stream()
                    .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                    .collect(Collectors.toList());

            if (!tmpList.isEmpty())
                addDependencies(tmpList, stock, dependencies);

        } catch (Exception e) {
            // None found, none added
        }
        try {
            tmp = xmlDataset.getAdministrativeInformation().getPublicationAndOwnership()
                    .getReferenceToOwnershipOfDataSet();
            if (tmp != null && !ignoreListFinal.contains(tmp.getRefObjectId()))
                addDependency(tmp, stock, dependencies);

        } catch (Exception e) {
            // None found, none added
        }
        try {
            tmpList = xmlDataset.getContactInformation().getDataSetInformation().getReferenceToContact().stream()
                    .filter(ref -> ref != null && !ignoreListFinal.contains(ref.getRefObjectId()))
                    .collect(Collectors.toList());

            if (!tmpList.isEmpty())
                addDependencies(tmpList, stock, dependencies);
        } catch (Exception e) {
            // None found, none added
        }
        try {
            tmp = xmlDataset.getContactInformation().getDataSetInformation().getReferenceToLogo();
            if (tmp != null && !ignoreListFinal.contains(tmp.getRefObjectId()))
                addDependency(tmp, stock, dependencies);
        } catch (Exception e) {
            // None found, none added
        }
        return dependencies;
    }

    @Override
    public Contact getSupersedingDataSetVersion(String uuid) {
        // TODO Auto-generated method stub
        return null;
    }
}
