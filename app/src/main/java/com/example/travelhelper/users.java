package com.example.travelhelper;

public class users {

    // variables for storing our data.
    private String email, Password;
    public users() {
        // empty constructor
        // required for Firebase.
    }

    public users(String email, String Password) {
        this.email = email;
        this.Password = Password;
    }
    // Constructor for all variables.

    // getter methods for all variables.
    public String getemail() {
        return email;
    }

    public void setemail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return Password;
    }

    // setter method for all variables.
    public void setPassword(String Password) {
        this.Password = Password;
    }

}

