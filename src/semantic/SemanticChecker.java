package semantic;
import java.util.ArrayList;

public class SemanticChecker {
	
	private Table symbolTable;
	private ArrayList<Error> error_list;
	private int errorCount;

	public SemanticChecker(){
		symbolTable = new Table();
		errorCount = 0;
		error_list = new ArrayList<Error>();
	}

	public void runSemanticCheck(HIRTree hr,Table symbolTable){
		HIRTree child;
    	switch (hr.getId()){
    	case "Module":
    		boolean firstTable = true;
    		for(int i = 0; i < hr.getChildren().length; i++){
    			child = hr.getChild(i);
    			if(i == 0){ //Add module name to table
    				this.symbolTable.insert(child.getVal(), "module name", true);
    			}else if( i == 1 && child.getId().equals("DeclarationList")){

    				for(int k=0 ; k<child.getChildren().length ; k++){
						if(child.getChild(k).getId().equals("Assign")){
							//System.out.println("Assign...");
							addAssign(child.getChild(k),this.symbolTable);
						}else{
							//System.out.println("Starting to look for " + child.getChild(k).getVal());
							HIRTree subChild = child.getChild(k);
							Symbol lookup;
							if(subChild.getId().equals("Array"))
								lookup = this.symbolTable.lookup(subChild.getChild(0).getVal());
							else
								lookup = this.symbolTable.lookup(subChild.getVal());
							if( lookup == null){
								//System.out.println("null lookup on " + child.getChild(k).getVal());
								if(child.getChild(k).getId().equals("Id")){
									this.symbolTable.insert(child.getChild(k).getVal(), "int", false);
								}else if(child.getChild(k).getId().equals("Array")){
									this.symbolTable.insert(subChild.getChild(0).getVal(), "array", false);
								}
							}else{
								//retornar erro "var;" again
								System.out.println("DeclarationList error, redeclaration " + child.getChild(k).getVal());
							}
						}
					}
    			}else if(child.getId().equals("Function")){
    				if(firstTable){
    					initModuleChildTables(hr, this.symbolTable);
    					firstTable=false;
    				}
    				runSemanticCheck(child, this.symbolTable.getChild(i-2));
    			}
    		}
       		break;
    	case "Function":
    		for(int i = 1; i < hr.getChildren().length; i++){
    			child = hr.getChild(i);
    			if(child.getId().equals("Assign")){
    				addAssign(child, symbolTable);
    			}else if(child.getId().equals("If")){
    				Table ifTable = new Table(symbolTable);
    				runSemanticCheck(child,ifTable);
    				//Criar nova tabela e preencher e depois adicionar a tabela function
    			}else if(child.getId().equals("While")){
    				Table whileTable = new Table(symbolTable);
    				runSemanticCheck(child,whileTable);
    				//Criar nova tabela e preencher e depois adicionar a tabela function
    			}else if(child.getId().equals("Call")){
    				//isto pode estar aqui, ou é só no assign?
    				checkCall(child, symbolTable);
    			}
    		}
    		break;
    	case "If":
    		for(int i=0; i < hr.getChildren().length; i++){
    			child=hr.getChild(i);
	    		if(child.getId().equals("Exprtest")){
	    			//validar expressão
	    		}else if(child.getId().equals("Assign")){
	    			addAssign(child,symbolTable);
	    		}else if(child.getId().equals("If")){
	    			Table ifTable = new Table(symbolTable);
    				runSemanticCheck(child,ifTable);
	    			//Criar nova tabela e preencher e depois adicionar a tabela function
	    		}else if(child.getId().equals("While")){
	    			Table whileTable = new Table(symbolTable);
    				runSemanticCheck(child,whileTable);
	    			//Criar nova tabela e preencher e depois adicionar a tabela function
	    		}else if(child.getId().equals("Call")){
	    			//isto pode estar aqui, ou é só no assign?
	    			checkCall(child, symbolTable);
	    		}
	    	}
    		break;
    	case "While":
    		for(int i=0; i < hr.getChildren().length; i++){
    			child=hr.getChild(i);
	    		if(child.getId().equals("Exprtest")){
	    			//validar expressão
	    		}else if(child.getId().equals("Assign")){
	    			addAssign(child,symbolTable);
	    		}else if(child.getId().equals("If")){
	    			Table ifTable = new Table(symbolTable);
    				runSemanticCheck(child,ifTable);
	    			//Criar nova tabela e preencher e depois adicionar a tabela function
	    		}else if(child.getId().equals("While")){
	    			Table whileTable = new Table(symbolTable);
    				runSemanticCheck(child,whileTable);
	    			//Criar nova tabela e preencher e depois adicionar a tabela function
	    		}else if(child.getId().equals("Call")){
	    			//isto pode estar aqui, ou é só no assign?
	    			checkCall(child, symbolTable);
	    		}
	    	}
    		break;
    	}
	}

	public void addAssign(HIRTree tree, Table symbolTable){
		Symbol lookup;
		for(int i=0; i<tree.getChildren().length ; i++){
			if(i==0){
				if(tree.getChild(i).getId().equals("Id")){
					lookup = symbolTable.lookup(tree.getChild(i).getVal());
					//System.out.println("null assign lookup on " + tree.getChild(i).getVal());
					if( lookup == null){
						if(tree.getChild(i + 1).getId().equals("ArraySize")){
							if(Integer.parseInt(tree.getChild(i+1).getChild(0).getVal())>0){
								symbolTable.insert(tree.getChild(i).getVal(), "array", false);
								System.out.println("Define " + tree.getChild(i).getVal());
							}else{
								System.out.println("ArraySize must be greater than 0");
							}
						}else if(tree.getChild(i + 1).getId().equals("Integer")){
							symbolTable.insert(tree.getChild(i).getVal(), "int", true);
							System.out.println("Initialize " + tree.getChild(i).getVal());
						}else if(tree.getChild(i + 1).getId().equals("Id")){
							symbolTable.insert(tree.getChild(i).getVal(), "int", true);
							System.out.println("Initialize " + tree.getChild(i).getVal());
						}
					}else{
						if(tree.getChild(i + 1).getId().equals("ArraySize")){
							if(Integer.parseInt(tree.getChild(i+1).getChild(0).getVal())<=0)
								System.out.println("ArraySize must be greater than 0");
						}else{
							lookup.setInitialized();
						}
					}
				}
			}else if(tree.getChild(i).getId().equals("Arith")){
				addArithm(tree.getChild(i), symbolTable);
			}else if(tree.getChild(i).getId().equals("Call")){
				//lookup function name and verify arguments, see if used vars are initialized
				checkCall(tree.getChild(i), symbolTable);
			}else if(tree.getChild(i).getId().equals("Id")){
				//lookup se if var is initialized
				lookup = symbolTable.lookup(tree.getChild(i).getVal());
				//System.out.println(lookup.getName());
				if(lookup == null){
					System.out.println("Variable " + tree.getChild(i).getVal() + " not defined");
				}else if(!lookup.isInitialized()){
					//erro\warning not initialized
					System.out.println("Variable " + tree.getChild(i).getVal() + " not initialized");
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
					System.out.println("Variable " + tree.getChild(i).getVal() + " not defined");
				}else if(!lookup.isInitialized()){
					//erro\warning not initialized
					System.out.println("Variable " + tree.getChild(i).getVal() + " not initialized");
				}
			}else if(tree.getChild(i).getId().equals("Call")){
				checkCall(tree.getChild(i), symbolTable);
			}else if(tree.getChild(i).getId().equals("Arith")){
				addArithm(tree.getChild(i), symbolTable);
			}
		}
	}

	//THIS IS HERE JUST TO MAKE SURE I DIDN'T MISS ANYTHING FROM CHANGES
	/*public void initModuleChildTables(HIRTree moduleNode, Table moduleTable){
		HIRTree[] children = moduleNode.getChildren();
		HIRTree currFunct;
		HIRTree parameters;
		if(children.length > 2){ 
			Table funcTable;
			for(int i = 2; i < children.length; i++){
				currFunct = children[i];
				funcTable = new Table(moduleTable);
				if(currFunct.getChild(0).getId().equals("Return")){
					//FUTURE allow same name but not same parameters
					if(funcTable.lookupFunction(currFunct.getChild(0).getChild(1).getVal()) == null){
						funcTable.insert(currFunct.getChild(0).getChild(1).getVal(), "function name", true);
					}else{
						System.out.println("Function " + currFunct.getChild(0).getChild(1).getVal() +" redefinition");
						funcTable.insert(currFunct.getChild(0).getChild(1).getVal(), "function name", false);
					}

					if(funcTable.lookup(currFunct.getChild(0).getChild(0).getVal()) == null){
						funcTable.insert(currFunct.getChild(0).getChild(0).getVal(), "return", true);
					}else{
						System.out.println("Return variable " + currFunct.getChild(0).getChild(0).getVal() + " already existis in global context");
						funcTable.insert(currFunct.getChild(0).getChild(0).getVal(), "return", false);
					}
				}else{
					//FUTURE allow same name but not same parameters
					if(funcTable.lookupFunction(currFunct.getChild(0).getVal()) == null){
						funcTable.insert(currFunct.getChild(0).getVal(), "function name", true);
					}else{
						System.out.println("Function " + currFunct.getChild(0).getVal() +" redefinition");
						funcTable.insert(currFunct.getChild(0).getVal(), "function name", false);
					}
				}
				if(currFunct.getChildren().length > 1){
					if(currFunct.getChild(1).getId().equals("Parameters")){
						parameters = currFunct.getChild(1);
						for(HIRTree param : parameters.getChildren()){
							if(param.getId().equals("Array")){
								funcTable.insert(param.getVal(), "parameter array", true);
							}
							else if(param.getId().equals("Id")){
								funcTable.insert(param.getVal(), "parameter int", true);
							}
						}
					}
				}
				moduleTable.insertChildTable(funcTable);
			}
		}
	}*/
	
	public void initModuleChildTables(HIRTree moduleNode, Table moduleTable){
		HIRTree[] children = moduleNode.getChildren();
		HIRTree currFunct;
		HIRTree parameters;
		if(children.length > 2){ 
			Table funcTable;
			for(int i = 2; i < children.length; i++){
				currFunct = children[i];
				funcTable = new Table(moduleTable);
				if(currFunct.getChild(0).getId().equals("Return")){
					//FUTURE allow same name but not same parameters
					if(funcTable.lookupFunction(currFunct.getChild(0).getChild(1).getVal()) == null){
						funcTable.insert(currFunct.getChild(0).getChild(1).getVal(), "function name", true);
					}else{
						System.out.println("Function " + currFunct.getChild(0).getChild(1).getVal() +" redefinition");
						funcTable.insert(currFunct.getChild(0).getChild(1).getVal(), "function name", false);
					}

					if(funcTable.lookup(currFunct.getChild(0).getChild(0).getVal()) == null){
						funcTable.insert(currFunct.getChild(0).getChild(0).getVal(), "return", true);
					}else{
						System.out.println("Return variable " + currFunct.getChild(0).getChild(0).getVal() + " already existis in global context");
						funcTable.insert(currFunct.getChild(0).getChild(0).getVal(), "return", false);
					}
				}else{
					//FUTURE allow same name but not same parameters
					if(funcTable.lookupFunction(currFunct.getChild(0).getVal()) == null){
						funcTable.insert(currFunct.getChild(0).getVal(), "function name", true);
					}else{
						System.out.println("Function " + currFunct.getChild(0).getVal() +" redefinition");
						funcTable.insert(currFunct.getChild(0).getVal(), "function name", false);
					}
				}
				if(currFunct.getChildren().length > 1){
					if(currFunct.getChild(1).getId().equals("Parameters")){
						parameters = currFunct.getChild(1);
						for(HIRTree param : parameters.getChildren()){
							if(param.getId().equals("Array")){
								funcTable.insert(param.getVal(), "parameter array", true);
							}
							else if(param.getId().equals("Id")){
								funcTable.insert(param.getVal(), "parameter int", true);
							}
						}
					}
				}
				moduleTable.insertChildTable(funcTable);
			}
		}
	}

	public void checkCall(HIRTree tree, Table symbolTable){
		// io.println
		if(tree.getChild(0).equals("io") && tree.getChild(1).equals("println")){
			if(tree.getChild(3).equals("ArgumentList")){
				HIRNode[] children = tree.getChildren();
				for(int i=0; i<children.length;i++){
					if (symbolTable.lookup(children[i].getId())!= null){
						System.out.println("Print Variavel: " + children[i].getId() );
					}
					else{
						System.out.println(" Variavel: " + children[i].getId() +" Nao existe (nao pode fazer print) ");
					}
				}
			}
			
		}
		
		//

	}
	
	public Table getTable(){
		return this.symbolTable;
	}
}