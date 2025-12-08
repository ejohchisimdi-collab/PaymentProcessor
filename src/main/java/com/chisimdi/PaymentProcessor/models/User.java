package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int id;
    @NotNull
    @Column(unique = true)
   private String userName;
    @NotNull
            @Pattern(regexp = "^[A-Za-z]+$",message = "Name must have no spaces and only be letters")
    private String name;
    @NotNull
    private String password;
    @NotNull
            @Pattern(regexp = "^Customer|Admin|Merchant$",message = "Role must be Customer, Admin or Merchant")
    private String role;
    private Boolean approved=false;
    @Version
    int version;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }
}
