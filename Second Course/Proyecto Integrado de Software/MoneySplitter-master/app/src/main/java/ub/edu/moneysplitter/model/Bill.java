package ub.edu.moneysplitter.model;

import java.io.Serializable;

public class Bill implements Serializable {


    private String billId;
    private String name;
    private float price;
    private String date;
    private String hour;
    private String payerID;
    private String groupName;
    private String pictureUrl;

    private boolean settled;


    public Bill(String billId, String name, float price, String date, String hour, String payer, boolean settled, String pictureUrl){
        this.name = name;
        this.price = price;
        this.billId = billId;
        this.date = date;
        this.hour = hour;
        this.payerID = payer;
        this.settled = settled;
        this.pictureUrl = pictureUrl;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public String getID() {
        return billId;
    }


    public String getDate() {
        return date;
    }
    public String getHour() {
        return hour;
    }
    public String getPayerName() {
        return payerID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupID(String groupName){
        this.groupName = groupName;
    }

    public boolean isSettled() {
        return this.settled;
    }
    public String getPictureUrl(){ return this.pictureUrl;}
}
