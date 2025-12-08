package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
Page<User>findByRole(String role, Pageable pageable);
List<User> findByRole(String role);
User findByIdAndRole(int id,String role);
User findByUserName(String userName);
User findByIdAndApproved(int id,Boolean approved);

}
