import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import com.heroku.sdk.jdbc.DatabaseUrl;

import simplexml.MenuWeek;

public class Updater {

	private final static String TABLENAME = "bergmenu";
	
	public static void main(String args[]) {
		BergParser p = new BergParser();
		MenuWeek m = p.start();
		
		Connection conn = null;
		try {
			
			conn = DatabaseUrl.extract().getConnection();
			
			//check if table exists. if not, this is the first time,
			//so we should populate the db
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet res = meta.getTables(null, null, TABLENAME, new String[] {"TABLE"});
			//if true, table exists
			if(res.next()) {
				//check if it is time to update
				Statement read = conn.createStatement();
				ResultSet latest = read.executeQuery(
						  "SELECT datestamp "
						+ "FROM " + TABLENAME + " "
						+ "WHERE age(datestamp) >= '7 DAY' "
						+ "ORDER BY datestamp DESC "
						+ "LIMIT 1");

				//if this set contains anything, we should update the table
				//otherwise, we don't have to do anything
				if(latest.next()) {
					update(conn);
				} else {
					//do nothing
				}
				
			} else {
				create(conn);
			}
		} catch(Exception e) {e.printStackTrace();}
	}
	
	/**
	 * Creates a new table <code>tablename</code>
	 * Immediately updates it using the update() method 
	 * Passes this connection to it
	 * @param conn
	 * @throws Exception
	 */
	private static void create(Connection conn) throws Exception {
		System.out.println("creating table");
		
		//create the table
		Statement create = conn.createStatement();
		create.execute("CREATE TABLE " + TABLENAME + "("
				+ "weeklymenu XML, "
				+ "datestamp timestamp)");
		
		//immediately update
		update(conn);
	}
	
	/**
	 * Updates the table <code>tablename</code>
	 * Creates a new BergParser object and starts it
	 * Stores the response with a timestamp into db
	 * @param conn
	 * @throws Exception
	 */
	private static void update(Connection conn) throws Exception {
		System.out.println("updating table");
		
		BergParser parser = new BergParser();
		MenuWeek week = parser.start();
		
		Serializer ser = new Persister(new AnnotationStrategy());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ser.write(week, baos);

		String sql = "INSERT INTO " + TABLENAME + " VALUES(XML(?), 'now()')";
		PreparedStatement update = conn.prepareStatement(sql);
		update.setString(1, new String(baos.toByteArray(), "UTF-8"));
		
		update.execute();
		update.close();
	}
}
