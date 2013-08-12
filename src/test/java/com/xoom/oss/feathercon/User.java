package com.xoom.oss.feathercon;

public class User {
    public String name;
    public String emailAddress;

    public User() {
    }

    public User(String name, String emailAddress) {
        this.name = name;
        this.emailAddress = emailAddress;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                '}';
    }
}
