package com.chisimdi.PaymentProcessor;

import com.chisimdi.PaymentProcessor.Exceptions.InvalidCredentialsException;
import com.chisimdi.PaymentProcessor.models.User;
import com.chisimdi.PaymentProcessor.models.UserDTO;
import com.chisimdi.PaymentProcessor.repository.UserRepository;
import com.chisimdi.PaymentProcessor.services.JwtUtilService;
import com.chisimdi.PaymentProcessor.services.UserService;
import com.chisimdi.PaymentProcessor.utils.LoginRequest;
import com.chisimdi.PaymentProcessor.utils.LoginResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

   @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;
   @Mock
    UserRepository userRepository;
   @Mock
    JwtUtilService jwtUtilService;

   @InjectMocks
    UserService userService;

   @Test
    void registerTest(){
       User user=new User();
       user.setPassword("2000");
       when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn(user.getPassword());
       when(userRepository.save(user)).thenReturn(user);
       UserDTO userDTO=userService.register(user);
       assertThat(user.getPassword()).isEqualTo(bCryptPasswordEncoder.encode("2000"));
       assertThat(user.getApproved()).isEqualTo(false);
       verify(userRepository).save(user);


   }
   @Test
   void loginTest(){
       User user=new User();
       user.setUserName("Chisimdi");
       user.setPassword("2000");
       user.setRole("Customer");
       user.setId(1);
       user.setApproved(false);
       LoginRequest loginRequest=new LoginRequest();
       loginRequest.setPassword("2000");
       loginRequest.setUserName("Chisimdi");
       String token= jwtUtilService.generateToken("Chisimdi",1,"Customer");
       when(userRepository.findByUserName(loginRequest.getUserName())).thenReturn(user);
       when(bCryptPasswordEncoder.matches(loginRequest.getPassword(),user.getPassword())).thenReturn(true);
       when(jwtUtilService.generateToken("Chisimdi",1,"Customer")).thenReturn(token);
       when(jwtUtilService.extractUserName(token)).thenReturn("Chisimdi");
       when(jwtUtilService.extractUserId(token)).thenReturn(1);
       when(jwtUtilService.extractRole(token)).thenReturn("Customer");

       LoginResponse loginResponse=userService.login(loginRequest);

       assertThat(loginResponse.getUserId()).isEqualTo(1);
       assertThat(loginResponse.getUserName()).isEqualTo("Chisimdi");
       assertThat(loginResponse.getRole()).isEqualTo("Customer");

       verify(userRepository).findByUserName(loginRequest.getUserName());
       verify(bCryptPasswordEncoder).matches(loginRequest.getPassword(),user.getPassword());
       verify(jwtUtilService,times(2)).generateToken("Chisimdi",1,"Customer");
       verify(jwtUtilService).extractUserName(token);
       verify(jwtUtilService).extractUserId(token);
       verify(jwtUtilService).extractRole(token);

   }

   @Test
   void loginTest_ThrowsInvalidCredentialsExceptionForPassword(){
       User user=new User();
       user.setUserName("Chisimdi");
       user.setPassword("2000");
       user.setRole("Merchant");
       user.setId(1);
       LoginRequest loginRequest=new LoginRequest();
       loginRequest.setPassword("2000");
       loginRequest.setUserName("Chisimdi");

       when(userRepository.findByUserName(loginRequest.getUserName())).thenReturn(user);
        when(bCryptPasswordEncoder.matches(loginRequest.getPassword(),"2000")).thenReturn(false);



       assertThatThrownBy(()->userService.login(loginRequest)).isInstanceOf(InvalidCredentialsException.class);



       verify(userRepository).findByUserName(loginRequest.getUserName());
       verify(bCryptPasswordEncoder).matches(loginRequest.getPassword(),user.getPassword());
       verify(jwtUtilService,never()).generateToken("Chisimdi",1,"Merchant");


   }
   @Test
   void loginTest_ThrowsInvalidCredentialsExceptionForMerchant(){
       User user=new User();
       user.setUserName("Chisimdi");
       user.setPassword("2000");
       user.setRole("Merchant");
       user.setId(1);
       LoginRequest loginRequest=new LoginRequest();
       loginRequest.setPassword("2000");
       loginRequest.setUserName("Chisimdi");


       when(userRepository.findByUserName(loginRequest.getUserName())).thenReturn(user);
       when(bCryptPasswordEncoder.matches(loginRequest.getPassword(),"2000")).thenReturn(false);



       assertThatThrownBy(()->userService.login(loginRequest)).isInstanceOf(InvalidCredentialsException.class);



       verify(userRepository).findByUserName(loginRequest.getUserName());
       verify(bCryptPasswordEncoder).matches(loginRequest.getPassword(),user.getPassword());
       verify(jwtUtilService,never()).generateToken("Chisimdi",1,"Merchant");


   }
    @Test
    void loginTest_ThrowsInvalidCredentialsExceptionForAdmin(){
        User user=new User();
        user.setUserName("Chisimdi");
        user.setPassword("2000");
        user.setRole("Admin");
        user.setId(1);
        LoginRequest loginRequest=new LoginRequest();
        loginRequest.setPassword("2000");
        loginRequest.setUserName("Chisimdi");


        when(userRepository.findByUserName(loginRequest.getUserName())).thenReturn(user);
        when(bCryptPasswordEncoder.matches(loginRequest.getPassword(),"2000")).thenReturn(false);



        assertThatThrownBy(()->userService.login(loginRequest)).isInstanceOf(InvalidCredentialsException.class);



        verify(userRepository).findByUserName(loginRequest.getUserName());
        verify(bCryptPasswordEncoder).matches(loginRequest.getPassword(),user.getPassword());
        verify(jwtUtilService,never()).generateToken("Chisimdi",1,"Merchant");


    }
    @Test
    void approveUserTest(){
User user=new User();
user.setApproved(false);

when(userRepository.findById(1)).thenReturn(Optional.of(user));
when(userRepository.save(user)).thenReturn(user);

UserDTO userDTO=userService.approveUsers(1);

assertThat(user.getApproved()).isEqualTo(true);

verify(userRepository).save(user);
verify(userRepository).findById(1);
    }






}
