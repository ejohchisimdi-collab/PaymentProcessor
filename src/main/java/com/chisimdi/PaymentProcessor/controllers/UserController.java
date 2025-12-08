package com.chisimdi.PaymentProcessor.controllers;

import com.chisimdi.PaymentProcessor.models.User;
import com.chisimdi.PaymentProcessor.models.UserDTO;
import com.chisimdi.PaymentProcessor.services.UserService;
import com.chisimdi.PaymentProcessor.utils.LoginRequest;
import com.chisimdi.PaymentProcessor.utils.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService){
        this.userService=userService;
    }

    @Operation(summary = "Register users",description = "Register a user available to all")
    @PostMapping("/register")
    public UserDTO register(@Valid @RequestBody User user){
        return userService.register(user);
    }

    @Operation(summary = "View all users",description = "Retrieves all users, available to only admins")
    @PreAuthorize("hasRole('ROLE_Admin')")
    @GetMapping("/")
    public List<UserDTO> viewAllUsers(@RequestParam(defaultValue = "0")int pageNumber, @RequestParam(defaultValue = "10")int size){
        return userService.viewAllUsers(pageNumber, size);
    }

    @Operation(summary = "View all customers",description = "Retrieves all customers, Available only to admins")
@PreAuthorize("hasRole('ROLE_Admin')")
    @GetMapping("/customers")
    public List<UserDTO>viewAllCustomers(@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return userService.findAllCustomers(pageNumber, size);
    }

    @Operation(summary = "view all Merchants",description = "Retrieves all merchants, Available only to admins")
@PreAuthorize("hasRole('ROLE_Admin')")
    @GetMapping("/merchants")
    public List<UserDTO>viewAllMerchants(@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return userService.findAllMerchants(pageNumber, size);
    }

    @Operation(summary = "Log in",description = "Logs all customers in. however admins and Merchants have to be approved before logging in")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest){
        return userService.login(loginRequest);
    }

    @Operation(summary = "Approves users",description = "Approves users for log in")
    @PreAuthorize("hasRole('ROLE_Admin')")
    @PostMapping("/approval/{userId}")
    public UserDTO approveUsers(@PathVariable("userId")int userId){
        return userService.approveUsersWithRetries(userId);
    }
}
