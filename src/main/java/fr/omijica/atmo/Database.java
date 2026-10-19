package fr.omijica.atmo;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;

public class Database {

    private final File file;
    private final String resolve;
    private final Map<String, Set<UUID>> zonePlayers = new HashMap<>();

    public Database(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "data.db");
        resolve = "jdbc:sqlite:" + file.getAbsolutePath();
    }

    public void initialise() throws IOException, SQLException {
        Files.createDirectories(file.getParentFile().toPath());

        try (Connection connection = DriverManager.getConnection(resolve); Statement query = connection.createStatement()) {

            query.executeUpdate("CREATE TABLE IF NOT EXISTS zone_players (" + "zone TEXT NOT NULL, " + "uuid TEXT NOT NULL, " + "PRIMARY KEY (zone, uuid)" + ")");
            loadCache();
        }
    }

    public void addPlayer(String zoneName, UUID playerUuid) throws SQLException{
        String sql = "INSERT OR IGNORE INTO zone_players (zone, uuid) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(resolve);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, zoneName);
            statement.setString(2, playerUuid.toString());

            statement.executeUpdate();
            zonePlayers.computeIfAbsent(zoneName, key -> new HashSet<>()).add(playerUuid);
        }
    }

    public void removePlayer(String zoneName, UUID playerUuid) throws SQLException {

        String sql = "DELETE FROM zone_players WHERE zone = ? AND uuid = ?";

        try (Connection connection = DriverManager.getConnection(resolve);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, zoneName);
            statement.setString(2, playerUuid.toString());

            statement.executeUpdate();
            Set<UUID> players = zonePlayers.get(zoneName);

            if (players != null) {
                players.remove(playerUuid);
            }
        }
    }

    public boolean getPlayer(String zoneName, Player player) {

        Set<UUID> players = zonePlayers.get(zoneName);

        return players != null && players.contains(player.getUniqueId());
    }

    private void loadCache() throws SQLException {
        String sql = "SELECT zone, uuid FROM zone_players";

        zonePlayers.clear();

        try (Connection connection = DriverManager.getConnection(resolve); PreparedStatement statement = connection.prepareStatement(sql); ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                String zone = results.getString("zone");
                UUID uuid = UUID.fromString(results.getString("uuid"));

                zonePlayers.computeIfAbsent(zone, key -> new HashSet<>()).add(uuid);
            }
        }

    }
}
