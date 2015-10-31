import java.io.IOException;

import org.json.JSONObject;
import org.jsoup.nodes.Document;

public interface Parser {
	public Document getWebpage(String url) throws IOException;
	String parse(Document doc);
}
