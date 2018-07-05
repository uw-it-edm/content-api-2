package edu.uw.edm.contentapi2.controller.content.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.uw.edm.contentapi2.security.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * This should be deleted as soon as we find a solution for user profile
 *
 * @author Maxime Deravet Date: 7/5/18
 */
@RestController
@RequestMapping("/content/v1/user")
@Deprecated
public class UserController {

    @Value("#{'${user.v1.accounts:}'.split(',')}")
    List<String> hardcodedAccounts;
    @Value("#{'${user.v1.roles:}'.split(',')}")
    List<String> hardcodedRoles;

    @Getter
    @Setter
    @AllArgsConstructor
    class Permission {
        private String permissionName;
        private String permissionType;
        private String permissionLevel;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    class UserInfo {
        private String userName;
        private List<Permission> accounts;
        private List<Permission> roles;
    }


    @RequestMapping(method = RequestMethod.GET)
    public UserInfo get(@AuthenticationPrincipal User user) {

        return new UserInfo(user.getUsername(), getAccountList(), getRoleList());
    }

    private List<Permission> getRoleList() {

        return hardcodedRoles.stream().map(s -> new Permission(s, "role", "admin")).collect(Collectors.toList());
    }

    private List<Permission> getAccountList() {
        return hardcodedAccounts.stream().map(s -> new Permission(s, "account", "rwd")).collect(Collectors.toList());

    }
}
