package semantic;
import java.util.ArrayList;

public class SemanticChecker {
	
	private Table symbolTable;
	private ArrayList<Error> error_list;
	//TODO MODULE WARNINGS
	private HIRTree tree;
	private int errorCount;

	public SemanticChecker(){ //(/*HIRTree tree*/) {
		symbolTable = new Table();
		errorCount = 0;
		error_list = new ArrayList<Error>();
	//	this.tree = tree;
	}

	public ArrayList<Error> runSemanticCheck(HIRTree hr,Table symbolTable){
		ArrayList<Error> erros = new  ArrayList<Error>(); //em vez disto adicionar ao error_list
		HIRTree child;
    	switch (hr.getId()){
    	case "Module":
    		for(int i = 0; i < hr.getChildren().length; i++){
    			child = hr.getChild(i);
    			if(i == 0){ //Add module name to table
    				this.symbolTable.insert(child.getVal(), "module name", true);
    			}else if( i == 1 && child.getId().equals("DeclarationList")){
    				Table new_table = new Table(this.symbolTable);
    	    		runSemanticCheck(child,new_table); //talvez funcione assim, pedir opinião
    	    		this.symbolTable.children.add(new_table) ;
    			}else if(child.getId().equals("Function")){
    				Table new_table = new Table(this.symbolTable);
    				runSemanticCheck(child,new_table);
    				this.symbolTable.children.add(new_table);
    			}
    		}
       		break;
    	case "DeclarationList":
    		for(int i=0 ; i<hr.getChildren().length ; i++){
				if(hr.getChild(i).getId() == "assign"){
					addAssign(hr.getChild(i),symbolTable);
				}else{
					 Symbol lookup = symbolTable.lookup(hr.getChild(i).getVal());
					if( lookup == null){
						symbolTable.insert(hr.getChild(i).getVal(), "int", false);
					}else{
						//retornar erro "var;" again
						System.out.println("DeclarationList error, redeclaration");
					}
				}
			}
    		break;
    	case "Function":
    		for(int i = 0; i < hr.getChildren().length; i++){
    			child = hr.getChild(i);
    			if(i==0){
	    			if(child.getId().equals("Return")){
	    				symbolTable.insert(child.getChild(1).getVal(), "function name", true);
	    				symbolTable.insert(child.getChild(0).getVal(), "return", false);
	    			}else{
	    				symbolTable.insert(child.getVal(), "name", true);
	    			}
    			}else if(child.getId().equals("Parameters")){
    				for(int j=0; j < child.getChildren().length ; j++){
    					if(child.getChild(j).getId().equals("Id"))
    						symbolTable.insert(child.getChild(j).getVal(), "parameter int", true);
    					else if(child.getChild(j).getId().equals("Array"))
    						symbolTable.insert(child.getChild(j).getVal(), "parameter array", true);
    				}
    			}else if(child.getId().equals("Assign")){

    			}else if(child.getId().equals("If")){
    				//Criar nova tabela e preencher e depois adicionar a tabela function
    			}else if(child.getId().equals("While")){
    				//Criar nova tabela e preencher e depois adicionar a tabela function
    			}else if(child.getId().equals("Call")){
    				//isto pode estar aqui, ou é só no assign?
    			}
    		}
    		break;
    	case "If":
    		for(int i=0; i < hr.getChildren().length; i++){
    			child=hr.getChild(i);
	    		if(child.getId().equals("Assign")){

	    		}else if(child.getId().equals("If")){
	    			//Criar nova tabela e preencher e depois adicionar a tabela function
	    		}else if(child.getId().equals("While")){
	    			//Criar nova tabela e preencher e depois adicionar a tabela function
	    		}else if(child.getId().equals("Call")){
	    			//isto pode estar aqui, ou é só no assign?
	    		}
	    	}
    		break;
    	case "While":
    		for(int i=0; i < hr.getChildren().length; i++){
    			child=hr.getChild(i);
	    		if(child.getId().equals("Assign")){

	    		}else if(child.getId().equals("If")){
	    			//Criar nova tabela e preencher e depois adicionar a tabela function
	    		}else if(child.getId().equals("While")){
	    			//Criar nova tabela e preencher e depois adicionar a tabela function
	    		}else if(child.getId().equals("Call")){
	    			//isto pode estar aqui, ou é só no assign?
	    		}
	    	}
    		break;
    	}
		return erros; //em vez de ^^^
	}

	public void addAssign(HIRTree tree, Table symbolTable){
		for(int i=0; i<tree.getChildren().length ; i++){
			if(i==0){
				 Symbol lookup = symbolTable.lookup(tree.getChild(i).getVal());
				if( lookup == null){
					if(tree.getChild(i).getId().equals("Array")){
						if(tree.getChild(i+1).getId().equals("ArrayAccess")){
							//erro, array has to be defined before;
							System.out.println("Array error, not defined");
						}else{
							symbolTable.insert(tree.getChild(i).getVal(), "array", false);
						}
					}
					symbolTable.insert(tree.getChild(i).getVal(), "int", true);
				}else{
					//podes mudar o tamanho do array depois de criar primeira vez?
					lookup.setInitialized();
				}
			}else if(tree.getChild(i).getId().equals("Arith")){
				addArithm(tree.getChild(i), symbolTable);
			}else if(tree.getChild(i).getId().equals("Call")){
				//lookup function name and verify arguments, see if used vars are initialized
				checkCall(tree.getChild(i), symbolTable);
			}else if(tree.getChild(i).getId().equals("Id")){
				//lookup se if var is initialized
				Symbol lookup = symbolTable.lookup(tree.getChild(i).getVal());
				if(lookup == null){
					//erro not defined;
				}else if(!lookup.isInitialized()){
					//erro\warning not initialized
				}
			}
		}
	}

	public void addArithm(HIRTree tree, Table symbolTable){
		//verificar primeiro elemento e recursividade a direita
		for(int i=0; i<tree.getChildren().length; i++){
			if(tree.getChild(i).getId().equals("Id")){
				//lookup
				Symbol lookup = symbolTable.lookup(tree.getChild(i).getVal());
				if(lookup == null){
					//erro not defined;
				}else if(!lookup.isInitialized()){
					//erro\warning not initialized
				}
			}else if(tree.getChild(i).getId().equals("Call")){
				checkCall(tree.getChild(i), symbolTable);
			}else if(tree.getChild(i).getId().equals("Arith")){
				addArithm(tree.getChild(i), symbolTable);
			}
		}
	}
	
	public void initModuleChildTables(HIRTree moduleNode, Table moduleTable){
		HIRTree[] children = moduleNode.getChildren();
		HIRTree currFunct;
		if(children.length > 2){ 
			Table funcTable;
			for(int i = 2; i < children.length; i++){
				currFunct = children[i].getChild(i);
				funcTable = new Table(moduleTable);
				if(currFunct.getChild(0).getId().equals("Return")){
					funcTable.insert(currFunct.getChild(0).getChild(0).getVal(), "return", true);
					funcTable.insert(currFunct.getChild(0).getChild(1).getVal(), "function name", true);
				}else{
					funcTable.insert(currFunct.getChild(0).getVal(), "function name", true);
				}
				moduleTable.insertChildTable(funcTable);
			}
		}
	}

	public void checkCall(HIRTree tree, Table symbolTable){
		//lookup function name and verify arguments, see if used vars are initialized
	}


	/*NOT USED
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
	}*/

	/*NOT USED
	public void addDeclarationList(HIRTree tree){
		for(int i=0 ; i<tree.getChildren().length ; i++){
			if(tree.getChild(i).getId() == "assign"){
				addAssign(tree.getChild(i));
			}else{
				 lookup = symbolTable.lookup(tree.getChild(i).getVal());
				if( lookup == null){
					symbolTable.insert(tree.getChild(i).getVal(), "int", false);
				}else{
					//retornar erro "var;" again, afinal ja retorna
				}
			}
		}
	}*/

	/*NOT USED
	public void addFunction(HIRTree tree){
		//TODO -> criar tabela, e por nessa tabela o nome, retorno e parametros
	}*/
}