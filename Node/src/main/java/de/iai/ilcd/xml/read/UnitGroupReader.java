package de.iai.ilcd.xml.read;

import de.fzk.iai.ilcd.service.model.common.IMultiLangString;
import de.iai.ilcd.model.common.DataSetType;
import de.iai.ilcd.model.unitgroup.Unit;
import de.iai.ilcd.model.unitgroup.UnitGroup;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author clemens.duepmeier
 */
public class UnitGroupReader extends DataSetReader<UnitGroup> {

    public static Logger logger = LogManager.getLogger(de.iai.ilcd.xml.read.UnitGroupReader.class);

    Namespace unitGroupNamespace = Namespace.getNamespace("ilcd", "http://lca.jrc.it/ILCD/UnitGroup");

    @Override
    public UnitGroup parse(JXPathContext context, PrintWriter out) {

        context.registerNamespace("ilcd", "http://lca.jrc.it/ILCD/UnitGroup");

        UnitGroup ugroup = new UnitGroup();

        // OK, now read in all fields common to all DataSet types
        this.readCommonFields(ugroup, DataSetType.UNITGROUP, context, out);

        // get all the units and set the reference unit
        String referenceUnitString = this.parserHelper.getStringValue("/ilcd:unitGroupDataSet/ilcd:unitGroupInformation/ilcd:quantitativeReference/ilcd:referenceToReferenceUnit");
        Integer referenceIndex = (referenceUnitString != null ? Integer.parseInt(referenceUnitString) : null);

        List<Unit> units = this.readUnits(context);
        for (Unit unit : units) {
            ugroup.addUnit(unit);
            if (referenceIndex != null && referenceIndex == unit.getInternalId()) {
                ugroup.setReferenceUnit(unit);
            }
        }

        return ugroup;
    }

    private List<Unit> readUnits(JXPathContext context) {
        List<Unit> units = new ArrayList<Unit>();

        List<Element> unitElements = this.parserHelper.getElements("/ilcd:unitGroupDataSet/ilcd:units/ilcd:unit");

        for (Element elem : unitElements) {
            String internalId = elem.getAttributeValue("dataSetInternalID");
            String meanValue = elem.getChild("meanValue", this.unitGroupNamespace).getText();
            String name = elem.getChild("name", this.unitGroupNamespace).getText();
            // logger.debug("name of unit: " + name);
            IMultiLangString description = this.parserHelper.getIMultiLanguageString(elem, "generalComment", this.unitGroupNamespace);
            // logger.debug("unit beschreibung: " +
            // description.getDefaultValue());

            Unit unit = new Unit();
            unit.setInternalId(Integer.parseInt(internalId));
            // logger.debug("parse meanValue: " +meanValue);
            unit.setMeanValue(Double.parseDouble(meanValue));
            unit.setName(name);
            unit.setDescription(description);

            units.add(unit);
        }

        return units;
    }
}
