package ub.edu.moneysplitter.model;

public class MemberDebt {
    private String userID;

    private float debt;

    private String destUserID;
    private String debtID;
    private String date;


    public MemberDebt(String userID, float debt, String destUserID){
        this.userID = userID;
        this.debt = debt;
        this.destUserID =destUserID;
    }


    public String getUserID(){
        return this.userID;
    }

    public String getDestUserID(){
        return this.destUserID;
    }

    public String getUserDebt(){
        return String.valueOf(debt);
    }

    public String getDebtID(){
        return debtID;
    }

    public void setDebtID(String id) {
        this.debtID = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }
}



