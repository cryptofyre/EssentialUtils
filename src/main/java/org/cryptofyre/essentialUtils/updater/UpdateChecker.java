package org.cryptofyre.essentialUtils.updater;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Checks for plugin updates via GitHub Releases API.
 * Supports notification and optional auto-download.
 */
public class UpdateChecker {
    
    private static final String GITHUB_API_URL = "https://api.github.com/repos/%s/%s/releases/latest";
    private static final String USER_AGENT = "EssentialUtils-UpdateChecker";
    
    private final JavaPlugin plugin;
    private final String owner;
    private final String repo;
    private final boolean enabled;
    private final boolean checkOnStartup;
    private final boolean notifyAdmins;
    private final boolean autoDownload;
    private final String downloadPath;
    
    private volatile String latestVersion = null;
    private volatile String downloadUrl = null;
    private volatile boolean updateAvailable = false;
    
    public UpdateChecker(JavaPlugin plugin, String owner, String repo,
                         boolean enabled, boolean checkOnStartup, 
                         boolean notifyAdmins, boolean autoDownload, String downloadPath) {
        this.plugin = plugin;
        this.owner = owner;
        this.repo = repo;
        this.enabled = enabled;
        this.checkOnStartup = checkOnStartup;
        this.notifyAdmins = notifyAdmins;
        this.autoDownload = autoDownload;
        this.downloadPath = downloadPath;
    }
    
    /**
     * Check for updates asynchronously.
     * @return CompletableFuture that completes when check is done
     */
    public CompletableFuture<Boolean> checkForUpdates() {
        if (!enabled) {
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String apiUrl = String.format(GITHUB_API_URL, owner, repo);
                HttpURLConnection connection = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", USER_AGENT);
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    plugin.getLogger().warning("Failed to check for updates: HTTP " + responseCode);
                    return false;
                }
                
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    
                    JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                    String tagName = json.get("tag_name").getAsString();
                    
                    // Remove 'v' prefix if present
                    latestVersion = tagName.startsWith("v") ? tagName.substring(1) : tagName;
                    
                    // Get download URL for the JAR asset
                    if (json.has("assets") && json.getAsJsonArray("assets").size() > 0) {
                        for (var asset : json.getAsJsonArray("assets")) {
                            JsonObject assetObj = asset.getAsJsonObject();
                            String name = assetObj.get("name").getAsString();
                            if (name.endsWith(".jar")) {
                                downloadUrl = assetObj.get("browser_download_url").getAsString();
                                break;
                            }
                        }
                    }
                    
                    String currentVersion = plugin.getPluginMeta().getVersion();
                    updateAvailable = isNewerVersion(currentVersion, latestVersion);
                    
                    return updateAvailable;
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to check for updates", e);
                return false;
            }
        });
    }
    
    /**
     * Compare versions to determine if remote is newer.
     * Handles versions like "1.0.0-build.42" and "1.0.0-dev"
     */
    private boolean isNewerVersion(String current, String remote) {
        if (current == null || remote == null) return false;
        
        // Dev builds always show as outdated compared to release builds
        if (current.endsWith("-dev")) {
            return true;
        }
        
        try {
            // Parse build numbers from format like "1.0.0-build.42"
            int currentBuild = extractBuildNumber(current);
            int remoteBuild = extractBuildNumber(remote);
            
            if (currentBuild > 0 && remoteBuild > 0) {
                return remoteBuild > currentBuild;
            }
            
            // Fallback to string comparison for base versions
            String currentBase = current.split("-")[0];
            String remoteBase = remote.split("-")[0];
            
            String[] currentParts = currentBase.split("\\.");
            String[] remoteParts = remoteBase.split("\\.");
            
            for (int i = 0; i < Math.max(currentParts.length, remoteParts.length); i++) {
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
                int remotePart = i < remoteParts.length ? Integer.parseInt(remoteParts[i]) : 0;
                
                if (remotePart > currentPart) return true;
                if (remotePart < currentPart) return false;
            }
            
            return false;
        } catch (NumberFormatException e) {
            // If parsing fails, do simple string comparison
            return !current.equals(remote);
        }
    }
    
    /**
     * Extract build number from version string like "1.0.0-build.42"
     */
    private int extractBuildNumber(String version) {
        if (version.contains("-build.")) {
            try {
                String buildPart = version.substring(version.indexOf("-build.") + 7);
                return Integer.parseInt(buildPart);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
    
    /**
     * Download the latest version to the specified path.
     * @return CompletableFuture with the downloaded file path, or null on failure
     */
    public CompletableFuture<Path> downloadUpdate() {
        if (downloadUrl == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path downloadDir = plugin.getDataFolder().toPath().getParent().resolve(downloadPath);
                Files.createDirectories(downloadDir);
                
                String fileName = "EssentialUtils-" + latestVersion + ".jar";
                Path targetPath = downloadDir.resolve(fileName);
                
                plugin.getLogger().info("Downloading update to: " + targetPath);
                
                HttpURLConnection connection = (HttpURLConnection) URI.create(downloadUrl).toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", USER_AGENT);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(60000);
                connection.setInstanceFollowRedirects(true);
                
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    plugin.getLogger().warning("Failed to download update: HTTP " + responseCode);
                    return null;
                }
                
                try (InputStream in = connection.getInputStream()) {
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                
                plugin.getLogger().info("Update downloaded successfully: " + fileName);
                plugin.getLogger().info("Restart the server to apply the update.");
                
                return targetPath;
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to download update", e);
                return null;
            }
        });
    }
    
    /**
     * Run the update check on startup if enabled.
     */
    public void runStartupCheck() {
        if (!enabled || !checkOnStartup) {
            return;
        }
        
        checkForUpdates().thenAccept(hasUpdate -> {
            if (hasUpdate) {
                plugin.getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                plugin.getLogger().info("A new version of EssentialUtils is available!");
                plugin.getLogger().info("Current: " + plugin.getPluginMeta().getVersion());
                plugin.getLogger().info("Latest:  " + latestVersion);
                plugin.getLogger().info("Download: https://github.com/" + owner + "/" + repo + "/releases/latest");
                plugin.getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
                if (autoDownload && downloadUrl != null) {
                    downloadUpdate();
                }
            } else {
                plugin.getLogger().info("EssentialUtils is up to date.");
            }
        });
    }
    
    /**
     * Notify a player (admin) about available updates.
     */
    public void notifyPlayer(Player player) {
        if (!enabled || !notifyAdmins || !updateAvailable) {
            return;
        }
        
        if (!player.hasPermission("essentialutils.admin")) {
            return;
        }
        
        String releaseUrl = "https://github.com/" + owner + "/" + repo + "/releases/latest";
        
        Component message = Component.text()
            .append(Component.text("[EssentialUtils] ", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("A new version is available! ", NamedTextColor.YELLOW))
            .append(Component.text("v" + latestVersion, NamedTextColor.GREEN))
            .append(Component.text(" - "))
            .append(Component.text("[Download]", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(releaseUrl)))
            .build();
        
        // Schedule to run on the main thread (Folia-compatible)
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            player.sendMessage(message);
        });
    }
    
    // Getters
    public boolean isUpdateAvailable() { return updateAvailable; }
    public String getLatestVersion() { return latestVersion; }
    public String getDownloadUrl() { return downloadUrl; }
    public boolean isEnabled() { return enabled; }
    public boolean shouldNotifyAdmins() { return notifyAdmins; }
}
