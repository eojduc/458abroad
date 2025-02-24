package com.example.abroad.service.page.admin;

import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.FacultyLeadRepository;
import com.example.abroad.respository.LocalUserRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.SSOUserRepository;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
public class AdminUserService{
        private final LocalUserRepository localUserRepository;
        private final SSOUserRepository ssoUserRepository;
        private final FacultyLeadRepository facultyLeadRepository;
        private final ProgramRepository programRepository;
        private final UserService userService;

        public AdminUserService(
        LocalUserRepository localUserRepository,
        SSOUserRepository ssoUserRepository,
        FacultyLeadRepository facultyLeadRepository,
        ProgramRepository programRepository,
        UserService userService
    ) {
        this.localUserRepository = localUserRepository;
        this.ssoUserRepository = ssoUserRepository;
        this.facultyLeadRepository = facultyLeadRepository;
        this.programRepository = programRepository;
        this.userService = userService;
}
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

    public sealed interface ModifyUserResult {
        record Success(User modifiedUser) implements ModifyUserResult {}
        record UserNotFound() implements ModifyUserResult {}
        record UserNotAdmin() implements ModifyUserResult {}
        record CannotModifySuperAdmin() implements ModifyUserResult {}
        record RequiresConfirmation(
                String username,
                List<Program> affectedPrograms
        ) implements ModifyUserResult {}
    }

    @Transactional(rollbackFor = Exception.class)
    public ModifyUserResult modifyUserAdminStatus(
            HttpSession session,
            String targetUsername,
            boolean grantAdmin,
            boolean confirmed
    ) {
        // Check if requesting user is admin
        var adminUser = userService.findUserFromSession(session).orElse(null);
        if (adminUser == null) {
            return new ModifyUserResult.UserNotFound();
        }
        if (!adminUser.isAdmin()) {
            return new ModifyUserResult.UserNotAdmin();
        }

        // Find target user
        var targetUser = userService.findByUsername(targetUsername).orElse(null);
        if (targetUser == null) {
            return new ModifyUserResult.UserNotFound();
        }

        // Prevent modifying super admin
        if ("admin".equals(targetUsername)) {
            return new ModifyUserResult.CannotModifySuperAdmin();
        }

        // If revoking admin privileges, check faculty lead status
        if (!grantAdmin) {
            var facultyLeadPrograms = getFacultyLeadPrograms(targetUsername);
            if (!facultyLeadPrograms.isEmpty() && !confirmed) {
                return new ModifyUserResult.RequiresConfirmation(targetUsername, facultyLeadPrograms);
            }

            // If confirmed and user is only faculty lead for any program,
            // transfer faculty lead to admin
            if (confirmed) {
                handleFacultyLeadTransfer(targetUsername, facultyLeadPrograms);
            }
        }

        // Modify user admin status
        var updatedUser = targetUser.withRole(grantAdmin ? User.Role.ADMIN : User.Role.STUDENT);
        if (targetUser.isLocal()) {
            localUserRepository.save((User.LocalUser) updatedUser);
        } else {
            ssoUserRepository.save((User.SSOUser) updatedUser);
        }

        return new ModifyUserResult.Success(updatedUser);
    }

    private List<Program> getFacultyLeadPrograms(String username) {
        var facultyLeads = facultyLeadRepository.findById_Username(username);
        return facultyLeads.stream()
                .map(lead -> programRepository.findById(lead.programId())
                        .orElseThrow(() -> new RuntimeException("Program not found")))
                .toList();
    }

    private void handleFacultyLeadTransfer(String username, List<Program> programs) {
        programs.forEach(program -> {
            var leads = facultyLeadRepository.findById_ProgramId(program.id());
            if (leads.size() == 1 && leads.get(0).username().equals(username)) {
                // This is the only faculty lead, transfer to admin
                facultyLeadRepository.delete(leads.get(0));
                facultyLeadRepository.save(new Program.FacultyLead(program.id(), "admin"));
            }
        });
    }
}

