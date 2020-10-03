package Registers;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class Variable {
    protected int varNum;
    protected boolean varUsed;
    protected Queue<Integer> linesAssigned; // The line numbers this variable is reassigned at

    public Variable(int varNum){
        linesAssigned = new LinkedList<Integer>();
        this.varNum = varNum;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return varNum == variable.varNum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(varNum);
    }
}
