import java.util.*;

import org.javatuples.Triplet;
import soot.Unit;

public class ECFG {
    private List<Triplet<Integer, Integer, Integer>> edges = new ArrayList<>();
    private List<Stmt> stmts = new ArrayList<>();
    private Map<Unit, Integer> stmtMap = new HashMap<>();
    private Stmt entry, exit;

    private List<Block> blocks = new ArrayList<>();
    ECFG(){
        entry = new Stmt(0, null);
        exit = new Stmt(1, null);
        stmts.add(entry);
        stmts.add(exit);
    }

    void linkEntry(Integer x){
        appendEdge(0, x, 0);
    }

    void linkExit(Integer x){
        appendEdge(x, 1, 0);
    }

    void appendEdge(Integer u, Integer v, Integer t){
        edges.add(Triplet.with(u, v, t));
    }
    void appendUnit(Unit u){
        Integer nodeID = getNextID();
        stmts.add(new Stmt(nodeID, u));
        stmtMap.put(u, nodeID);
    }
    private Integer getNextID(){
        return stmts.size();
    }
    Integer FindUnit(Unit u){
        return stmtMap.get(u);
    }

    public String toString(){
        return edges.toString();
    }

    void simplify(){
        ArrayList[] g = new ArrayList[stmts.size()];
        Arrays.fill(g, new ArrayList<Integer>());
        boolean[] visit = new boolean[stmts.size()];
        for(Triplet<Integer, Integer, Integer> edge : edges){
            Integer u = edge.getValue0(), v = edge.getValue1(), t = edge.getValue2();
            g[u].add(v);
        }

    }
}
