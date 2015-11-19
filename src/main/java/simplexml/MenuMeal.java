package simplexml;

import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

public class MenuMeal {

	public MenuMeal(String n) {
		this.name = n;
		items = new ArrayList<MenuItem>();
	}
	
	@Attribute
	public String name;
	
	@ElementList
	public ArrayList<MenuItem> items;
}
