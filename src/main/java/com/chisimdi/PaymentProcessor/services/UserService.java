package com.chisimdi.PaymentProcessor.services;

import com.chisimdi.PaymentProcessor.Exceptions.InvalidCredentialsException;
import com.chisimdi.PaymentProcessor.Exceptions.ResourceNotFoundException;
import com.chisimdi.PaymentProcessor.models.User;
import com.chisimdi.PaymentProcessor.models.UserDTO;
import com.chisimdi.PaymentProcessor.repository.UserRepository;
import com.chisimdi.PaymentProcessor.utils.LoginRequest;
import com.chisimdi.PaymentProcessor.utils.LoginResponse;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserRepository userRepository;
    private JwtUtilService jwtUtilService;

    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository,JwtUtilService jwtUtilService) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.jwtUtilService=jwtUtilService;
    }

    public UserDTO toUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        if (user.getName() != null) {
            userDTO.setName(user.getName());
        }
        if (user.getRole() != null) {
            userDTO.setRole(user.getRole());
        }
        if(user.getApproved()!=null){
            userDTO.setApproved(user.getApproved());
        }
        userDTO.setId(user.getId());
        return userDTO;
    }

    public List<UserDTO> viewAllUsers(int pageNumber, int size) {
        List<UserDTO> userDTOS = new ArrayList<>();
        Page<User> users = userRepository.findAll(PageRequest.of(pageNumber, size));
        for (User u : users) {
            userDTOS.add(toUserDTO(u));
        }
        return userDTOS;
    }

    public List<UserDTO> findAllCustomers(int pageNumber, int size) {
        List<UserDTO> userDTOS = new ArrayList<>();
        Page<User> users = userRepository.findByRole("Customer", PageRequest.of(pageNumber, size));
        for (User u : users) {
            userDTOS.add(toUserDTO(u));
        }
        return userDTOS;
    }

    public List<UserDTO> findAllMerchants(int pageNumber, int size) {
        List<UserDTO> userDTOS = new ArrayList<>();
        Page<User> users = userRepository.findByRole("Merchant", PageRequest.of(pageNumber, size));
        for (User u : users) {
            userDTOS.add(toUserDTO(u));
        }
        return userDTOS;
    }
    public UserDTO register(User user){
        log.info("Registering user wit id {}",user.getId());
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        log.info("User registered sucessfully");

        return toUserDTO(userRepository.save(user));
    }
    public LoginResponse login(LoginRequest loginRequest){
        User user= userRepository.findByUserName(loginRequest.getUserName());
        if(user==null){
            throw new InvalidCredentialsException("User name is not valid");
        }
        if(!bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new InvalidCredentialsException("Password invalid");
        }
        if(user.getApproved()==false&&user.getRole().equals("Merchant")){
            throw new InvalidCredentialsException("User has not yet been approved");
        }
        if(user.getApproved()==false&&user.getRole().equals("Admin")){
            throw new InvalidCredentialsException("User has not yet been approved");
        }
        String token =jwtUtilService.generateToken(user.getUserName(), user.getId(), user.getRole());
        LoginResponse loginResponse=new LoginResponse();
        loginResponse.setUserName(jwtUtilService.extractUserName(token));
        loginResponse.setUserId(jwtUtilService.extractUserId(token));
        loginResponse.setRole(jwtUtilService.extractRole(token));
        loginResponse.setToken(token);
        return loginResponse;
    }
    public UserDTO approveUsers(int userId){
        User user= userRepository.findByIdAndApproved(userId,false);
        if (user==null){
            throw new ResourceNotFoundException("User with id "+userId+" and approved status not approved not found");

        }
        user.setApproved(true);
        return toUserDTO(userRepository.save(user));

    }

    public UserDTO approveUsersWithRetries(int userId){
        int retries=0;
        while (retries<5){
            try {
                approveUsers(userId);
            }
            catch (OptimisticLockException e){
                retries++;
            }
        }
        throw new OptimisticLockException("Number of retries has been exceeded");
    }

}