package com.weimi.weimichat.bean.chat;

/**
 * Created by yxl on 2018/9/18.
 */

public class Groups {
    private String id;
    private String name;
    private String portraitUri;
    private String displayName;
    private String role;
    private String bulletin;
    private String timestamp;
    private String nameSpelling;
    public Groups(String id) {
        this.id = id;
    }

    public Groups(String id, String name, String portraitUri) {
        this.id = id;
        this.name = name;
        this.portraitUri = portraitUri;
    }
    public Groups() {
        super();
    }

    public Groups(String groupsId, String name, String portraitUri, String displayName, String role, String bulletin, String timestamp) {
        this(groupsId, name, portraitUri);
        this.displayName = displayName;
        this.role = role;
        this.bulletin = bulletin;
        this.timestamp = timestamp;
    }

    public Groups(String timestamp, String role, String displayName, String portraitUri, String name, String groupsId) {
        this(groupsId, name, portraitUri);;
        this.timestamp = timestamp;
        this.role = role;
        this.displayName = displayName;
    }

    public Groups(String groupsId, String name, String portraitUri, String role) {
        this(groupsId, name, portraitUri);
        this.role = role;
    }

    public Groups(String groupsId, String name, String portraitUri, String displayName, String role, String bulletin, String timestamp, String nameSpelling) {
        this(groupsId, name, portraitUri);
        this.displayName = displayName;
        this.role = role;
        this.bulletin = bulletin;
        this.timestamp = timestamp;
        this.nameSpelling = nameSpelling;
    }
    /** Not-null value. */
    public String getGroupsId() {
        return getUserId();
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setGroupsId(String groupsId) {
        setUserId(groupsId);
    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBulletin() {
        return bulletin;
    }

    public void setBulletin(String bulletin) {
        this.bulletin = bulletin;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNameSpelling() {
        return nameSpelling;
    }

    public void setNameSpelling(String nameSpelling) {
        this.nameSpelling = nameSpelling;
    }

    public String getUserId() {
        return id;
    }

    public void setUserId(String userId) {
        this.id = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPortraitUri() {
        return portraitUri;
    }

    public void setPortraitUri(String uri) {
        this.portraitUri = uri;
    }
}
