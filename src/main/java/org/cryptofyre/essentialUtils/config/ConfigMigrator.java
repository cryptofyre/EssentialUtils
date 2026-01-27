package org.cryptofyre.essentialUtils.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

/**
 * Handles config version tracking and migrations.
 * Supports:
 * - Sequential version migrations (v0 -> v1 -> v2, etc.)
 * - Smart merge that preserves user values while updating comments
 * - Key renaming, restructuring, and value transforms
 */
public class ConfigMigrator {
    
    /**
     * Current config version. Increment this when adding new migrations.
     */
    private static final int CURRENT_VERSION = 1;
    
    private final JavaPlugin plugin;
    
    public ConfigMigrator(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Run migrations if needed. Call this before loading the config.
     * @return true if migrations were run, false if config was already current
     */
    public boolean migrate() {
        // Ensure config file exists
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
            return false; // Fresh config, no migration needed
        }
        
        // Load current config
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        int userVersion = config.getInt("config-version", 0);
        
        if (userVersion >= CURRENT_VERSION) {
            return false; // Already up to date
        }
        
        plugin.getLogger().info("Migrating config from version " + userVersion + " to " + CURRENT_VERSION);
        
        // Run migrations sequentially
        try {
            if (userVersion < 1) {
                migrateToV1(config);
            }
            // Future migrations:
            // if (userVersion < 2) migrateToV2(config);
            // if (userVersion < 3) migrateToV3(config);
            
            // Smart merge: refresh comments while preserving values
            smartMerge(config);
            
            plugin.getLogger().info("Config migration completed successfully!");
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Config migration failed!", e);
            // Create backup of corrupted config
            createBackup(configFile, "failed-migration");
            // Reset to defaults
            plugin.saveResource("config.yml", true);
            return true;
        }
    }
    
    /**
     * Migration to version 1.
     * This is the initial versioned config - handles upgrades from pre-versioned configs.
     */
    private void migrateToV1(FileConfiguration config) {
        plugin.getLogger().info("  Running migration to v1...");
        
        // Example migrations for future reference:
        // 
        // Rename a key:
        // if (config.contains("old.key.path")) {
        //     config.set("new.key.path", config.get("old.key.path"));
        //     config.set("old.key.path", null);
        // }
        //
        // Transform a value:
        // if (config.contains("some.boolean.setting")) {
        //     boolean oldValue = config.getBoolean("some.boolean.setting");
        //     config.set("some.enum.setting", oldValue ? "ENABLED" : "DISABLED");
        //     config.set("some.boolean.setting", null);
        // }
        //
        // Move a section:
        // ConfigurationSection oldSection = config.getConfigurationSection("old.section");
        // if (oldSection != null) {
        //     for (String key : oldSection.getKeys(true)) {
        //         config.set("new.section." + key, oldSection.get(key));
        //     }
        //     config.set("old.section", null);
        // }
        
        // V1 is the baseline - no actual migrations needed for existing configs
        // The smart merge will add any missing keys with defaults
    }
    
    /**
     * Smart merge: Updates config file with new comments and keys from defaults
     * while preserving all user-customized values.
     */
    private void smartMerge(FileConfiguration userConfig) throws IOException {
        // Read default config from JAR as string (preserves comments)
        String defaultConfigStr = readDefaultConfig();
        if (defaultConfigStr == null) {
            plugin.getLogger().warning("Could not read default config for smart merge");
            // Fall back to simple save
            userConfig.set("config-version", CURRENT_VERSION);
            plugin.saveConfig();
            return;
        }
        
        // Parse default config to get structure
        YamlConfiguration defaultConfig = new YamlConfiguration();
        try {
            defaultConfig.loadFromString(defaultConfigStr);
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to parse default config", e);
            userConfig.set("config-version", CURRENT_VERSION);
            plugin.saveConfig();
            return;
        }
        
        // Collect all user values (flattened key -> value map)
        Map<String, Object> userValues = flattenConfig(userConfig);
        
        // Process the default config string, replacing default values with user values
        String[] lines = defaultConfigStr.split("\n");
        StringBuilder result = new StringBuilder();
        
        for (String line : lines) {
            String processedLine = processLine(line, userValues, defaultConfig);
            result.append(processedLine).append("\n");
        }
        
        // Update config-version in result
        String finalConfig = result.toString();
        finalConfig = finalConfig.replaceFirst(
            "config-version: \\d+",
            "config-version: " + CURRENT_VERSION
        );
        
        // Write to file
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        createBackup(configFile, "pre-migration");
        Files.writeString(configFile.toPath(), finalConfig, StandardCharsets.UTF_8);
    }
    
    /**
     * Process a single line from the default config, substituting user values where appropriate.
     */
    private String processLine(String line, Map<String, Object> userValues, YamlConfiguration defaultConfig) {
        // Skip comments and empty lines
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return line;
        }
        
        // Check if this is a key: value line (not a section header)
        int colonIndex = line.indexOf(':');
        if (colonIndex == -1) {
            return line;
        }
        
        // Check if there's a value after the colon (not just a section header)
        String afterColon = line.substring(colonIndex + 1).trim();
        
        // Remove inline comment to check if there's an actual value
        String valueWithoutComment = afterColon;
        int commentIndex = findInlineCommentIndex(afterColon);
        if (commentIndex > 0) {
            valueWithoutComment = afterColon.substring(0, commentIndex).trim();
        }
        
        // If empty after colon, it's a section header - keep as is
        if (valueWithoutComment.isEmpty()) {
            return line;
        }
        
        // Calculate the full key path based on indentation
        String key = extractKeyPath(line, colonIndex);
        if (key == null) {
            return line;
        }
        
        // Check if user has a value for this key
        if (userValues.containsKey(key)) {
            Object userValue = userValues.get(key);
            
            // Get the inline comment if any
            String inlineComment = "";
            if (commentIndex > 0) {
                inlineComment = " " + afterColon.substring(commentIndex);
            }
            
            // Reconstruct the line with user value
            String indent = line.substring(0, line.indexOf(line.trim()));
            String keyPart = line.substring(0, colonIndex + 1).trim();
            String formattedValue = formatYamlValue(userValue);
            
            return indent + keyPart + " " + formattedValue + inlineComment;
        }
        
        return line;
    }
    
    /**
     * Find the index of an inline comment (# not inside quotes).
     */
    private int findInlineCommentIndex(String str) {
        boolean inQuotes = false;
        char quoteChar = 0;
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            
            if (!inQuotes && (c == '"' || c == '\'')) {
                inQuotes = true;
                quoteChar = c;
            } else if (inQuotes && c == quoteChar) {
                inQuotes = false;
            } else if (!inQuotes && c == '#') {
                return i;
            }
        }
        
        return -1;
    }
    
    // Track current path during line processing
    private final List<String> currentPath = new ArrayList<>();
    private int lastIndent = -1;
    
    /**
     * Extract the full key path for a line based on indentation tracking.
     */
    private String extractKeyPath(String line, int colonIndex) {
        String keyName = line.substring(0, colonIndex).trim();
        if (keyName.isEmpty()) {
            return null;
        }
        
        // Calculate indentation level (assuming 2 spaces per level)
        int indent = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') indent++;
            else break;
        }
        int level = indent / 2;
        
        // Adjust current path based on indentation
        if (level <= lastIndent) {
            // Going back up or staying same level - trim path
            while (currentPath.size() > level) {
                currentPath.remove(currentPath.size() - 1);
            }
        }
        
        // Check if this line has a value (not a section header)
        String afterColon = line.substring(colonIndex + 1).trim();
        String valueWithoutComment = afterColon;
        int commentIdx = findInlineCommentIndex(afterColon);
        if (commentIdx > 0) {
            valueWithoutComment = afterColon.substring(0, commentIdx).trim();
        }
        
        if (valueWithoutComment.isEmpty()) {
            // Section header - add to path for children
            currentPath.add(keyName);
            lastIndent = level;
            return null;
        }
        
        // Build full path
        StringBuilder fullPath = new StringBuilder();
        for (String part : currentPath) {
            fullPath.append(part).append(".");
        }
        fullPath.append(keyName);
        
        lastIndent = level;
        return fullPath.toString();
    }
    
    /**
     * Flatten a configuration into a map of dotted paths to values.
     */
    private Map<String, Object> flattenConfig(FileConfiguration config) {
        Map<String, Object> result = new LinkedHashMap<>();
        flattenSection(config, "", result);
        return result;
    }
    
    private void flattenSection(ConfigurationSection section, String prefix, Map<String, Object> result) {
        for (String key : section.getKeys(false)) {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            Object value = section.get(key);
            
            if (value instanceof ConfigurationSection) {
                flattenSection((ConfigurationSection) value, fullKey, result);
            } else {
                result.put(fullKey, value);
            }
        }
    }
    
    /**
     * Format a value for YAML output.
     */
    private String formatYamlValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            String str = (String) value;
            // Quote strings that contain special characters or look like other types
            if (str.isEmpty() || str.contains(":") || str.contains("#") || 
                str.contains("\"") || str.contains("'") || str.contains("\n") ||
                str.startsWith(" ") || str.endsWith(" ") ||
                str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false") ||
                str.equalsIgnoreCase("null") || str.equalsIgnoreCase("~") ||
                looksLikeNumber(str)) {
                // Use double quotes and escape internal quotes
                return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
            }
            return "\"" + str + "\"";
        }
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }
        if (value instanceof List) {
            // For simple lists, use flow style
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                return "[]";
            }
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatYamlValue(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        // Fallback
        return value.toString();
    }
    
    private boolean looksLikeNumber(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Read the default config.yml from the plugin JAR.
     */
    private String readDefaultConfig() {
        try (InputStream is = plugin.getResource("config.yml")) {
            if (is == null) {
                return null;
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to read default config", e);
            return null;
        }
    }
    
    /**
     * Create a backup of the config file.
     */
    private void createBackup(File configFile, String suffix) {
        if (!configFile.exists()) {
            return;
        }
        
        try {
            String backupName = "config-backup-" + suffix + "-" + System.currentTimeMillis() + ".yml";
            File backupFile = new File(plugin.getDataFolder(), backupName);
            Files.copy(configFile.toPath(), backupFile.toPath());
            plugin.getLogger().info("Created config backup: " + backupName);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to create config backup", e);
        }
    }
    
    /**
     * Get the current config version constant.
     */
    public static int getCurrentVersion() {
        return CURRENT_VERSION;
    }
}
