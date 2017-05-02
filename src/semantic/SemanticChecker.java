package semantic;
import java.util.ArrayList;

public class SemanticChecker {
	
	private SymbolTable symbolTable;
	private ArrayList<Error> error_list;
	//TODO MODULE WARNINGS
	private HIRTree tree;
	private int errorCount;

	public SemanticChecker(){ //(/*HIRTree tree*/) {
		symbolTable = new SymbolTable(symbolTable);
		errorCount = 0;
		error_list = new ArrayList<Error>();
	//	this.tree = tree;
	}

	public ArrayList<Error> runSemanticCheck(HIRTree hr,SymbolTable symbolTable){
		ArrayList<Error> erros = new  ArrayList<Error>() ;
		HIRTree child;
    	switch (hr.getId()){
    	case "Module":
    		for(int i = 0; i < hr.getChildren().length; i++){
    			child = hr.getChild(i);
    			if(i == 0){ //Add module name to table
    				symbolTable.insert(child.getVal(), child.getId(),true);
    			}
    			if( i == 1 && child.getId().equals("DeclarationList")){
    				SymbolTable new_table = new SymbolTable(symbolTable);
    				this.symbolTable.children.add(new_table) ;
    	    		runSemanticCheck(child,symbolTable);
    			}
    			if(child.getId().equals("Function")){
    				SymbolTable tabela = new SymbolTable(symbolTable);
    				runSemanticCheck(child,tabela);
    			}
    		}
       		break;
    	case "DeclarationList":
    		//symbolTable.insert("null", "Declaration",true);
    		//runSemanticChecker(child,symbolTable)
    		for(int i = 0; i < hr.getChildren().length; i++){
    			child = hr.getChild(i);
    			if(child.getId()=="Assign"){
    				symbolTable.insert(child.getChild(0).getVal(), child.getChild(0).getId(), true);
    			}
    			else
    			{
    				symbolTable.insert(child.getVal(), child.getId(), false);
    			}
    			
    		}
    		break;
    	case "Function":
    		for(int i = 0; i < hr.getChildren().length; i++){
    			child = hr.getChild(i);
    			
    			if(child.getId().equals("Return")){
    				symbolTable.insert(child.getChildren()[1].getVal(), child.getChildren()[1].getId(), true);
    				//insert tipo de retorno filho[0]
    			}
    		}
    		
    		
    		
    	}
		return erros;

	}

	public void checkModule(HIRTree tree){
		for(int i=0; i<tree.getChildren().length; i++){
			if(i==0)
				symbolTable.insert(tree.getChild(i).getVal(), "name", false);
			else if(tree.getChild(i).getId() == "DeclarationList"){
				addDeclarationList(tree.getChild(i));
			}else if(tree.getChild(i).getId() == "Function"){
				addFunction(tree.getChild(i));
			}
		}
	}

	public void addDeclarationList(HIRTree tree){
		for(int i=0 ; i<tree.getChildren().length ; i++){
			if(tree.getChild(i).getId() == "assign"){
				addAssign(tree.getChild(i));
			}else{
				Symbol lookupSymbol = symbolTable.lookup(tree.getChild(i).getVal());
				if( lookupSymbol == null){
					symbolTable.insert(tree.getChild(i).getVal(), "int", false);
				}else{
					//retornar erro "var;" again
				}
			}
		}
	}

	public void addFunction(HIRTree tree){
		//TODO -> criar tabela, e por nessa tabela o nome, retorno e parametros
	}

	public void addAssign(HIRTree assign){
		for(int i=0; i<assign.getChildren().length ; i++){
			if(i==0){
				Symbol lookupSymbol = symbolTable.lookup(tree.getChild(i).getVal());
				if( lookupSymbol == null){
					symbolTable.insert(tree.getChild(i).getVal(), "int", true);
				}else{
					lookupSymbol.setInitialized();
				}
			}else if(true){
				//TODO rest of assign;
			}
		}
	}

	public void addArithm(HIRTree arithm){
		//Recursividade aritmetica
	}
}