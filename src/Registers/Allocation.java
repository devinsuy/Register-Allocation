package Registers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

public class Allocation {
    protected Variable[] vars; // Each variable is started @ index (var# - 1) : EX var #1 @ index 0
    protected Line[] lines;
    protected Vertex[] vertices;
    protected int[] scopeCount; // EX: scopeCount[3] is the # of times variable 4 is assigned
    protected int maxScopeCount = 0;
    protected ScopeVariable[][] scopeVars; // Corresponds to: [scopeNum][varNum]

    public Allocation(){
        inputVariables();
        initializeGraph();
        colorGraph();
        writeOutput();
//        for(Vertex v : vertices){
//            System.out.println(v);
//        }
    }

    // Initialization function
    public void inputVariables(){
        try {
            Scanner input = new Scanner(new File("input.txt"));
            String currentLine;
            String[] lineValues;
            int numVariables, numLines;
            numVariables = numLines = 0;

            // Finds the number of variables and lines in "input.txt"
            while(input.hasNextLine()){
                currentLine = input.nextLine();
                lineValues = currentLine.split(" ");
                for(String s : lineValues){
                    if(Integer.valueOf(s) > numVariables) { numVariables = Integer.valueOf(s); }
                }
                numLines++;
            }
            // Initialize all variables
            vars = new Variable[numVariables];
            scopeCount = new int[numVariables];
            for(int i = 0; i < numVariables; i++){
                vars[i] = new Variable(i+1);
            }
            // Initialize lines
            lines = new Line[numLines];
            int lineNum = 0;
            input = new Scanner(new File("input.txt"));

            while(input.hasNextLine()){
                currentLine = input.nextLine();
                lines[lineNum] = new Line((1 + lineNum++), currentLine);
            }

            int currentCount;
            for(Line l: lines){
                scopeCount[l.assignedVar-1] += 1;
                currentCount = scopeCount[l.assignedVar-1];
                l.scopeNum = currentCount;
                if(currentCount > maxScopeCount) {
                        maxScopeCount = currentCount;
                }
                vars[l.assignedVar-1].linesAssigned.add(l.lineNum);
            }

            // Determines which variables even show up in our input
            boolean varFound;
            for(Variable v : vars){
                varFound = false;
                for(Line l : lines){
                    if(l.variables.contains(v.varNum)){
                        varFound = true;
                        break;
                    }
                }
                v.varUsed = varFound;
            }
            input.close();
        }
        catch(IOException io) { io.printStackTrace(); }
    }

    /**
     * Finds the first mention of a particular variable,
     * also known as when the variable becomes "alive"
     * @param varNum The variable to search for
     * @return The index of the line with the FIRST occurrence of varNum, -1 if not found
     */
    public int findAlive(int varNum){
        for(int i = 0; i < lines.length; i++){
            if(lines[i].hasVar(varNum)){
                return (i+1);
            }
        }
        return -1;
    }

    /**
     * Finds the last mention of a particular variable,
     * also known as when the variable "dies"
     * @param var The variable to search for
     * @return The index of the line with the LAST occurrence of varNum, -1 if not found
     */
    public int findDeath(ScopeVariable var){
        int nextScope;
        if(var.scopeCount != (maxScopeCount-1) && scopeVars[var.scopeCount+1][var.varNum-1] != null){
            nextScope = scopeVars[var.scopeCount+1][var.varNum-1].line_num_alive - 2;
        }
        else{ // Our ScopeVariable is the last instance of this variable
            nextScope = lines.length - 1;
        }

        // First check for the var1 = var 2 + var 1 case
        if(nextScope != (lines.length-1)){
            if(lines[nextScope+1].duplicates.contains(var.varNum)){
                return nextScope + 2;
            }
        }

        // Gets the last time the variable is used before reassigned
        for(int i = nextScope; i >= var.line_num_alive-1; i--){
            if(lines[i].hasVar(var.varNum)){
                return i+1;
            }
        }
        return -1;
    }

    /**
     * Checks whether two variables have overlapping alive times
     * @param i The first variable
     * @param j The other variable
     * @return Whether or not there is overlap
     */
    public boolean overlaps(ScopeVariable i, ScopeVariable j){
        HashSet<Integer> rangeOne = new HashSet<Integer>();
        HashSet<Integer> rangeTwo = new HashSet<Integer>();
        for(int x = i.line_num_alive; x < i.line_num_dead; x++){ rangeOne.add(x); }
        for(int x = j.line_num_alive; x < j.line_num_dead; x++) { rangeTwo.add(x); }
        // If a variable dies the same line it comes alive, value is still stored
        if(rangeOne.isEmpty()) { rangeOne.add(i.line_num_alive); }
        if(rangeTwo.isEmpty()) { rangeTwo.add(j.line_num_alive); }

        // Intersect rangeOne with rangeTwo, rangeOne now contains all overlapping lines between i and j
        rangeOne.retainAll(rangeTwo);

        return !rangeOne.isEmpty(); // If the intersection between the two ranges is not empty, there is overlap
    }

    // Creates vertices where each variable is a vertex and
    // edges between two vertices is overlapping alive time
    public void initializeGraph() {
        vertices = new Vertex[lines.length]; // One vertex needed for each scope of a variable
        int init_index = 0;
        int scopeCount;
        ScopeVariable currentScopeVar;
        scopeVars = new ScopeVariable[maxScopeCount][vars.length];

        for(Variable var : vars){
            if(!var.varUsed) { continue; } // No vertices created for variables that don't show up in our input
            scopeCount = 0;
            for(int line : var.linesAssigned){
                currentScopeVar = new ScopeVariable(scopeCount, var.varNum, line);
                scopeVars[scopeCount][var.varNum-1] = currentScopeVar;
                vertices[init_index++] = new Vertex(currentScopeVar, scopeCount);
                scopeCount++;
            }
        }

        for(Vertex v : vertices){
            v.var.line_num_dead = findDeath(v.var);
            if(v.var.line_num_dead == -1) {
                throw new RuntimeException("Could not determine death line of Scope " + v.var.scopeCount + " Variable #" + v.var.varNum );
            }
        }

        // Create edges based on alive time overlap
        for (Vertex i : vertices) {
            for (Vertex j : vertices) {
                // Avoid creating edges to self, duplicate edges
                if (j.var == i.var || i.edges.contains(j)) { continue; }
                else if (overlaps(i.var, j.var)) {
                    i.createEdge(j); // Undirected edge, also creates for j -> i
                }
            }
        }
    }


    /**
     * Iterates through vertices currently in the graph and returns
     * the first Vertex where |Edges| < k, otherwise null
     * @param k The number of edges the vertex must have less than
     * @param inGraph A HashSet of the current vertices in the graph
     * @return A vertex that has less than k edges, otherwise null
     */
    public Vertex findRemoval(int k, HashSet<Vertex> inGraph){
        for(Vertex v : inGraph){
            if(v.numEdges < k) { return v; }
        }
        return null;
    }


    public void colorGraph(){
        LinkedList<Vertex> reinsertOrder = new LinkedList<Vertex>();
        LinkedList<Vertex> rollbackOrder = new LinkedList<Vertex>();
        HashSet<Vertex> inGraph = new HashSet<Vertex>();
        Vertex currentVertex = null;
        int numRegisters = 1;

        // Add all vertices in
        for(Vertex v : vertices){
            inGraph.add(v);
        }
        // Determine the number of registers(colors) needed
        while(currentVertex == null){
            numRegisters++;
            currentVertex = findRemoval(numRegisters, inGraph);
        }
        for(Vertex v : inGraph){ v.initializeColors(numRegisters); }
        // Queue the vertices until the graph is completely empty
        while(!inGraph.isEmpty()){
            currentVertex = findRemoval(numRegisters, inGraph);
            if(currentVertex == null) { // numRegisters is too low, perform a rollback
                while(!reinsertOrder.isEmpty()){
                    currentVertex = reinsertOrder.removeFirst();
                    inGraph.add(currentVertex);
                    for(Vertex v : currentVertex.edges) { v.numEdges++; }
                }
                numRegisters++; // Try again with a higher k value
                for(Vertex v : inGraph){ v.initializeColors(numRegisters); }
                continue;
            }
            reinsertOrder.addFirst(currentVertex);
            inGraph.remove(currentVertex);
            for(Vertex v : currentVertex.edges){ v.numEdges--; }  // Decrement edge count of all adjacent vertices
        }
        // Color the graph
        boolean colorSuccess;
        while(!reinsertOrder.isEmpty()){
            currentVertex = reinsertOrder.removeFirst();
            rollbackOrder.addFirst(currentVertex);
            colorSuccess = currentVertex.setColor();

            if(!colorSuccess){
                while(!rollbackOrder.isEmpty()){
                    currentVertex = rollbackOrder.removeFirst();
                    reinsertOrder.addFirst(currentVertex);
                    for(Vertex v : currentVertex.edges) { v.numEdges++; }
                }
                numRegisters++; // Try again with higher k value
            }
            currentVertex.var.color = currentVertex.color;
        }
        System.out.println("Interference graph successfully colored");
        System.out.println("Number of variables: " + vars.length);
        System.out.println("Number of registers needed: " + numRegisters);
    }

    public void writeOutput(){
        int currentColor;
        scopeCount = new int[vars.length];
        for(int i = 0; i < scopeCount.length; i++) { scopeCount[i] = -1; }

        try{
            FileWriter fw = new FileWriter(new File("output.txt"));
            boolean firstVar;

            for(Line l : lines){
                scopeCount[Integer.valueOf(l.assignedVar)-1] += 1;
                firstVar = true;
                for(String varNum : l.lineValues){
                    if(l.lineNum == 326 && Integer.valueOf(varNum) == 114){
                        System.out.println();
                    }

                    if(!firstVar && Integer.valueOf(varNum) == l.assignedVar){
                        int scope = scopeCount[Integer.valueOf(varNum)-1]-1;
                        currentColor = scopeVars[scope][Integer.valueOf(varNum)-1].color + 1;
                    }
                    else{
                        currentColor = scopeVars[scopeCount[Integer.valueOf(varNum)-1]][Integer.valueOf(varNum)-1].color + 1;
                    }
                    fw.write(String.valueOf(currentColor));
                    fw.write(" ");
                    if(Integer.valueOf(varNum) == l.assignedVar) { firstVar = false; }
                }
                fw.write("\n");
            }
            fw.close();
        }
        catch(IOException io) { io.printStackTrace(); }
        System.out.println("\nRegisters numbers written to \"output.txt\"");
    }

}
