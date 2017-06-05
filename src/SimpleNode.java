/* Generated By:JJTree: Do not edit this line. SimpleNode.java Version 4.3 */
/* JavaCCOptions:MULTI=false,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class SimpleNode implements Node {

  protected Node parent;
  protected Node[] children;
  protected int id;
  protected Object value;
  protected Parser parser;
  
  protected int line;
  protected int column;
  
  //Added
  public String val = "Undefined";

  public SimpleNode(int i) {
	  
    id = i;
    line = parser.getToken(id).endLine ;
    column = parser.getToken(id).beginColumn;
  }

  public SimpleNode(Parser p, int i) {
    this(i);
    parser = p;
   
  }

  public void jjtOpen() {
  }

  public void jjtClose() {
  }

  public void jjtSetParent(Node n) { parent = n; }
  public Node jjtGetParent() { return parent; }

  public void jjtAddChild(Node n, int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }

  public Node jjtGetChild(int i) {
    return children[i];
  }

  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  public void jjtSetValue(Object value) { this.value = value; }
  public Object jjtGetValue() { return value; }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */

  public String toString() { return ParserTreeConstants.jjtNodeName[id]; }
  public String toString(String prefix) { return prefix + toString(); }

  /* Override this method if you want to customize how the node dumps
     out its children. */

  public void dump(String prefix, semantic.HIRTree hr) {
    boolean makeChild = true;
    hr.setLineColumn(line, column);
    if (children != null) {
      switch (this.id){
      case ParserTreeConstants.JJTPARAM:
    	  if(children.length != 1){
    		  SimpleNode child = (SimpleNode)children[0];
    		  hr.setContents("Array", child.val);
    		  this.children = new Node[0];
    	  }else{
    		  prefix = prefix.substring(0, prefix.length()-1);
    		  makeChild = false;
    	  }
    	  break;
      case ParserTreeConstants.JJTDECLARATION:
    	  if(this.val != "Undefined"){
    		 //System.out.println(prefix + this.val);
    		 hr.setContents("Assign",this.val);
    	  }else{
    		  prefix = prefix.substring(0, prefix.length()-1);
    		  makeChild = false;
    	  }
    	  
    	  break;
      case ParserTreeConstants.JJTDECLARATIONRHS:
    	  if(this.val !="Undefined"){
      		//System.out.println(prefix+this.val);
      		hr.setContents("Arith", this.val);
	      	}
	      	else{
	      		prefix = prefix.substring(0, prefix.length()-1);
	      		makeChild = false;
	      	}
    	  break;
      case ParserTreeConstants.JJTEXPRTEST:
      	//System.out.println(prefix+this.val);
      	hr.setContents("Exprtest", this.val);
      	break;
      case ParserTreeConstants.JJTASSIGN:
        	//System.out.println(prefix+this.val);
        	hr.setContents("Assign", this.val);
        	break;
      case ParserTreeConstants.JJTRHS:
    	if(this.val !="Undefined"){
    		if(this.val.equals(".size")){
        		hr.setContents("SizeAccess", null);
        	}
    		else
    			hr.setContents("Arith", this.val);
    	}
    	else{
    		prefix = prefix.substring(0, prefix.length()-1);
    		makeChild = false;
    	}
      	break;
      case ParserTreeConstants.JJTTERM:
      	if(children.length > 1){
      		SimpleNode aux = (SimpleNode)children[1];
      		switch(aux.id){
      		case ParserTreeConstants.JJTARRAYACCESS:
      		//	System.out.println(prefix + "Array");
      			hr.setId("Array");
      		}
      	}
      	else{
      		prefix = prefix.substring(0, prefix.length()-1);
      		makeChild = false;
      	}
      	break;
      case ParserTreeConstants.JJTRETURN:
    	  hr.setContents("Return", null);
    	  if(children.length > 2){
    		  semantic.HIRTree child = new semantic.HIRTree(hr);
    		  SimpleNode aux = (SimpleNode)children[0];
    		  child.setContents("Array", aux.val);
    		  hr.addChild(child);
    		  child = new semantic.HIRTree(hr);
    		  aux = (SimpleNode)children[2];
    		  child.setContents("Id", aux.val);
    		  hr.addChild(child);
    		  return;
    	  }
    	  break;
      default:
    	//System.out.println(toString(prefix));
    	hr.setId(toString());
      	break;
      
      }
      for (int i = 0; i < children.length; ++i) {
        SimpleNode n = (SimpleNode)children[i];
        if (n != null) {
          semantic.HIRTree child = makeChild == true ? new semantic.HIRTree(hr) : hr;
          if(makeChild)hr.addChild(child);
          n.dump(prefix + " ", child);
        }
      }
    }else{
    	
    	hr.setContents(toString(), this.val);
    	}
  }
}

/* JavaCC - OriginalChecksum=f32205b061e34e2a71252a4fbe0921a7 (do not edit this line) */
