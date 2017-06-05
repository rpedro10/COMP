package semantic;

public class HIRTree implements HIRNode {
	protected HIRTree parent;
	protected HIRTree children[];
	protected String id; //Identifiers, Module, Integer, Array, Expr, Add, Assign, Function, ect.
	protected String value; //Value of the identifiers, names, numbers, arithexpr etc..
	
	//TODO add begin line
	public HIRTree(){
		
	}
	public HIRTree(HIRTree parent){
		this.parent = parent;
	}
	
	@Override
	public void setParent(HIRTree n) {
		this.parent = n;
		
	}
	@Override
	public HIRTree getParent() {
		return this.parent;
	}
	@Override
	public void addChild(HIRTree n) {
		if(this.children == null){
			this.children = new HIRTree[1];
			this.children[0] = n;
		}
		else{
			int aux = this.children.length;
			HIRTree c[] = new HIRTree[aux + 1];
			System.arraycopy(this.children,0,c,0,aux);
			c[aux] = n;
			this.children = c;
		}
		
	}
	@Override
	public void setChildren(HIRTree[] nr) {
		int size = nr.length;
		if(this.children == null){
			this.children = new HIRTree[size];
			System.arraycopy(nr, 0, this.children, 0, size);
		}else{
			int csize = this.children.length;
			HIRTree aux[] = new HIRTree[size + csize];
			System.arraycopy(this.children, 0, aux, 0, csize);
			System.arraycopy(nr, 0, aux, csize, size);
			this.children = aux;
		}
	}
	@Override
	public HIRTree getChild(int ind) {
		return this.children[ind];
	}
	@Override
	public HIRTree[] getChildren() {
		return this.children;
	}

	@Override
	public void setId(String id) {this.id = id;}

	@Override
	public String getId() {return this.id;}

	@Override
	public void setVal(String val) {this.value = val;}

	@Override
	public String getVal() {return this.value;}

	@Override
	public void setContents(String id, String val) {this.id = id; this.value = val;}
	
	public void dump(String prefix){
		String value = this.value != null ? " : "+this.value : "";
		if(this.children != null){
			System.out.println(prefix + this.id + value);
			for(int i = 0; i < children.length; i++){
				getChild(i).dump(prefix + " ");
			}
		}
		else{
			System.out.println(prefix + this.id +value);
		}
	}

}
