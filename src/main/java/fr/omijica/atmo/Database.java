package fr.omijica.atmo;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Database {

    private final File file;
    private final String resolve;

    public Database(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "data.db");
        resolve = "jdbc:sqlite" + file.getAbsolutePath();
    }

    public void initialise() throws IOException, SQLException {
        Files.createDirectories(file.getParentFile().toPath());

        try (Connection connection = DriverManager.getConnection(resolve); Statement query = connection.createStatement()) {

            query.executeUpdate("CREATE TABLE IF NOT EXISTS zone_players (" + "zone TEXT NOT NULL, " + "uuid TEXT NOT NULL, " + "PRIMARY KEY (zone, uuid)" + ")"
            );
        }
    }

    public void addPlayer(String zoneName, UUID playerUuid) throws SQLException{
        String sql = "INSERT OR IGNORE INTO zone_players (zone, uuid) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(resolve);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, zoneName);
            statement.setString(2, playerUuid.toString());

            statement.executeUpdate();
        }
    }

    public void removePlayer(String zoneName, UUID playerUuid) throws SQLException {

        String sql = "DELETE FROM zone_players WHERE zone = ? AND uuid = ?";

        try (Connection connection = DriverManager.getConnection(resolve);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, zoneName);
            statement.setString(2, playerUuid.toString());

            statement.executeUpdate();
        }
    }

    public Set<UUID> getPlayers(String zoneName) throws SQLException {
        Set<UUID> players = new HashSet<>();

        String sql = "SELECT uuid FROM zone_players WHERE zone = ?";

        try (Connection connection = DriverManager.getConnection(resolve);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, zoneName);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    UUID playerUuid =  UUID.fromString(results.getString("uuid"));
                    players.add(playerUuid);
                }
            }
        }

        return players;
    }
}
