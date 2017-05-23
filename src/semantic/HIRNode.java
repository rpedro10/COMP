package semantic;

public interface HIRNode {
	
	public void setParent(HIRTree n);
	public HIRTree getParent();
	
	public void addChild(HIRTree n);
	public void setChildren(HIRTree[] nr);
	
	public HIRTree getChild(int ind);
	public HIRTree[] getChildren();
	
	public void setId(String id);
	public String getId();
	
	public void setVal(String val);
	public String getVal();
	
	public void setContents(String  id, String val);

}
