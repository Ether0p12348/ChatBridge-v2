package com.ethanrobins.chatbridge_v2.drivers;

import com.ethanrobins.chatbridge_v2.ChatBridge;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;

/**
 * <b>Handles the management of a MySQL database connection and related operations.</b>
 * <p>
 * This class is responsible for establishing and managing a MySQL database connection,
 * querying the database, retrieving user-related data, and maintaining connection status.
 * It also provides utilities for handling and closing connections effectively to ensure system stability.
 * </p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *     <li>Establishes a connection to the MySQL database using credentials fetched from the {@link ChatBridge} secret manager.</li>
 *     <li>Maintains the status of the connection through a {@link Status} object to track and categorize the current state.</li>
 *     <li>Provides methods to retrieve user-specific data, such as their {@link DiscordLocale}.</li>
 *     <li>Ensures proper connection management with static and instance-level methods for closing connections.</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>
 *     try {
 *         MySQL mysql = new MySQL();
 *         Connection connection = mysql.getConnection();
 *         // Perform operations
 *         mysql.close();
 *     } catch (SQLException e) {
 *         e.printStackTrace();
 *     }
 * </pre>
 *
 * <h2>Concurrency and Resource Management:</h2>
 * <p>
 * The class maintains a static counter to track the number of active MySQL connections. This ensures that
 * the system has visibility into the number of open connections and can manage resource limitations effectively.
 * </p>
 *
 * <h2>Error Handling:</h2>
 * <p>
 * In development environments, database connections are skipped, and the system operates without a physical
 * database connection. The {@link State#NO_CONNECTION} is set in such cases to indicate the absence of a connection.
 * </p>
 *
 * <h2>Dependencies:</h2>
 * <ul>
 *     <li>{@link Status} - Tracks the current connection or query status.</li>
 *     <li>{@link State} - Represents the specific connection or query-related states.</li>
 *     <li>{@link DiscordLocale} - Used for user-related locale retrieval.</li>
 * </ul>
 *
 * @see ChatBridge
 * @see Connection
 */
public class MySQL {
    @Getter
    private static int counter = 0;

    private static final String ADDRESS = ChatBridge.getSecret().get("mysql", "address");
    private static final String PORT = ChatBridge.getSecret().get("mysql", "port");
    private static final String DB = ChatBridge.getSecret().get("mysql", "db");
    private static final String USER = ChatBridge.getSecret().get("mysql", "user");
    private static final String PASS = ChatBridge.getSecret().get("mysql", "pass");

    private static final String URL = "jdbc:mysql://" + ADDRESS + ":" + PORT + "/" + DB;

    @Getter
    private final Status status = new Status();
    private final Connection conn;

    /**
     * Initializes a new MySQL connection.
     * <p>
     * Attempts to establish a connection to the MySQL database using the provided credentials.
     * If the connection is successful, the {@link State#CONNECTED} status is set.
     * In development mode, the connection is skipped, the {@link State#NO_CONNECTION} is kept, and database usage is disabled.
     * </p>
     *
     * @throws SQLException If an error occurs while connecting to the database or if the MySQL driver is not found.
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
                this.status.setStatus(State.CONNECTED);
            } catch (SQLException ex) {
                this.status.setException(ex);
                throw new SQLException("Error connecting to the database: " + ex.getMessage(), ex);
            } catch (ClassNotFoundException ex) {
                this.status.setException(ex);
                throw new SQLException("MySQL JDBC Driver not found.", ex);
            }
        } else {
            System.out.println("Running in dev mode! Database usage is disabled!");
            this.conn = null;
        }
    }

    /**
     * Retrieves this instance's {@link Connection}, if available.
     * @return The associated {@link Connection}, or {@code null} if the instance is not connected.
     */
    @Nullable
    public Connection getConnection() {
        if (this.status.isConnected()) {
            return null;
        } else {
            return this.conn;
        }
    }

    /**
     * Closes <u>this</u> {@link MySQL} connection.
     */
    public void close() {
        if (!this.status.isClosed()) {
            MySQL.close(this.conn);
            this.status.setStatus(State.CLOSED);
        }
    }

    /**
     * Closes a given {@link MySQL} connection.
     * @param mysql The {@link MySQL} instance to be closed.
     */
    public static void close(MySQL mysql) {
        if (!mysql.getStatus().isClosed()) {
            MySQL.close(mysql.conn);
            mysql.status.setStatus(State.CLOSED);
        }
    }

    /**
     * Closes a given MySQL {@link Connection}.
     * <br><b>WARNING:</b> This method should only be used for emergency or direct {@link Connection} closures.
     * To properly handle and update the status of a {@link MySQL} object, close it directly through the {@link MySQL} instance instead.
     * @param connection The {@link Connection} to be closed.
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
     * Retrieves the locale set for the specified user.
     * <p>
     * This method attempts to retrieve the {@link DiscordLocale} associated with the given user's Discord ID.
     * If no locale is set, or if an error occurs during retrieval, it defaults to {@link DiscordLocale#ENGLISH_US}.
     * </p>
     *
     * @param userId The Discord User ID of the user whose locale is being retrieved.
     * @return The {@link DiscordLocale} of the user's set locale,
     *         defaulting to {@link DiscordLocale#ENGLISH_US} in case of null or error.
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
     * Retrieves the locale set for the specified user with customizable fallback behavior.
     * <p>
     * This method queries the user's locale from the database based on their Discord User ID. If the locale retrieved is
     * {@link DiscordLocale#UNKNOWN}, it defaults to {@link DiscordLocale#ENGLISH_US}. Additionally, the behavior in cases
     * of missing rows or errors can be controlled using the {@code returnIfNotExists} parameter.
     * </p>
     *
     * @param userId            The Discord User ID of the user whose locale is being retrieved.
     * @param returnNullIfNotExists If {@code true}, the method returns {@code null} if no locale is found or rows are missing.
     *                          Otherwise, an {@link SQLException} is thrown in such cases.
     * @return The {@link DiscordLocale} of the user's set locale, or {@code null} if {@code returnIfNotExists} is {@code true}
     *         and no locale is found. Defaults to {@link DiscordLocale#ENGLISH_US} if the locale is {@link DiscordLocale#UNKNOWN}.
     * @throws SQLException If an error occurs during query execution and {@code returnIfNotExists} is {@code false}.
     */
    @Nullable
    public DiscordLocale getLocale(@NotNull String userId, boolean returnNullIfNotExists) throws SQLException {
        if (this.status.isConnected()) {
            try {
                PreparedStatement stmt = this.conn.prepareStatement("SELECT locale FROM chatbridge_userstore WHERE user_id=?");
                stmt.setString(1, userId);
                ResultSet result = stmt.executeQuery();
                DiscordLocale locale = DiscordLocale.from(result.getString("locale"));

                if (!returnNullIfNotExists && locale == DiscordLocale.UNKNOWN) {
                    this.status.setFailed(new SQLException("Locale is unknown!"));
                    locale = DiscordLocale.ENGLISH_US;
                } else {
                    this.status.setStatus(State.SUCCESS);
                }

                return locale;
            } catch (SQLException ex) {
                if (returnNullIfNotExists) {
                    this.status.setStatus(State.SUCCESS);
                    return null;
                } else {
                    this.status.setFailed(ex);
                    return DiscordLocale.ENGLISH_US;
                }
            }
        } else {
            if (returnNullIfNotExists) {
                if (ChatBridge.isDev()) {
                    this.status.setStatus(State.INSERTED);
                }
                return null;
            } else {
                if (ChatBridge.isDev()) {
                    this.status.setStatus(State.INSERTED);
                }
                return DiscordLocale.ENGLISH_US;
            }
        }
    }

    /**
     * Inserts or updates the locale for the specified user by locale tag.
     * <p>
     * This method updates the user's locale in the database using a locale tag (e.g., "en_US") and
     * delegates the operation to {@link #updateLocale(String, DiscordLocale)} after converting the tag
     * to a {@link DiscordLocale}. If the user already has a locale entry, it will be updated; otherwise,
     * a new entry will be created.
     * </p>
     *
     * @param userId The Discord User ID of the user whose locale is being updated or set.
     * @param locale The locale tag (string) of the {@link DiscordLocale} to be inserted or updated.
     * @return A {@link Status} object representing the result of the operation:
     *         {@link State#UPDATED} if an existing entry was updated, {@link State#INSERTED} for new entries,
     *         or a failed status if an error occurs.
     */
    public Status updateLocale(@NotNull String userId, @NotNull String locale) {
        return updateLocale(userId, DiscordLocale.from(locale));
    }

    /**
     * Inserts or updates the locale for the specified user.
     * <p>
     * This method updates the user's locale in the database using a {@link DiscordLocale} object. If the
     * user already has an entry in the database, the locale is updated to the provided value. Otherwise,
     * a new entry is created. The operation utilizes an "INSERT ON DUPLICATE KEY UPDATE" SQL statement for
     * efficient handling of insert-or-update logic.
     * </p>
     *
     * @param userId The Discord User ID of the user whose locale is being updated or set.
     * @param locale The {@link DiscordLocale} object to be inserted or updated.
     * @return A {@link Status} object representing the result of the operation:
     *         {@link State#UPDATED} if an existing entry was updated, {@link State#INSERTED} for new entries,
     *         or a failed status if an error occurs.
     */
    public Status updateLocale(@NotNull String userId, @NotNull DiscordLocale locale) {
        if (this.status.isConnected()) {
            try {
                Status status = this.status.setStatus(getLocale(userId, true) != null ? State.UPDATED : State.INSERTED);

                PreparedStatement stmt = this.conn.prepareStatement("INSERT INTO chatbridge_userstore (user_id, locale) VALUES (?, ?) ON DUPLICATE KEY UPDATE locale=?");
                stmt.setString(1, userId);
                stmt.setString(2, locale.getLocale());
                stmt.setString(3, locale.getLocale());
                stmt.executeUpdate();

                return status;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return this.status.setFailed(ex);
            }
        } else {
            return this.status.setStatus(State.INSERTED);
        }
    }

    public @NotNull Map<String, Boolean> getGuilds() throws SQLException {
        if (this.status.isConnected()) {
            Map<String, Boolean> guilds = new HashMap<>();
            try {
                PreparedStatement stmt = this.conn.prepareStatement("SELECT * FROM chatbridge_guildstore");
                ResultSet result = stmt.executeQuery();

                while (result.next()) {
                    guilds.put(result.getString("guild_id"), result.getBoolean("status"));
                }

                if (guilds.isEmpty()) {
                    this.status.setFailed(new SQLException("No guilds found!"));
                } else {
                    this.status.setStatus(State.SUCCESS);
                }
                return guilds;
            } catch (SQLException ex) {
                this.status.setFailed(ex);
                return guilds;
            }
        }

        if (ChatBridge.isDev()) {
            this.status.setStatus(State.SUCCESS);
            return new HashMap<>();
        }
        throw new SQLException("MySQL is not connected!");
    }

    public @Nullable Boolean getGuildStatus(@NotNull String guildId) {
        if (this.status.isConnected()) {
            try {
                PreparedStatement stmt = this.conn.prepareStatement("SELECT `status` FROM chatbridge_guildstore WHERE guild_id=? LIMIT 1");
                stmt.setString(1, guildId);
                ResultSet result = stmt.executeQuery();

                if (!result.next()) {
                    this.status.setStatus(State.SUCCESS);
                    if (ChatBridge.isDebug()) {
                        System.out.println("Guild status for " + guildId + ": <no row>");
                    }
                    return null;
                }

                boolean guildStatus = result.getBoolean("status");
                if (result.wasNull()) {
                    this.status.setStatus(State.SUCCESS);
                    if (ChatBridge.isDebug()) {
                        System.out.println("Guild status for " + guildId + ": <NULL column>");
                    }
                    return null;
                }

                this.status.setStatus(State.SUCCESS);
                if (ChatBridge.isDebug()) {
                    System.out.println("Guild status for " + guildId + ": " + guildStatus);
                }
                return guildStatus;
            } catch (SQLException ex) {
                if (ChatBridge.isDev()) {
                    this.status.setStatus(State.SUCCESS);
                } else {
                    this.status.setFailed(ex);
                }
                if (ChatBridge.isDebug()) {
                    System.out.println("Guild status for " + guildId + ": null: " + ex.getMessage());
                }
                return null;
            }
        }

        return null;
    }

    public Status setGuildStatus(@NotNull String guildId, boolean guildStatus) {
        if (this.status.isConnected()) {
            try {
                Status status = this.status.setStatus(getGuildStatus(guildId) != null ? State.UPDATED : State.INSERTED);

                PreparedStatement stmt = this.conn.prepareStatement("INSERT INTO chatbridge_guildstore (guild_id, status) values (?, ?) ON DUPLICATE KEY UPDATE status=?");
                stmt.setString(1, guildId);
                stmt.setBoolean(2, guildStatus);
                stmt.setBoolean(3, guildStatus);
                stmt.executeUpdate();

                return status;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return this.status.setFailed(ex);
            }
        }

        if (ChatBridge.isDev()) {
            return this.status.setStatus(State.SUCCESS);
        }
        return this.status.setFailed(new SQLException("MySQL is not connected!"));
    }

    /**
     * Represents the status of a {@link MySQL} connection or operation.
     * <p>
     * This nested static class manages connection and query-related states for a {@link MySQL} instance.
     * It tracks the current states (e.g., connection or response states),
     * exceptions encountered during lifecycle operations, and supports chaining for method calls.
     * </p>
     *
     * <h2>Key Features:</h2>
     * <ul>
     *     <li>Tracks and manages multiple states associated with a {@link MySQL} object's connection or query.</li>
     *     <li>Stores both current states and lifetime exceptions for comprehensive error reporting and debugging.</li>
     *     <li>Provides utility methods to manipulate states, record exceptions, and inspect the connection or query conditions.</li>
     * </ul>
     *
     * <h2>State Management:</h2>
     * <p>
     * The {@linkplain Status} class maintains an internal list of {@link State} objects, with rules to
     * ensure that only one {@link State} per category (e.g., {@link StateCategory#CONNECTION}) is active at any time.
     * It provides methods to set, update, and inspect these states for both connection and response categories.
     * </p>
     *
     * <h2>Exception Handling:</h2>
     * <p>
     * The {@linkplain Status} class tracks exceptions associated with database interactions. It stores
     * the most recent exception as well as a lifetime list of all exceptions recorded during its lifecycle,
     * making it ideal for debugging or error analysis.
     * </p>
     *
     * <h2>Conditional State Utilities:</h2>
     * <p>
     * The class provides convenience methods to determine whether the {@link MySQL} object is in specific states, such as:
     * <ul>
     *     <li>Connected, closed, failed, or updated.</li>
     *     <li>Whether it has encountered a specific type of state or exception during its lifecycle.</li>
     * </ul>
     * </p>
     *
     * <h2>Usage Example:</h2>
     * <pre>
     *     MySQL.Status status = new MySQL.Status();
     *     status.setStatus(State.CONNECTED);
     *     if (status.isConnected()) {
     *         System.out.println("MySQL is connected!");
     *     }
     *     status.setFailedConnection(new SQLException("Connection failed!"));
     *     System.out.println("Recent exception: " + status.getException().getMessage());
     * </pre>
     *
     * @see MySQL
     * @see State
     * @see StateCategory
     */
    public static class Status {
        private final List<State> states = new ArrayList<>(2);
        private Exception ex = null;
        private List<Exception> exceptions = new ArrayList<>();

        /**
         * Initializes a new {@link Status} instance with a default state.
         * <p>
         * This constructor sets up the initial state of the {@link Status} object by
         * adding {@link State#NO_CONNECTION} to the internal list of states. This represents
         * that there is no active MySQL connection at the time of initialization.
         * </p>
         */
        public Status() {
            this.states.add(State.NO_CONNECTION);
        }

        /**
         * Updates the current {@link Status} with the specified state.
         * <p>
         * This method replaces an existing state within the same category as the provided state,
         * ensuring that only one state per category is present in the internal list of states.
         * If a state of the same category exists, it is removed, and the new state is added.
         * If no matching category is found, no changes are made.
         * </p>
         *
         * @param state The {@link State} to be set in the {@link Status}.
         * @return The current {@link Status} instance for chaining.
         */
        public Status setStatus(State state) {
            for (State s : this.states) {
                if (s.isCategory(state.getCategory())) {
                    this.states.remove(s);
                    if (s != State.FAILED_CONNECTION && s != State.FAILED) {
                        this.ex = null;
                    }
                    break;
                }
            }
            this.states.add(state);
            return this;
        }

        /**
         * Sets the exception associated with the current {@link Status}.
         * <p>
         * This method assigns the provided {@link Exception} to the internal exception field of the {@link Status} object
         * and adds it to an internal collection of exceptions. This allows the {@link Status} to track error conditions
         * for debugging or error-handling purposes, supporting the storage of multiple exceptions encountered during
         * the object's lifecycle.
         * </p>
         *
         * @param ex The {@link Exception} to be associated with this {@link Status}.
         * @return The current {@link Status} instance for method chaining.
         */
        public Status setException(Exception ex) {
            this.ex = ex;
            this.exceptions.add(ex);
            return this;
        }

        /**
         * Updates the {@link Status} to reflect a failed connection and associates an exception with it.
         * <p>
         * This method sets the {@link State} of the {@link Status} to {@link State#FAILED_CONNECTION} to indicate
         * a connection failure and records the provided {@link Exception} using {@link #setException(Exception)}
         * for tracking the cause of the failure.
         * </p>
         *
         * @param ex The {@link Exception} associated with the failed connection.
         * @return The current {@link Status} instance for method chaining.
         */
        public Status setFailedConnection(Exception ex) {
            setStatus(State.FAILED_CONNECTION);
            setException(ex);
            return this;
        }

        /**
         * Updates the {@link Status} to reflect a general failure and associates an exception with it.
         * <p>
         * This method sets the {@link State} of the {@link Status} to {@link State#FAILED} to indicate
         * a failure and records the provided {@link Exception} using {@link #setException(Exception)}
         * to track the cause of the failure.
         * </p>
         *
         * @param ex The {@link Exception} associated with the failure.
         * @return The current {@link Status} instance for method chaining.
         */
        public Status setFailed(Exception ex) {
            setStatus(State.FAILED);
            setException(ex);
            return this;
        }

        /**
         * Retrieves the first {@link State} that represents a connection-related state.
         * <p>
         * This method iterates through the collection of states in the current {@link Status} and returns the
         * first {@link State} for which {@link State#isConnection()} evaluates to {@code true}. If no such state
         * exists, the method returns {@code null}.
         * </p>
         *
         * @return The first {@link State} representing a connection-related state, or {@code null} if none is found.
         */
        public State getConnection() {
            for (State s : this.states) {
                if (s.isConnection()) {
                    return s;
                }
            }
            return null;
        }

        /**
         * Retrieves the first {@link State} that represents a response-related state.
         * <p>
         * This method iterates through the collection of states in the current {@link Status} and returns the
         * first {@link State} for which {@link State#isResponse()} evaluates to {@code true}. If no such state
         * exists, the method returns {@code null}.
         * </p>
         *
         * @return The first {@link State} representing a response-related state, or {@code null} if none is found.
         */
        public State getResponse() {
            for (State s : this.states) {
                if (s.isResponse()) {
                    return s;
                }
            }
            return null;
        }

        /**
         * Retrieves all {@link State} instances from the current {@link Status}.
         * <p>
         * This method converts the internal collection of states into an array of {@link State} objects.
         * </p>
         *
         * @return An array containing all {@link State} instances.
         */
        public State[] get() {
            return this.states.toArray(new State[0]);
        }

        /**
         * Retrieves the most recent {@link Exception} associated with the current {@link Status}.
         * <p>
         * This method returns the latest {@link Exception} that was recorded in the status instance.
         * If no exception has been set, the method returns {@code null}.
         * </p>
         *
         * @return The most recent {@link Exception}, or {@code null} if no exception is available.
         */
        public Exception getException() {
            return this.ex;
        }

        /**
         * Retrieves the list of all {@link Exception} instances recorded over the lifetime of the current {@link Status}.
         * <p>
         * This method returns a collection of exceptions that have been captured during the lifecycle of the status
         * instance. The returned list reflects all recorded exceptions, including past and present.
         * </p>
         *
         * @return A {@link List} containing all {@link Exception} instances recorded over the lifetime of this status.
         */
        public List<Exception> getLifetimeExceptions() {
            return this.exceptions;
        }

        /**
         * Checks whether there is a response-related {@link State} in the current {@link Status}.
         * <p>
         * This method iterates through the collection of states and determines if any {@link State}
         * satisfies the {@link State#isResponse()} condition.
         * </p>
         *
         * @return {@code true} if there is at least one response-related {@link State};
         *         {@code false} otherwise.
         */
        public boolean hasResponse() {
            for (State s : this.states) {
                if (s.isResponse()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Determines whether the current {@link Status} represents a connected state.
         * <p>
         * This method checks the internal collection of states to verify if the {@link State#CONNECTED}
         * condition is met, while also ensuring that {@link State#NO_CONNECTION} is not present.
         * </p>
         *
         * @return {@code true} if the {@link Status} is connected (i.e., contains {@link State#CONNECTED}
         *         and does not contain {@link State#NO_CONNECTION}); {@code false} otherwise.
         */
        public boolean isConnected() {
            if (!this.states.contains(State.NO_CONNECTION)) {
                return this.states.contains(State.CONNECTED);
            }
            return false;
        }

        /**
         * Checks whether the current {@link Status} is in a closed state.
         * <p>
         * This method verifies if the internal state collection contains {@link State#CLOSED}.
         * </p>
         *
         * @return {@code true} if the {@link Status} is closed (i.e., contains {@link State#CLOSED});
         *         {@code false} otherwise.
         */
        public boolean isClosed() {
            return this.states.contains(State.CLOSED);
        }

        /**
         * Determines whether the current {@link Status} is in a failed state.
         * <p>
         * This method checks if the internal state collection contains {@link State#FAILED}.
         * </p>
         *
         * @return {@code true} if the {@link Status} is failed (i.e., contains {@link State#FAILED});
         *         {@code false} otherwise.
         */
        public boolean isFailed() {
            return this.states.contains(State.FAILED);
        }

        /**
         * Checks whether the current {@link Status} is in an updated state.
         * <p>
         * This method verifies if the internal state collection contains {@link State#UPDATED}.
         * </p>
         *
         * @return {@code true} if the {@link Status} is updated (i.e., contains {@link State#UPDATED});
         *         {@code false} otherwise.
         */
        public boolean isUpdated() {
            return this.states.contains(State.UPDATED);
        }

        /**
         * Checks whether the current {@link Status} is in an inserted state.
         * <p>
         * This method verifies if the internal state collection contains {@link State#INSERTED}.
         * </p>
         *
         * @return {@code true} if the {@link Status} is inserted (i.e., contains {@link State#INSERTED});
         *         {@code false} otherwise.
         */
        public boolean isInserted() {
            return this.states.contains(State.INSERTED);
        }

        @Override
        public String toString() {
            return "Status{" + this.states + "}";
        }
    }

    /**
     * Represents various states within the system, each associated with a specific {@link StateCategory}.
     */
    @Getter
    public enum State {
        /**
         * The database connection has not been established.
         * <p>
         * <i>Typically occurs when ChatBridge is in Dev mode.</i>
         * </p>
         */
        NO_CONNECTION(StateCategory.CONNECTION),
        /**
         * The database connection attempt failed.
         */
        FAILED_CONNECTION(StateCategory.CONNECTION),
        /**
         * The database connection is successfully established.
         */
        CONNECTED(StateCategory.CONNECTION),
        /**
         * The database connection is closed.
         * <p>
         * This often requires creating a new {@link MySQL} object to reconnect.
         * </p>
         */
        CLOSED(StateCategory.CONNECTION),
        /**
         * The operation succeeded.
         * <p>
         * <i>Typically shown when a SELECT operation succeeds.</i>
         * </p>
         */
        SUCCESS(StateCategory.RESPONSE),
        /**
         * The operation has failed.
         */
        FAILED(StateCategory.RESPONSE),
        /**
         * The UPDATE query was executed successfully.
         */
        UPDATED(StateCategory.RESPONSE),
        /**
         * The INSERT query was executed successfully.
         */
        INSERTED(StateCategory.RESPONSE);

        /**
         * Retrieves the {@link StateCategory} associated with this {@link State}.
         * The {@link StateCategory} of this {@link State}.
         */
        private final StateCategory category;

        /**
         * Constructs a new {@link State} with the specified {@link StateCategory}.
         *
         * @param category The {@link StateCategory} to which this {@link State} belongs.
         */
        State(StateCategory category) {
            this.category = category;
        }

        /**
         * Determines whether this {@link State} belongs to the {@link StateCategory#CONNECTION} category.
         *
         * @return {@code true} if this state is part of the {@link StateCategory#CONNECTION} category;
         *         {@code false} otherwise.
         */
        public boolean isConnection() {
            return this.category == StateCategory.CONNECTION;
        }

        /**
         * Determines whether this {@link State} belongs to the {@link StateCategory#RESPONSE} category.
         *
         * @return {@code true} if this state is part of the {@link StateCategory#RESPONSE} category;
         *         {@code false} otherwise.
         */
        public boolean isResponse() {
            return this.category == StateCategory.RESPONSE;
        }

        /**
         * Checks if this {@link State} falls under the specified {@link StateCategory}.
         *
         * @param category The {@link StateCategory} to compare against.
         * @return {@code true} if this state is part of the specified {@link StateCategory};
         *         {@code false} otherwise.
         */
        public boolean isCategory(StateCategory category) {
            return this.category == category;
        }
    }

    /**
     * Represents the categories that a {@link State} can belong to.
     * <p>
     * This enum groups different {@link State} values into logical categories
     * based on their purpose or functionality.
     * </p>
     */
    public enum StateCategory {
        /**
         * Represents states related to database connection management.
         * <p>
         * States in this category describe the lifecycle of a database connection,
         * including its establishment, usage, closure, or failure.
         * </p>
         */
        CONNECTION,
        /**
         * Represents states that describe database query responses.
         * <p>
         * States in this category indicate response outcomes, such as
         * the success or failure of specific queries (e.g., INSERT or UPDATE).
         * </p>
         */
        RESPONSE;
    }
}
