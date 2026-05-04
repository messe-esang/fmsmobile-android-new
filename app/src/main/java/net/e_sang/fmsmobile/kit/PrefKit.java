package net.e_sang.fmsmobile.kit;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import net.e_sang.fmsmobile.data.OCRNameCard;
import net.e_sang.fmsmobile.data.UserInfo;

public class PrefKit {

    public static final String PREF_USER_INFO = "user_info";
    public static final String PREF_IS_TEST_MODE = "is_test_mode";
    public static final String PREF_USER_ADID = "user_adid";
    public static final String PREF_API_TOKEN = "api_token";
    public static final String PREF_NAME_CARD = "name_card";

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public String getDefaultPreferenceString(Context context, String key, String defaultValue) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return defaultPreference.getString(key, defaultValue);
    }

    static public void setDefaultPreferenceString(Context context, String key, String value) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = defaultPreference.edit();

        editor.putString(key, value);
        editor.commit();
    }

    static public boolean getDefaultPreferenceBoolean(Context context, String key, boolean defaultValue) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return defaultPreference.getBoolean(key, defaultValue);
    }

    static public void setDefaultPreferenceBoolean(Context context, String key, boolean value) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = defaultPreference.edit();

        editor.putBoolean(key, value);
        editor.commit();
    }

    static public int getDefaultPreferenceInt(Context context, String key, int defaultValue) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return defaultPreference.getInt(key, defaultValue);
    }

    static public void setDefaultPreferenceInt(Context context, String key, int value) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = defaultPreference.edit();

        editor.putInt(key, value);
        editor.commit();
    }

    static public long getDefaultPreferenceLong(Context context, String key, long defaultValue) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return defaultPreference.getLong(key, defaultValue);
    }

    static public void setDefaultPreferenceLong(Context context, String key, long value) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = defaultPreference.edit();

        editor.putLong(key, value);
        editor.commit();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public boolean getTestMode(Context context) {
        return getDefaultPreferenceBoolean(context, PREF_IS_TEST_MODE, false);
    }

    static public void setTestMode(Context context, boolean isTestMode) {
        setDefaultPreferenceBoolean(context, PREF_IS_TEST_MODE, isTestMode);
    }

    static public UserInfo getUserInfo(Context context) {
        String json = getDefaultPreferenceString(context, PREF_USER_INFO, "");
        return new Gson().fromJson(json, UserInfo.class);
    }

    static public void setUserInfo(Context context, UserInfo userInfo) {
        setDefaultPreferenceString(context, PREF_USER_INFO, new Gson().toJson(userInfo));
    }

    static public String getUserAdId(Context context) {
        return getDefaultPreferenceString(context, PREF_USER_ADID, "");
    }

    static public void setUserAdId(Context context, String adid) {
        setDefaultPreferenceString(context, PREF_USER_ADID, adid);
    }

    static public String getApiToken(Context context) {
        return getDefaultPreferenceString(context, PREF_API_TOKEN, "");
    }

    static public void setApiToken(Context context, String api_token) {
        setDefaultPreferenceString(context, PREF_API_TOKEN, api_token);
    }

    static public OCRNameCard getNameCard(Context context) {
        String json = getDefaultPreferenceString(context, PREF_NAME_CARD, "");
        return new Gson().fromJson(json, OCRNameCard.class);
    }

    static public void setNameCard(Context context, OCRNameCard OCRNameCard) {
        setDefaultPreferenceString(context, PREF_NAME_CARD, new Gson().toJson(OCRNameCard));
    }
}
