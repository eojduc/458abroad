package com.example.abroad.controller;

import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.view.Alerts;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/partner")
public record PartnerController(
        UserService userService,
        ProgramService programService,
        ApplicationService applicationService
) {


    // Define Sort enum for program sorting
    public enum Sort {
        TITLE, SEMESTER, FACULTY, DEADLINE, TOTAL, PAID
    }

    @GetMapping("/programs")
    public String listPrograms(
            HttpSession session,
            Model model,
            @RequestParam Optional<String> error,
            @RequestParam Optional<String> success,
            @RequestParam Optional<String> warning,
            @RequestParam Optional<String> info
    ) {
        // Get user from session
        Optional<User> userOpt = userService.findUserFromSession(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login?error=Please log in to access the partner portal";
        }

        User user = userOpt.get();
        // Verify the user is a partner
        if (!userService.isPartner(user)) {
            return "redirect:/?error=You do not have access to the partner portal";
        }

        // Get programs associated with this partner with default sorting (by TITLE ascending)
        List<ProgramService.ProgramWithCounts> programs = programService.getPartnerPrograms(user.username());
        List<ProgramWithFaculty> programsWithFaculty = preparePrograms(programs);

        model.addAttribute("user", user);
        model.addAttribute("programs", programsWithFaculty);
        model.addAttribute("alerts", new Alerts(error, success, warning, info));
        model.addAttribute("sort", "TITLE");
        model.addAttribute("ascending", true);

        return "partner/program-list :: page";
    }

    @GetMapping("/programs/table")
    public String getProgramsTable(
            HttpSession session,
            Model model,
            @RequestParam Sort sort,
            @RequestParam Boolean ascending
    ) {
        // Get user from session
        Optional<User> userOpt = userService.findUserFromSession(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login?error=Please log in to access the partner portal";
        }

        User user = userOpt.get();
        // Verify the user is a partner
        if (!userService.isPartner(user)) {
            return "redirect:/?error=You do not have access to the partner portal";
        }

        // Get programs associated with this partner with sorting
        List<ProgramService.ProgramWithCounts> programs = programService.getPartnerProgramsSorted(user.username(), sort, ascending);
        List<ProgramWithFaculty> programsWithFaculty = preparePrograms(programs);

        model.addAttribute("user", user);
        model.addAttribute("programs", programsWithFaculty);
        model.addAttribute("sort", sort.name());
        model.addAttribute("ascending", ascending);

        return "partner/program-list :: programTable";
    }

    private List<ProgramWithFaculty> preparePrograms(List<ProgramService.ProgramWithCounts> programs) {
        // Add faculty lead information for each program
        return programs.stream()
                .map(program -> {
                    List<? extends User> facultyLeads = programService.findFacultyLeads(program.program());
                    List<String> facultyLeadNames = facultyLeads.stream()
                            .map(User::displayName)
                            .toList();
                    return new ProgramWithFaculty(program, facultyLeadNames);
                })
                .toList();
    }

    // Add this record to your controller
    public record ProgramWithFaculty(
            ProgramService.ProgramWithCounts program,
            List<String> facultyLeadNames
    ) {}

    @GetMapping("/programs/{programId}")
    public String viewProgram(
            @PathVariable Integer programId,
            HttpSession session,
            Model model,
            @RequestParam Optional<String> error,
            @RequestParam Optional<String> success,
            @RequestParam Optional<String> warning,
            @RequestParam Optional<String> info
    ) {
        // Get user from session
        Optional<User> userOpt = userService.findUserFromSession(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login?error=Please log in to access the partner portal";
        }

        User user = userOpt.get();
        // Verify the user is a partner
        if (!userService.isPartner(user)) {
            return "redirect:/?error=You do not have access to the partner portal";
        }

        // Check if this partner is associated with this program
        if (!programService.isPartnerForProgram(user.username(), programId)) {
            return "redirect:/partner/programs?error=You do not have access to this program";
        }

        // Get program details
        Optional<Program> programOpt = programService.getProgram(programId);
        if (programOpt.isEmpty()) {
            return "redirect:/partner/programs?error=Program not found";
        }

        Program program = programOpt.get();

        // Get faculty leads for this program
        List<? extends User> facultyLeads = programService.findFacultyLeads(program);
        List<String> facultyLeadNames = facultyLeads.stream()
                .map(User::displayName)
                .toList();

        // Get approved/enrolled students for this program with payment status
        var applicants = applicationService.getApprovedAndEnrolledApplicants(programId);

        model.addAttribute("user", user);
        model.addAttribute("program", program);
        model.addAttribute("facultyLeadNames", facultyLeadNames);
        model.addAttribute("applicants", applicants);
        model.addAttribute("alerts", new Alerts(error, success, warning, info));

        return "partner/program-detail :: page";
    }

    @PostMapping("/programs/{programId}/update-payment")
    public String updatePaymentStatus(
            @PathVariable Integer programId,
            @RequestParam String username,
            @RequestParam String paymentStatus,
            HttpSession session
    ) {
        // Get user from session
        Optional<User> userOpt = userService.findUserFromSession(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login?error=Please log in to access the partner portal";
        }

        User user = userOpt.get();
        // Verify the user is a partner
        if (!userService.isPartner(user)) {
            return "redirect:/?error=You do not have access to the partner portal";
        }

        // Check if this partner is associated with this program
        if (!programService.isPartnerForProgram(user.username(), programId)) {
            return "redirect:/partner/programs?error=You do not have access to this program";
        }

        // Update the payment status
        boolean updated = applicationService.updatePaymentStatus(programId, username, paymentStatus);

        if (updated) {
            return "redirect:/partner/programs/" + programId + "?success=Payment status updated";
        } else {
            return "redirect:/partner/programs/" + programId + "?error=Failed to update payment status";
        }
    }

}