package bank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mysql.jdbc.PreparedStatement;

/**
 * A simple implementation of the ReservationManager interface. Each object of
 * this class must create a dedicated connection to the database.
 * <p>
 * <b>Note: DO NOT alter this class's interface.</b>
 * 
 * @author Busca
 * 
 */
public class BankManagerImpl implements BankManager {

    // CLASS FIELDS
    //
    // example of a create table statement executed by createDB()
    private static final String CREATE_TABLE_ACCOUNTS = "create table ACCOUNTS (" + 
	    "id int NOT NULL, " +
    	"balance int NOT NULL DEFAULT '0'," +
	    "primary key (id)" + 
	    ");";
    
    private static final String CREATE_TABLE_TRANSFERS = "create table TRANSFERS (" +
    	"id int NOT NULL AUTO_INCREMENT," +
	    "account_from int NOT NULL, " +
    	"account_to int NOT NULL," +
	    "amount int NOT NULL," +
    	"date DATETIME NOT NULL," +
	    "primary key (id)," +
	    "foreign key (account_from) references ACCOUNTS(id)," +
	    "foreign key (account_to) references ACCOUNTS(id)" +
	    ")";
    
    private static final String CREATE_TABLE_OPERATIONS = "create table OPERATIONS (" + 
	    "id int NOT NULL AUTO_INCREMENT, " +
    	"amount int NOT NULL," +
    	"date DATETIME NOT NULL," +
    	"account int NOT NULL," +
	    "primary key (id)," + 
    	"foreign key (account) references ACCOUNTS(id)"+
	    ")";
    
    private static final String CREATE_TRIGGER_BALANCE = "create trigger POSITIVE_BALANCE " +
    	"BEFORE UPDATE ON ACCOUNTS " +
    	"FOR EACH ROW BEGIN "
    	+ "IF (NEW.balance >= 0) THEN INSERT INTO OPERATIONS(amount,date,account) VALUES (NEW.balance - OLD.balance, CURRENT_TIMESTAMP, NEW.id); "
    	+ "ELSE " +
    	"SET NEW.balance = OLD.balance; END IF; END";
    
    
    private final Connection connection;

    /**
     * Creates a new ReservationManager object. This creates a new connection to
     * the specified database.
     * 
     * @param url
     *            the url of the database to connect to
     * @param user
     *            the login name of the user
     * @param password
     *            his password
     */
    public BankManagerImpl(String url, String user, String password) throws SQLException {
    	connection = DriverManager.getConnection(url, user, password);
    }

    @Override
    public void createDB() throws SQLException {
    	Statement stmt = connection.createStatement();

    	stmt.executeUpdate("drop table if exists TRANSFERS");
    	stmt.executeUpdate("drop table if exists OPERATIONS");
    	stmt.executeUpdate("drop table if exists ACCOUNTS");
    	stmt.executeUpdate(CREATE_TABLE_ACCOUNTS);
    	stmt.executeUpdate(CREATE_TABLE_TRANSFERS);
    	stmt.executeUpdate(CREATE_TABLE_OPERATIONS);
    	stmt.executeUpdate(CREATE_TRIGGER_BALANCE);
    }

    @Override
    public boolean createAccount(int number) throws SQLException {
    	
    	Statement stmt = connection.createStatement();
    	if(stmt.executeUpdate("INSERT INTO ACCOUNTS VALUES (" + number + ", 0)")==1) return true;
    	return false;
    }

    @Override
    public double getBalance(int number) throws SQLException {
    	Statement stmt = connection.createStatement();
    	ResultSet result = stmt.executeQuery("SELECT balance FROM ACCOUNTS WHERE id="+number);
    	result.next();
    	return result.getDouble(1);
    }

    @Override
    public double addBalance(int number, double amount) throws SQLException {
    	Statement stmt = connection.createStatement();
    	stmt.executeUpdate("UPDATE ACCOUNTS SET balance=balance+"+amount+" WHERE id="+number);
    	return getBalance(number);
    }

    @Override
    public boolean transfer(int from, int to, double amount) throws SQLException {
	// TODO Auto-generated method stub
    	addBalance(from, -amount);
    	addBalance(to, amount);
	return false;
    }

    @Override
    public List<Operation> getOperations(int number, Date from, Date to) throws SQLException {
	// TODO Auto-generated method stub
    	List<Operation> result_list= new ArrayList<Operation>();
    	Statement stmt = connection.createStatement();
    	ResultSet result = stmt.executeQuery("SELECT * FROM OPERATIONS");
    	while (result.next()) {
			result_list.add(new Operation(result.getInt(4), result.getDouble(2), result.getDate(3)));
		}
	return result_list;
    }

}
