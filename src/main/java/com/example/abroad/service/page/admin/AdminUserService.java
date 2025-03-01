package com.example.abroad.service.page.admin;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Predicate;

@Service
public record AdminUserService(
  ProgramService programService,
  ApplicationService applicationService,
  UserService userService,
  PasswordEncoder passwordEncoder
) {

  public enum Sort {
    NAME, USERNAME, EMAIL, ROLE, USER_TYPE
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
    Map<String, String> applicationPrograms
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

    // Create a map of application ID to program name
    Map<String, String> applicationPrograms = new HashMap<>();
    for (Application app : applications) {
      programService.findById(app.programId()).ifPresent(program ->
              applicationPrograms.put(app.id(), program.title())
      );
    }

    return new UserInfo(user, facultyLeadPrograms, applications, applicationPrograms);
  }

  private Predicate<UserInfo> matchesSearchFilter(String searchTerm) {
    return userInfo -> {
      if (searchTerm == null || searchTerm.isEmpty()) {
        return true;
      }
      String lowercaseSearch = searchTerm.toLowerCase();
      return userInfo.user().displayName().toLowerCase().contains(lowercaseSearch) ||
              userInfo.user().username().toLowerCase().contains(lowercaseSearch) ||
              userInfo.user().email().toLowerCase().contains(lowercaseSearch);
    };
  }

  private Comparator<UserInfo> getSortComparator(Sort sort, Boolean ascending) {
    Comparator<UserInfo> comparator = switch (sort) {
      case NAME -> Comparator.comparing(userInfo -> userInfo.user().displayName());
      case USERNAME -> Comparator.comparing(userInfo -> userInfo.user().username());
      case EMAIL -> Comparator.comparing(userInfo -> userInfo.user().email());
      case ROLE -> Comparator.comparing(userInfo -> userInfo.user().role().toString());
      case USER_TYPE -> Comparator.comparing(userInfo -> userInfo.user().isLocal() ? "Local" : "SSO");
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
    if (!adminUser.isAdmin()) {
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
        var updateUser = targetUser.withRole(User.Role.STUDENT);
        userService.save(updateUser);
        return new ModifyUserResult.Success(updateUser);
      }

      //will only proceed past this point if user is faculty lead
      if (!confirmed) {
        return new ModifyUserResult.RequiresConfirmation(targetUsername, facultyLeadPrograms);
      }
      for (Program program : facultyLeadPrograms) {
        programService.removeFacultyLead(program, targetUser);
      }
    }

    // Modify user admin status
    var updatedUser = targetUser.withRole(grantAdmin ? User.Role.ADMIN : User.Role.STUDENT);
    userService.save(updatedUser);

    return new ModifyUserResult.Success(updatedUser);
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
    if (!adminUser.isAdmin()) {
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
      localUser.role(),
      localUser.displayName(),
      localUser.theme()
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
    if (!adminUser.isAdmin()) {
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
        note.applicationId(),
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