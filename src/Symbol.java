
import java.util.Vector;

public class Symbol {
	
	String name;
	String type;
	Boolean initialized;
	
	public Symbol(String name, String type,boolean init) {
		super();
		this.name = name;
		this.type = type;
		this.initialized=init;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	
	

}

