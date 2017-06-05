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
							addAssign(child.getChild(k),this.symbolTable);
						}else{
							HIRTree subChild = child.getChild(k);
							Symbol lookup;
							if(subChild.getId().equals("Array"))
								lookup = this.symbolTable.lookup(subChild.getChild(0).getVal());
							else
								lookup = this.symbolTable.lookup(subChild.getVal());
							if( lookup == null){
								if(child.getChild(k).getId().equals("Id")){
									this.symbolTable.insert(child.getChild(k).getVal(), "int", false);
								}else if(child.getChild(k).getId().equals("Array")){
									this.symbolTable.insert(subChild.getChild(0).getVal(), "array", false);
								}
							}else{
								//retornar erro "var;" again
								// var linha msg
								Error rr = new Error(child.getChild(k).getVal(),child.getChild(k).getLine(),"DeclarationList error, redeclaration of: ");
								error_list.add(rr);
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
    				symbolTable.insertChildTable(ifTable);
    				//Criar nova tabela e preencher e depois adicionar a tabela function
    			}else if(child.getId().equals("Else")) {
    				Table elseTable = new Table(symbolTable);
    				runSemanticCheck(child,elseTable);
    				symbolTable.insertChildTable(elseTable);
    			}else if(child.getId().equals("While")){
    				Table whileTable = new Table(symbolTable);
    				runSemanticCheck(child,whileTable);
    				symbolTable.insertChildTable(whileTable);
    				//Criar nova tabela e preencher e depois adicionar a tabela function
    			}else if(child.getId().equals("Call")){
    				//isto pode estar aqui, ou é só no assign?
    				checkCall(child, symbolTable);
    			}
    		}
    		if(!symbolTable.getSymbol(1).isInitialized()){
    			Error rr = new Error(symbolTable.getSymbol(0).getName(),hr.getChild(0).getLine(), "return variable must be initialized in function ");
    			error_list.add(rr);
    		}
    		break;
    	case "If":
    	case "Else":
    		for(int i=0; i < hr.getChildren().length; i++){
    			child=hr.getChild(i);
	    		if(child.getId().equals("Exprtest")){
	    			for(HIRTree subChild : child.getChildren()){
		    			if(subChild.getId().equals("Id")){
		    				Symbol lookup = symbolTable.lookup(subChild.getVal());
							if(lookup == null){
								Error rr = new Error(subChild.getVal(),subChild.getLine(),"Variable not defined: ");
								error_list.add(rr);
							}else if(!lookup.isInitialized()){
								//erro\warning not initialized
								Error rr = new Error(subChild.getVal(),subChild.getLine(),"Variable not intialized: ");
								error_list.add(rr);
							}
		    			}if(subChild.getId().equals("Array")){
		    				Symbol lookup = symbolTable.lookup(subChild.getChild(0).getVal());
							if(lookup == null){
								Error rr = new Error(subChild.getVal(),subChild.getLine(),"Variable not defined: ");
								error_list.add(rr);
							}else if(!lookup.isInitialized()){
								//erro\warning not initialized
								Error rr = new Error(subChild.getChild(0).getVal(),subChild.getChild(0).getLine()," Variable not initialized: ");
								error_list.add(rr);
							}
		    			}else if(subChild.getId().equals("Arith")){
		    				addArithm(subChild, symbolTable);
		    			}
		    		}
	    		if(child.getId().equals("Assign")){
    				addAssign(child, symbolTable);
    			}else if(child.getId().equals("If")){
    				Table ifTable = new Table(symbolTable);
    				runSemanticCheck(child,ifTable);
    				symbolTable.insertChildTable(ifTable);
    				//Criar nova tabela e preencher e depois adicionar a tabela function
    			}else if(child.getId().equals("Else")) {
    				Table elseTable = new Table(symbolTable);
    				runSemanticCheck(child,elseTable);
    				symbolTable.insertChildTable(elseTable);
    			}else if(child.getId().equals("While")){
    				Table whileTable = new Table(symbolTable);
    				runSemanticCheck(child,whileTable);
    				symbolTable.insertChildTable(whileTable);
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
	    			for(HIRTree subChild : child.getChildren()){
		    			if(subChild.getId().equals("Id")){
		    				Symbol lookup = symbolTable.lookup(subChild.getVal());
							if(lookup == null){
								Error rr = new Error(subChild.getVal(),subChild.getLine(),"Variable not defined: ");
								error_list.add(rr);
							}else if(!lookup.isInitialized()){
								//erro\warning not initialized
								Error rr = new Error(subChild.getVal(),subChild.getLine(),"Variable not initialized: ");
								error_list.add(rr);
							}
		    			}else if(subChild.getId().equals("Array")){
		    				Symbol lookup = symbolTable.lookup(subChild.getChild(0).getVal());
							if(lookup == null){
								Error rr = new Error(subChild.getChild(0).getVal(),subChild.getChild(0).getLine(),"Variable not defined: ");
								error_list.add(rr);
							}else if(!lookup.isInitialized()){
								//erro\warning not initialized
								Error rr = new Error(subChild.getChild(0).getVal(),subChild.getChild(0).getLine()," WARNING: Variable not INITIALIZED");
								error_list.add(rr);
							}
		    			}else if(subChild.getId().equals("Arith")){
		    				addArithm(subChild, symbolTable);
		    			}
		    		}
	    		if(child.getId().equals("Assign")){
    				addAssign(child, symbolTable);
    			}else if(child.getId().equals("If")){
    				Table ifTable = new Table(symbolTable);
    				runSemanticCheck(child,ifTable);
    				symbolTable.insertChildTable(ifTable);
    				//Criar nova tabela e preencher e depois adicionar a tabela function
    			}else if(child.getId().equals("Else")) {
    				Table elseTable = new Table(symbolTable);
    				runSemanticCheck(child,elseTable);
    				symbolTable.insertChildTable(elseTable);
    			}else if(child.getId().equals("While")){
    				Table whileTable = new Table(symbolTable);
    				runSemanticCheck(child,whileTable);
    				symbolTable.insertChildTable(whileTable);
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
					if( lookup == null){
						if(tree.getChild(i + 1).getId().equals("ArraySize")){
							try{
								if(Integer.parseInt(tree.getChild(i+1).getChild(0).getVal())>0){
									symbolTable.insert(tree.getChild(i).getVal(), "array", false);
								}else{
									Error rr = new Error(tree.getChild(i).getVal(),tree.getChild(i).getLine(),"ArraySize must be greater than 0: ");
									error_list.add(rr);
								}
							}
							catch(NumberFormatException e){
								symbolTable.insert(tree.getChild(i).getVal(), "array", false);
							}
						}else if(tree.getChild(i + 1).getId().equals("Integer")){
							symbolTable.insert(tree.getChild(i).getVal(), "int", true);
						}else if(tree.getChild(i + 1).getId().equals("Call")){
							//if function, check function return is int or array
							symbolTable.insert(tree.getChild(i).getVal(), "int", true);
						}else if(tree.getChild(i+1).getId().equals("SizeAccess")){
							symbolTable.insert(tree.getChild(i).getVal(), "int", true); //Para poder trabalhar com x = array.size
						}else if(tree.getChild(i + 1).getId().equals("Id")){
							Symbol lookup2 = symbolTable.lookup(tree.getChild(i+1).getVal());
							if(lookup2 == null){
								Error rr = new Error(tree.getChild(i+1).getVal(),tree.getChild(i+1).getLine(),"Variable is not defined: ");
								error_list.add(rr);
							}else if(lookup2.getType().equals("int") || lookup2.getType().equals("parameter int") || lookup2.getType().equals("return int")) {
								if(lookup2.isInitialized()){
									symbolTable.insert(tree.getChild(i).getVal(), "int", true);	
								}else{
									Error rr = new Error(tree.getChild(i+1).getVal(),tree.getChild(i+1).getLine(),"Variable is not initialized: ");
									error_list.add(rr);
								}
							}else if(lookup2.getType().equals("array") || lookup2.getType().equals("parameter array") || lookup2.getType().equals("return array")) {
								if(lookup2.isInitialized()){
									symbolTable.insert(tree.getChild(i).getVal(), "array", true);	
								}else{
									Error rr = new Error(tree.getChild(i+1).getVal(),tree.getChild(i+1).getLine(),"Variable is not initialized: ");
									error_list.add(rr);
								}
							}
							
						}else if(tree.getChild(i + 1).getId().equals("Arith")){					/*===================*/
							symbolTable.insert(tree.getChild(i).getVal(), "int", true);			//Estas linhas são um//
																								//remendo para que   //
						}else if(tree.getChild(i + 1).getId().equals("Array")){					//codegen funcione   //
							symbolTable.insert(tree.getChild(i).getVal(), "array", true);		//depois vejam melhor//
																								/*==================*/
						}
					}else{
						if(tree.getChild(i + 1).getId().equals("ArraySize")){
							if(!lookup.getType().equals("int") && !lookup.getType().equals("parameter int") && !lookup.getType().equals("return int")){
								if(Integer.parseInt(tree.getChild(i+1).getChild(0).getVal())<=0){
									Error rr = new Error(tree.getChild(i).getVal(),tree.getChild(i).getLine(),"ArraySize must be greater than 0: ");
									error_list.add(rr);
								}
							}else{
								Error rr = new Error(tree.getChild(i).getVal(),tree.getChild(i).getLine(),"Variable is not an array");
								error_list.add(rr);
							}
						}else{
							lookup.setInitialized();
						}
					}
				}else if(tree.getChild(i).getId().equals("Array")){
					lookup = symbolTable.lookup(tree.getChild(i).getChild(0).getVal());
					if(lookup == null){
						//erro not defined;
						Error rr = new Error(tree.getChild(i).getChild(0).getVal(),tree.getChild(i).getLine(),"Variable is not defined: ");
						error_list.add(rr);
					}else if(!lookup.getType().equals("array")  && !lookup.getType().equals("parameter array") && !lookup.getType().equals("return array")){
						Error rr = new Error( tree.getChild(i).getChild(0).getVal(),tree.getChild(i).getChild(0).getLine(),"Variable is not an array: ");
						error_list.add(rr);
					}else if(!lookup.isInitialized()){
						//erro\warning not initialized
						//Error rr = new Error(tree.getChild(i).getChild(0).getVal(),tree.getChild(i).getChild(0).getLine(),"Variable is not initialized: ");
						//error_list.add(rr);
						lookup.setInitialized(); //just for now
					}
				}
			}else if(tree.getChild(i).getId().equals("Array")){
				lookup = symbolTable.lookup(tree.getChild(i).getChild(0).getVal());
				if(lookup == null){
					//erro not defined;
					Error rr = new Error(tree.getChild(i).getChild(0).getVal(),tree.getChild(i).getChild(0).getLine(),"Variable is not defined: ");
					error_list.add(rr);
				}else if(!lookup.getType().equals("array") && !lookup.getType().equals("parameter array") && !lookup.getType().equals("return array")){
					Error rr = new Error(tree.getChild(i).getChild(0).getVal(),tree.getChild(i).getChild(0).getLine(),"Variable is not an array: ");
					error_list.add(rr);
				}else if(!lookup.isInitialized()){
					//erro\warning not initialized
					Error rr = new Error(tree.getChild(i).getChild(0).getVal(),tree.getChild(i).getChild(0).getLine(),"Variable is not initialized: ");
					error_list.add(rr);
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
					Error rr = new Error(tree.getChild(i).getVal(),tree.getChild(i).getLine(),"Variable is not defined: ");
					error_list.add(rr);
				}else if(!lookup.isInitialized()){
					//erro\warning not initialized
					Error rr = new Error(tree.getChild(i).getVal(),tree.getChild(i).getLine(),"Variable is not initialized: ");
					error_list.add(rr);
				}
			}
		}
	}

	public void addArithm(HIRTree tree, Table symbolTable){
		//verificar primeiro elemento e recursividade a direita
		for(int i=0; i<tree.getChildren().length; i++){
			if(tree.getChild(i).getId().equals("Id")){
				Symbol lookup = symbolTable.lookup(tree.getChild(i).getVal());
				if(lookup == null){
					Error rr = new Error(tree.getChild(i).getVal() ,tree.getChild(i).getLine(),"Variable is not defined: ");
					error_list.add(rr);
				}else if(!lookup.isInitialized()){
					//erro\warning not initialized
					Error rr = new Error(tree.getChild(i).getVal(),tree.getChild(i).getLine(),"Variable is not initialized: ");
					error_list.add(rr);
				}
			}else if(tree.getChild(i).getId().equals("Array")){
				Symbol lookup = symbolTable.lookup(tree.getChild(i).getChild(0).getVal());
				if(lookup == null){
					//erro not defined;
					Error rr = new Error(tree.getChild(i).getVal() ,tree.getChild(i).getLine(),"Variable id not defined: ");
					error_list.add(rr);
				}else if(!lookup.getType().equals("array") && !lookup.getType().equals("parameter array") && !lookup.getType().equals("return array")){
					Error rr = new Error(tree.getChild(i).getChild(0).getVal() ,tree.getChild(i).getChild(0).getLine(),"Variable is not an array: ");
					error_list.add(rr);
				}else if(!lookup.isInitialized()){
					//erro\warning not initialized
					Error rr = new Error(tree.getChild(i).getChild(0).getVal(),tree.getChild(i).getChild(0).getLine(),"Variable is not initialized: ");
					error_list.add(rr);
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
						Error rr = new Error(currFunct.getChild(0).getChild(1).getVal(),currFunct.getChild(0).getChild(1).getLine(),"Function redefinition: ");
						error_list.add(rr);
						funcTable.insert(currFunct.getChild(0).getChild(1).getVal(), "function name", false);
					}

					if(funcTable.lookup(currFunct.getChild(0).getChild(0).getVal()) == null){
						if(currFunct.getChild(0).getChild(0).getId().equals("Id"))
							funcTable.insert(currFunct.getChild(0).getChild(0).getVal(), "return int", false);
						else
							funcTable.insert(currFunct.getChild(0).getChild(0).getVal(), "return array", false);
					}else{
						Error rr = new Error(currFunct.getChild(0).getChild(0).getVal(),currFunct.getChild(0).getChild(0).getLine(),"Return variable already exists in global context: ");
						error_list.add(rr);
						funcTable.insert(currFunct.getChild(0).getChild(0).getVal(), "return null", false);
					}
				}else{
					//FUTURE allow same name but not same parameters
					if(funcTable.lookupFunction(currFunct.getChild(0).getVal()) == null){
						funcTable.insert(currFunct.getChild(0).getVal(), "function name", true);
					}else{
						Error rr = new Error(currFunct.getChild(0).getVal(),currFunct.getChild(0).getLine(),"Function redefinition: ");
						error_list.add(rr);
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
		if(tree.getChild(0).equals("io") && (tree.getChild(1).equals("println")||tree.getChild(1).equals("print"))){
			if(tree.getChild(3).equals("ArgumentList")){
				HIRNode[] children = tree.getChild(3).getChildren();
				for(int i=0; i<children.length;i++){
					if (symbolTable.lookup(children[i].getVal())!= null){
						// System.out.println("Print Variavel: " + children[i].getId() );
					}
					else{
						Error rr = new Error(children[i].getVal() ,children[i].getLine(),"Variable not defined: ");
						error_list.add(rr);
						System.out.println(" Variavel: " + children[i].getVal() +" Nao existe (nao pode fazer print) ");
					}
				}
			}
			
		}
		
		//

	}
	
	public Table getTable(){
		return this.symbolTable;
	}
	
	public ArrayList<Error> getError_list() {
		return error_list;
	}


	public void setError_list(ArrayList<Error> error_list) {
		this.error_list = error_list;
	}
}