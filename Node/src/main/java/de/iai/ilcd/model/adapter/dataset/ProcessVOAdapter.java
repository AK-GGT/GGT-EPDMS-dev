package de.iai.ilcd.model.adapter.dataset;

import de.fzk.iai.ilcd.service.client.impl.vo.dataset.ProcessDataSetVO;
import de.fzk.iai.ilcd.service.client.impl.vo.types.process.AccessInformationType;
import de.fzk.iai.ilcd.service.client.impl.vo.types.process.LCIMethodInformationType;
import de.fzk.iai.ilcd.service.client.impl.vo.types.process.TimeInformationType;
import de.fzk.iai.ilcd.service.model.IProcessListVO;
import de.fzk.iai.ilcd.service.model.IProcessVO;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.process.IComplianceSystem;
import de.fzk.iai.ilcd.service.model.process.ILCIMethodInformation;
import de.fzk.iai.ilcd.service.model.process.IReferenceFlow;
import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.model.adapter.*;
import de.iai.ilcd.model.utils.DatasourcesFilterUtil;

import java.util.List;
import java.util.Set;

/**
 * Adapter for Processes
 */
public class ProcessVOAdapter extends AbstractDatasetAdapter<ProcessDataSetVO, IProcessListVO, IProcessVO> {

    /**
     * Create adapter for {@link IProcessListVO process list value object}
     *
     * @param adaptee list value object to adapt
     */
    public ProcessVOAdapter(IProcessListVO adaptee) {
        super(new ProcessDataSetVO(), adaptee);
    }

    /**
     * Create adapter for the {@link IProcessVO value object}
     *
     * @param adaptee value object to adapt
     */
    public ProcessVOAdapter(IProcessVO adaptee) {
        super(new ProcessDataSetVO(), adaptee);
    }

    /**
     * Create adapter for {@link IProcessListVO process list value object} with language filtering
     *
     * @param adaptee list value object to adapt
     */
    public ProcessVOAdapter(IProcessListVO adaptee, String language, boolean langFallback) {
        super(new ProcessDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * Create adapter for the {@link IProcessVO value object} with language filtering
     *
     * @param adaptee value object to adapt
     */
    public ProcessVOAdapter(IProcessVO adaptee, String language, boolean langFallback) {
        super(new ProcessDataSetVO(), adaptee, language, langFallback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(IProcessListVO src, ProcessDataSetVO dst) {
        AccessInformationType accessInformation = new AccessInformationType();
        LStringAdapter.copyLStrings(src.getAccessInformation().getUseRestrictions(), accessInformation.getUseRestrictions(), this.language);
        accessInformation.setLicenseType(src.getAccessInformation().getLicenseType());
        dst.setAccessInformation(accessInformation);
        dst.setContainsProductModel(src.getContainsProductModel());

        TimeInformationType timeInformation = new TimeInformationType();
        timeInformation.setReferenceYear(src.getTimeInformation().getReferenceYear());
        timeInformation.setValidUntil(src.getTimeInformation().getValidUntil());
        dst.setTimeInformation(timeInformation);

        DatasourcesFilterUtil dfu = new DatasourcesFilterUtil();
        List<IGlobalReference> dataSourceList = dfu.filterDataSources(src.getDataSources());

        for (IGlobalReference g : dataSourceList)
            dst.getDataSources().add(new GlobalReferenceAdapter(g, this.language, this.langFallback));

        dst.setType(src.getType());
        dst.setSubType(src.getSubType());

        ILCIMethodInformation i = src.getLCIMethodInformation();
        if (i != null) {
            LCIMethodInformationType lciMethodInformation = new LCIMethodInformationType();
            lciMethodInformation.setMethodPrinciple(i.getMethodPrinciple());
            lciMethodInformation.getApproaches().addAll(i.getApproaches());
            dst.setLCIMethodInformation(lciMethodInformation);
        }

        dst.setLocation(src.getLocation());

        Set<IComplianceSystem> srcComplSystems = src.getComplianceSystems();
        if (srcComplSystems != null) {
            Set<IComplianceSystem> destComplSystems = dst.getComplianceSystems();
            for (IComplianceSystem compSys : srcComplSystems) {
                destComplSystems.add(new ComplianceSystemAdapter(compSys, this.language, this.langFallback));
            }
        }
        dst.setOwnerReference(new GlobalReferenceAdapter(src.getOwnerReference(), this.language, this.langFallback));

        dst.setOverallQuality(src.getOverallQuality());

        if (src.getRegistrationAuthority() != null)
            dst.setRegistrationAuthority(new GlobalReferenceAdapter(src.getRegistrationAuthority(), this.language, this.langFallback));

        dst.setRegistrationNumber(src.getRegistrationNumber());

        if (ConfigurationService.INSTANCE.isShowReferenceProducts()) {
            for (IReferenceFlow f : src.getReferenceFlows()) {
                dst.getReferenceFlows().add(new ReferenceFlowAdapter(f, this.language));
            }
        }

        if (src.getMetaDataOnly() != null)
            dst.setMetaDataOnly(src.getMetaDataOnly());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValues(IProcessVO src, ProcessDataSetVO dst) {
        this.copyValues((IProcessListVO) src, dst);

        dst.setApprovedBy(new GlobalReferenceAdapter(src.getApprovedBy(), this.language));

        LStringAdapter.copyLStrings(src.getBaseName(), dst.getBaseName(), this.language);

        dst.setCompletenessProductModel(src.getCompletenessProductModel());

        dst.setFormat(src.getFormat());

        dst.setQuantitativeReference(new QuantitativeReferenceAdapter(src.getQuantitativeReference(), this.language));

        ReviewAdapter.copyReviews(src.getReviews(), dst.getReviews(), this.language);

        LStringAdapter.copyLStrings(src.getSynonyms(), dst.getSynonyms(), this.language);
        LStringAdapter.copyLStrings(src.getTechnicalPurpose(), dst.getTechnicalPurpose(), this.language);
        LStringAdapter.copyLStrings(src.getTechnologyDescription(), dst.getTechnologyDescription(), this.language);
        LStringAdapter.copyLStrings(src.getUseAdvice(), dst.getUseAdvice(), this.language);
    }

}
