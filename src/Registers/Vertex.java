package Registers;

import java.util.HashSet;
import java.util.Objects;

public class Vertex {
    protected ScopeVariable var;
    protected HashSet<Vertex> edges;
    protected boolean[] availableColors;
    protected int color;
    protected int numEdges; // Count of psuedo edges, used in Allocation.findRemoval()
    protected int scopeNum;

    public Vertex(ScopeVariable v, int scopeNum){
        edges = new HashSet<Vertex>();
        this.var = v;
        this.color = -1;
        this.numEdges = 0;
        this.scopeNum = scopeNum;
    }

    public boolean hasEdge(Vertex v){
        return edges.contains(v);
    }

    public void createEdge(Vertex v){
        edges.add(v);
        this.numEdges++;
        v.edges.add(this);
        v.numEdges++;
    }

    public void initializeColors(int numColors){
        availableColors = new boolean[numColors];
        for(int i = 0; i < availableColors.length; i++) { availableColors[i] = true; }
    }

    public void updateAvailColors(){
        for(Vertex v : edges){
            if(v.color == -1) { continue; }
            availableColors[v.color] = false;
        }
    }

    public int getNextColor(){
        updateAvailColors();
        for(int i = 0; i < availableColors.length; i++){
            if(availableColors[i]){
                return i;
            }
        }
        return -1;
    }

    public boolean setColor(){
        this.color = getNextColor();
        if(this.color == -1) {
            return false; // Failed to color, numRegisters is too low
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vertex #" + var.varNum + " has color(" + color + ") and has edges to: ");
        for(Vertex i : edges){
            sb.append("#" + i.var.varNum + ", ");
        }
        sb.append("\n   And Var(" + this.var.scopeCount + ", #" + this.var.varNum + ": alive @" +
                this.var.line_num_alive + ", dies @" + this.var.line_num_dead);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return Objects.equals(var, vertex.var);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var);
    }
}
