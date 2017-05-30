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
    
    public Table getChild(int index){return children.get(index);}
    
    public ArrayList<Symbol> getVariables(){return tbl;}
    
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
    
    public Symbol lookup(String name, String type) { //Procura pelo simbolo com nome e tipo especificado
		if(tbl.size() > 0){
			for(Symbol s : tbl){
				if(s.getName() == name && s.getType() == type)
					return s;
			}
		}
		if(parent != null)
			return parent.lookup(name);
		else
			return null;
	}
    
   public Table lookupFunction(String name){
	   if(parent!=null){
		   parent.lookupFunction(name);
	   }
	   else{
		   for(Table t : parent.children){
			  if(t.getSymbol(0).getName().equals(name))
				  return t;
		   }
	   }
	   return null;
   }
   
   public int checkForReturnPosition(){ //Obter posição em termos de stack da variavel de retorno
	   Symbol ret = tbl.get(1);
	   if(ret.getType().equals("return")){
		   for(int i = 2; i < tbl.size(); i++){
			   if(ret.getName().equals(tbl.get(i).getName()))
				   return i;
		   }
	   	   return -1;
	   	}
	   else
		   return -1;
   }
	
}