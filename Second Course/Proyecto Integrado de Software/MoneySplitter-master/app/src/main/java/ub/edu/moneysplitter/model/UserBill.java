package ub.edu.moneysplitter.model;

public class UserBill {

    private String userID;
    private User user;
    private Bill bill;
    private String billID;
    private float userBill;


    public UserBill(String userID, String billID, float userBill){
        this.userID = userID;
        this.userBill = userBill;
        this.billID = billID;
    }


    public String getUserID(){return userID;}
    public String getBillID(){return billID;}
    public float getUserBillAmount(){return userBill;}
}
