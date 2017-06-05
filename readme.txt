P17 -SIMPLE

Grupo 31


NAME1: <Miguel Teixeira>, NR: <up201607941>, GRADE: <16 value>, CONTRIBUTION: <33 %>
NAME2: <Diogo Cepa>, NR: <up201403367>, GRADE: <16 value>, CONTRIBUTION: <33 %>
NAME3: <Rui Araujo>, NR: <up201403263>, GRADE: <16 value>, CONTRIBUTION: <33 %>



** SUMMARY: (Describe what your tool does and its main features.)

Compilador de linguagem Simple (yal) com output JVM bytecodes em formato jasmin.
Main Features: Analise Sintatica 
				criação de uma AST
				Criação de uma HIR tree
				Analise Semantica
				Geração de codigo JVM bytecodes em formato jasmin


** EXECUTE: (indicate how to run your tool)

javacc Parser.jjt
javac *.java
java Parser <File.yal>

**DEALING WITH SYNTACTIC ERRORS:

A análise sintatica da linguagem SIMPLE é efetuada 
O programa para quando encontra o primeiro erro, mostrando a sua linha, o que lá tem e o que esperava.

**SEMANTIC ANALYSIS: (Refer the possible semantic rules implemented by your tool.)

A analise semantica faz verificações de tipos de variáveis, verificação das inicializações de variaveis. Operações de assign, aritmeticas,
chamadas de funções...

**CODE GENERATION: (when applicable, describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)
 O code generation vai percorrendo descendentemente a AST e fazendo a conversao 
 
 **OVERVIEW: (refer the approach used in your tool, the main algorithms, the third-party tools and/or packages, etc.)
 Defaul Package com o Parser  e AST
 Package Semantic onde é realizada a analise semanticae contem a arvore HIR;
 Package codeGenerator onde é feito o code generation
 
 
**TESTSUITE AND TEST INFRASTRUCTURE: (Describe the content of your testsuite regarding the number of examples, the approach to automate the test, etc.)
Explicação dos exemplos presentes na pasta testSuite

**TASK DISTRIBUTION: (Identify the set of tasks done by each member of the project.)

Analise Sintatica + Geração AST + Geração HIR tree --> os 3 elementos
Analise Semantica --> Rui e Diogo
Code Generation --> Miguel


 
**PROS: (Identify the most positive aspects of your tool) 
Faz uma conversao correta de yal para JVM bytecodes em formato jasmin
 
**CONS: (Identify the most negative aspects of your tool)
Pequeno bug na analise semantica
 
 


