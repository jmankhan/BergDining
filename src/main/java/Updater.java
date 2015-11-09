import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.heroku.sdk.jdbc.DatabaseUrl;

public class Updater {

	public static void main(String args[]) {
		Connection conn = null;

		try {
			conn = DatabaseUrl.extract().getConnection();
			
			Statement stmt = conn.createStatement();
			
			//select if there is a menu from the past week
			ResultSet rs = stmt.executeQuery("SELECT (time, jsonstring) FROM menu WHERE age(time) <= '7 day' "
					+ "ORDER BY time DESC LIMIT 1");

			if(!rs.next()) {
				//initiate parse
				BergParser parser = new BergParser();
				org.jsoup.Connection.Response resp = parser.getWebpage(parser.url);
				String result = parser.parse(resp.parse(), resp.statusCode());

				//store the parsed the data into the db
				stmt.executeUpdate("INSERT INTO menu (time, jsonstring) VALUES (now(),"+result+")");
			} else {
				//the updater has not yet reached one week
			}
		} catch(Exception e) {e.printStackTrace(); System.out.println("Error updating");}
	}
}
