package Registers;

import com.sun.source.tree.Scope;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class Line {
    protected int lineNum; // Same as index within Line[] lines
    protected Queue<Integer> variables;
    protected HashSet<ScopeVariable> scopeVars;
    protected int assignedVar;
    protected int scopeNum; // The scopeCount value this line's assignment represents
    protected String[] lineValues;

    // EX: var 1 = var 2 + var 1
    // The value of var 1 on the right should be from the previous scope
    // var 1 will be added to duplicates
    protected HashSet<Integer> duplicates;

    public Line(int lineNum, String line){
        scopeVars = new HashSet<ScopeVariable>();
        this.lineNum = lineNum;
        variables = new LinkedList<Integer>();
        lineValues = line.split(" ");
        assignedVar = Integer.valueOf(lineValues[0]); // The first var# is the one being assigned

        HashSet<Integer> seenVars = new HashSet<Integer>();
        duplicates = new HashSet<Integer>();
        int sInt;

        for(String s : lineValues){
            sInt = Integer.valueOf(s);
            variables.add(sInt);

            // Detect duplicates
            if(!seenVars.contains(sInt)){
                seenVars.add(sInt);
            }
            else{
                duplicates.add(sInt);
            }
        }


    }

    public boolean hasVar(int varNum){
        return variables.contains(varNum);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Line #" + lineNum + ": ");
        for(int i : variables){
            sb.append(i);
            sb.append(", ");
        }
        return sb.toString();
    }
}
