import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import simplexml.MenuDay;
import simplexml.MenuItem;
import simplexml.MenuMeal;
import simplexml.MenuWeek;

public class BergParser  {
	String url;

	public BergParser() {
		url = "http://dining.muhlenberg.edu/WeeklyMenu.htm";
	}

	public MenuWeek start() {
		
		MenuWeek err = null;
		try {
			Response r = getWebpage(url);
			err = parse(r.parse(), r.statusCode());
		} catch (IOException e) {e.printStackTrace();}
		finally {
			return err;
		}
	}
	
	public MenuWeek parse(Document doc, int status) {
		Elements script = doc.select("script");
		String nuts = script.get(script.size() - 2).toString();

		Map<String, MenuItem> items = new HashMap<String, MenuItem>();

		// scan through each nutrition fact dataset
		// grab the id inside the dataset, compare with the id in the map
		// input data from the dataset into the BergMenu object
		Scanner in = new Scanner(nuts);
		while (in.hasNextLine()) {
			String line = in.nextLine();
			if (line.startsWith("aData[")) {
				String id = line.substring(line.indexOf("[") + 2, line.indexOf("]") - 1);
				line = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")"));
				String[] f = line.split(",");
				for(int i=0; i<f.length; i++) {
					f[i] = f[i].substring(1, f[i].length()-1);
				}
				
				MenuItem item = new MenuItem();
				item.facts.serv_size = f[0];
				item.facts.calories = f[1];
				item.facts.calfat = f[2];
				item.facts.fat = f[3];
				item.facts.fat_pct_dv = f[4];
				item.facts.satfat = f[5];
				item.facts.satfat_pct_dv = f[6];
				item.facts.transfat = f[7];
				item.facts.chol = f[8];
				item.facts.chol_pct_dv = f[9];
				item.facts.sodium = f[10];
				item.facts.sodium_pct_dv = f[11];
				item.facts.carbo = f[12];
				item.facts.carbo_pct_dv = f[13];
				item.facts.dfib = f[14];
				item.facts.dfib_pct_dv = f[15];
				item.facts.sugars = f[16];
				item.facts.protein = f[17];
				item.facts.vita_pct_dv = f[18];
				item.facts.vitc_pct_dv = f[19];
				item.facts.calcium_pct_dv = f[20];
				item.facts.iron_pct_dv = f[21];
				item.facts.item_name = f[22];
				item.facts.item_desc = f[23];
				item.facts.allergens = f[24];

				item.facts.id = id;
				items.put(id, item);
			}
		}

		in.close();

		MenuWeek week = new MenuWeek();

		for (MenuDay day : week.days) {
			Elements d = doc.select("#" + day.name);
			
			for (MenuMeal meal : day.meal) {
				Elements m = d.select("." + meal.name);

				String station = "";
				for (Element i : m) {
					Elements parents = i.select("td");
					
					for(Element p : parents) {
						if(p.hasAttr("class") && p.attr("class").equalsIgnoreCase("station")) {
							if(!p.text().equals("&nbsp;") && !p.text().equals("\u00a0"))
								station = p.text();
						}
					}
					
					if (!i.select("span").isEmpty()) {
						String id = i.select("span").attr("onclick");
						id = id.substring(id.indexOf("'")+1, id.lastIndexOf("'"));
						
						MenuItem item = items.get(id);
						item.name = i.select("span").text(); 
						item.facts.station = station;
						item.facts.meal = meal.name;
						item.facts.day = day.name;
						
						meal.items.add(item);
					}
				}
			}
		}

		return week;
	}

	public Response getWebpage(String url) throws IOException {
		Response resp = Jsoup.connect(url)
				.userAgent(
						"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
				.timeout(3000).execute();

		return resp;
	}
}