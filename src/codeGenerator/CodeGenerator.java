package codeGenerator;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
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
				System.out.println("\n===CODE GENERATION===");
				symbolTable.dump("");
				setModuleHeader(st.getVariables().get(0).getName(), jvm);
				if(st.getVariables().size() > 1) /*Se tiver globais definir agora*/
					setGlobals(st.getVariables(), ast.getChild(1), jvm, st.getSymbol(0).getName()); 
				jvm.append("\n");
				if(ast.getChildren().length > 2){ //Verificar se existem funções
					for(int i = 2; i < ast.getChildren().length; i++){
						translator(jvm, ast.getChild(i), st.getChild(i-2));
					}
				}
				jvm.append(this.arrayIniters);
				writeJasminFile(jvm, st.getSymbol(0).getName());
				break;
			case "Function":
				/*Obter posições das variaveis para alocar a stack e saber onde fica o retorno*/
				boolean isVoid = isVoid(st);
				int paramOffset = getParamStart(st, isVoid), localOffset, retIndex = 0;
				//localOffset = paramOffset == 0 ? 0 : getLocalStart(st, paramOffset) - paramOffset;
				localOffset = getLocalStart(st, paramOffset);
				if(!isVoid)
					retIndex = getReturnVarIndex(st, paramOffset);
				/*Inicializar nome de função e stack*/
				setFunctionHeader(jvm, st, paramOffset, isVoid);
				limitStack(jvm,ast);
				int numLocals = st.getVariables().size() - getLocalStart(st, paramOffset);
				if(numLocals > 1){
					jvm.append(".limit locals "+numLocals+"\n");
				}
				/*Gerar código*/
				int i = paramOffset == 0 ? 1 : 2;
				while( i < ast.getChildren().length){
					HIRTree operation = ast.getChild(i);
					switch (operation.getId()){
					case "Call":
						genCallCode(jvm, st, operation, paramOffset, localOffset);
						break;
					case "Assign":
						genAssignCode(jvm, st, operation, paramOffset, localOffset);
					}
					i++;
				}
				/*Colocar valores de retorno*/
				if(isVoid)
					jvm.append("return\n.end method\n");
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
	public void setGlobals(ArrayList<Symbol> vars, HIRTree tree,StringBuilder jvm, String modName){
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
					addInitArray(s.getName(), tree.getChild(i - 1).getChild(1).getChild(0).getVal(), modName);
				}
			}
		}
		jvm.append(gvars);
	}
	
	/*Create array init functions*/
	public void addInitArray(String var, String val, String mName){
		arrayIniters = arrayIniters +
				".method static public <clinit>()V\n"+
				".limit stack 2\n"+
				".limit locals 0\n"+
				"bipush "+val+"\n"+
				"newarray int\n"+
				"putstatic "+mName+"/" + var + " [I\n"+
				"return\n"+
				".end method\n";
	}
	
	/*Inicializar função*/
	public void setFunctionHeader(StringBuilder jvm, Table st, int paramStart, boolean isVoid){
		jvm.append(".method public static ");
		if(st.getSymbol(0).getName().equals("main")) //Is this function an entry point?
		{
			jvm.append("main([Ljava/lang/String;)V\n");
		}
		else{
			jvm.append(st.getSymbol(0).getName()+"(");
			String[] splits;
			if(paramStart > 0){
				for(int i = paramStart; i < st.getVariables().size(); i++){
					splits = st.getSymbol(i).getType().split(" ");
					if(splits[0].equals("parameter")){
						if(splits[1].equals("int"))
							jvm.append("I");
						else
							jvm.append("[I");
					}
					else
						break;
				}
			}
			if(isVoid)
				jvm.append(")V\n");
			else
				jvm.append(")I\n");
		}	
	}
	
	public void limitStack(StringBuilder jvm, HIRTree ast){
		int max = 0, curr = 0;
		for(HIRTree op : ast.getChildren()){
			if(op.getId().equals("Assign")){
				if(op.getChild(0).getId().equals("Array"))
					curr = 2;
				if(op.getChild(1).getId().equals("Arith")){
					HIRTree child = op.getChild(1);
					if(child.getChild(0).getId().equals("Array") && child.getChild(1).getId().equals("Array"))
						curr += 3;
					else
						curr += 2;
				}else{
					if(op.getChild(1).getId().equals("ArraySize")){
						curr += 2;
					}
					else{
						curr++;
					}
				}
			}
			else if(op.getId().equals("Call")){
				curr = op.getChild(op.getChildren().length - 1).getChildren().length;
			}
			if(curr > max)
				max = curr;
			curr = 0;
		}
		if( max > 0)
			jvm.append(".limit stack "+max+"\n");
		else
			jvm.append(".limit stack 1\n");
	}
	
	public boolean isVoid(Table st){
		try{
			if(st.getSymbol(1).getType().equals("return"))
				return false;
			else
				return true;
			}
		catch(IndexOutOfBoundsException e){
			return true;
		}
	}
	
	public int getParamStart(Table st, boolean isVoid){
		if(!isVoid){
			if(st.getSymbol(2).getType().split(" ")[0].equals("parameter"))
				return 2;
			else
				return 0;
		}
		else{ 
			try{
				if(st.getSymbol(1).getType().split(" ")[0].equals("parameter")){
						return 1;
				}
				else
					return 0;
			}
			catch(IndexOutOfBoundsException e){
				return 0;
			}
		}
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
	
	public void genCallCode(StringBuilder jvm, Table st, HIRTree node, int params, int locals){
		if(node.getChild(0).getVal().equals("io")){ //Is it an IO operation?
			if(node.getChild(1).getVal().equals("print") || node.getChild(1).getVal().equals("println")){ //Is it outputting data?
				HIRTree arguments = node.getChild(2);
				String argBuffer = "";
				for(HIRTree arg : arguments.getChildren()){
					switch (arg.getId()){
					case "String":
						jvm.append("ldc "+arg.getVal()+"\n");
						argBuffer = argBuffer + "Ljava/lang/String;";
						break;
					case "Id":
						argBuffer = argBuffer + "I;";
						Symbol s = st.lookup(arg.getVal());
						int ssPosition = st.getVariables().indexOf(s) - (params + locals);
						jvm.append("iload_"+ssPosition+"\n");
						break;
					}
				}
				if(argBuffer.split(";").length > 1){
					argBuffer = argBuffer.substring(0, argBuffer.lastIndexOf(';'));
				}
				jvm.append("invokestatic io/"+node.getChild(1).getVal()+"("+argBuffer+")V\n");
			}
		}
	}
	
	public void genAssignCode(StringBuilder jvm, Table st, HIRTree node, int params, int locals){
		String S1 = "", S2 = "";
		if(node.getChild(0).getId().equals("Id")){
			Symbol s = st.lookup(node.getChild(0).getVal());
			int position = st.getVariables().indexOf(s) - (params + locals);
			if(s.getType().equals("array")){
				S2 = "astore_"+position+"\n";
				String arraysize = node.getChild(1).getChild(0).getVal();
				if(node.getChild(1).getChild(0).getId().equals("Integer")){
					int numInt = Integer.parseInt(arraysize);
					if(numInt > 5)
						S1 = "bipush " + numInt+"\n";
					else
						S1 = "iconst_"+numInt+"\n";
				}else{
					Symbol ss = st.lookup(arraysize);
					int ssPosition = st.getVariables().indexOf(ss) - (params + locals);
					S1 = "iload_"+ssPosition+"\n";
				}
				S1 = S1 + "newarray int\n";
				jvm.append(S1+S2);
			}
			else{
				S2 = "istore_"+position+"\n";
				S1 = genRHSCode(st, node.getChild(1), params + locals);
				jvm.append(S1 + S2);
			}
		}
		else{
			Symbol s = st.lookup(node.getChild(0).getChild(0).getVal());
			int position = st.getVariables().indexOf(s) - (params + locals);
			S1 = "aload_"+position+"\n";
			String val = node.getChild(0).getChild(1).getVal();
			try{
				int numInt = Integer.parseInt(val);
				if(numInt > 5)
					S1 = S1 + "bipush " + numInt+"\n";
				else
					S1 = S1+ "iconst_"+numInt+"\n";
			}catch (NumberFormatException e){
				Symbol ss = st.lookup(val);
				int ssPosition = st.getVariables().indexOf(ss) - (params + locals);
				S1 = S1 + "iload_"+ssPosition+"\n";
			}
			S2 = "iastore\n";
			S1 = S1 + genRHSCode(st, node.getChild(1), params + locals);
			jvm.append(S1 + S2);
		}
	}
	
	public String genRHSCode(Table st, HIRTree node,  int offset){
		String code = "";
		if(node.getId().equals("Id")){
			String val = node.getVal();
			Symbol s = st.lookup(val);
			int sp = st.getVariables().indexOf(s) - offset;
			code = "iload_"+sp+"\n";
		} else if(node.getId().equals("Integer")){
			String val = node.getVal();
			int numVal = Integer.parseInt(val);
			if(numVal > 5)
				code = "bipush "+numVal+"\n";
			else
				code = "iconst_"+numVal+"\n";
		}
		else if(node.getId().equals("Array")){
			Symbol s = st.lookup(node.getChild(0).getVal());
			int position = st.getVariables().indexOf(s) - offset;
			code = "iaload\n";
			String val = node.getChild(1).getVal();
			try{
				int numInt = Integer.parseInt(val);
				if(numInt > 5)
					code = "bipush " + numInt+"\n"+code;
				else
					code = "iconst_"+numInt+"\n" + code;
			}catch (NumberFormatException e){
				Symbol ss = st.lookup(val);
				int ssPosition = st.getVariables().indexOf(ss) - offset;
				code = "iload_"+ssPosition+"\n" + code;
			}
			code = "aload_"+position+"\n"+code;
		}
		else{
			switch (node.getVal()){
				case "+":
					code = "iadd\n";
					break;
				case "-":
					code = "isub\n";
					break;
				case "*":
					code = "imul\n";
					break;
				case "/":
					code = "idiv\n";
					break;
			}
			for(HIRTree arg : node.getChildren()){
				String val = arg.getVal();
				if(arg.getId().equals("Id")){
					Symbol s = st.lookup(val);
					int sp = st.getVariables().indexOf(s) - offset;
					code = "iload_"+sp+"\n" + code;
				}else if(arg.getId().equals("Integer")){
					int numVal = Integer.parseInt(val);
					if(numVal > 5)
						code = "bipush "+numVal+"\n" + code;
					else
						code = "iconst_"+numVal+"\n" + code;
				}
				else{
					code = "iaload\n" + code;
					
					try{
						int numVal = Integer.parseInt(arg.getChild(1).getVal());
						if(numVal > 5)
							code = "bipush "+numVal+"\n" + code;
						else
							code = "iconst_"+numVal+"\n" + code;
					}catch(NumberFormatException e){
						Symbol s = st.lookup(arg.getChild(1).getVal());
						int sp = st.getVariables().indexOf(s) - offset;
						code = "iload_"+sp+"\n" + code;
					}
					
					Symbol s = st.lookup(arg.getChild(0).getVal());
					int sp = st.getVariables().indexOf(s) - offset;
					code = "aload_"+sp+"\n"+code;
					
				}
			}
			
		}
		return code;
	}
	
	public void writeJasminFile(StringBuilder jvm, String className){
		String path = Paths.get("").toAbsolutePath().toString();
		path = path.substring(0, path.lastIndexOf("/"));
		path = path + "/src/testFiles/" + className + ".j";
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(path));
			bw.write(jvm.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
