package semantic;
import java.util.ArrayList;

import javax.xml.soap.Node;

public class Error {
	
	String funcName;
	String callType;
	int line;
	Node calledArgs;

  
	public Error (String fName, String cType, int errorLine, Node args) {
	    funcName = fName;
	    callType = cType;
	    line = errorLine;
	    calledArgs = args;
	}

}