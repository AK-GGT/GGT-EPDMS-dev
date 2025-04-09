package de.iai.ilcd.webgui.controller.ui;

import de.fzk.iai.ilcd.api.binding.generated.contact.ContactDataSetType;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.IContactVO;
import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.dao.ContactDao;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import javax.faces.bean.ManagedBean;

/**
 * Backing bean for contact detail view
 */
@ManagedBean
public class ContactHandler extends AbstractDataSetHandler<IContactVO, Contact, ContactDao, ContactDataSetType> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 1618629753241533506L;

    /**
     * Initialize backing bean
     */
    public ContactHandler() {
        super(new ContactDao(), DatasetTypes.CONTACTS.getValue(), ILCDTypes.CONTACT);
    }

    /**
     * Convenience method, delegates to {@link #getDataSet()}
     *
     * @return represented contact
     */
    public IContactVO getContact() {
        return this.getDataSet();
    }

    /**
     * Try to check if given email address is valid
     *
     * @param email address to check
     * @return <code>true</code> if email address is valid, else <code>false</code>
     */
    public boolean isValidEMailAddress(String email) {
        if (StringUtils.isNotBlank(email)) {
            if (EmailValidator.getInstance().isValid(email)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}
