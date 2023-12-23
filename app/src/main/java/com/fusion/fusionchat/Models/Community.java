package com.fusion.fusionchat.Models;

import java.util.ArrayList;

public class Community {
    String communityName,communityPicResId,creatorUid,communityId;
    ArrayList<String> members = new ArrayList<>();
    ArrayList<MessageModel> chats = new ArrayList<>();

    public Community() {}

    public Community(String communityName, String communityPicResId) {
        this.communityName = communityName;
        this.communityPicResId = communityPicResId;
    }

    public ArrayList<MessageModel> getChats() {
        return chats;
    }

    public void setChats(ArrayList<MessageModel> chats) {
        this.chats = chats;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public String getCommunityPicResId() {
        return communityPicResId;
    }

    public void setCommunityPicResId(String communityPicResId) {
        this.communityPicResId = communityPicResId;
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }
}
