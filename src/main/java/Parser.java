import java.io.IOException;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

public interface Parser {
	public Response getWebpage(String url) throws IOException;
	String parse(Document doc, int status);
}
