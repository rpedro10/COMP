package semantic;

public class Symbol {
	
	private String name;
	private String type;
	private boolean initialized;

	public Symbol(String name, String type, boolean initialized) {
		super();
		this.name = name;
		this.type = type;
		this.initialized = initialized;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isInitialized() {
		return initialized;
	}
	
	public void setInitialized(){
		initialized = true;
	}

}

