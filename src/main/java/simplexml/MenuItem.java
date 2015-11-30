package simplexml;


import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="menuItem")
public class MenuItem implements Serializable {

	public MenuItem() {
		facts = new MenuFact();
	}

	@Element
	public String name;
	
	@Element
	public MenuFact facts;
}
