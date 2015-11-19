import static spark.Spark.get;
import static spark.SparkBase.port;
import static spark.SparkBase.staticFileLocation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.heroku.sdk.jdbc.DatabaseUrl;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.template.freemarker.FreeMarkerEngine;

public class Main {

	public static void main(String[] args) {
		port(Integer.valueOf(System.getenv("PORT")));
		staticFileLocation("/public");
		
		get("/berg", new Route() {
			public Object handle(final Request request, final Response response) {
				BergParser p = new BergParser();
				
				
				return p.start();
			}
		});

		get("/", (req, res) -> {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("message", "Hello World!");

			return new ModelAndView(attributes, "home.ftl");

		} , new FreeMarkerEngine());
		
		get("/d", (req, res) -> {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("results", System.getProperty("user.dir"));

			System.out.println(System.getProperty("user.dir"));
			return new ModelAndView(attributes, "berg.ftl");

		} , new FreeMarkerEngine());
		
		get("/db", (req, res) -> {
			Connection connection = null;
			Map<String, Object> attributes = new HashMap<>();
			try {
				connection = DatabaseUrl.extract().getConnection();

				Statement stmt = connection.createStatement();

				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS menu (time TIMESTAMP, jsonstring JSON)");
				stmt.executeUpdate("INSERT INTO menu (time, jsonstring) VALUES (now(),'{\"key\":\"value\"}')");
				ResultSet rs = stmt.executeQuery(
						"SELECT time FROM menu WHERE age(time) = '7 day' " + "ORDER BY time DESC LIMIT 1");

				ArrayList<String> output = new ArrayList<String>();
				while (rs.next()) {
					output.add(rs.getString("time"));
				}

				attributes.put("results", output);
				return new ModelAndView(attributes, "db.ftl");
			} catch (Exception e) {
				attributes.put("message", "There was an error: " + e);
				return new ModelAndView(attributes, "error.ftl");
			} finally {
				if (connection != null)
					try {
						connection.close();
					} catch (SQLException e) {
					}
			}
		} , new FreeMarkerEngine());

		get("/update", new Route() {
			public Object handle(final Request request, final Response response) {
				
				Connection conn = null;
				try {
					//add the most recent menu
					conn = DatabaseUrl.extract().getConnection();
					Statement stmt = conn.createStatement();
					
					BergParser parser = new BergParser();
					org.jsoup.Connection.Response resp = parser.getWebpage(parser.url);
					String result = parser.parse(resp.parse(), resp.statusCode());

					// store the parsed the data into the db
					stmt.executeUpdate("INSERT INTO menu (time, jsonstring) VALUES (now()," + result + ")");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return "added a new menu to the database";
			}
		});
	}
}