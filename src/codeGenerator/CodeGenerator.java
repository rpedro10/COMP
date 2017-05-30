package codeGenerator;
import java.util.ArrayList;

import semantic.HIRTree;
import semantic.Symbol;
import semantic.Table;

public class CodeGenerator {
	protected HIRTree tree;
	protected Table symbolTable;
	protected String arrayIniters;
	
	public CodeGenerator(HIRTree tree, Table symbolTable){
		this.tree = tree;
		this.symbolTable = symbolTable;
		this.arrayIniters = "\n";
	}
	
	public void generateCode(){
		StringBuilder codeHolder = new StringBuilder("");
		translator(codeHolder, tree, symbolTable);
		System.out.print(codeHolder);
	}
	
	public void translator(StringBuilder jvm, HIRTree ast, Table st){
		switch (ast.getId()){
			case "Module":
				setModuleHeader(st.getVariables().get(0).getName(), jvm);
				if(st.getVariables().size() > 1) /*Se tiver globais definir agora*/
					setGlobals(st.getVariables(), ast.getChild(1), jvm); 
				jvm.append("\n");
				if(ast.getChildren().length > 2){ //Verificar se existem funções
					for(int i = 2; i < ast.getChildren().length; i++){
						translator(jvm, ast.getChild(i), st.getChild(i-2));
					}
				}
				jvm.append(this.arrayIniters);
				break;
			case "Function":
				/*Obter posições das variaveis para alocar a stack e saber onde fica o retorno*/
				boolean isVoid = isVoid(st);
				int paramOffset = getParamStart(st, isVoid), localOffset, retIndex = 0;
				localOffset = paramOffset == 0 ? 0 : getLocalStart(st, paramOffset) - paramOffset;
				if(!isVoid)
					retIndex = getReturnVarIndex(st, paramOffset);
				/*DEBUG*/
				System.out.println("");
				for(Symbol s : st.getVariables()){
					System.out.println(s.getType() + " : " + s.getName());
				}
				System.out.println("");
				/*Inicializar nome de função e stack*/
				setFunctionHeader(jvm, st);
				/*Colocar valores de retorno*/
				if(isVoid)
					jvm.append(" return\n.end method\n");
				else
					jvm.append(" iload "+retIndex+"\n ireturn\n.end method\n");
		}
	}
	
	/*Iniciar os imports e nome do modulo*/
	public void setModuleHeader(String name, StringBuilder jvm){
		String c;
		c = ".class public " + name + "\n";
		c = c + ".super java/lang/Object\n";
		jvm.append(c);
	}
	
	/*Definir globais*/
	public void setGlobals(ArrayList<Symbol> vars, HIRTree tree,StringBuilder jvm){
		String gvars = "";
		Symbol s;
		for(int i = 1; i < vars.size(); i++){
			s = vars.get(i);
			if(s.getType() == "int"){
				gvars = gvars + ".field static " + s.getName() + " I";
				if(tree.getChild(i - 1).getId().equals("Assign"))
					gvars = gvars + " = " + tree.getChild(i - 1).getChild(1).getVal() +"\n"; //Atribuir valor de constantes
				else
					gvars = gvars + "\n";
			}else if(s.getType() == "array"){
				gvars = gvars + ".field static " + s.getName() + " [I\n";
				if(tree.getChild(i - 1).getChild(1).getId().equals("ArraySize")){
					addInitArray(s.getName(), tree.getChild(i - 1).getChild(1).getChild(0).getVal());
				}
			}
		}
		jvm.append(gvars);
	}
	
	/*Create array init functions*/
	public void addInitArray(String var, String val){
		arrayIniters = arrayIniters +
				"method static public <clinit>()V\n"+
				" .limit stack 2\n"+
				" .limit locals 0\n"+
				" bipush "+val+"\n"+
				" newarray int\n"+
				" putstatic fields1/" + var + " [I\n"+
				" return\n"+
				".end method\n";
	}
	
	/*Inicializar funnção*/
	public void setFunctionHeader(StringBuilder jvm, Table ast){
		jvm.append(
				".method public static\n"+ast.getSymbol(0).getName()+"([Ljava/lang/String;)V\n"
				);
	}
	
	public boolean isVoid(Table st){
		if(st.getSymbol(1).getType().equals("return"))
			return false;
		else
			return true;
	}
	
	public int getParamStart(Table st, boolean isVoid){
		if(!isVoid){
			if(st.getSymbol(2).getType().split(" ")[0].equals("parameter"))
				return 2;
			else
				return 0;
		}
		else if(st.getSymbol(1).getType().split(" ")[0].equals("parameter")){
				return 1;
		}
		else
			return 0;
	}
	
	public int getLocalStart(Table st, int paramStart){
		int i = paramStart;
		while(i < st.getVariables().size() && !st.getSymbol(i).getType().equals("int") && !st.getSymbol(i).getType().equals("array")){
			i++;
		}
		if( i == st.getVariables().size())
			return 0;
		else
			return i;
	}
	
	public int getReturnVarIndex(Table st, int localStart){
		String ret = st.getSymbol(1).getName();
		int i = localStart;
		while(st.getSymbol(i).getName().equals(ret)){
			i++;
		}
		return i;
	}
}
