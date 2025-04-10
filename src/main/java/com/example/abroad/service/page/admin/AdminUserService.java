package com.example.abroad.service.page.admin;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import com.example.abroad.model.User.Role;
import com.example.abroad.model.User.Role.ID;
import com.example.abroad.model.User.Role.Type;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public record AdminUserService(
        ProgramService programService,
        ApplicationService applicationService,
        UserService userService,
        PasswordEncoder passwordEncoder
) {

  public enum Sort {
    NAME, USERNAME, EMAIL, ROLE, USER_TYPE, ULINK
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
          List<Program> facultyLeadPrograms,
          List<Application> applications,
          Map<String, String> applicationPrograms,
          boolean isAdmin,
          String role
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
    if (!userService.isAdmin(user)) {
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
    var usersInfo =
            userService.findAll()
                    .stream()
                    .map(this::getUserInfo)
                    .filter(matchesSearchFilter(searchFilter))
                    .sorted(getSortComparator(sort, ascending))
                    .toList();

    return new GetAllUsersInfo.Success(usersInfo, adminUser);
  }

  private UserInfo getUserInfo(User user) {
    var facultyLeadPrograms = programService.findFacultyPrograms(user);
    var applications = applicationService.findByStudent(user);
    var isAdmin = userService.isAdmin(user);

    // Get all roles for this user
    List<Role> userRoles = userService.roleRepository().findById_Username(user.username());
    String role;

    if (userRoles.isEmpty()) {
      role = "STUDENT";
    } else {
      role = userRoles.stream()
              .map(r -> r.type().toString())
              .sorted()
              .collect(Collectors.joining(", "));
    }

    Map<String, String> applicationPrograms = new HashMap<>();
    for (Application app : applications) {
      programService.findById(app.programId()).ifPresent(program ->
              applicationPrograms.put(app.programId().toString(), program.title())
      );
    }

    return new UserInfo(user, facultyLeadPrograms, applications, applicationPrograms, isAdmin, role);
  }

  private Predicate<UserInfo> matchesSearchFilter(String searchTerm) {
    return userInfo -> {
      if (searchTerm == null || searchTerm.isEmpty()) {
        return true;
      }
      String lowercaseSearch = searchTerm.toLowerCase();
      return userInfo.user().displayName().toLowerCase().contains(lowercaseSearch) ||
              userInfo.user().username().toLowerCase().contains(lowercaseSearch) ||
              userInfo.user().email().toLowerCase().contains(lowercaseSearch) ||
              (userInfo.user().uLink() != null &&
                      userInfo.user().uLink().toLowerCase().contains(lowercaseSearch));
    };
  }

  private Comparator<UserInfo> getSortComparator(Sort sort, Boolean ascending) {
    Comparator<UserInfo> comparator = switch (sort) {
      case NAME -> Comparator.comparing(userInfo -> userInfo.user().displayName());
      case USERNAME -> Comparator.comparing(userInfo -> userInfo.user().username());
      case EMAIL -> Comparator.comparing(userInfo -> userInfo.user().email());
      case ROLE -> Comparator.comparing(userInfo -> userInfo.role);
      case USER_TYPE -> Comparator.comparing(userInfo -> userInfo.user().isLocal() ? "Local" : "SSO");
      case ULINK -> Comparator.comparing((UserInfo userInfo) -> {
        String uLink = userInfo.user().uLink();

        // Handle different scenarios for uLink
        if (uLink == null || uLink.isEmpty()) {
          return ""; // Use empty string for "Not Connected"
        }

        return uLink;
      }).thenComparing(userInfo -> {
        // Secondary sorting to ensure "Not Connected" is always last
        String uLink = userInfo.user().uLink();
        return uLink == null || uLink.isEmpty() ? 1 : 0;
      });
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

    record CannotModifySelf() implements ModifyUserResult {
    }

    record RequiresConfirmation(
            String username,
            List<Program> affectedPrograms
    ) implements ModifyUserResult {
    }
  }

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
    if (!userService.isAdmin(adminUser)) {
      return new ModifyUserResult.UserNotAdmin();
    }

    // Find target user
    var targetUser = userService.findByUsername(targetUsername).orElse(null);
    if (targetUser == null) {
      return new ModifyUserResult.UserNotFound();
    }

    // Prevent modifying super admin
    if (targetUsername.equals("admin")) {
      return new ModifyUserResult.CannotModifySuperAdmin();
    }

    if(targetUsername.equals(adminUser.username())) {
      return new ModifyUserResult.CannotModifySelf();
    }

    // If revoking admin privileges, check faculty lead status
    if (!grantAdmin) {
      var facultyLeadPrograms = programService.findFacultyPrograms(targetUser);
      if (facultyLeadPrograms.isEmpty()) {
        // No faculty lead programs, so just remove the ADMIN role
        userService.removeRole(targetUser, Role.Type.ADMIN);
        return new ModifyUserResult.Success(targetUser);
      }

      // Will only proceed past this point if user is faculty lead
      if (!confirmed) {
        return new ModifyUserResult.RequiresConfirmation(targetUsername, facultyLeadPrograms);
      }

      /*
      for (Program program : facultyLeadPrograms) {
        programService.removeFacultyLead(program, targetUser);
      }
      */


      // After handling faculty lead status, remove the ADMIN role
      userService.removeRole(targetUser, Role.Type.ADMIN);
    } else {
      // Add admin role to user
      userService.addRole(targetUser, Role.Type.ADMIN);
    }

    return new ModifyUserResult.Success(targetUser);
  }

  public ModifyUserResult modifyUserRole(
          HttpSession session,
          String targetUsername,
          Role.Type roleType,
          boolean grantRole,
          boolean confirmed
  ) {
    // Check if requesting user is admin
    var adminUser = userService.findUserFromSession(session).orElse(null);
    if (adminUser == null) {
      return new ModifyUserResult.UserNotFound();
    }
    if (!userService.isAdmin(adminUser)) {
      return new ModifyUserResult.UserNotAdmin();
    }

    // Find target user
    var targetUser = userService.findByUsername(targetUsername).orElse(null);
    if (targetUser == null) {
      return new ModifyUserResult.UserNotFound();
    }

    // Prevent modifying super admin
    if (targetUsername.equals("admin")) {
      return new ModifyUserResult.CannotModifySuperAdmin();
    }

    // Prevent self-modification
    if (targetUsername.equals(adminUser.username())) {
      return new ModifyUserResult.CannotModifySelf();
    }

    // Special handling for FACULTY role removal - check faculty lead status
    if (roleType == Role.Type.FACULTY && !grantRole) {
      var facultyLeadPrograms = programService.findFacultyPrograms(targetUser);
      if (facultyLeadPrograms.isEmpty()) {
        // No faculty lead programs, just remove the role
        userService.removeRole(targetUser, Role.Type.FACULTY);
        return new ModifyUserResult.Success(targetUser);
      }

      // User is faculty lead for some programs, check for confirmation
      if (!confirmed) {
        return new ModifyUserResult.RequiresConfirmation(targetUsername, facultyLeadPrograms);
      }

      // Remove as faculty lead from all programs
      for (Program program : facultyLeadPrograms) {
        programService.removeFacultyLead(program, targetUser);
      }

      // After handling faculty lead programs, remove the FACULTY role
      userService.removeRole(targetUser, Role.Type.FACULTY);
      return new ModifyUserResult.Success(targetUser);
    }

    // Special handling for ADMIN role removal
    if (roleType == Role.Type.ADMIN && !grantRole) {
      return modifyUserAdminStatus(session, targetUsername, false, false);
    }

    // For other role types or adding roles
    if (grantRole) {
      userService.addRole(targetUser, roleType);
    } else {
      userService.removeRole(targetUser, roleType);
    }

    return new ModifyUserResult.Success(targetUser);
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
   * Resets a user's password
   */
  public PasswordResetResult resetUserPassword(
          HttpSession session,
          String targetUsername,
          String newPassword,
          String confirmPassword
  ) {
    // Check if requesting user is admin
    var adminUser = userService.findUserFromSession(session).orElse(null);
    if (adminUser == null) {
      return new PasswordResetResult.UserNotFound();
    }
    if (!userService.isAdmin(adminUser)) {
      return new PasswordResetResult.UserNotAdmin();
    }

    // Find target user
    var targetUser = userService.findByUsername(targetUsername).orElse(null);
    if (targetUser == null) {
      return new PasswordResetResult.UserNotFound();
    }

    // Prevent resetting super admin's password
    if ("admin".equals(targetUsername)) {
      return new PasswordResetResult.CannotResetSuperAdmin();
    }

    // Check if user is local
    if (!(targetUser instanceof User.LocalUser localUser)) {
      return new PasswordResetResult.CannotResetSSOUser();
    }

    // Validate passwords match
    if (!newPassword.equals(confirmPassword)) {
      return new PasswordResetResult.PasswordsDoNotMatch();
    }

    // Validate password length (minimum 8 characters)
    if (newPassword.length() < 8) {
      return new PasswordResetResult.PasswordTooShort();
    }

    // Update the password - assuming you have a way to hash passwords
    String hashedPassword = hashPassword(newPassword);
    User.LocalUser updatedUser = new User.LocalUser(
            localUser.username(),
            hashedPassword,
            localUser.email(),
            localUser.displayName(),
            localUser.theme(),
            localUser.uLink(),
            localUser.isMfaEnabled(),
            localUser.mfaSecret()
    );

    // Save the updated user
    userService.save(updatedUser);

    return new PasswordResetResult.Success();
  }


  private String hashPassword(String plainPassword) {
    return passwordEncoder.encode(plainPassword);
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
  public DeleteUserResult deleteUser(HttpSession session, String targetUsername) {
    // Check if requesting user is admin
    var adminUser = userService.findUserFromSession(session).orElse(null);
    if (adminUser == null) {
      return new DeleteUserResult.UserNotFound();
    }
    if (!userService.isAdmin(adminUser)) {
      return new DeleteUserResult.UserNotAdmin();
    }

    // Find target user
    var targetUser = userService.findByUsername(targetUsername).orElse(null);
    if (targetUser == null) {
      return new DeleteUserResult.UserNotFound();
    }

    // Prevent deleting super admin
    if ("admin".equals(targetUsername)) {
      return new DeleteUserResult.CannotDeleteSuperAdmin();
    }

    // Prevent deleting self
    if (adminUser.username().equals(targetUsername)) {
      return new DeleteUserResult.CannotDeleteSelf();
    }

    // Prevent deleting SSO user
    if (!targetUser.isLocal()) {
      return new DeleteUserResult.CannotDeleteSSOUser();
    }

    // Find relevant data for confirmation dialog
    List<Program> facultyLeadPrograms = programService.findFacultyPrograms(targetUser);

    // Handle faculty lead transfers
    for (Program program : facultyLeadPrograms) {
      programService.removeFacultyLead(program, targetUser);
    }

    // Update any application notes authored by the user
    List<Application.Note> userNotes = applicationService.findNotesByAuthor(targetUser);
    for (Application.Note note : userNotes) {
      Application.Note updatedNote = new Application.Note(
              note.programId(),
              note.student(),
              "DELETED USER",
              note.content(),
              note.timestamp()
      );
      applicationService.deleteNote(note.id());
      applicationService.saveNote(updatedNote);
    }
    userService.findByUsername(targetUsername)
            .ifPresent(userService::deleteUser);

    return new DeleteUserResult.Success();
  }
}