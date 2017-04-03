package semantic;

public class Symbol {
	
	String name;
	String type;
	int scope;
	
	public Symbol(String name, String type,int scope) {
		super();
		this.name = name;
		this.type = type;
		this.scope =scope;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getScope() {
		return scope;
	}
	

}

