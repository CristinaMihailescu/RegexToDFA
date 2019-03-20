import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.ArrayList;
import java.util.HashMap;


public class SimpleGraphView {
    Graph<String, String> g;

    public SimpleGraphView(BinaryTree bt, HashMap<BinaryTree, ArrayList<BinaryTree>> followpos) {
        g = new SparseMultigraph<String, String>();

        for(BinaryTree key : followpos.keySet()) {
            if(key.position != 0) {
                System.out.print(key.position + ": ");

                if(!g.containsVertex(Integer.toString(key.position))) {
                    g.addVertex(Integer.toString(key.position));
                }

                for(BinaryTree i : followpos.get(key)) {
                    if(!g.containsVertex(Integer.toString(i.position))) {
                        g.addVertex(Integer.toString(i.position));
                    }
                    g.addEdge(key.position + " " + i.position, Integer.toString(key.position), Integer.toString(i.position), EdgeType.DIRECTED);

                    System.out.print(i.position + " ");

                }

                System.out.println();
            }
        }
    }
}
