package semantic;
import java.util.ArrayList;


class SymbolTable {
	SymbolTable parent;
	ArrayList<SymbolTable> children;
    ArrayList<Symbol> tbl;
    int scope=0;
    
    public SymbolTable(HIRTree hr){
    	this.buildTable(hr);
    }
    
    public SymbolTable(SymbolTable parent, int scope){
    	this.parent = parent;
    	this.scope = scope;
    }
    
    public void buildTable(HIRTree hr){
    	HIRTree child;
    	switch (hr.getId()){
    	case "Module":
    		for(int i = 0; i < hr.getChildren().length; i++){
    			child = hr.getChild(i);
    			if(i == 0){ //Add module name to table
    				tbl.add(new Symbol(child.getVal(), child.getId(),0));
    			}
    			if( i == 1 && child.getId().equals("Declaration")){
    				this.children = new SymbolTable[1];
    				SymbolTable decl = new SymbolTable(this, 0);
    				this.children[0] = decl;
    				decl.buildTable(child);
    			}
    		}
       		break;
    	case "Declaration":
    		tbl.add(new Symbol(null, "Declaration", 0));
    		for(int i = 0; i < hr.getChildren().length; i++){
    			
    		}
    		break;
    	}
    }
    
    Symbol insert(String name,String type, boolean initialized) {
		Symbol symbol = new Symbol(name,type,initialized);
		tbl.add(symbol);
		return symbol;
	}
    
    Symbol lookup(String name) {
		Symbol symbol = null;
		for (int i = tbl.size() - 1; i >= 0; i--) {
			symbol = tbl.get(i);
			if (symbol.getName().equals(name)) {
				return symbol;
			}
		}
		return null;
	}
    
    Integer getScope() {
		return scope;
	}
    void beginScope() {
		scope++;
	}
	
	void endScope() {
		for (int i = tbl.size() - 1; i >= 0; i--) {
			Symbol symbol = tbl.get(i);
			if (symbol.getScope()== this.getScope()) {
				tbl.remove(i);
			} else {
				break;
			}
		}
		scope --;
	}
	Symbol scopeContains(String name) {
		Symbol symbol = null;
		for (int i = tbl.size() - 1; i >= 0; i--) {
			symbol = tbl.get(i);
			if (symbol.getScope()==this.getScope()) {
				if (symbol.getName().equals(name)) {
					return symbol;
				}
			} else {
				break;
			}
		}
		return null;
	}
}