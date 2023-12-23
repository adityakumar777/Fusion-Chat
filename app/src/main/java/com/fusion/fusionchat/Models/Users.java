package com.fusion.fusionchat.Models;

public class Users {
    String username, usersID, number, about, ProfilePicResId,token;
    public Users() {} //Default for Firebase

    public Users(String username, String number, String ProfilePicId,String about) {
        this.username = username;
        this.number = number;
        this.ProfilePicResId = ProfilePicId;
        this.about = about;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getProfilePicResId() {
        return ProfilePicResId;
    }
    public void setProfilePicResId(String profilePicResId) {
        ProfilePicResId = profilePicResId;
    }
    public String getUsersID() {
        return usersID;
    }
    public void setUsersID(String usersID) {
        this.usersID = usersID;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getAbout() {
        return about;
    }
    public void setAbout(String about) {
        this.about = about;
    }
}
