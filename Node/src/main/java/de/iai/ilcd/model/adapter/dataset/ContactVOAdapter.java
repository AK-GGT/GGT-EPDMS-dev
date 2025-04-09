package de.iai.ilcd.model.adapter.dataset;

import de.fzk.iai.ilcd.service.client.impl.vo.dataset.ContactDataSetVO;
import de.fzk.iai.ilcd.service.model.IContactListVO;
import de.fzk.iai.ilcd.service.model.IContactVO;
import de.iai.ilcd.model.adapter.LStringAdapter;

/**
 * Adapter for Contacts
 */
public class ContactVOAdapter extends AbstractDatasetAdapter<ContactDataSetVO, IContactListVO, IContactVO> {

    /**
     * Create adapter for {@link IContactListVO contact list value object}
     *
     * @param adaptee list value object to adapt
     */
    public ContactVOAdapter(IContactListVO adaptee) {
        super(new ContactDataSetVO(), adaptee);
    }

    /**
     * Create adapter for the {@link IContactVO contact value object}
     *
     * @param adaptee value object to adapt
     */
    public ContactVOAdapter(IContactVO adaptee) {
        super(new ContactDataSetVO(), adaptee);
    }

    /**
     * Create adapter for {@link IContactListVO contact list value object}
     *
     * @param adaptee list value object to adapt
     */
    public ContactVOAdapter(IContactListVO adaptee, String language) {
        super(new ContactDataSetVO(), adaptee, language);
    }

    /**
     * Create adapter for {@link IContactListVO contact list value object}
     *
     * @param adaptee list value object to adapt
     */
    public ContactVOAdapter(IContactListVO adaptee, String language, boolean langFallback) {
        super(new ContactDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * Create adapter for the {@link IContactVO contact value object}
     *
     * @param adaptee value object to adapt
     */
    public ContactVOAdapter(IContactVO adaptee, String language) {
        super(new ContactDataSetVO(), adaptee, language);
    }

    /**
     * Create adapter for the {@link IContactVO contact value object}
     *
     * @param adaptee value object to adapt
     */
    public ContactVOAdapter(IContactVO adaptee, String language, boolean langFallback) {
        super(new ContactDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(IContactListVO src, ContactDataSetVO dst) {
        dst.setEmail(src.getEmail());

        dst.setPhone(src.getPhone());

        dst.setWww(src.getWww());

        LStringAdapter.copyLStrings(src.getShortName(), dst.getShortName(), this.language, this.langFallback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(IContactVO src, ContactDataSetVO dst) {
        this.copyValues((IContactListVO) src, dst);

        dst.setCentralContactPoint(src.getCentralContactPoint());
        dst.setFax(src.getFax());
    }

}
