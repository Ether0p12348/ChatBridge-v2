package com.ethanrobins.chatbridge_v2;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.Objects;

public class MySQL {
    private static int counter = 0;

    private static final String URL = "jdbc:mysql://localhost:3306/" + ChatBridge.getSecret().get("mysql", "db");
    private static final String USER = ChatBridge.getSecret().get("mysql", "user");
    private static final String PASS = ChatBridge.getSecret().get("mysql", "pass");

    private final Connection conn;

    /**
     * Start a MySQL connection
     * @throws SQLException
     */
    public MySQL() throws SQLException {
        if (!ChatBridge.isDev()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                this.conn = DriverManager.getConnection(URL, USER, PASS);
                counter++;
                if (ChatBridge.isDebug()) {
                    System.out.println("MySQL connection started. There are " + counter + " MySQL connections open.");
                }
            } catch (SQLException ex) {
                throw new SQLException("Error connecting to the database: " + ex.getMessage(), ex);
            } catch (ClassNotFoundException ex) {
                throw new SQLException("MySQL JDBC Driver not found.", ex);
            }
        } else {
            System.out.println("Running in dev mode! Database usage is disabled!");
            this.conn = null;
        }
    }

    /**
     * @return this instance's {@link Connection}
     */
    @Nullable
    public Connection getConnection() {
        return this.conn;
    }

    /**
     * @return the number of open MySQL connections
     */
    public static int getCounter() {
        return counter;
    }

    /**
     * Close this MySQL connection
     */
    public void close() {
        MySQL.close(this.conn);
    }

    /**
     * Close a MySQL connection
     * @param mysql The {@link MySQL} to be closed
     */
    public static void close(MySQL mysql) {
        MySQL.close(mysql.conn);
    }

    /**
     * Close a MySQL connection
     * @param connection The {@link Connection} to be closed
     */
    public static void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                counter--;
                if (ChatBridge.isDebug()) {
                    System.out.println("MySQL connection closed. There are " + counter + " MySQL connections open.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.out.println("MySQL connection failed to close. There are " + counter + " MySQL connections open.");
            }
        }
    }

    /**
     * Get a user's set locale
     * @param userId The user's Discord User ID
     * @return {@link DiscordLocale} of the user's set locale, defaulting to {@link DiscordLocale#ENGLISH_US}
     */
    @NotNull
    public DiscordLocale getLocale(@NotNull String userId) {
        try {
            DiscordLocale locale = getLocale(userId, false);
            return Objects.requireNonNullElse(locale, DiscordLocale.ENGLISH_US);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return DiscordLocale.ENGLISH_US;
        }
    }

    /**
     * Get a user's set locale
     * @param userId The user's Discord User ID
     * @param returnIfNotExists If it should return {@code null} if there are no rows
     * @return {@link DiscordLocale} of the user's set locale
     * @throws SQLException if {@code returnIfNotExists} == {@code false}
     */
    @Nullable
    public DiscordLocale getLocale(@NotNull String userId, boolean returnIfNotExists) throws SQLException {
        if (this.conn != null) {
            try {
                PreparedStatement stmt = this.conn.prepareStatement("SELECT locale FROM userstore WHERE user_id=?");
                stmt.setString(1, userId);
                ResultSet result = stmt.executeQuery();
                DiscordLocale locale = DiscordLocale.from(result.getString("locale"));
                if (locale == DiscordLocale.UNKNOWN) {
                    locale = DiscordLocale.ENGLISH_US;
                }

                return locale;
            } catch (SQLException ex) {
                if (returnIfNotExists) {
                    return null;
                } else {
                    throw ex;
                }
            }
        } else {
            return DiscordLocale.ENGLISH_US;
        }
    }

    /**
     * INSERT or UPDATE a user's locale
     * @param userId The user's Discord User ID
     * @param locale The DiscordLocale by tag to be updated or set
     * @return {@link Status}
     */
    public Status updateLocale(@NotNull String userId, @NotNull String locale) {
        return updateLocale(userId, DiscordLocale.from(locale));
    }

    /**
     * INSERT or UPDATE a user's locale
     * @param userId The user's Discord User ID
     * @param locale The DiscordLocale to be updated or set
     * @return {@link Status}
     */
    public Status updateLocale(@NotNull String userId, @NotNull DiscordLocale locale) {
        if (this.conn != null) {
            try {
                Status returnVal = getLocale(userId, true) != null ? Status.UPDATED : Status.INSERTED;

                PreparedStatement stmt = this.conn.prepareStatement("INSERT INTO userstore (user_id, locale) VALUES (?, ?) ON DUPLICATE KEY UPDATE locale=?");
                stmt.setString(1, userId);
                stmt.setString(2, locale.getLocale());
                stmt.setString(3, locale.getLocale());
                stmt.executeUpdate();

                return returnVal;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return Status.FAILED;
            }
        } else {
            return Status.INSERTED;
        }
    }

    public enum Status {
        FAILED,
        UPDATED,
        INSERTED;
    }
}
