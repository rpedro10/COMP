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
				if(ast.getChildren().length > 2){ //Verificar se existem funções
					for(int i = 2; i < ast.getChildren().length; i++){
						
					}
				}
				break;
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
		jvm.append(gvars + arrayIniters);
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
}
