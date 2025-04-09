package de.iai.ilcd.security.role;

import de.iai.ilcd.configuration.ConfigurationService;
import de.iai.ilcd.security.ProtectableType;
import de.iai.ilcd.security.ProtectionType;
import de.iai.ilcd.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller("sodaRoleMapping")
public class RoleMapping {
    private static final Logger log = LoggerFactory.getLogger(RoleMapping.class);

    /**
     * expand role to detailed shiro permissions as defined in the Configuration under OidcRoleMapping
     *
     * @param role as defined in oidcRoleMapping
     * @return A semi-colon delimited shiro-compatible wildcard string
     * @see ConfigurationService#getOidcRoleMappings()
     */
    public String expand(String role) {
        var rolesMappings = ConfigurationService.INSTANCE.getOidcRoleMappings();
//		var rolesMappings = "CM_XL, 1, READ, EXPORT; CM_M, 23, READ, EXPORT";
//		var rolesMappings = "CM_XL, 1, READ, EXPORT; CM_XL, 2, READ, EXPORT; CM_M, 4, READ, EXPORT";

        List<String> erg = new ArrayList<String>();
        for (var m : rolesMappings.split(";")) {
            var parts = m.split(",");
            if (parts[0].trim().equalsIgnoreCase(role.trim()))
                erg.add(String.format("%s:%s:%s", "STOCK", joinExceptFirst2(parts), parts[1]));
        }

        log.debug("{} role mappings have been mapped from role attribute: {}", erg.size(), erg);

        erg.add(SecurityUtil.toWildcardString(
                ProtectableType.USER,
                new ProtectionType[]{ProtectionType.READ, ProtectionType.WRITE},
                "-420")); // user read his own profile

        return String.join(";", erg);
    }

    private String joinExceptFirst2(String parts[]) {
        // maybe trim too?
        return Arrays.asList(parts).stream().skip(2).collect(Collectors.joining(","));
    }
}
