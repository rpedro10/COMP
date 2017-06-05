package semantic;
import java.util.ArrayList;

import javax.xml.soap.Node;

public class Error {
	
	String variavel;
	String mensagem;
	int line;

	

  
	public Error (String var, int linha, String msg) {
	    line = linha;
	    mensagem = msg;
	    variavel = var;
	}
	
	
	public void printError(){
		
		System.out.printf("linha: "+ line  +" --> "+ mensagem + variavel +'\n');
	}

}