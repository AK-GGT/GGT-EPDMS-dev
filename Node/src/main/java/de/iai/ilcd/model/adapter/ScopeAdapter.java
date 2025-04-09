package de.iai.ilcd.model.adapter;

import de.fzk.iai.ilcd.service.client.impl.vo.types.process.ReviewType;
import de.fzk.iai.ilcd.service.model.enums.MethodOfReviewValue;
import de.fzk.iai.ilcd.service.model.process.IScope;

import java.util.List;
import java.util.Set;

/**
 * Adapter for {@link ReviewType.Scope}
 */
public class ScopeAdapter extends ReviewType.Scope {

    /**
     * Create the adapter
     *
     * @param adaptee object to adapt
     */
    public ScopeAdapter(IScope adaptee) {
        if (adaptee != null) {
            this.setName(adaptee.getName());

            final Set<MethodOfReviewValue> adapteeMethods = adaptee.getMethods();
            if (adapteeMethods != null) {
                final List<Method> adapterMethods = this.getMethod();
                for (MethodOfReviewValue enumVal : adapteeMethods) {
                    Method m = new Method();
                    m.setName(enumVal);
                    adapterMethods.add(m);
                }
            }
        }
    }

    /**
     * Copy scope via adapter
     *
     * @param src source set
     * @param dst destination set
     */
    public static void copyScopes(Set<IScope> src, Set<IScope> dst) {
        if (src != null && dst != null) {
            for (IScope srcItem : src) {
                dst.add(new ScopeAdapter(srcItem));
            }
        }
    }

}
