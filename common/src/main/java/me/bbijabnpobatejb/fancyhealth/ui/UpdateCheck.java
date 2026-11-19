package me.bbijabnpobatejb.fancyhealth.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.experimental.UtilityClass;
import me.bbijabnpobatejb.fancyhealth.FancyHealth;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class UpdateCheck {

    public final String PROJECT = "fancy-health-indicators";
    private volatile String latest;
    private volatile String url;
    private boolean started;
    public boolean hidden;

    public void start() {
        if (started || !FancyHealth.config().general.updateCheck) return;
        started = true;
        Thread thread = new Thread(UpdateCheck::check, "fancyhealth-update");
        thread.setDaemon(true);
        thread.start();
    }

    private void check() {
        try {
            String query = "?loaders=" + URLEncoder.encode("[\"" + FancyHealth.platform().loader() + "\"]", "UTF-8")
                    + "&game_versions=" + URLEncoder.encode("[\"" + FancyHealth.platform().minecraftVersion() + "\"]", "UTF-8");
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.modrinth.com/v2/project/" + PROJECT + "/version" + query).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "fancyhealth/" + FancyHealth.platform().modVersion());
            if (connection.getResponseCode() != 200) return;
            try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                JsonArray versions = new JsonParser().parse(reader).getAsJsonArray();
                if (versions.size() == 0) return;
                JsonObject top = versions.get(0).getAsJsonObject();
                String number = top.get("version_number").getAsString();
                if (newer(number, FancyHealth.platform().modVersion())) {
                    url = "https://modrinth.com/mod/" + PROJECT + "/version/" + top.get("id").getAsString();
                    latest = number;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public boolean newer(String remote, String local) {
        String[] a = remote.split("[^0-9]+"), b = local.split("[^0-9]+");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length && !a[i].isEmpty() ? Integer.parseInt(a[i]) : 0;
            int y = i < b.length && !b[i].isEmpty() ? Integer.parseInt(b[i]) : 0;
            if (x != y) return x > y;
        }
        return false;
    }

    public String latest() {
        return hidden || !FancyHealth.config().general.updateCheck ? null : latest;
    }

    public String url() {
        return url;
    }
}
