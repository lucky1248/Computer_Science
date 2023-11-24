package ub.edu.moneysplitter.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPreference {

    static final String LOGGED_IN_PREF= "username";
    static final String NOTIFICATIONS_PREF= "notifications";
    static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Set the Login Status
     * @param context
     * @param loggedIn
     */
    public static void setLoggedIn(Context context, boolean loggedIn, String user, String pswd) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(LOGGED_IN_PREF, loggedIn);
        editor.putString("user", user);
        editor.putString("pswd", pswd);
        editor.apply();
    }


    /**
     * Set the Login Status
     * @param context
     * @param loggedIn
     */
    public static void setGroupsNotifications(Context context, boolean loggedIn,String newGroups) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(NOTIFICATIONS_PREF, loggedIn);
        editor.putString("newGroups", newGroups);
        editor.apply();
    }
    public static void setBillsNotifications(Context context, boolean loggedIn, String newBills) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(NOTIFICATIONS_PREF, loggedIn);
        editor.putString("newBills", newBills);
        editor.apply();
    }
    public static void setPendingNotifications(Context context, boolean loggedIn, String pendentPays) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(NOTIFICATIONS_PREF, loggedIn);
        editor.putString("pendentPays", pendentPays);
        editor.apply();
    }
    public static void setRecievedNotifications(Context context, boolean loggedIn, String recievedPays) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(NOTIFICATIONS_PREF, loggedIn);
        editor.putString("recievedPays", recievedPays);
        editor.apply();
    }

    /**
     * Get the Login Status
     * @param context
     * @return boolean: login status
     */
    public static boolean getNotificationsStatus(Context context) {
        return getPreferences(context).getBoolean(NOTIFICATIONS_PREF, false);
    }
    public static String getnewBills(Context context){
        return getPreferences(context).getString("newBills", "");
    }
    public static String getnewGroups(Context context){
        return getPreferences(context).getString("newGroups", "");
    }
    public static String getrecievedPays(Context context){
        return getPreferences(context).getString("recievedPays", "");
    }
    public static String getpendentPays(Context context){
        return getPreferences(context).getString("pendentPays", "");
    }

    /**
     * Get the Login Status
     * @param context
     * @return boolean: login status
     */
    public static boolean getLoggedStatus(Context context) {
        return getPreferences(context).getBoolean(LOGGED_IN_PREF, false);
    }

    public static String getUser(Context context){
        return getPreferences(context).getString("user", "");
    }

    public static String getPswd(Context context){
        return getPreferences(context).getString("pswd", "");
    }
}