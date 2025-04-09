package de.iai.ilcd.model.adapter.dataset;

import de.fzk.iai.ilcd.service.client.impl.vo.dataset.LifeCycleModelDataSetVO;
import de.fzk.iai.ilcd.service.model.ILifeCycleModelListVO;
import de.fzk.iai.ilcd.service.model.ILifeCycleModelVO;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.process.IComplianceSystem;
import de.iai.ilcd.model.adapter.ComplianceSystemAdapter;
import de.iai.ilcd.model.adapter.GlobalReferenceAdapter;
import de.iai.ilcd.model.adapter.LStringAdapter;
import de.iai.ilcd.model.adapter.ReviewAdapter;

import java.util.Set;

public class LifeCycleModelVOAdapter
        extends AbstractDatasetAdapter<LifeCycleModelDataSetVO, ILifeCycleModelListVO, ILifeCycleModelVO> {

    public LifeCycleModelVOAdapter(LifeCycleModelDataSetVO adapterObject, ILifeCycleModelListVO adaptee,
                                   String language, boolean langFallback) {
        super(new LifeCycleModelDataSetVO(), adaptee, language, langFallback);
    }

    public LifeCycleModelVOAdapter(LifeCycleModelDataSetVO adapterObject, ILifeCycleModelListVO adaptee,
                                   String language) {
        super(new LifeCycleModelDataSetVO(), adaptee, language);
    }

    public LifeCycleModelVOAdapter(LifeCycleModelDataSetVO adapterObject, ILifeCycleModelListVO adaptee) {
        super(new LifeCycleModelDataSetVO(), adaptee);
    }

    public LifeCycleModelVOAdapter(LifeCycleModelDataSetVO adapterObject, ILifeCycleModelVO adaptee, String language) {
        super(new LifeCycleModelDataSetVO(), adaptee, language);
    }

    public LifeCycleModelVOAdapter(LifeCycleModelDataSetVO adapterObject, ILifeCycleModelVO adaptee) {
        super(new LifeCycleModelDataSetVO(), adaptee);
    }

    public LifeCycleModelVOAdapter(ILifeCycleModelListVO adaptee, String language, boolean langFallback) {
        super(new LifeCycleModelDataSetVO(), adaptee, language);
    }

    @Override
    protected void copyValues(ILifeCycleModelVO src, LifeCycleModelDataSetVO dst) {
        this.copyValues((ILifeCycleModelListVO) src, dst);

        LStringAdapter.copyLStrings(src.getBaseName(), dst.getBaseName(), this.language);

        ReviewAdapter.copyReviews(src.getReviews(), dst.getReviews(), this.language);

    }

    @Override
    protected void copyValues(ILifeCycleModelListVO src, LifeCycleModelDataSetVO dst) {

        Set<IComplianceSystem> srcComplSystems = src.getComplianceSystems();
        if (srcComplSystems != null) {
            Set<IComplianceSystem> destComplSystems = dst.getComplianceSystems();
            for (IComplianceSystem compSys : srcComplSystems) {
                destComplSystems.add(new ComplianceSystemAdapter(compSys, this.language, this.langFallback));
            }
        }

        if (src.getReferenceToOwnershipOfDataSet() != null)
            for (IGlobalReference ref : src.getReferenceToOwnershipOfDataSet())
                dst.getReferenceToOwnershipOfDataSet()
                        .add(new GlobalReferenceAdapter(ref, this.language, this.langFallback));

        if (src.getReferenceToExternalDocumentation() != null)
            for (IGlobalReference ref : src.getReferenceToExternalDocumentation())
                dst.getReferenceToExternalDocumentation()
                        .add(new GlobalReferenceAdapter(ref, this.language, this.langFallback));

        if (src.getReferenceToDiagram() != null)
            for (IGlobalReference ref : src.getReferenceToDiagram())
                dst.getReferenceToDiagram()
                        .add(new GlobalReferenceAdapter(ref, this.language, this.langFallback));

        if (src.getReferenceToResultingProcess() != null)
            for (IGlobalReference ref : src.getReferenceToResultingProcess())
                dst.getReferenceToResultingProcess()
                        .add(new GlobalReferenceAdapter(ref, this.language, this.langFallback));

        if (src.getReferenceToProcess() != null)
            for (IGlobalReference ref : src.getReferenceToProcess())
                dst.getReferenceToProcess()
                        .add(new GlobalReferenceAdapter(ref, this.language, this.langFallback));
    }

}
