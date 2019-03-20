import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.decorators.*;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import java.awt.Dimension;
import javax.swing.JFrame;
import java.util.ArrayList;
import java.util.HashMap;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public class Main {


    public static boolean IsOp(Character c) {
        if (c == '*' || c == '|' || c == '.')
            return true;
        else return false;
    }

    /* TO DO INFIX TO POSTFIX AND PREFIX TO POSTFIX
    public static String InfixToPostfix(String expr) {
        String ptf = new String();
        for(int i = 0; i<expr.length(); i++) {
            if(!IsOp(expr.charAt(i))) { //if in language
                ptf = ptf.substring(0, i) + expr.charAt(i) + ptf.substring(i + 1);
            }
        }
        return ptf;
    }
    */

    public static int i = 0;
    public static int posAssign = 1;
    public static BinaryTree bt;
    public static HashMap<BinaryTree, ArrayList<BinaryTree>> firstpos;
    public static HashMap<BinaryTree, ArrayList<BinaryTree>> lastpos;
    public static HashMap<BinaryTree, ArrayList<BinaryTree>> followpos;
    public static HashMap<BinaryTree, Boolean> nullable;

    public static void CreateBinaryTree(BinaryTree root, String expr) {
        root.value = expr.charAt(i);
        System.out.println("We're at: " + root.value + " and i = " + i);
        if (IsOp(expr.charAt(i))) { //if char at i is operator
            int r = i;
            i--;
            root.right = new BinaryTree();
            CreateBinaryTree(root.right, expr);
            if (expr.charAt(r) != '*') { //for kleene we have only 1 child
                root.left = new BinaryTree();
                CreateBinaryTree(root.left, expr);
            } else {
                return;
            }
        } else { //for leafs
            nullable.put(root, false);
            ArrayList<BinaryTree> al = new ArrayList<BinaryTree>();
            al.add(root);
            firstpos.put(root, al);
            lastpos.put(root, al);
            i--;
        }
        return;
    }

    public static ArrayList<BinaryTree> Reunion(BinaryTree left, BinaryTree right, HashMap<BinaryTree, ArrayList<BinaryTree>> pos) {
        ArrayList<BinaryTree> al = new ArrayList<BinaryTree>();
        for (BinaryTree e : pos.get(left)) {
            al.add(e);
        }
        for (BinaryTree e : pos.get(right)) {
            if (!pos.get(left).contains(e)) { //no duplicates since it's reunion
                al.add(e);
            }
        }
        return al;
    }

    public static void ComputeFollowpos(BinaryTree root) {
        if(root.value == '.') {
            for(BinaryTree i : lastpos.get(root.left)) {
                ArrayList<BinaryTree> al = new ArrayList<BinaryTree>();
                if(followpos.containsKey(i)) {
                    for(BinaryTree j : followpos.get(i)) {
                        al.add(j);
                    }
                }
                for(BinaryTree j : firstpos.get(root.right)) {
                    if(!followpos.containsKey(i) || (followpos.containsKey(i) && !followpos.get(i).contains(j))) { //no duplicates
                        al.add(j);
                    }
                }
                if(followpos.containsKey(i)) {
                    followpos.replace(i,  al);
                } else {
                    followpos.put(i,  al);
                }

            }
        } else {
            if(root.value == '*') {
                for(BinaryTree i : lastpos.get(root)) {
                    ArrayList<BinaryTree> al = new ArrayList<BinaryTree>();
                    if(followpos.containsKey(i)) {
                        for(BinaryTree j : followpos.get(i)) {
                            al.add(j);
                        }
                    }
                    for(BinaryTree j : firstpos.get(root)) {
                        if(!followpos.containsKey(i) || (followpos.containsKey(i) && !followpos.get(i).contains(j))) { //no duplicates
                            al.add(j);
                        }
                    }
                    if(followpos.containsKey(i)) {
                        followpos.replace(i,  al);
                    } else {
                        followpos.put(i,  al);
                    }
                }
            }
        }
        if(root.left != null) {
            ComputeFollowpos(root.left);
        }
        if(root.right != null) {
            ComputeFollowpos(root.right);
        }
        return;
    }

    public static void ComputeNullableFirstposLastpos(BinaryTree root) {
        //Only check nullable, there's no need to check firstpost/lastpos
        if (root.left != null && !nullable.containsKey(root.left)) { //null means there's no value associated with root.left
            ComputeNullableFirstposLastpos(root.left);
        }
        if (root.right != null && !nullable.containsKey(root.right)) { //null means there's no value associated with root.right
            ComputeNullableFirstposLastpos(root.right);
        }

        //Leafs already have values
        if (root.value == '|') {
            //NULLABLE
            nullable.put(root, nullable.get(root.left) || nullable.get(root.right));
            firstpos.put(root, Reunion(root.left, root.right, firstpos));
            lastpos.put(root, Reunion(root.left, root.right, lastpos));
        } else {
            if(root.value == '.') {
                nullable.put(root, nullable.get(root.left) && nullable.get(root.right));
                if(nullable.get(root.left)) { //if nullable(c1)
                    firstpos.put(root, Reunion(root.left, root.right, firstpos)); //firstpos(c1) U firstpos(c2)
                } else {
                    firstpos.put(root, firstpos.get(root.left)); //else firstpos(c1)
                }

                if(nullable.get(root.right)) {
                    lastpos.put(root, Reunion(root.left, root.right, lastpos)); //lastpos(c1) U lastpos(c2)
                } else {
                    lastpos.put(root, lastpos.get(root.right)); //else lastpos(c2)
                }
            } else {
                if(root.value == '*') {
                    nullable.put(root, true);
                    firstpos.put(root, firstpos.get(root.right));
                    lastpos.put(root, lastpos.get(root.right));
                }
            }
        }

        return;
    }


    public static void AssignLeafPositions(BinaryTree btl) {
        System.out.println(btl.value);
        if (btl.left != null) {
            AssignLeafPositions(btl.left);
        }
        if (btl.right != null) {
            AssignLeafPositions(btl.right);
        }
        if(btl.left == null && btl.right == null) { //it's a leaf
            btl.position = posAssign;
            posAssign++;
            System.out.println("Assigned pos " + btl.position + " to " + btl.value);
        }
        return;
    }

    public static void main(String[] args) {

        //MODIFY HERE - language stuff
        // +
        // infix to postfix
        // and
        // prefix to postfix


        //System.out.println("Alfabet: ");
        //Scanner scanner = new Scanner(System.in);
        //String lang = scanner.nextLine();
        //System.out.println("Expresie: ");
        //String expr = scanner.nextLine();

        String expr = "ab.*a.ba|.";
        ///////String expr = "ab|*a.b.b.";
        expr = expr + "#.";

        //System.out.println("Input: " + lang + " -> " + expr);

        //Binary tree stuff
        nullable = new HashMap<BinaryTree, Boolean>();
        followpos = new HashMap<BinaryTree, ArrayList<BinaryTree>>();
        lastpos = new HashMap<BinaryTree, ArrayList<BinaryTree>>();
        firstpos = new HashMap<BinaryTree, ArrayList<BinaryTree>>();

        bt = new BinaryTree();

        bt.value = '.';
        bt.right = new BinaryTree('#');
        nullable.put(bt.right, false);
        ArrayList<BinaryTree> al = new ArrayList<BinaryTree>();
        al.add(bt.right);
        firstpos.put(bt.right, al);
        lastpos.put(bt.right, al);

        bt.left = new BinaryTree();
        i = expr.length() - 3;
        CreateBinaryTree(bt.left, expr);

        AssignLeafPositions(bt);

        ComputeNullableFirstposLastpos(bt);

        //Output firstpos, lastpos, nullable
        /*for(BinaryTree key : firstpos.keySet()) {
            System.out.print("[" + key.position + "] " + key.value + ": firstpos -> ");

            for(BinaryTree i : firstpos.get(key)) {
                System.out.print(i.position + "(" + i.value + ") ");
            }
            System.out.print("; lastpos -> ");
            for(BinaryTree i : lastpos.get(key)) {
                System.out.print(i.position + " ");
            }
            System.out.println("; nullable -> " + nullable.get(key));
        }*/

        ComputeFollowpos(bt);

        SimpleGraphView sgv = new SimpleGraphView(bt, followpos);
        Layout<String, String> layout = new CircleLayout(sgv.g);
        layout.setSize(new Dimension(800,800));
        BasicVisualizationServer<String,String> vv =
                new BasicVisualizationServer<String,String>(layout);
        vv.setPreferredSize(new Dimension(850,850));
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

        JFrame frame = new JFrame("Simple Graph View");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);

    }
}
