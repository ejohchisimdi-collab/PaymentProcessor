package com.chisimdi.PaymentProcessor.runners;

import com.chisimdi.PaymentProcessor.models.User;
import com.chisimdi.PaymentProcessor.repository.UserRepository;
import jakarta.persistence.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminRunner implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void run(String... args) throws Exception {
createAdmin();
    }
    public void createAdmin(){
        List<User>users=userRepository.findByRole("Admin");
        if(users.isEmpty()){
            User user=new User();
            user.setRole("Admin");
            user.setPassword(bCryptPasswordEncoder.encode("Admin"));
            user.setUserName("Admin");
            user.setApproved(true);
            user.setName("Ifeanyi");
            userRepository.save(user);
        }
    }

}
