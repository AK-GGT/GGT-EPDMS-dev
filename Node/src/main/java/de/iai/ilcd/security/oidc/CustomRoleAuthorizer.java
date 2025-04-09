package de.iai.ilcd.security.oidc;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.security.ProtectableType;
import de.iai.ilcd.security.SecurityUtil;
import de.iai.ilcd.security.role.RemoteRole;
import de.iai.ilcd.security.role.RoleMapping;
import de.iai.ilcd.security.sql.IlcdShiroPermissions;
import eu.europa.ec.jrc.lca.commons.util.ApplicationContextHolder;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.pac4j.core.authorization.generator.FromAttributesAuthorizationGenerator;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;


/**
 * <p>
 * A second AuthorizationGenerator (activated from roleAdminAuthGenerator2 in shiro.ini)
 * that populate pac4j's Profile roles and permissions for a customer with a vague business requirements.
 * </p>
 *
 * <p>
 * Translate roles from a set of predefined values (in soda4LCA.properties)
 * to stock permissions understandable by Shiro.
 * </p>
 *
 * <p>
 * Example: Given the following string from ConfigurationService: <br/>
 * <code>oidc.roleMappings = CM_XL, 1, READ, EXPORT; CM_M, 23, READ, EXPORT;...</code>
 * </p>
 * <p>
 * This implies that the role key in the token might have: "CM_XL, CM_M" and <br/>
 * AuthorizationGenerator job is to generate "STOCK:READ,EXPORT:1" and "STOCK:READ,EXPORT:23".
 * </p>
 *
 * @author MK
 * @since soda4LCA 7.0.0
 */
public class CustomRoleAuthorizer extends FromAttributesAuthorizationGenerator {

    private static final Logger log = LoggerFactory.getLogger(CustomRoleAuthorizer.class);

    private Collection<String> roleAttributes;
    private Collection<String> permissionAttributes;
    private String splitChar = ",";
    private RoleMapping roleMapping;

    public CustomRoleAuthorizer() {
        super();
        this.roleMapping = ApplicationContextHolder.getApplicationContext().getBean(RoleMapping.class);
    }

    public CustomRoleAuthorizer(Collection<String> roleAttributes, Collection<String> permissionAttributes) {
        super(roleAttributes, permissionAttributes);
    }

    public CustomRoleAuthorizer(String[] roleAttributes, String[] permissionAttributes) {
        super(roleAttributes, permissionAttributes);
    }

    @Override
    public Optional<UserProfile> generate(WebContext context, SessionStore sessionStore, UserProfile profile) {
        var remote = ApplicationContextHolder.getApplicationContext().getBean(RemoteRole.class);
        var extra_roles = remote.licenseRoles(ConfigurationService.INSTANCE.getOidcExternalRolesUrl(), profile.getAttribute("access_token").toString());

        generateAuth(profile, this.roleAttributes);
        for (var r : extra_roles)
            addRoleOrPermissionToProfile(profile, r);

//        generateAuth(profile, this.permissionAttributes);
        return Optional.of(profile);
    }


    private void generateAuth(final UserProfile profile, final Iterable<String> attributes) {
        if (attributes == null) {
            return;
        }

        for (final var attribute : attributes) {
            final var value = profile.getAttribute(attribute);
            if (value == null) continue;
            if (value instanceof String) {
                final var st = new StringTokenizer((String) value, this.splitChar);
                while (st.hasMoreTokens()) {
                    addRoleOrPermissionToProfile(profile, st.nextToken());
                }
            } else if (value.getClass().isArray()
                    && value.getClass().getComponentType().isAssignableFrom(String.class)) {
                for (var item : (Object[]) value) {
                    addRoleOrPermissionToProfile(profile, item.toString());
                }
            } else if (Collection.class.isAssignableFrom(value.getClass())) {
                for (Object item : (Collection<?>) value) {
                    if (item.getClass().isAssignableFrom(String.class)) {
                        addRoleOrPermissionToProfile(profile, item.toString());
                    }
                }
            }
        }
    }

    private void addRoleOrPermissionToProfile(final UserProfile profile, final String value) {
        log.trace("adding role {}", value);
        profile.addRole(value);
        profile.addPermissions(List.of(roleMapping.expand(value).replaceAll(" ", "").split(";")));
        profile.addPermission(new WildcardPermission(ProtectableType.IMPORT_AREA.name()).toString()); // TODO: toggle import permission
//		profile.addPermission(new WildcardPermission(ProtectableType.ADMIN_AREA.name()).toString());

        //merge in guest permissions
        for (var p : IlcdShiroPermissions.read(null))
            // example: stock::10 has empty protection type (value 0 / hidden datastock)
            // super admin can see that datastock but regular user cannot
            if (SecurityUtil.getCurrentUser().isPrivilegedUser() || !p.toString().contains(":") || !p.toString().split(":")[1].isEmpty())
                profile.addPermission(p.toString());
    }

    @Override
    public String getSplitChar() {
        // TODO Auto-generated method stub
        return super.getSplitChar();
    }

    @Override
    public void setSplitChar(String splitChar) {
        // TODO Auto-generated method stub
        super.setSplitChar(splitChar);
    }

    public Collection<String> getRoleAttributes() {
        return roleAttributes;
    }

    @Override
    public void setRoleAttributes(String roleAttributesStr) {
        // TODO Auto-generated method stub
        super.setRoleAttributes(roleAttributesStr);
    }

    public void setRoleAttributes(Collection<String> roleAttributes) {
        this.roleAttributes = roleAttributes;
    }

    public Collection<String> getPermissionAttributes() {
        return permissionAttributes;
    }

    @Override
    public void setPermissionAttributes(String permissionAttributesStr) {
        // TODO Auto-generated method stub
        super.setPermissionAttributes(permissionAttributesStr);
    }

    public void setPermissionAttributes(Collection<String> permissionAttributes) {
        this.permissionAttributes = permissionAttributes;
    }

}
