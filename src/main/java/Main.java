import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static spark.Spark.*;
import spark.template.freemarker.FreeMarkerEngine;
import spark.utils.IOUtils;
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
				Connection connection = null;
				String result = "";
				try {
					connection = DatabaseUrl.extract().getConnection();

					Statement stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT (time, jsonstring) FROM menu ORDER BY time DESC LIMIT 1");
					rs.next();
					result = rs.getString(1);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
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

		get("/xml", new Route() {
			public Object handle(final Request request, final Response response) {
				byte[] out = null;
				try {
					out = IOUtils.toByteArray(new FileInputStream("main/resources/public/menu.xml"));
					response.raw().setContentType("text/xml, application/xml");
					response.raw().getOutputStream().write(out, 0, out.length);
					System.out.println("testerino");
				} catch (IOException e) {e.printStackTrace();}

				return "";
			}
		});
		
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