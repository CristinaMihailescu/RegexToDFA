import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class SimpleGraphView {
    Graph<String, String> g;
    int edgeCount = 1;

    public String ConcatArray(ArrayList<BinaryTree> al) {
        String res = "";
        for (BinaryTree i : al) {
            res = res + Integer.toString(i.position) + "; ";
        }
        return res;
    }

    public String ConcatArray2(ArrayList<Integer> al) {
        String res = "";
        for (Integer i : al) {
            res = res + i + "; ";
        }
        return res;
    }

    public void AddStates(ArrayList<Integer> al, String state, String lang, HashMap<Integer, ArrayList<BinaryTree>> finalfollowpos, HashMap<Integer, String> thisPosIs) {

        System.out.println("Element: " + state);

        for (char symbol : lang.toCharArray()) { //pentru fiecare simbol
            ArrayList<Integer> newStatePos = new ArrayList<Integer>();
            for (Integer i : al) { //parcurgem lista cu pozitii
                if(thisPosIs.get(i).equals(Character.toString(symbol))) { //iau toate tranz cu a/b/c..
                    newStatePos.add(i);
                }
            }

            if(newStatePos.isEmpty())
                continue;

            //concatenez ffp de pozitiile care au simbol comun
            ArrayList<Integer> res = new ArrayList<Integer>();
            for(Integer j : newStatePos) {
                if(finalfollowpos.containsKey(j)){ //make sure it exists (error free)
                    for(BinaryTree i : finalfollowpos.get(j)) {
                        if(!res.contains(i.position)) { //no duplicates
                            res.add(i.position);
                        }
                    }
                }
            }

            Collections.sort(res);

            //am obtinut o noua stare in res
            String newState = ConcatArray2(res);
            if(!g.containsVertex(newState)) {
                g.addVertex(newState); //add new state
                g.addEdge(Character.toString(symbol) + " (" + Integer.toString(edgeCount) + ")", state, newState, EdgeType.DIRECTED); //IMPORTANT ADD LABEL ON ARROW
                edgeCount++;

                System.out.println(state + " --- (" + symbol + ") ---> " + newState);

                AddStates(res, newState, lang, finalfollowpos, thisPosIs);
            } else {
                System.out.println("Tranzitie: " + state + " --- (" + symbol + ") ---> " + newState);
                g.addEdge(Character.toString(symbol) + " (" + Integer.toString(edgeCount) + ")", state, newState, EdgeType.DIRECTED); //IMPORTANT ADD LABEL ON ARROW
                edgeCount++;
            }
        }

        return;
    }

    public SimpleGraphView(BinaryTree bt, HashMap<Integer, ArrayList<BinaryTree>> finalfollowpos, HashMap<Integer, String> thisPosIs, String lang, HashMap<BinaryTree, ArrayList<BinaryTree>> firstpos) {
        g = new SparseMultigraph<String, String>();

        String initialState = ConcatArray(firstpos.get(bt));
        g.addVertex(initialState); //add initial state

        ArrayList<Integer> res = new ArrayList<Integer>();

        for(BinaryTree i : firstpos.get(bt)) {
            res.add(i.position);
        }

        Collections.sort(res);

        AddStates(res, initialState, lang, finalfollowpos, thisPosIs);
    }
}
