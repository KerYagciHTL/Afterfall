package at.htl.afterfall.persistence;

import at.htl.afterfall.model.Economy;
import at.htl.afterfall.model.Satisfaction;
import java.sql.*;

public class EconomyDao {
    public void load(int saveId, Economy economy, Satisfaction satisfaction) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("SELECT * FROM economy WHERE save_id = ?")) {
            ps.setInt(1, saveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    economy.setBalance(rs.getDouble("balance"));
                    economy.setNetWorth(rs.getDouble("net_worth"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("SELECT * FROM satisfaction WHERE save_id = ?")) {
            ps.setInt(1, saveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) satisfaction.setValue(rs.getDouble("value"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(int saveId, Economy economy, Satisfaction satisfaction) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO economy (save_id, balance, net_worth, ticket_price) VALUES (?, ?, ?, 0)")) {
            ps.setInt(1, saveId);
            ps.setDouble(2, economy.getBalance());
            ps.setDouble(3, economy.getNetWorth());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO satisfaction (save_id, value) VALUES (?, ?)")) {
            ps.setInt(1, saveId);
            ps.setDouble(2, satisfaction.getValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
