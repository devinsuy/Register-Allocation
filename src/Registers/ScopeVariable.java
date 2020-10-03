package Registers;

import java.util.Objects;

public class ScopeVariable {
    protected int varNum;
    protected int scopeCount;
    protected int line_num_alive;
    protected int line_num_dead;
    protected int color;

    public ScopeVariable(int scopeCount, int varNum, int assignedNum){
        this.scopeCount = scopeCount;
        this.varNum = varNum;
        this.line_num_alive = assignedNum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Variable #" + varNum + ": Alive @ Line #" + line_num_alive + ", Dies @ Line #" + line_num_dead +
                ", scopeCount: " + scopeCount);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScopeVariable that = (ScopeVariable) o;
        return varNum == that.varNum &&
                scopeCount == that.scopeCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(varNum, scopeCount);
    }
}
