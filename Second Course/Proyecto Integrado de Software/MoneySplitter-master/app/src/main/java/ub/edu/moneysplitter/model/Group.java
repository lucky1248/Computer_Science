package ub.edu.moneysplitter.model;

import java.io.Serializable;

public class Group implements Serializable {


    private String mId;
    private String mName;
    private String mDescription;
    private String mDate;
    private String mPictureURL;
    private GroupMembers members;


    public Group(String id, String name, String description, String date, String picture_url, GroupMembers members) {
        this.mId = id;
        this.mName = name;
        this.mDescription = description;
        this.mDate = date;
        this.mPictureURL = picture_url;

        this.members = members;
    }

    public String getDescription() {
        return mDescription;
    }
    public String getDate() {
        return mDate;
    }
    public String getName() {
        return mName;
    }

    public String getURL() {
        return mPictureURL;
    }
    public String getID() {
        return mId;
    }

    public GroupMembers getMembers(){return members;}
}
