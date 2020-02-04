import soot.Unit;

import java.util.ArrayList;
import java.util.List;

public class Block {
    private List<Unit> unitList = new ArrayList<>();

    void addUnit(Unit u){
        unitList.add(u);
    }
}
