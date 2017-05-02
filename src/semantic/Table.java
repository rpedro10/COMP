package semantic;
import java.util.ArrayList;


class SymbolTable {
	SymbolTable parent;
	ArrayList<SymbolTable> children;
    ArrayList<Symbol> tbl;
    
    public SymbolTable(HIRTree hr){
    	//this.buildTable(hr);
    }
    
    public SymbolTable(SymbolTable parent){
    	this.parent = parent;
    }
    
    public SymbolTable(){
    }
    
/**
	public void buildTable(HIRTree hr){
    	HIRTree child;
    	switch (hr.getId()){
    	case "Module":
    		for(int i = 0; i < hr.getChildren().length; i++){
    			child = hr.getChild(i);
    			if(i == 0){ //Add module name to table
    				tbl.add(new Symbol(child.getVal(), child.getId(),true));
    			}
    			if( i == 1 && child.getId().equals("Declaration")){
    				SymbolTable new_table = new SymbolTable(this);
    				this.children.add(new_table) ;
    				new_table.buildTable(child);
    			}
    		}
       		break;
    	case "DeclarationList":
    		tbl.add(new Symbol("null", "Declaration",true));
    		for(int i = 0; i < hr.getChildren().length; i++){
    			
    		}
    		break;
    	}
    }
    
    */
    void insert(String name,String type, boolean initialized) {
		Symbol symbol = new Symbol(name,type,initialized);
		tbl.add(symbol);
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
    
   
	
}