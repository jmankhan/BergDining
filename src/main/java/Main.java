import static spark.Spark.get;
import static spark.SparkBase.port;
import static spark.SparkBase.staticFileLocation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import com.heroku.sdk.jdbc.DatabaseUrl;

import simplexml.MenuWeek;
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
				Connection connection = null;
				try {
					connection = DatabaseUrl.extract().getConnection();
					Statement stmt = connection.createStatement();

					ResultSet latest = stmt.executeQuery("SELECT weeklymenu, datestamp " + "FROM bergmenu "
							+ "ORDER BY datestamp DESC " + "LIMIT 1");

					if (latest.next()) {
						Serializer ser = new Persister(new AnnotationStrategy());
						MenuWeek week = ser.read(MenuWeek.class, latest.getString(1));
						ser.write(week, response.raw().getOutputStream());
					} else {
						return "Error reading from db";
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					return "Error reading from db";
				}
			}
		});

		get("/", (req, res) -> {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("message", "Hello World!");

			return new ModelAndView(attributes, "home.ftl");

		} , new FreeMarkerEngine());
	}
}