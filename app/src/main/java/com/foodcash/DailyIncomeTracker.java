package com.foodcash;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyIncomeTracker {
    private static final String PREF_NAME = "DailyIncomePrefs";
    private static final String KEY_INCOME = "dailyIncome";
    private static final String KEY_DATE = "currentDate";
    private static final String KEY_USER_ID = "currentUserId";

    private SharedPreferences sharedPreferences;
    private String currentUserId;

    public DailyIncomeTracker(Context context, String userId) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.currentUserId = userId;
    }

    // Get total income for the current day
    public int getDailyIncome() {
        resetIncomeIfDateOrUserChanged();
        return sharedPreferences.getInt(KEY_INCOME, 0);
    }

    // Add income for the current day
    public void addIncome(int amount) {
        resetIncomeIfDateOrUserChanged();
        int currentIncome = getDailyIncome();
        sharedPreferences.edit().putInt(KEY_INCOME, currentIncome + amount).apply();
    }

    // Reset income if date or user ID has changed
    private void resetIncomeIfDateOrUserChanged() {
        String savedDate = sharedPreferences.getString(KEY_DATE, "");
        String currentDate = getCurrentDate();
        String savedUserId = sharedPreferences.getString(KEY_USER_ID, "");

        // Check if the date or user ID has changed
        if (!currentDate.equals(savedDate) || !currentUserId.equals(savedUserId)) {
            sharedPreferences.edit()
                    .putString(KEY_DATE, currentDate)
                    .putString(KEY_USER_ID, currentUserId)
                    .putInt(KEY_INCOME, 0)
                    .apply();
        }
    }

    // Get current date in yyyy-MM-dd format
    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
