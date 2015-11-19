package simplexml;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

public class MenuItem {

	public MenuItem() {
		facts = new MenuFact();
	}
	@Element
	public String name;
	
	@Element
	public MenuFact facts; 
}
