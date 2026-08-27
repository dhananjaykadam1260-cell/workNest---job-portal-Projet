package com.example.job.portal.controller;






import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.job.portal.model.User;
import com.example.job.portal.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        String result = userService.register(user);
        if (result.equals("exists")) {
            model.addAttribute("error", "Email already registered");
            return "register";
        }
        return "redirect:/login?registered=true";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        User user = userService.login(email, password);
        if (user == null) {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
        session.setAttribute("loggedUser", user);
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {

        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("loggedUser", loggedUser);

        return "profile";
    }
    
    @GetMapping("/profile/edit")
    public String editProfile(HttpSession session, Model model) {

        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedUser);

        return "edit-profile";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User user,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        user.setId(loggedUser.getId());
        user.setPassword(loggedUser.getPassword());

        userService.updateProfile(user);

        User updatedUser = userService.getUserById(user.getId());

        session.setAttribute("loggedUser", updatedUser);

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");

        return "redirect:/profile";
    }
    
    
    
}