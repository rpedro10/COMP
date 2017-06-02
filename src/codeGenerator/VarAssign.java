package codeGenerator;
import java.util.ArrayList;
import semantic.Table;
import semantic.Symbol;
import semantic.HIRTree;
public class VarAssign {
	
	protected class StackVarPair{
		protected String var;
		protected int stackPostion;
		protected int[] lifetime;
		//////////////////////////////////////////////
		protected String getVar() {return var;}
		protected void setVar(String var) {this.var = var;}
		protected int getStackPostion() {return stackPostion;}
		protected void setStackPostion(int stackPostion) {this.stackPostion = stackPostion;}
		////////////////////////////////////////////////
		protected StackVarPair(String var, int pos, boolean lft){
			this.var = var;
			this.stackPostion = pos;
			if(lft)
				this.lifetime = new int[2];
		}
	}
	
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
	
	public void subOptimalAssign(){
		int i = 0;
		String retBuffer = null;
		for(Symbol s : symbolTable.getVariables()){
			switch (s.getType()){
				case "return":
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
			for(Table ts : symbolTable.getChildTables()){
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
