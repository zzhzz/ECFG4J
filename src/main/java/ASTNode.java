import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ASTNode {
    String nodeLable;
    private List<ASTNode> childrens = new ArrayList<>();

    ASTNode(String label){
        this.nodeLable = label;
    }

    void addChild(ASTNode node){
        childrens.add(node);
    }

    void addAll(Collection<? extends ASTNode> list) {
        childrens.addAll(list);
    }

    public String toString(){
        return nodeLable + " children { " + childrens.stream()
                .map(ASTNode::toString)
                .collect(Collectors.joining(";"))
                + "}";
    }

    List<ASTNode> getChildrens(){
        return childrens;
    }

    String getLabel(){
        return nodeLable;
    }

}
