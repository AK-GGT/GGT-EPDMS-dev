package de.iai.ilcd.webgui.controller.ui;

import de.fzk.iai.ilcd.api.binding.generated.lciamethod.LCIAMethodDataSetType;
import de.fzk.iai.ilcd.api.dataset.ILCDTypes;
import de.fzk.iai.ilcd.service.client.impl.DatasetTypes;
import de.fzk.iai.ilcd.service.model.ILCIAMethodVO;
import de.iai.ilcd.model.dao.LCIAMethodDao;
import de.iai.ilcd.model.lciamethod.LCIAMethod;
import de.iai.ilcd.model.lciamethod.LCIAMethodCharacterisationFactor;

import javax.faces.bean.ManagedBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backing bean for source LCIA method view
 */
@ManagedBean(name = "lciamethodHandler")
public class LCIAMethodHandler extends AbstractDataSetHandler<ILCIAMethodVO, LCIAMethod, LCIAMethodDao, LCIAMethodDataSetType> {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 8501172387748377828L;

    private List<LCIAMethodCharacterisationFactor> characterisationFactors;
    private Map<String, String> cats = new HashMap<String, String>();

    /**
     * Initialize handler
     */
    public LCIAMethodHandler() {
        super(new LCIAMethodDao(), DatasetTypes.LCIAMETHODS.getValue(), ILCDTypes.LCIAMETHOD);
    }

    /**
     * Convenience method, delegates to {@link #getDataSet()}
     *
     * @return represented LCIA method instance
     */
    public ILCIAMethodVO getLciamethod() {
        return this.getDataSet();
    }

    @Override
    protected void datasetLoaded(LCIAMethod m) {

        // as the classification of the flows referenced by the CFs are not available directly from the
        // original LCIAmethod dataset, we're getting these from the domain object and providing them
        // to the view

        this.characterisationFactors = m.getCharactarisationFactors();

        for (LCIAMethodCharacterisationFactor cf : this.characterisationFactors) {
            try {
                String cat = cf.getReferencedFlowInstance().getCategorization().getClassHierarchyAsString();
                this.cats.put(cf.getReferencedFlowInstance().getUuidAsString(), cat);
            } catch (Exception e) {
            }
        }

    }

    public String getCategory(String uuid) {
        return this.cats.get(uuid);
    }

    public List<LCIAMethodCharacterisationFactor> getCharacterisationFactors() {
        return this.characterisationFactors;
    }
}
