import java.io.IOException;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BergParser implements Parser {
	String url;
	JSONObject results;
	
	public BergParser() {
		url = "http://dining.muhlenberg.edu/WeeklyMenu.htm#saturday";
	}
	
	@Override
	public String parse(Document doc) {
		return doc.html();
	}

	@Override
	public Document getWebpage(String url) throws IOException {
		return Jsoup.connect(url).get();
	}

	public JSONObject getResults() {
		return results;
	}
}
