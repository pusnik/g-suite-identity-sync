package eu.hlavki.identity.services.util;

import eu.hlavki.identity.services.config.AppConfiguration;
import eu.hlavki.identity.services.ldap.impl.LdapAccountServiceImpl;
import eu.hlavki.identity.services.model.AccountInfo;
import eu.hlavki.identity.services.model.PrepareAccountData.Role;
import com.unboundid.ldap.sdk.SearchResultEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.apache.cxf.rs.security.oidc.common.UserInfo;

public final class AccountUtil {

    private AccountUtil() {
    }


    public static final boolean isInternalAccount(UserInfo info, AppConfiguration cfg) {
        return cfg.getGSuiteDomain().equals(info.getProperty("hd"));
    }


    public static final Role getAccountRole(AppConfiguration cfg, UserInfo userInfo) {
        return isInternalAccount(userInfo, cfg) ? Role.INTERNAL : Role.EXTERNAL;
    }


    public static final boolean isAmAdmin(AppConfiguration cfg, UserInfo userInfo) {
        return cfg.getAdmins().contains(userInfo.getSubject());
    }


    public static final String getAccountDN(String email, AppConfiguration cfg) {
        return "uid=" + email + "," + cfg.getLdapUserBaseDN();
    }


    public static final String getGroupDN(String groupName, AppConfiguration cfg) {
        return LdapAccountServiceImpl.GROUP_NAME_ATTR + "=" + groupName + "," + cfg.getLdapGroupsBaseDN();
    }


    public static final String getLdapGroupName(String groupEmail) {
        return groupEmail.substring(0, groupEmail.indexOf('@'));
    }


    public static final AccountInfo fromLdap(SearchResultEntry entry) {
        AccountInfo info = new AccountInfo();
        info.setUsername(entry.getAttributeValue("uid"));
        String[] emails = entry.getAttributeValues("mail");
        info.setEmails(emails != null ? new HashSet<>(Arrays.asList(emails)) : Collections.emptySet());
        info.setGivenName(entry.getAttributeValue("givenName"));
        info.setFamilyName(entry.getAttributeValue("sn"));
        info.setSubject(entry.getAttributeValue("employeeNumber"));
        info.setName(entry.getAttributeValue("cn"));
        info.setRole(Role.valueOf(entry.getAttributeValue("employeeType")));
        return info;
    }
}
