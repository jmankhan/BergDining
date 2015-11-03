import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BergParser implements Parser {
	String url;
	JSONObject results;
	final static String[] days = { "sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday" };

	public BergParser() {
		url = "http://dining.muhlenberg.edu/WeeklyMenu.htm";
	}

	@Override
	public String parse(Document doc, int status) {
		JSONObject menu = new JSONObject();
		menu.put("status", status);
		
		JSONArray week = new JSONArray();
		for(String day : days) {
			week.put(parseDay(day, doc));
		}
		
		menu.put("menu", week);
		
		return menu.toString();
	}

	@Override
	public Response getWebpage(String url) throws IOException {
		Response resp = Jsoup.connect(url)
				.userAgent(
						"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
				.timeout(3000).execute();

		return resp;
	}

	public JSONObject getResults() {
		return results;
	}

	private JSONObject parseDay(String day, Document doc) {
		JSONArray dayItems = new JSONArray();

		Elements breakfast = doc.select("#" + day).select(".brk").select("td");
		JSONObject b = new JSONObject();
		b.put("breakfast", parseMeal(breakfast));
		
		Elements lunch = doc.select("#" + day).select(".lun").select("td");
		JSONObject l = new JSONObject();
		l.put("lunch", parseMeal(lunch));

		Elements dinner = doc.select("#" + day).select(".din").select("td");
		JSONObject d = new JSONObject();
		d.put("dinner", parseMeal(dinner));

		dayItems.put(b);
		dayItems.put(l);
		dayItems.put(d);
		
		JSONObject today = new JSONObject();
		today.put(day, dayItems);
		return today;
	}

	private JSONArray parseMeal(Elements ele) {
		JSONArray meal = new JSONArray();
		String station = "";
		for (Element e : ele) {
			if (e.attr("class").equals("station")) {
				String text = e.text().replace("\u00A0", "").replace("\u0092", "'").trim();
				if (!text.isEmpty())
					station = text;
			} else if (e.attr("class").equals("menuitem")) {
				boolean vegetarian = false, vegan = false;
				
				Elements imgs = e.select("img");
				for(Element img : imgs) {
					if(img.hasAttr("alt")) {
						if(img.attr("alt").equalsIgnoreCase("Vegetarian"))
							vegetarian = true;
						if(img.attr("alt").equalsIgnoreCase("Vegan"))
							vegan = true;
					}
				}
				
				JSONObject item = new JSONObject();
				item.put("station", station);
				item.put("menuitem", e.text());
				item.put("vegetarian", vegetarian);
				item.put("vegan", vegan);
				meal.put(item);
			}
		}

		return meal;
	}

}
