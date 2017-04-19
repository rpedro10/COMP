package semantic;
import java.util.ArrayList;

public class SemanticChecker {
	
	private SymbolTable symbolTable;
	private ArrayList<Error> error_list;
	//TODO MODULE WARNINGS
	//private HIRTree tree;
	private int errorCount;

	public SemanticChecker(){ //(/*HIRTree tree*/) {
		symbolTable = new SymbolTable();
		errorCount = 0;
		symbolTable.beginScope();
		error_list = new ArrayList<Error>();	
		//this.tree = tree;
	}

	public ArrayList<Error> runSemanticCheck(HIRTree tree){
		switch(id){
			case "Module":
				checkModule(tree);
				break;
		}

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
			}else if(){
				//TODO rest of assign;
			}
		}
	}

	public void addArithm(HIRTree arithm){
		//Recursividade aritmetica
	}
}