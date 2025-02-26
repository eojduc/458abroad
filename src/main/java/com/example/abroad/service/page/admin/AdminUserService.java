package com.example.abroad.service.page.admin;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.*;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
public class AdminUserService {
    private final LocalUserRepository localUserRepository;
    private final SSOUserRepository ssoUserRepository;
    private final FacultyLeadRepository facultyLeadRepository;
    private final ProgramRepository programRepository;
    private final ApplicationRepository applicationRepository;
    private final NoteRepository noteRepository;
    private final DocumentRepository documentRepository;
    private final ResponseRepository responseRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            LocalUserRepository localUserRepository,
            SSOUserRepository ssoUserRepository,
            FacultyLeadRepository facultyLeadRepository,
            ProgramRepository programRepository,
            ApplicationRepository applicationRepository,
            NoteRepository noteRepository,
            DocumentRepository documentRepository,
            ResponseRepository responseRepository,
            @Lazy PasswordEncoder passwordEncoder,
            UserService userService
    ) {
        this.localUserRepository = localUserRepository;
        this.ssoUserRepository = ssoUserRepository;
        this.facultyLeadRepository = facultyLeadRepository;
        this.programRepository = programRepository;
        this.applicationRepository = applicationRepository;
        this.noteRepository = noteRepository;
        this.documentRepository = documentRepository;
        this.responseRepository = responseRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public enum Sort {
        NAME, EMAIL, ROLE, USER_TYPE
    }

    public sealed interface GetAllUsersInfo {
        record Success(
                List<UserInfo> users,
                User adminUser
        ) implements GetAllUsersInfo {
        }

        record UserNotFound() implements GetAllUsersInfo {
        }

        record UserNotAdmin() implements GetAllUsersInfo {
        }
    }

    public record UserInfo(
            User user,
            String userType // "LOCAL" or "SSO"
    ) {
    }

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
        record Success(User modifiedUser) implements ModifyUserResult {
        }

        record UserNotFound() implements ModifyUserResult {
        }

        record UserNotAdmin() implements ModifyUserResult {
        }

        record CannotModifySuperAdmin() implements ModifyUserResult {
        }

        record RequiresConfirmation(
                String username,
                List<Program> affectedPrograms
        ) implements ModifyUserResult {
        }
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
            System.out.println("FACULTY LEAD PRGRAM SIZE IS " + facultyLeadPrograms.size());
            if (facultyLeadPrograms.isEmpty()) {
                var updateUser = targetUser.withRole(grantAdmin ? User.Role.ADMIN : User.Role.STUDENT);
                if (targetUser.isLocal()) {
                    localUserRepository.save((User.LocalUser) updateUser);
                } else {
                    ssoUserRepository.save((User.SSOUser) updateUser);
                }
                return new ModifyUserResult.Success(targetUser);
            }

            //will only proceed past this point if user is faculty lead
            if (!confirmed) {
                System.out.println("GOINT TO REQUIRES CONFIRMATION, EVENTHOUGH FACULTY LEAD PRGRAM SIZE IS " + facultyLeadPrograms.size());
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
        // Get all faculty leads for this username directly
        List<Program.FacultyLead> userLeads = facultyLeadRepository.findById_Username(username);

        // For each of the user's leads
        userLeads.forEach(userLead -> {
            // Delete the current user's lead entry
            facultyLeadRepository.delete(userLead);

            // Check how many leads remain for this program after deletion
            List<Program.FacultyLead> remainingLeads = facultyLeadRepository.findById_ProgramId(userLead.programId());

            // If no leads remain for this program, make admin the lead
            if (remainingLeads.isEmpty()) {
                facultyLeadRepository.save(new Program.FacultyLead(userLead.programId(), "admin"));
            }
        });
    }

    // Add these interfaces and methods to your AdminUserService class

    public sealed interface PasswordResetValidationResult {
        record Valid() implements PasswordResetValidationResult {
        }

        record UserNotFound() implements PasswordResetValidationResult {
        }

        record UserNotAdmin() implements PasswordResetValidationResult {
        }

        record CannotResetSSOUser() implements PasswordResetValidationResult {
        }

        record CannotResetSuperAdmin() implements PasswordResetValidationResult {
        }
    }

    public sealed interface PasswordResetResult {
        record Success() implements PasswordResetResult {
        }

        record UserNotFound() implements PasswordResetResult {
        }

        record UserNotAdmin() implements PasswordResetResult {
        }

        record CannotResetSSOUser() implements PasswordResetResult {
        }

        record CannotResetSuperAdmin() implements PasswordResetResult {
        }

        record PasswordsDoNotMatch() implements PasswordResetResult {
        }

        record PasswordTooShort() implements PasswordResetResult {
        }
    }

    /**
     * Validates if the password reset operation is allowed
     */
    public PasswordResetValidationResult validatePasswordReset(
            HttpSession session,
            String targetUsername
    ) {
        // Check if requesting user is admin
        var adminUser = userService.findUserFromSession(session).orElse(null);
        if (adminUser == null) {
            return new PasswordResetValidationResult.UserNotFound();
        }
        if (!adminUser.isAdmin()) {
            return new PasswordResetValidationResult.UserNotAdmin();
        }

        // Find target user
        var targetUser = userService.findByUsername(targetUsername).orElse(null);
        if (targetUser == null) {
            return new PasswordResetValidationResult.UserNotFound();
        }

        // Prevent resetting super admin's password
        if ("admin".equals(targetUsername)) {
            return new PasswordResetValidationResult.CannotResetSuperAdmin();
        }

        // Check if user is local
        if (!targetUser.isLocal()) {
            return new PasswordResetValidationResult.CannotResetSSOUser();
        }

        return new PasswordResetValidationResult.Valid();
    }

    /**
     * Resets a user's password
     */
    @Transactional(rollbackFor = Exception.class)
    public PasswordResetResult resetUserPassword(
            HttpSession session,
            String targetUsername,
            String newPassword,
            String confirmPassword
    ) {
        // Validate the reset operation
        var validationResult = validatePasswordReset(session, targetUsername);
        if (validationResult instanceof PasswordResetValidationResult.UserNotFound) {
            return new PasswordResetResult.UserNotFound();
        } else if (validationResult instanceof PasswordResetValidationResult.UserNotAdmin) {
            return new PasswordResetResult.UserNotAdmin();
        } else if (validationResult instanceof PasswordResetValidationResult.CannotResetSSOUser) {
            return new PasswordResetResult.CannotResetSSOUser();
        } else if (validationResult instanceof PasswordResetValidationResult.CannotResetSuperAdmin) {
            return new PasswordResetResult.CannotResetSuperAdmin();
        }

        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            return new PasswordResetResult.PasswordsDoNotMatch();
        }

        // Validate password length (minimum 8 characters)
        if (newPassword.length() < 8) {
            return new PasswordResetResult.PasswordTooShort();
        }

        // Get the local user
        User.LocalUser localUser = (User.LocalUser) userService.findByUsername(targetUsername).orElse(null);
        if (localUser == null) {
            return new PasswordResetResult.UserNotFound();
        }

        // Update the password - assuming you have a way to hash passwords
        String hashedPassword = hashPassword(newPassword);
        User.LocalUser updatedUser = new User.LocalUser(
                localUser.username(),
                hashedPassword,
                localUser.email(),
                localUser.role(),
                localUser.displayName(),
                localUser.theme()
        );

        // Save the updated user
        localUserRepository.save(updatedUser);

        return new PasswordResetResult.Success();
    }


    private String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    public sealed interface DeleteUserValidationResult {
        record Valid(
                String username,
                List<Program> facultyLeadPrograms,
                List<Application> applications
        ) implements DeleteUserValidationResult {}
        record UserNotFound() implements DeleteUserValidationResult {}
        record UserNotAdmin() implements DeleteUserValidationResult {}
        record CannotDeleteSelf() implements DeleteUserValidationResult {}
        record CannotDeleteSSOUser() implements DeleteUserValidationResult {}
        record CannotDeleteSuperAdmin() implements DeleteUserValidationResult {}
    }

    public sealed interface DeleteUserResult {
        record Success() implements DeleteUserResult {}
        record UserNotFound() implements DeleteUserResult {}
        record UserNotAdmin() implements DeleteUserResult {}
        record CannotDeleteSelf() implements DeleteUserResult {}
        record CannotDeleteSSOUser() implements DeleteUserResult {}
        record CannotDeleteSuperAdmin() implements DeleteUserResult {}
    }

    /**
     * Validates if a user can be deleted
     */
    public DeleteUserValidationResult validateUserDeletion(
            HttpSession session,
            String targetUsername
    ) {
        // Check if requesting user is admin
        var adminUser = userService.findUserFromSession(session).orElse(null);
        if (adminUser == null) {
            return new DeleteUserValidationResult.UserNotFound();
        }
        if (!adminUser.isAdmin()) {
            return new DeleteUserValidationResult.UserNotAdmin();
        }

        // Find target user
        var targetUser = userService.findByUsername(targetUsername).orElse(null);
        if (targetUser == null) {
            return new DeleteUserValidationResult.UserNotFound();
        }

        // Prevent deleting super admin
        if ("admin".equals(targetUsername)) {
            return new DeleteUserValidationResult.CannotDeleteSuperAdmin();
        }

        // Prevent deleting self
        if (adminUser.username().equals(targetUsername)) {
            return new DeleteUserValidationResult.CannotDeleteSelf();
        }

        // Prevent deleting SSO user
        if (!targetUser.isLocal()) {
            return new DeleteUserValidationResult.CannotDeleteSSOUser();
        }

        // Find relevant data for confirmation dialog
        List<Program> facultyLeadPrograms = getFacultyLeadPrograms(targetUsername);
        List<Application> applications = applicationRepository.findByStudent(targetUsername);

        return new DeleteUserValidationResult.Valid(targetUsername, facultyLeadPrograms, applications);
    }


    @Transactional(rollbackFor = Exception.class)
    public DeleteUserResult deleteUser(
            HttpSession session,
            String targetUsername
    ) {
        var validationResult = validateUserDeletion(session, targetUsername);

        if (validationResult instanceof DeleteUserValidationResult.UserNotFound) {
            return new DeleteUserResult.UserNotFound();
        } else if (validationResult instanceof DeleteUserValidationResult.UserNotAdmin) {
            return new DeleteUserResult.UserNotAdmin();
        } else if (validationResult instanceof DeleteUserValidationResult.CannotDeleteSSOUser) {
            return new DeleteUserResult.CannotDeleteSSOUser();
        } else if (validationResult instanceof DeleteUserValidationResult.CannotDeleteSuperAdmin) {
            return new DeleteUserResult.CannotDeleteSuperAdmin();
        } else if (validationResult instanceof DeleteUserValidationResult.CannotDeleteSelf) {
            return new DeleteUserResult.CannotDeleteSelf();
        }

        // Handle faculty lead transfers
        DeleteUserValidationResult.Valid valid = (DeleteUserValidationResult.Valid) validationResult;
        if (valid.facultyLeadPrograms() != null && !valid.facultyLeadPrograms().isEmpty()) {
            handleFacultyLeadTransfer(targetUsername, valid.facultyLeadPrograms());
        }

        // Update all notes authored by this user to show "DELETED USER"
        List<Application.Note> userNotes = noteRepository.findAll().stream()
                .filter(note -> note.username().equals(targetUsername))
                .toList();

        for (Application.Note note : userNotes) {
            // Create a new note with the same properties but with "DELETED USER" as the username
            Application.Note updatedNote = new Application.Note(
                    note.applicationId(),
                    "DELETED USER",
                    note.content(),
                    note.timestamp()
            );
            noteRepository.delete(note);
            noteRepository.save(updatedNote);
        }

        // Delete the user (this will cascade to delete all owned records)
        localUserRepository.deleteById(targetUsername);

        return new DeleteUserResult.Success();
    }



}