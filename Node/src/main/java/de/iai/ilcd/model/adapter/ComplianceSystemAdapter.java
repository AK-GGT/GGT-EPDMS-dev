package de.iai.ilcd.model.adapter;

import de.fzk.iai.ilcd.service.client.impl.vo.types.process.ComplianceSystemType;
import de.fzk.iai.ilcd.service.model.process.IComplianceSystem;

/**
 * Adapter for {@link IComplianceSystem}
 */
public class ComplianceSystemAdapter extends ComplianceSystemType {

    /**
     * Create the adapter.
     *
     * @param adaptee instance to adapt
     */
    public ComplianceSystemAdapter(IComplianceSystem adaptee, String language, boolean langFallback) {
        super();
        if (adaptee != null) {
            this.setDocumentationCompliance(adaptee.getDocumentationCompliance());
            this.setMethodologicalCompliance(adaptee.getMethodologicalCompliance());
            this.setName(adaptee.getName());
            this.setNomenclatureCompliance(adaptee.getNomenclatureCompliance());
            this.setOverallCompliance(adaptee.getOverallCompliance());
            this.setQualityCompliance(adaptee.getQualityCompliance());
            this.setReference(new GlobalReferenceAdapter(adaptee.getReference(), language, langFallback));
            this.setReviewCompliance(adaptee.getReviewCompliance());
        }
    }

}
