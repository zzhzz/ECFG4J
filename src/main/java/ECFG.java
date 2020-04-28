import java.util.*;

import com.google.gson.annotations.Expose;
import org.javatuples.Triplet;
import soot.Unit;

public class ECFG {
    private List<Triplet<Integer, Integer, Integer>> edges = new ArrayList<>();
    private List<Stmt> stmts = new ArrayList<>();
    private Map<Unit, Integer> stmtMap = new HashMap<>();
    private Stmt entry, exit;
    private String packageName, class_name;

    @Expose
    public List<Block> blocks = new ArrayList<>();

    @Expose
    private Set<Triplet<Integer, Integer, Integer>> edgeSet = new HashSet<>();

    @Expose
    private Block ENTRY, EXIT;

    @Expose
    String method_name;
    
    private int beginLine;

    ECFG(String packageName, String class_name, String method_name, int beginLine){
        entry = new Stmt(0, null);
        exit = new Stmt(1, null);
        ENTRY = new Block(0, beginLine);
        EXIT = new Block(1, beginLine+1);
        stmts.add(entry);
        stmts.add(exit);
        blocks.add(ENTRY);
        blocks.add(EXIT);
        this.class_name = class_name;
        this.method_name = method_name;
        this.packageName = packageName;
        this.beginLine = beginLine;
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

    public String getClassName(){
        return class_name;
    }

    private void compress(int u, ArrayList[] g, Block block, Integer[] index){
        if(u < 2) return;
        ArrayList<Integer> adjList = g[u];
        index[u] = block.ID;
        block.addUnit(stmts.get(u).getUnit());
        if(adjList.size() == 1){
            if(index[adjList.get(0)] == -1)
                compress(adjList.get(0), g, block, index);
        }
    }

    void simplify(){
        ArrayList[] g = new ArrayList[stmts.size()];
        Integer[] index = new Integer[stmts.size()];
        for(int i = 0; i < stmts.size(); i++){
            g[i] = new ArrayList<Integer>();
        }
        Arrays.fill(index, -1);
        index[0] = 0;
        index[1] = 1;
        for(Triplet<Integer, Integer, Integer> edge : edges){
            Integer u = edge.getValue0(), v = edge.getValue1(), t = edge.getValue2();
            g[u].add(v);
        }
        for(int i = 2; i < stmts.size(); i++){
            if(index[i] == -1){
                Block block = new Block(blocks.size(), this.beginLine);
                block.setPackageName(packageName);
                compress(i, g, block, index);
                blocks.add(block);
            }
        }
        for(Triplet<Integer, Integer, Integer> edge : edges){
            Integer u = edge.getValue0(), v = edge.getValue1(), t = edge.getValue2();
            if (!index[u].equals(index[v])){
                edgeSet.add(Triplet.with(index[u], index[v], t));
            }
        }
        for(Block block : blocks) block.transToAST();
    }
}
