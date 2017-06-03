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
	protected VarAssign assigs;
	
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
				int paramOffset = getParamStart(st, isVoid), retIndex = 0;
				assigs = new VarAssign(ast, st);
				assigs.subOptimalAssign();
				assigs.dump();
				if(!isVoid)
					retIndex = getReturnVarIndex(st);
				/*Inicializar nome de função e stack*/
				setFunctionHeader(jvm, st, paramOffset, isVoid);
				limitStack(jvm,ast);
				if(assigs.maxAssig > 0){
					jvm.append(".limit locals "+(assigs.maxAssig + 1)+"\n");
				}
				/*Gerar código*/
				int i = paramOffset == 0 ? 1 : 2;
				int tableCounter = 0;
				while( i < ast.getChildren().length){
					HIRTree operation = ast.getChild(i);
					switch (operation.getId()){
					case "Call":
						genCallCode(jvm, st, operation);
						break;
					case "Assign":
						genAssignCode(jvm, st, operation);
						break;
					case "If":
						try{
							HIRTree elseBlock = ast.getChild(i + 1);
							if(elseBlock.getId().equals("Else")){
								Table ifTbl = st.getChild(tableCounter);
								Table elseTbl = st.getChild(tableCounter + 1);
								genIfElseCode(jvm, ifTbl, elseTbl, operation, elseBlock, tableCounter);
								i++;
								tableCounter += 2;
							}
							else{
								Table ifTbl = st.getChild(tableCounter);
								genIfElseCode(jvm, ifTbl, null, operation, null, tableCounter);
								tableCounter++;
							}
						}catch(ArrayIndexOutOfBoundsException e){}
						break;
					}
					i++;
				}
				/*Colocar valores de retorno*/
				if(isVoid)
					jvm.append("return\n.end method\n");
				else
					jvm.append("iload "+retIndex+"\nireturn\n.end method\n");
		}
	}
	
	public void genIfElseCode(StringBuilder jvm, Table ifTbl, Table elseTbl, HIRTree ifOp, HIRTree elseOp, int label){
	
		StringBuilder jmp = new StringBuilder("");
		for(HIRTree comp : ifOp.getChild(0).getChildren()){
			if(comp.getId().equals("Integer")){
				int numInt = Integer.parseInt(comp.getVal());
				if(numInt > 5)
					jmp.append("bipush " + numInt+"\n");
				else
					jmp.append("iconst_"+numInt+"\n");
			}else{
				jmp.append(varLoad(comp.getVal(), ifTbl));
			}
		}
		jmp.append("isub\n");
		//if (5 > 2) --> 5 - 2 > 0 ? ---> iload5 iload 2 isub
		switch (ifOp.getChild(0).getVal()){
			case ">":
				jmp.append("ifle ");
				break;
			case ">=":
				jmp.append("iflt ");
				break;
			case "<":
				jmp.append("ifge ");
				break;
			case "<=":
				jmp.append("ifgt ");
				break;
			case "==":
				jmp.append("ifne ");
				break;
			case "!=":
				jmp.append("ifeq ");
				break;
		}
		jmp.append("label"+label+"\n");
		int tblcounter = 0, i = 1;
		while(i < ifOp.getChildren().length){
			HIRTree operation = ifOp.getChild(i);
			switch (operation.getId()){
			case "Call":
				genCallCode(jmp, ifTbl, operation);
				break;
			case "Assign":
				genAssignCode(jmp, ifTbl, operation);
				break;
			case "If":
				try{
					HIRTree elseBlock2 = ifOp.getChild(i + 1);
					if(elseBlock2.getId().equals("Else")){
						Table ifTbl2 = ifTbl.getChild(tblcounter);
						Table elseTbl2 = ifTbl.getChild(tblcounter + 1);
						genIfElseCode(jmp, ifTbl2, elseTbl2, operation, elseBlock2, (tblcounter+1)*10);
						i++;
						tblcounter += 2;
					}
					else{
						Table ifTbl2 = ifTbl.getChild(tblcounter);
						genIfElseCode(jmp, ifTbl2, null, operation, null, (tblcounter+1)*10);
						tblcounter++;
					}
				}catch(ArrayIndexOutOfBoundsException e){}
				break;
			}
			i++;
		}
		if(elseTbl != null){
			jmp.append("goto label"+(label + 1)+"\nlabel"+label+":\n");
			tblcounter = 0; 
			i = 0;
			while(i < elseOp.getChildren().length){
				HIRTree operation = elseOp.getChild(i);
				switch (operation.getId()){
				case "Call":
					genCallCode(jmp, elseTbl, operation);
					break;
				case "Assign":
					genAssignCode(jmp, elseTbl, operation);
					break;
				case "If":
					try{
						HIRTree elseBlock2 = elseOp.getChild(i + 1);
						if(elseBlock2.getId().equals("Else")){
							Table ifTbl2 = elseTbl.getChild(tblcounter);
							Table elseTbl2 = elseTbl.getChild(tblcounter + 1);
							genIfElseCode(jmp, ifTbl2, elseTbl2, operation, elseBlock2, (tblcounter+2)*10);
							i++;
							tblcounter += 2;
						}
						else{
							Table ifTbl2 = elseTbl.getChild(tblcounter);
							genIfElseCode(jmp, ifTbl2, null, operation, null, (tblcounter+2)*10);
							tblcounter++;
						}
					}catch(ArrayIndexOutOfBoundsException e){}
					break;
				}
				i++;
			}
			jmp.append("label"+(label+1)+":\n");
		}else{
			jmp.append("label"+label+":\n");
		}
		jvm.append(jmp.toString());
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
					if(arrayIniters.equals("\n"))
						arrayIniters = arrayIniters+".method static public <clinit>()V\n.limit stack 2\n.limit locals 0\n";
					addInitArray(s.getName(), tree.getChild(i - 1).getChild(1).getChild(0).getVal(), modName);
				}
			}
		}
		if(!arrayIniters.equals("\n"))
			arrayIniters = arrayIniters + "return\n.end method\n";
		jvm.append(gvars);
	}
	
	/*Create array init functions*/
	public void addInitArray(String var, String val, String mName){
		arrayIniters = arrayIniters +
				"bipush "+val+"\n"+
				"newarray int\n"+
				"putstatic "+mName+"/" + var + " [I\n";
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
				if(op.getChild(op.getChildren().length - 1).getChildren() != null)
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
	
	public int getReturnVarIndex(Table st){
		String ret = st.getSymbol(1).getName();
		return assigs.getStackNumber(ret);
	}
	
	public void genCallCode(StringBuilder jvm, Table st, HIRTree node){
		String argBuffer = "";
		if(node.getChild(0).getVal().equals("io")){ //Is it an IO operation?
			if(node.getChild(1).getVal().equals("print") || node.getChild(1).getVal().equals("println")){ //Is it outputting data?
				HIRTree arguments = node.getChild(2);
				for(HIRTree arg : arguments.getChildren()){
					switch (arg.getId()){
					case "String":
						jvm.append("ldc "+arg.getVal()+"\n");
						argBuffer = argBuffer + "Ljava/lang/String;";
						break;
					case "Id":
						argBuffer = argBuffer + "I";
						jvm.append(varLoad(arg.getVal(), st));
						break;
					}
				}
				/*if(argBuffer.split(";").length > 1){
					argBuffer = argBuffer.substring(0, argBuffer.lastIndexOf(';'));
				}*/
				jvm.append("invokestatic io/"+node.getChild(1).getVal()+"("+argBuffer+")V\n");
			}
		}
		else{
			HIRTree arguments = node.getChild(node.getChildren().length - 1);
			if(arguments.getChildren() != null){
				for(HIRTree arg : arguments.getChildren()){
					argBuffer = argBuffer + "I";
					jvm.append(varLoad(arg.getVal(),st));
				}
			}
			jvm.append("invokestatic "+st.getModuleName()+"/"+node.getChild(0).getVal()+"("+argBuffer+")");
			if(isVoid(st.lookupFunction(node.getChild(0).getVal())))
				jvm.append("V\n");
			else
				jvm.append("I\n");
		}
	}
	
	public String varLoad(String name, Table st){
		Symbol s = st.lookup(name);
		if(st.isGlobal(s))
			return "getstatic " +st.getModuleName()+"/"+s.getName()+" I\n";
		else
		{
			return "iload_"+assigs.getStackNumber(s.getName())+"\n";
		}
	}
	
	public void genAssignCode(StringBuilder jvm, Table st, HIRTree node){
		String S1 = "", S2 = "";
		if(node.getChild(0).getId().equals("Id")){
			Symbol s = st.lookup(node.getChild(0).getVal());
			boolean isGlobal = st.isGlobal(s);
			int position = isGlobal ? 0 : assigs.getStackNumber(s.getName());
			if(s.getType().equals("array")){
				S2 = isGlobal ? "putstatic "+st.getModuleName()+"/"+s.getName()+" [I\n" : "astore_"+position+"\n";
				String arraysize = node.getChild(1).getChild(0).getVal();
				if(node.getChild(1).getChild(0).getId().equals("Integer")){
					int numInt = Integer.parseInt(arraysize);
					if(numInt > 5)
						S1 = "bipush " + numInt+"\n";
					else
						S1 = "iconst_"+numInt+"\n";
				}else{
					S1 = varLoad(arraysize, st);
				}
				S1 = S1 + "newarray int\n";
				jvm.append(S1+S2);
			}
			else{
				if(!matchPattern(jvm,isGlobal, s.getName(), node.getChild(1))){
					S2 = isGlobal ? "putstatic "+st.getModuleName()+"/"+s.getName()+" I\n" : "istore_"+position+"\n";
					S1 = genRHSCode(st, node.getChild(1));
					jvm.append(S1 + S2);
				}
			}
		}
		else{
			
			Symbol s = st.lookup(node.getChild(0).getChild(0).getVal());
			boolean isGlobal = st.isGlobal(s);
			int position = isGlobal ? 0 : assigs.getStackNumber(s.getName());
			S1 = isGlobal ? "getstatic "+st.getModuleName()+"/"+s.getName()+" [I\n" : "aload_"+position+"\n";
			String val = node.getChild(0).getChild(1).getVal();
			try{
				int numInt = Integer.parseInt(val);
				if(numInt > 5)
					S1 = S1 + "bipush " + numInt+"\n";
				else
					S1 = S1+ "iconst_"+numInt+"\n";
			}catch (NumberFormatException e){
				S1 = S1 + varLoad(val, st);
			}
			S2 = "iastore\n";
			S1 = S1 + genRHSCode(st, node.getChild(1));
			jvm.append(S1 + S2);
		}
	}
	
	public boolean matchPattern(StringBuilder jvm, boolean isGlobal, String val, HIRTree op){
		if(isGlobal)
			return false;
		else{
			if(!op.getVal().equals("+") && !op.getVal().equals("-"))
				return false;
			else{
				if(op.getChild(0).getVal().equals(val) && op.getChild(1).getId().equals("Integer")){
					jvm.append("iinc "+assigs.getStackNumber(val)+" ");
					int num = Integer.parseInt(op.getChild(1).getVal());
					if(op.getVal().equals("-"))
						num = -1*num;
					jvm.append(num + "\n");
					return true;
				}
				else{
					return false;
				}
			}
		}
	}
	
	public String genRHSCode(Table st, HIRTree node){
		String code = "";
		if(node.getId().equals("Id")){
			String val = node.getVal();
			code = varLoad(val, st);
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
			boolean isGlobal = st.isGlobal(s);
			int position = isGlobal ? 0 : assigs.getStackNumber(s.getName());
			code = "iaload\n";
			String val = node.getChild(1).getVal();
			try{
				int numInt = Integer.parseInt(val);
				if(numInt > 5)
					code = "bipush " + numInt+"\n"+code;
				else
					code = "iconst_"+numInt+"\n" + code;
			}catch (NumberFormatException e){
				code = varLoad(val, st) + code;
			}
			code = isGlobal ? "getstatic "+st.getModuleName()+"/"+s.getName()+" [I\n" : "aload_"+position+"\n"+code;
		}
		else if(node.getId().equals("Call")){
			StringBuilder temp = new StringBuilder("");
			genCallCode(temp, st, node);
			code = temp.toString();
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
					code = varLoad(val, st) + code;
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
						code = varLoad(arg.getChild(1).getVal(),st) + code;
					}
					
					Symbol s = st.lookup(arg.getChild(0).getVal());
					if(st.isGlobal(s))
						code = "getstatic "+st.getModuleName()+"/"+s.getName()+" [I\n"+code;
					else{
						code = "aload_"+assigs.getStackNumber(s.getName())+"\n"+code;
					}
					
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
