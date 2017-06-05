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
    
    public ArrayList<Table> getChildTables(){return children;}
    
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
				if(s.getName().equals(name))
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
				if(s.getName().equals(name) && s.getType().equals(type))
					return s;
			}
		}
		if(parent != null)
			return parent.lookup(name);
		else
			return null;
	}
    
   public Table lookupFunction(String name){
	   if(!parent.getSymbol(0).getType().equals(module name)){
		   return parent.lookupFunction(name);
	   }
	   else{
		   for(Table t : children){
			  if(t.getSymbol(0).getName().equals(name))
				  return t;
		   }
	   }
	   return null;
   }

   public Table lookupModule(String name){
	   if(parent!=null){
		   return parent.lookupFunction(name);
	   }
	   else{
		   for(Table t : children){
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
   
   public String getModuleName(){
	   Table aux = this;
	   while(aux.parent != null)
		   aux = aux.parent;
	   return aux.tbl.get(0).getName();
   }
   
   public boolean isExternal(Symbol s){
	   if(tbl.contains(s))
		   return false;
	   else{
		   if(parent.lookup(s.getName()) != null)
			   return true;
		   else
			   return false;
	   }
   }
   
   public boolean isGlobal(Symbol s){
	   Table aux = parent;
	   while(aux.parent != null)
		   aux = aux.parent;
	   if(aux.tbl.contains(s))
		   return true;
	   else
		   return false;
   }
   
   public void dump(String prefix){
	   for(Symbol s: tbl){
		   System.out.println(prefix + s.getType() + " : " + s.getName());
	   }
	   System.out.println(prefix + "CHILD TABLES");
	   for(Table t : children){
		   t.dump(prefix + " ");
		   System.out.println("");
	   }
   }
	
}