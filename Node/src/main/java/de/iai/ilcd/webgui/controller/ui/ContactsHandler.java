package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.dao.ContactDao;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

/**
 * Backing bean for contact list view
 */
@ManagedBean()
@ViewScoped
public class ContactsHandler extends AbstractDataSetsHandler<Contact, ContactDao> implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 7754645203930137310L;

    /**
     * Initialize Handler
     */
    public ContactsHandler() {
        super(Contact.class, new ContactDao());
    }

}
