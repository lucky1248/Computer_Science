package ub.edu.moneysplitter.model;

import java.io.Serializable;
import java.util.ArrayList;

public class GroupMembers implements Serializable {



    private ArrayList<String> groupMembers;


    public GroupMembers(ArrayList<String> groupMembers){
        this.groupMembers = groupMembers;
    }



    public ArrayList<String> getArray(){
       return groupMembers; //clone it to avoid future problems
    }
}
