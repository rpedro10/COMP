package codeGenerator;
import java.util.ArrayList;
import semantic.Table;
import semantic.Symbol;
import semantic.HIRTree;
public class VarAssign {
	
	
	
	protected HIRTree function;
	protected ArrayList<StackVarPair> assignments;
	protected Table symbolTable;
	protected int maxAssig;
	
	public VarAssign(HIRTree functionTree, Table functionTable){
		function = functionTree;
		symbolTable = functionTable;
		assignments = new ArrayList <StackVarPair>();
		maxAssig = 0;
	}
	
	public void optimalAssign(){
		subOptimalAssign();
		for(StackVarPair a : assignments){
			a.lifetime = new int[2];
			a.lifetime[0] = -1;
			a.lifetime[1] = -1;
			determineLifetime(a, function, 0);
			int end = determineEndOfLife(a, function, 0);
			if(a.lifetime[1] == -1)a.lifetime[1] = end;
		}
	}
	
	public boolean findVar(HIRTree node, String name){
		for(HIRTree c : node.getChildren()){
			if(c.getChildren() == null){
				if (c.getVal().equals(name))
					return true;
			}
			else{
				if(findVar(c, name))
					return true;
			}
		}
		return false;
	}
	
	public int determineEndOfLife(StackVarPair svp, HIRTree t, int i){
		for(HIRTree op : t.getChildren()){
			if(op.getChildren() != null){
				switch (op.getId()){
				case "Exprtest":
				case "Assign":
					if(op.getChild(1).getChildren() == null){
						if(op.getChild(1).getVal().equals(svp.getVar())){
							svp.lifetime[1] = i;
						}
					}
					else{
						if(findVar(op.getChild(1), svp.getVar())){
							svp.lifetime[1] = i;
						}
					}
					i++;
					break;
				case "If":
				case "While":
				case "Else":
					i = determineEndOfLife(svp, op, i);
					break;
				}
			}		
		}
		return i;
	}
	
	public int determineLifetime(StackVarPair svp, HIRTree t, int i){
		for(HIRTree op : t.getChildren()){
			if(op.getChildren() != null){
				switch (op.getId()){
				case "Assign":
					if(op.getChild(0).getVal().equals(svp.getVar())){
						svp.lifetime[0] = i;
						return i;
					}
					i++;
					break;
				case "If":
				case "While":
				case "Else":
					i = determineLifetime(svp, op, i);
					if(svp.lifetime[0] > -1)
						return i;
					break;
			}
		}
			
		}
		return i;
	}
	
	public void subOptimalAssign(){
		int i = 0;
		String retBuffer = null;
		for(Symbol s : symbolTable.getVariables()){
			switch (s.getType()){
				case "return int":
				case "return array":
					retBuffer = s.getName();
					break;
				case "parameter int":
				case "parameter array":
					assignments.add(new StackVarPair(s.getName(),i,false));
					i++;
					break;
				case "int":
				case "array":
					if(retBuffer != null){
						assignments.add(new StackVarPair(retBuffer,i,false));
						i++;
						retBuffer = null;
					}
					assignments.add(new StackVarPair(s.getName(),i,false));
					i++;
					break;
				default:
					break;
			}
		}
		if(retBuffer != null){
			assignments.add(new StackVarPair(retBuffer,i,false));
			i++;
			retBuffer = null;
		}
		maxAssig = i - 1;
		if(symbolTable.getChildTables().size() > 0)
		{
			for(Table t : symbolTable.getChildTables()){
				subBlockAssign(t, i);
			}
		}
	}
	
	public void dump(){
		for(StackVarPair svt : assignments)
			System.out.println(svt.getVar() + " => " + svt.getStackPostion());
	}
	
	public void subBlockAssign(Table t, int i){
		for(Symbol s : t.getVariables()){
			assignments.add(new StackVarPair(s.getName(),i,false));
			i++;
		}
		if((i - 1) > maxAssig)
			maxAssig = i - 1;
		if(t.getChildTables().size() > 0){
			for(Table ts : t.getChildTables()){
				subBlockAssign(ts, i);
			}
		}
	}
	
	public int getStackNumber(String var){
		for(StackVarPair svp : assignments){
			if(svp.getVar().equals(var))
				return svp.getStackPostion();
		}
		return -1;
	}
}
