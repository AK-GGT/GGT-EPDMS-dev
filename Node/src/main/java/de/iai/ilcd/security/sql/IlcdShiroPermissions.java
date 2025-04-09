package de.iai.ilcd.security.sql;

import de.iai.ilcd.model.dao.UserDao;
import de.iai.ilcd.model.dao.UserGroupDao;
import de.iai.ilcd.model.datastock.IDataStockMetaData;
import de.iai.ilcd.model.security.IUser;
import de.iai.ilcd.model.security.Organization;
import de.iai.ilcd.security.*;
import de.iai.ilcd.webgui.controller.ui.AvailableStockHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * read method was part of {@link IlcdAuthorizationInfo#loadPermissions()}
 * it was taken to here to be part of reading permissions in different realms
 * <p>
 * for example: adding all the existing permissions of an
 * existing (sql) user when generating a token
 *
 * @author sarai
 * @see IlcdAuthorizationInfo#loadPermissions()
 * @since soda4LCA 7.0.0.
 */
public class IlcdShiroPermissions { // TODO: break down into smaller pieces

    public static Set<Permission> read(IUser user) {
        var dao = new UserDao();
        var permissions = new HashSet<Permission>();

        try {
            // stock access rights
            StockAccessRightDao sarDao = new StockAccessRightDao();
            List<StockAccessRight> lstSar;
            if (user != null) {
                // wildcard for super admin + skip the rest ;-)
                if (user.isSuperAdminPermission()) {
                    permissions.add(new WildcardPermission(SecurityUtil.toWildcardString("*", "*", "*")));
                    return permissions;
                }
                // load the access rights, there was no super admin group
                lstSar = user.getStockAccessRights();
            } else {
                lstSar = sarDao.get(0L, null); // guest
            }

            for (StockAccessRight sar : lstSar) {
                permissions.add(new WildcardPermission(SecurityUtil.toWildcardString(ProtectableType.STOCK.name(), ProtectionType.toTypesCSV(sar
                        .getValue(), ","), sar.getStockId())));
            }

            // rest is irrelevant to guests
            if (user != null) {
                // user / groups access for org admin

                UserGroupDao ugDao = new UserGroupDao();

                // get all organizations that are being administrated by this user
                List<Organization> adminOrgs = user.getAdministratedOrganizations();
                List<Long> orgUsrIds = null;
                if (adminOrgs != null && !adminOrgs.isEmpty()) {
                    // user is org admin for at least one org, grant access to admin area
                    permissions.add(new WildcardPermission(ProtectableType.ADMIN_AREA.name()));

                    // user and group protection types array
                    ProtectionType[] usrGrpProtectionTypes = new ProtectionType[]{ProtectionType.READ, ProtectionType.WRITE, ProtectionType.CREATE,
                            ProtectionType.DELETE};

                    // get IDs of all users and groups that are being assigned to the administrated organizations
                    orgUsrIds = dao.getUserIds(adminOrgs);
                    List<Long> orgGrpIds = ugDao.getGroupIds(adminOrgs);

                    // and grant read/write permissions
                    if (orgUsrIds != null && !orgUsrIds.isEmpty()) {
                        permissions.add(new WildcardPermission(SecurityUtil.toWildcardString(ProtectableType.USER, usrGrpProtectionTypes, StringUtils
                                .join(orgUsrIds, ','))));
                    }
                    if (orgGrpIds != null && !orgGrpIds.isEmpty()) {
                        permissions.add(new WildcardPermission(SecurityUtil.toWildcardString(ProtectableType.GROUP, usrGrpProtectionTypes, StringUtils
                                .join(orgGrpIds, ','))));
                    }
                }

                // access to own user (profile) [if not already added by organization admin]
                if (orgUsrIds == null || !orgUsrIds.contains(user.getId())) {
                    permissions.add(new WildcardPermission(SecurityUtil.toWildcardString(ProtectableType.USER, new ProtectionType[]{
                            ProtectionType.READ, ProtectionType.WRITE}, user.getId().toString())));
                }

                // import right
                AvailableStockHandler stocksHandler = new AvailableStockHandler();
                List<IDataStockMetaData> possibleStocks = stocksHandler.getRootStocksMeta().stream()
                        .filter(stock -> SecurityUtil.hasImportPermission(stock)).collect(Collectors.toList());
                if (possibleStocks.size() > 0)
                    permissions.add(new WildcardPermission(ProtectableType.IMPORT_AREA.name()));
            }
        } catch (Exception e) {
            // Nop
            return permissions;
        }
        return permissions;
    }
}
