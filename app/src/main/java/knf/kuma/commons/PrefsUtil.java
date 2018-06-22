package knf.kuma.commons;

import android.content.Context;
import android.preference.PreferenceManager;

public class PrefsUtil {
    private static Context context;

    public static void init(Context context) {
        PrefsUtil.context = context;
    }

    public static boolean showProgress() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_progress", true);
    }

    public static String getThemeOption() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("theme_option", "0");
    }

    public static int getFavsOrder() {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("favs_order", 0);
    }

    public static void setFavsOrder(int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("favs_order", value).apply();
    }

    public static int getDirOrder() {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("dir_order", 0);
    }

    public static void setDirOrder(int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("dir_order", value).apply();
    }

    public static boolean isChapsAsc() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("asc_chapters", false);
    }
}