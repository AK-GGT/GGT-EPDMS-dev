package de.iai.ilcd.webgui.controller.url;

import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.dao.ContactDao;
import de.iai.ilcd.model.dao.DataSetDao;
import org.apache.commons.lang.StringUtils;

/**
 * URL Generator for contact links
 */
public class ContactURLGenerator extends AbstractDataSetURLGenerator<Contact> {

    /**
     * Create the generator
     *
     * @param bean URL bean
     */
    public ContactURLGenerator(URLGeneratorBean bean) {
        super(bean, StringUtils.lowerCase(DataSetType.CONTACT.name()), DatasetTypes.CONTACTS.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetDao<Contact, ?, ?> createDaoInstance() {
        return new ContactDao();
    }

}
