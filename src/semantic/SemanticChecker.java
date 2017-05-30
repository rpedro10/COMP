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
    				//{/*Alternativa ao code anterior, guardar decls na symbol list do module. Mais fácil no lookup*/}
    				
    				/*for(int j = 0; j < child.getChildren().length; j++){
    					if(child.getChild(j).getId().equals("Assign"))
    						addAssign(child.getChild(j), this.symbolTable);
    					else
    					{
    						if(this.symbolTable.lookup(child.getChild(j).getVal())==null)
    						{
    							System.out.println("null lookup on val: " + child.getChild(j).getVal());
    							if(child.getChild(j).getId().equals("Id"))
    								this.symbolTable.insert(child.getChild(j).getVal(), "int", false); //Push int
    							else
    								this.symbolTable.insert(child.getChild(j).getChild(0).getVal(), "array", false); //Push Array
    						}else{
    							//supostamente error var; again
    							System.out.println("Redeclaration");
    						}
    					}
    				}*/

    				for(int k=0 ; k<child.getChildren().length ; k++){
						if(child.getChild(k).getId().equals("Assign")){
							System.out.println("Assign...");
							addAssign(child.getChild(k),this.symbolTable);
						}else{
							System.out.println("Starting to look for " + child.getChild(k).getVal());
							Symbol lookup = this.symbolTable.lookup(child.getChild(k).getVal());
							if( lookup == null){
								System.out.println("null lookup on " + child.getChild(k).getVal());
								if(child.getChild(k).getId().equals("Id")){
									this.symbolTable.insert(child.getChild(k).getVal(), "int", false);
								}else if(child.getChild(k).getId().equals("Array")){
									this.symbolTable.insert(child.getChild(k).getVal(), "array", false);
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
		//return erros; //em vez de ^^^
	}

	public void addAssign(HIRTree tree, Table symbolTable){
		Symbol lookup;
		for(int i=0; i<tree.getChildren().length ; i++){
			if(i==0){
				if(tree.getChild(i).getId().equals("Id")){
					lookup = symbolTable.lookup(tree.getChild(i).getVal());
					System.out.println("null assign lookup on " + tree.getChild(i).getVal());
					if( lookup == null){
						if(tree.getChild(i + 1).getId().equals("ArraySize")){
							if(Integer.parseInt(tree.getChild(i+1).getChild(0).getVal())>0){
								symbolTable.insert(tree.getChild(i).getVal(), "array", false);
								System.out.println("Define " + tree.getChild(i).getVal());
							}else
								System.out.println("ArraySize must be greater than 0");
						}else if(tree.getChild(i + 1).getId().equals("Integer")){
							symbolTable.insert(tree.getChild(i).getVal(), "int", true);
							System.out.println("Initialize " + tree.getChild(i).getVal());
						}
					}else{
						if(tree.getChild(i + 1).getId().equals("ArraySize")){
							if(Integer.parseInt(tree.getChild(i+1).getChild(0).getVal())<=0)
								System.out.println("ArraySize must be greater than 0");
						}else
							lookup.setInitialized();
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
		HIRTree parameters;
		if(children.length > 2){ 
			Table funcTable;
			for(int i = 2; i < children.length; i++){
				currFunct = children[i];
				funcTable = new Table(moduleTable);
				if(currFunct.getChild(0).getId().equals("Return")){
					funcTable.insert(currFunct.getChild(0).getChild(1).getVal(), "function name", true);
					funcTable.insert(currFunct.getChild(0).getChild(0).getVal(), "return", false);
				}else{
					funcTable.insert(currFunct.getChild(0).getVal(), "function name", true);
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
				System.out.println("Table inserted");
			}
		}
	}

	public void checkCall(HIRTree tree, Table symbolTable){
		//lookup function name and verify arguments, see if used vars are initialized
	}
	
	public Table getTable(){
		return this.symbolTable;
	}
}