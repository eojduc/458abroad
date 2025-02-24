package com.example.abroad.service.page.admin;

import com.example.abroad.model.User;
import com.example.abroad.respository.LocalUserRepository;
import com.example.abroad.respository.SSOUserRepository;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
public record AdminUserService(
        LocalUserRepository localUserRepository,
        SSOUserRepository ssoUserRepository,
        UserService userService
) {
    public enum Sort {
        NAME, EMAIL, ROLE, USER_TYPE
    }

    public sealed interface GetAllUsersInfo {
        record Success(
                List<UserInfo> users,
                User adminUser
        ) implements GetAllUsersInfo {}

        record UserNotFound() implements GetAllUsersInfo {}
        record UserNotAdmin() implements GetAllUsersInfo {}
    }

    public record UserInfo(
            User user,
            String userType // "LOCAL" or "SSO"
    ) {}

    public GetAllUsersInfo getUsersInfo(
            HttpSession session,
            Sort sort,
            String searchFilter,
            Boolean ascending
    ) {
        var user = userService.findUserFromSession(session).orElse(null);
        if (user == null) {
            return new GetAllUsersInfo.UserNotFound();
        }
        if (!user.isAdmin()) {
            return new GetAllUsersInfo.UserNotAdmin();
        }
        return processAuthorizedRequest(sort, searchFilter, ascending, user);
    }

    private GetAllUsersInfo processAuthorizedRequest(
            Sort sort,
            String searchFilter,
            Boolean ascending,
            User adminUser
    ) {
        var usersInfo = Stream.concat(
                        localUserRepository.findAll().stream()
                                .map(user -> new UserInfo(user, "LOCAL")),
                        ssoUserRepository.findAll().stream()
                                .map(user -> new UserInfo(user, "SSO"))
                )
                .filter(matchesSearchFilter(searchFilter))
                .sorted(getSortComparator(sort, ascending))
                .toList();

        return new GetAllUsersInfo.Success(usersInfo, adminUser);
    }

    private Predicate<UserInfo> matchesSearchFilter(String searchTerm) {
        return userInfo -> {
            if (searchTerm == null || searchTerm.isEmpty()) {
                return true;
            }
            String lowercaseSearch = searchTerm.toLowerCase();
            return userInfo.user().displayName().toLowerCase().contains(lowercaseSearch) ||
                    userInfo.user().email().toLowerCase().contains(lowercaseSearch);
        };
    }

    private Comparator<UserInfo> getSortComparator(Sort sort, Boolean ascending) {
        Comparator<UserInfo> comparator = switch (sort) {
            case NAME -> Comparator.comparing(userInfo -> userInfo.user().displayName());
            case EMAIL -> Comparator.comparing(userInfo -> userInfo.user().email());
            case ROLE -> Comparator.comparing(userInfo -> userInfo.user().role().toString());
            case USER_TYPE -> Comparator.comparing(UserInfo::userType);
        };
        return ascending ? comparator : comparator.reversed();
    }
}

