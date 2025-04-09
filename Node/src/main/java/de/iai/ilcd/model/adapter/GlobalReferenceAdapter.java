package de.iai.ilcd.model.adapter;

import de.fzk.iai.ilcd.api.binding.generated.common.STMultiLang;
import de.fzk.iai.ilcd.service.client.impl.vo.types.common.GlobalReferenceType;
import de.fzk.iai.ilcd.service.client.impl.vo.types.common.LString;
import de.fzk.iai.ilcd.service.model.common.IGlobalReference;
import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;

import java.util.List;

/**
 * Adapter for global reference
 */
public class GlobalReferenceAdapter extends GlobalReferenceType {

    /**
     * Create the adapter.
     */
    public GlobalReferenceAdapter(IGlobalReference reference, String language, boolean langFallback) {
        if (reference != null) {
            this.setHref(reference.getHref());
            this.setType(reference.getType());
            this.setUri(reference.getUri());
            this.setVersion(reference.getVersionAsString());
            this.setRefObjectId(reference.getRefObjectId());

            if (langFallback)
                this.setShortDescription(new LStringAdapter(reference.getShortDescription(), language, true).getLStrings());
            else
                this.setShortDescription(new LStringAdapter(reference.getShortDescription(), language).getLStrings());
        }
    }

    /**
     * Create the adapter.
     */
    public GlobalReferenceAdapter(IGlobalReference reference, String language) {
        if (reference != null) {
            this.setHref(reference.getHref());
            this.setType(reference.getType());
            this.setUri(reference.getUri());
            this.setVersion(reference.getVersionAsString());
            this.setRefObjectId(reference.getRefObjectId());

            this.setShortDescription(new LStringAdapter(reference.getShortDescription(), language).getLStrings());
        }
    }

    /**
     * Create the adapter.
     */
    public GlobalReferenceAdapter(IGlobalReference reference) {
        if (reference != null) {
            this.setHref(reference.getHref());
            this.setType(reference.getType());
            this.setUri(reference.getUri());
            this.setVersion(reference.getVersionAsString());
            this.setRefObjectId(reference.getRefObjectId());
            this.setShortDescription(new LStringAdapter(reference.getShortDescription()).getLStrings());
        }
    }


    /**
     * Create the adapter.
     */
    public GlobalReferenceAdapter(de.fzk.iai.ilcd.api.binding.generated.common.GlobalReferenceType reference, String language) {
        if (reference != null) {
            this.setRefObjectId(reference.getRefObjectId());
            this.setType(GlobalReferenceTypeValue.fromValue(reference.getType().getValue()));
            this.setUri(reference.getUri());
            this.setVersion(reference.getVersion());

            for (STMultiLang ls : reference.getShortDescription()) {
                this.getShortDescription().getLStrings().add(new LString(ls.getLang(), ls.getValue()));
            }
        }
    }

    /**
     * Copy global references via adapter
     *
     * @param src source list
     * @param dst destination list
     */
    public static void copyGlobalReferences(List<IGlobalReference> src, List<IGlobalReference> dst, String language) {
        if (src != null && dst != null) {
            for (IGlobalReference srcItem : src) {
                dst.add(new GlobalReferenceAdapter(srcItem, language));
            }
        }
    }
}
