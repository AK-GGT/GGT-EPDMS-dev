package de.iai.ilcd.rest;

import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.service.client.impl.vo.dataset.ContactDataSetVO;
import de.fzk.iai.ilcd.service.model.IContactListVO;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.dao.ContactDao;
import de.iai.ilcd.model.dao.DataSetDao;
import de.iai.ilcd.model.dao.ProcessDao;
import de.iai.ilcd.model.utils.GlobalReferenceComparator;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Web Service for Contact
 */
@Component
@Path("contacts")
public class ContactResource extends AbstractDataSetResource<Contact> {

    public ContactResource() {
        super(DataSetType.CONTACT, ILCDTypes.CONTACT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataSetDao<Contact, ?, ?> getFreshDaoInstance() {
        return new ContactDao();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getXMLTemplatePath() {
        return "/xml/contact.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDataSetTypeName() {
        return StringUtils.lowerCase(DataSetType.CONTACT.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<Contact> getDataSetType() {
        return Contact.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHTMLDatasetDetailTemplatePath() {
        return "/html/contact.vm";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean userRequiresDatasetDetailRights() {
        return false;
    }

    /**
     * Get the datasetGenerator contacts
     *
     * @return datasetGenerator contacts
     */
    @GET
    @Path("datasetGenerators")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDatasetGenerators(@PathParam("stockIdentifier") String datastock, @QueryParam(AbstractResource.PARAM_FORMAT) String format) {
        ProcessDao pDao = new ProcessDao();

        final List<IGlobalReference> refs = new ArrayList<IGlobalReference>();

        refs.addAll(pDao.getDatasetGenerators(getAvailableDataStocks()));

        refs.sort(new GlobalReferenceComparator());

        List<IContactListVO> result = new ArrayList<IContactListVO>();

        for (IGlobalReference r : refs) {
            ContactDataSetVO c = new ContactDataSetVO();
            c.setUuid(r.getRefObjectId());
            c.setDataSetVersion(r.getVersionAsString());
            c.setShortName(r.getShortDescription().getDefaultValue());
            result.add(c);
        }

        return getResponse(result, result.size(), 0, result.size(), format, null, null, false);
    }

}
