package de.iai.ilcd.xml.read;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.contact.Contact;
import org.apache.commons.jxpath.JXPathContext;

import java.io.PrintWriter;

/**
 * @author clemens.duepmeier
 */
public class ContactReader extends DataSetReader<Contact> {

    @Override
    public Contact parse(JXPathContext context, PrintWriter out) {

        context.registerNamespace("ilcd", "http://lca.jrc.it/ILCD/Contact");

        Contact contact = new Contact();

        // OK, now read in all fields common to all DataSet types
        this.readCommonFields(contact, DataSetType.CONTACT, context, out);

        IMultiLangString shortName = this.parserHelper.getIMultiLanguageString("/ilcd:contactDataSet/ilcd:contactInformation/ilcd:dataSetInformation/common:shortName");
        String contactAddress = this.parserHelper.getStringValue("/ilcd:contactDataSet/ilcd:contactInformation/ilcd:dataSetInformation/ilcd:contactAddress");
        String phone = this.parserHelper.getStringValue("/ilcd:contactDataSet/ilcd:contactInformation/ilcd:dataSetInformation/ilcd:telephone");
        String fax = this.parserHelper.getStringValue("/ilcd:contactDataSet/ilcd:contactInformation/ilcd:dataSetInformation/ilcd:telefax");
        String email = this.parserHelper.getStringValue("/ilcd:contactDataSet/ilcd:contactInformation/ilcd:dataSetInformation/ilcd:email");
        String homePage = this.parserHelper.getStringValue("/ilcd:contactDataSet/ilcd:contactInformation/ilcd:dataSetInformation/ilcd:WWWAddress");
        String ccPoint = this.parserHelper.getStringValue("/ilcd:contactDataSet/ilcd:contactInformation/ilcd:dataSetInformation/ilcd:centralContactPoint");
        IMultiLangString contactDescription = this.parserHelper.getIMultiLanguageString("/ilcd:contactDataSet/ilcd:contactInformation/ilcd:dataSetInformation/ilcd:contactDescriptionOrComment");

        if (out != null) {
            if (shortName == null || contact.getName() == null) {
                out.println("Warning: One of the fields 'name' or 'shortName' of the contact data set is empty");
            }
        }
        contact.setShortName(shortName);
        if (contact.getName() == null) {
            contact.setName(shortName);
        }
        contact.setContactAddress(contactAddress);
        contact.setPhone(phone);
        contact.setFax(fax);
        contact.setEmail(email);
        contact.setWww(homePage);
        contact.setCentralContactPoint(ccPoint);
        contact.setDescription(contactDescription);

        return contact;
    }
}
