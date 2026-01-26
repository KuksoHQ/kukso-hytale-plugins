package com.kukso.hy.lib.util;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandSender;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// https://raw.githubusercontent.com/KuksoHQ/kukso-hy-lib/refs/heads/main/version.txt
public class VersionUtil {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public void check(CommandSender sender, String owner, String name, String version) {
        String latestTag = fetchLatestReleaseTag(owner, name);
        if (latestTag == null) {
            sender.sendMessage(ColorUtil.translation("§eCouldn't get the version info for " + name + ". Please check again later."));
        } else {
            // latestTag sample: "v1.2.3" or "1.2.3"
            // compare with version; basic semver comparison
            if (isOutdated(version, latestTag)) {
                sender.sendMessage(ColorUtil.translation("§eThere is a new version for §6" + name + "§e: §6" + latestTag + "§e. Please update!"));
            } else {
                sender.sendMessage(ColorUtil.translation("§2You are running the latest version for §6" + name + "§2."));
            }
        }
    }

    /**
     * Fetches latest release tag from GitHub API.
     */
    public String fetchLatestReleaseTag(String owner, String repo) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("User-Agent", "KuksoHyLib-VersionChecker");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            if (status != 200) {
                LOGGER.atWarning().log("Failed to fetch latest release for " + owner + "/" + repo + ". HTTP Status: " + status);
                return null;
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }

                String json = response.toString();
                // "tag_name":"v1.2.3" kısmını ayıkla
                String target = "\"tag_name\":\"";
                int idx = json.indexOf(target);
                if (idx == -1) {
                    LOGGER.atWarning().log("Could not find tag_name in GitHub API response for " + owner + "/" + repo);
                    return null;
                }
                int start = idx + target.length();
                int end = json.indexOf("\"", start);
                if (end == -1) return null;
                return json.substring(start, end);
            }
        } catch (Exception ex) {
            LOGGER.atSevere().log("Exception while fetching latest release from GitHub API for " + owner + "/" + repo + ": " + ex.getMessage());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Simple version comparison: It returns true if version < latestTag.
     * version and latestTag must be in “1.2.3” or “v1.2.3” format.
     */
    public boolean isOutdated(String localVersion, String latestTag) {
        String v1 = localVersion.startsWith("v") ? localVersion.substring(1) : localVersion;
        String v2 = latestTag.startsWith("v") ? latestTag.substring(1) : latestTag;

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int len = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < len; i++) {
            int num1 = i < parts1.length ? parseIntSafe(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseIntSafe(parts2[i]) : 0;
            if (num1 < num2) return true;
            else if (num1 > num2) return false;
        }
        return false; // Your version number is equal or higher
    }

    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
