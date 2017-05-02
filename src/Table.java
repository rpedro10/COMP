
import java.util.ArrayList;


class SymbolTable {
	
    ArrayList<Symbol> tbl;
    int scope=0;
    
    Symbol insert(String name,String type) {
		Symbol symbol = new Symbol(name,type,true);
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
	/**
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
	*/
}