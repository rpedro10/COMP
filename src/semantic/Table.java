package semantic;
import java.util.ArrayList;


public class Table {
	Table parent;
	ArrayList<Table> children;
    ArrayList<Symbol> tbl;
    
    public Table(Table parent){
    	this();
    	this.parent = parent;
    }
    
    public Table(){
    	children = new ArrayList<Table>();
    	tbl = new ArrayList<Symbol>();
    }
    
    public void insert(String name,String type, boolean initialized) {
		Symbol symbol = new Symbol(name,type,initialized);
		tbl.add(symbol);
	}
    
    public void insertChildTable(Table ct){
    	children.add(ct);
    }
    
    public Symbol getSymbol(int index){
    	return tbl.get(index);
    }
    
    public Symbol lookup(String name) {
		if(tbl.size() > 0){
			for(Symbol s : tbl){
				if(s.getName() == name)
					return s;
			}
		}
		if(parent != null)
			return parent.lookup(name);
		else
			return null;
		
	}
	
}