package today.tecktip.killbill.backend.db;

import java.util.Objects;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Base class for handling database connections and queries.
 * @author cs
 */
public class Database {
    /**
     * JDBC connection string for the database. Generated using env vars.
     */
    private static String JDBC = "jdbc:mysql://" + DatabaseEnv.getMySqlUser() + ":" + DatabaseEnv.getMySqlPass() + "@" + DatabaseEnv.getMySqlHost() + ":3306/" + DatabaseEnv.getMySqlDb();

    /**
     * Executes a query against the database with an operator for each record.
     * @param query Query to execute
     * @param rsOperator Method to execute against each record {@link ResultSet}
     * @throws SQLException Unable to contact database
     */
    public static void executeQuery(final String query, final ResultSetOperator rsOperator) throws SQLException{
        executeQuery(query, null, rsOperator);
    }

    /**
     * Executes a query against the database with an operator for the statement and each record.
     * @param query Query to execute
     * @param psOperator Method to execute against the {@link PreparedStatement} before running
     * @param rsOperator Method to execute against each record {@link ResultSet}
     * @throws SQLException Unable to contact database
     */
    public static void executeQuery(final String query, final PreparedStatementOperator psOperator, final ResultSetOperator rsOperator) throws SQLException {
        Objects.requireNonNull(query, "'query' cannot be null");
        Objects.requireNonNull(rsOperator, "'rsOperator' cannot be null");

        try (Connection connection = DriverManager.getConnection(JDBC)) {
            PreparedStatement ps = connection.prepareStatement(query);
            if (psOperator != null)
                psOperator.operate(ps);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                rsOperator.operate(rs);
            }
        }
    }

    /**
     * Executes a statement on the database.
     * @param query Query to execute
     * @throws SQLException Unable to contact database
     */
    public static void execute(final String query) throws SQLException {
        Objects.requireNonNull(query, "'query' cannot be null");

        try (Connection connection = DriverManager.getConnection(JDBC)) {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.execute();
        }
    }

    /**
     * Executes a statement on the database with an operator for the statement.
     * @param query Query to execute
     * @param operator Method to execute against the {@link PreparedStatement} before running
     * @throws SQLException Unable to contact database
     */
    public static void execute(final String query, final PreparedStatementOperator operator) throws SQLException {
        Objects.requireNonNull(query, "'query' cannot be null");
        Objects.requireNonNull(operator, "'operator' cannot be null");

        try (Connection connection = DriverManager.getConnection(JDBC)) {
            PreparedStatement ps = connection.prepareStatement(query);
            operator.operate(ps);
            ps.execute();
        }
    }

    /**
     * Lambda expression for modifying a {@link PreparedStatement}.
     */
    public interface PreparedStatementOperator {
        /**
         * Operate on a {@link PreparedStatement}.
         * @param s Statement that can be modified
         * @throws SQLException Unable to modify
         */
        public void operate(final PreparedStatement s) throws SQLException;
    }

    /**
     * Lambda expression for reading records from a {@link ResultSet}.
     */
    public interface ResultSetOperator {
        /**
         * Operate on a {@link ResultSet}.
         * @param rs Result set representing a single record from a query
         * @throws SQLException Unable to modify
         */
        public void operate(final ResultSet rs) throws SQLException;
    }

    /**
	 * This class should not be instantiated.
	 */
	private Database() { throw new AssertionError(); }
}