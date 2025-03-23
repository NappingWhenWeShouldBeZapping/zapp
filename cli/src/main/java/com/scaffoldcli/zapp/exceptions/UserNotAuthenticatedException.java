package com.scaffoldcli.zapp.exceptions;

public class UserNotAuthenticatedException extends Exception {
    public UserNotAuthenticatedException(){
        super("User is not authenticated");
    }
}
