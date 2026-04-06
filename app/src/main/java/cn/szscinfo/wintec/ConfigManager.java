package cn.szscinfo.wintec;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigManager {
    private static final String PREF_NAME = "dashboard_config";
    private static final String KEY_CURRENT_URL = "current_url";
    private static final String KEY_CURRENT_DASHBOARD_NAME = "current_dashboard_name";

    // 看板配置（你可以修改成自己的ERP看板地址）
    private final String[] dashboardNames = {
            "生产看板",
            "销售看板",
            "库存看板",
            "财务看板"
    };

    private final String[] dashboardUrls = {
            "https://www.jiapuwang.net/b/wintec/k.php",  // 生产看板地址
//            "https://www.jiapuwang.net/b/wintec/k2.php",       // 改成你的销售看板地址
            "https://www.jiapuwang.net/b/wintec/k2.php",   // 品质看板地址
//            "https://www.jiapuwang.net/b/wintec/index.php"      // 改成你的财务看板地址
    };

    private SharedPreferences prefs;

    public ConfigManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String[] getDashboardNames() {
        return dashboardNames;
    }

    public String[] getDashboardUrls() {
        return dashboardUrls;
    }

    public String getDashboardUrl(int index) {
        if (index >= 0 && index < dashboardUrls.length) {
            return dashboardUrls[index];
        }
        return dashboardUrls[0];
    }

    public String getCurrentUrl() {
        return prefs.getString(KEY_CURRENT_URL, dashboardUrls[0]);
    }

    public void saveCurrentUrl(String url) {
        prefs.edit().putString(KEY_CURRENT_URL, url).apply();
    }

    public String getCurrentDashboardName() {
        return prefs.getString(KEY_CURRENT_DASHBOARD_NAME, dashboardNames[0]);
    }

    public void saveCurrentDashboardName(String name) {
        prefs.edit().putString(KEY_CURRENT_DASHBOARD_NAME, name).apply();
    }

    public void saveCurrentDashboard(int index) {
        if (index >= 0 && index < dashboardNames.length) {
            saveCurrentUrl(dashboardUrls[index]);
            saveCurrentDashboardName(dashboardNames[index]);
        }
    }
}