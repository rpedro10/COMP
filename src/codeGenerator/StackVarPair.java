package codeGenerator;

public class StackVarPair {
		protected String var;
		protected int stackPostion;
		protected int[] lifetime;
		//////////////////////////////////////////////
		protected String getVar() {return var;}
		protected void setVar(String var) {this.var = var;}
		protected int getStackPostion() {return stackPostion;}
		protected void setStackPostion(int stackPostion) {this.stackPostion = stackPostion;}
		////////////////////////////////////////////////
		public StackVarPair(String var, int pos, boolean lft){
			this.var = var;
			this.stackPostion = pos;
			if(lft)
				this.lifetime = new int[2];
		}
}
