package de.iai.ilcd.rest.augmentations;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.fzk.iai.ilcd.service.model.enums.GlobalReferenceTypeValue;
import de.iai.ilcd.model.common.GlobalReference;
import de.iai.ilcd.model.dao.DataSetDaoType;
import de.iai.ilcd.model.dao.SourceDao;
import de.iai.ilcd.model.source.Source;
import de.iai.ilcd.util.url.LocalURLs;
import it.jrc.lca.ilcd.common.GlobalReferenceType;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.List;

public final class ResourceReference {

    @JsonUnwrapped()
    private GlobalReference globalReference;

    private List<URL> resourceURLs;

    public ResourceReference(@Nonnull final GlobalReference ref) {
        this.globalReference = ref;
        this.resourceURLs = resolveResourceURLs(this.globalReference);
    }

    public ResourceReference(@Nonnull final GlobalReferenceType grt) {
        this.globalReference = new GlobalReference(grt);
        this.resourceURLs = resolveResourceURLs(this.globalReference);
    }

    private List<URL> resolveResourceURLs(GlobalReference ref) {
        GlobalReferenceTypeValue typeValue = ref.getType();
        if (GlobalReferenceTypeValue.SOURCE_DATA_SET.equals(typeValue)) {
            SourceDao sDao = (SourceDao) DataSetDaoType.SOURCE.getDao();
            Source source = sDao.getByUuidAndOptionalVersion(ref.getRefObjectId(), ref.getVersion());
            if (source != null)
                return LocalURLs.getResourceFileURLs(source);
        }
        return null;
    }

    public GlobalReference getGlobalReference() {
        return globalReference;
    }

    public void setGlobalReference(GlobalReference globalReference) {
        this.globalReference = globalReference;
    }

    public List<URL> getResourceURLs() {
        return resourceURLs;
    }

    public void setResourceURLs(List<URL> resourceURLs) {
        this.resourceURLs = resourceURLs;
    }
}
