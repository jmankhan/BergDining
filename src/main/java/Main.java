import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static spark.Spark.*;
import spark.template.freemarker.FreeMarkerEngine;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;

import static spark.Spark.get;

import com.heroku.sdk.jdbc.DatabaseUrl;

public class Main {

	public static void main(String[] args) {

		port(Integer.valueOf(System.getenv("PORT")));
		staticFileLocation("/public");
		

		get("/berg", new Route() {
			@SuppressWarnings("finally")
			public Object handle(final Request request, final Response response) {
				BergParser parser = new BergParser();
				String result = "";
				try {
					org.jsoup.Connection.Response resp = parser.getWebpage(parser.url);
					result = parser.parse(resp.parse(), resp.statusCode());
					
				} catch (IOException e) {e.printStackTrace();}
				finally {
					return result;
				}
			}
		});
		
		get("/", (req, res) -> {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("message", "Hello World!");

			return new ModelAndView(attributes, "home.ftl");

		} , new FreeMarkerEngine());

		get("/db", (req, res) -> {
			Connection connection = null;
			Map<String, Object> attributes = new HashMap<>();
			try {
				connection = DatabaseUrl.extract().getConnection();

				Statement stmt = connection.createStatement();
				
				
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
				stmt.executeUpdate("INSERT INTO menu (time, json) VALUES (now())");
				ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

				ArrayList<String> output = new ArrayList<String>();
				while (rs.next()) {
					output.add("Read from DB: " + rs.getTimestamp("tick"));
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

	}

}