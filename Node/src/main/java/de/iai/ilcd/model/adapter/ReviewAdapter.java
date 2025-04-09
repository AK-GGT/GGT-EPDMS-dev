package de.iai.ilcd.model.adapter;

import de.fzk.iai.ilcd.service.client.impl.vo.types.process.ReviewType;
import de.fzk.iai.ilcd.service.model.process.IReview;

import java.util.List;

/**
 * Adapter for {@link IReview}
 */
public class ReviewAdapter extends ReviewType {

    /**
     * Create the adapter
     *
     * @param adaptee object to adapt
     */
    public ReviewAdapter(IReview adaptee, String language) {
        if (adaptee != null) {
            LStringAdapter.copyLStrings(adaptee.getOtherReviewDetails(), this.getOtherReviewDetails(), language);
            LStringAdapter.copyLStrings(adaptee.getReviewDetails(), this.getReviewDetails(), language);
            GlobalReferenceAdapter.copyGlobalReferences(adaptee.getReferencesToReviewers(), this.getReferencesToReviewers(), language);
            ScopeAdapter.copyScopes(adaptee.getScopes(), this.getScopes());
            DataQualityIndicatorAdapter.copyDataQualityIndicators(adaptee.getDataQualityIndicators(), this.getDataQualityIndicators());
            this.setType(adaptee.getType());
        }
    }

    /**
     * Copy reviewes via adapter
     *
     * @param src source set
     * @param dst destination set
     */
    public static void copyReviews(List<IReview> src, List<IReview> dst, String language) {
        if (src != null && dst != null) {
            for (IReview srcItem : src) {
                dst.add(new ReviewAdapter(srcItem, language));
            }
        }
    }

}
