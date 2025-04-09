package de.iai.ilcd.webgui.controller.admin;

import de.iai.ilcd.model.contact.Contact;
import de.iai.ilcd.model.dao.ContactDao;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Admin Contact List handler
 */
@ManagedBean
@ViewScoped
public class ContactListHandler extends AbstractDataSetListHandler<Contact> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 6411168747167696159L;

    /**
     * Create the contact list handler
     */
    public ContactListHandler() {
        super(Contact.class, new ContactDao());
    }

    /**
     * Legacy method for selected item access
     *
     * @return selected items
     * @see #getSelectedItems()
     * @deprecated use {@link #getSelectedItems()}
     */
    @Deprecated
    public Contact[] getSelectedContacts() {
        return this.getSelectedItems();
    }

    /**
     * Legacy method for selected item access
     *
     * @param selItems selected items
     * @see #setSelectedItems(Contact[])
     * @deprecated use {@link #setSelectedItems(Contact[])}
     */
    @Deprecated
    public void setSelectedContacts(Contact[] selItems) {
        this.setSelectedItems(selItems);
    }

    /**
     * Clears all table filters, including the non-default ones.
     */
    public void clearAllFilters() {
        super.clearAllFilters("contactTableForm:contactTable");
    }

}
