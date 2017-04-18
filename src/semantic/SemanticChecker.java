package semantic;
import java.util.ArrayList;

public class SemanticChecker {
	
		SymbolTable symbolTable;

		
		
		
		public SemanticChecker() {
		symbolTable = new SymbolTable();
		errorCount = 0;
		symbolTable.beginScope();
		error_list = new ArrayList<Erro>();

		
		
	}

}