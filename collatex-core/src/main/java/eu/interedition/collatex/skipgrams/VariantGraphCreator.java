package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleToken;

import java.util.*;

/* <name of class here>
 *
 * @author: Ronald Haentjens Dekker
 * Date: 25-10-2018
 *
 * This class is an iterative variant graph builder
 * The order in which tokens are supplied to this builder
 * determines whether tokens are grouped together in a node or not.
 * The order of tokens within a witness is maintained at all times.
 *
 */


/* Version 3
 * We create we a list of nodes in an order that is similar to the topological sort of the nodes of the variant
 * graph
 *
 * Insertion of a node
 * Input: We have a token (with a witness identifier and a position in the witness)
 * and the token of has a normalized form.
 *
 * Algorithmic steps:
 * 1. Filter the nodes of the list on the witness identifier..
 * 1b NOTE: that the start and the end vertices are always a special code (everything is higher than the start
 *    vertex and everything is lower than the end vertex).
 * 2. Navigate the existing nodes using a comparator, not on the identifier but on token position
 * 3. Find the node a bit lower and the node a bit higher
 * 4. Look at the nodes in between the lower and the higher using the normalized form.
 * 5. If not a normalized form match.. insert a new node
 *      This happens if a witness is th first to add a normalized form thereby creating a new column
 *      or in case of a transposition multiple nodes need to be created.
 * 6. If yes add token to that node
 */

public class VariantGraphCreator {
    VariantGraph variantGraph;
    List<VariantGraph.Vertex> verticesListInTopologicalOrder;

    VariantGraphCreator() {
        this.variantGraph = new VariantGraph();
        this.verticesListInTopologicalOrder = new ArrayList<>();
        this.verticesListInTopologicalOrder.add(variantGraph.getStart());
        this.verticesListInTopologicalOrder.add(variantGraph.getEnd());
    }

    void insertTokenInVariantGraph(SimpleToken token) {
        // This method should return two vertices: one that is higher than the one we want to insert
        // and one that is lower.
        VariantGraph.Vertex lower = variantGraph.getStart();
        VariantGraph.Vertex higher = variantGraph.getEnd();
        for (VariantGraph.Vertex v : verticesListInTopologicalOrder) {
            // rule 1b: start and end vertices are special
            // Although this is more of a fast path; if we remove this two conditions
            // it should still work
            if (v == variantGraph.getStart()) {
                continue;
            }
            if (v == variantGraph.getEnd()) {
//                System.out.println("The end node is higher than "+token.toString());
                break;
            }

            // search the other token
            String witnessId = token.getWitness().getSigil();
            Optional<Token> optionalTokenForThisWitness = v.tokens().stream().filter(p -> p.getWitness().getSigil().equals(witnessId)).findFirst();
            // Rule 1: If V does not contain the witness that we are looking for then skip
            if (!optionalTokenForThisWitness.isPresent()) {
                continue;
            }
            SimpleToken theOtherToken = (SimpleToken) optionalTokenForThisWitness.get();

            // Do the actual token comparison
//            System.out.println("Comparing other "+ theOtherToken+" and "+token+"!");
            int bla = theOtherToken.compareTo(token);
//            System.out.println("outcome:"+bla);

            if (bla < 0) {
//                System.out.println("token "+token+" is higher than "+theOtherToken);
                lower = v;
                continue;
            }
            if (bla > 0) {
//                System.out.println("token "+token+" is lower than "+theOtherToken);
                higher = v;
                break;
            }
        }
        // we need to decide whether we need to create a new vertex or not
        // we search in between the lower and the upper bound to see whether there is a vertex
        // that has the same
        // integer position of lower berekenen.
//        System.out.println("Token: "+token+" ; Lower: "+lower+" ; higher: "+higher);
        int i = verticesListInTopologicalOrder.indexOf(lower);
        int j = verticesListInTopologicalOrder.indexOf(higher);
        if (j-i == 1) {
            // lower and higher tokens are right next to each other.
            // We have to add a new node in the variant graph
            // NOTE: here there is no difference between inserting before the upper and after the lower bound!
            createVertexForToken(token, i);
            return;
        }


        // Search to see whether we need to create a new vertex or not
        VariantGraph.Vertex result = null;
        for (int x=i+1; x < j; x++) {
            VariantGraph.Vertex n = verticesListInTopologicalOrder.get(x);
            // all the tokens at one vertex have the same normalized form
            // so get the first token on the node....

            SimpleToken t = (SimpleToken) n.tokens().stream().findFirst().get();
            if (t.getNormalized().equals(token.getNormalized())) {
                result = n;
                break;
            }
        }

        // This happens when there is a node at a rank that is between lower and higher
        // but is not a normalized form match.
        // This will lead to a column with variation in the alignment table
        // Or to multiple nodes on the same rank un the variant graph.
        // So there already is a rank in the graph or a column in the tale
        // And need to add it to that.
        // TODO: replace this exception with useful commentary.
        // throw new RuntimeException("While trying to place "+token+" \n We searched for a normalized form match within the window "+(i+1)+"-"+(j-1)+", but could not find anything!");

        // NOTE: We do an insert before upper bound here!
        if (result==null) {
            createVertexForTokenInsertBefore(token, j);
            return;
        }

        // We searched within the window of potential normalized matches and we did find a normalized match.
        // Add this token to the existing vertex.
        result.tokens().add(token);

    }

    /*
     * creates a new vertex for a token
     * and inserts it in the topological prdered list of the vertices
     * where i stands for the position in the list
     * NOTE: we could at a method for when the i is not known
     * but I don't think that will happen often.
     * NOTE2: pay attention to +1.
     * This maybe confusing to callers of this method
     * Although of course this is a private method.
     */
    private void createVertexForToken(SimpleToken token, int i) {
        VariantGraph.Vertex vertex = new VariantGraph.Vertex(variantGraph);
        vertex.tokens().add(token);
        verticesListInTopologicalOrder.add(i+1, vertex);
    }


    private void createVertexForTokenInsertBefore(SimpleToken token, int upper) {
        VariantGraph.Vertex vertex = new VariantGraph.Vertex(variantGraph);
        vertex.tokens().add(token);
        verticesListInTopologicalOrder.add(upper, vertex);
    }

    /*
      This method takes the topological sort list of vertices and adds all the edges where
      needed

      This is of course used for the final graph
      But also to visualize the intermediate graphs.

     */
    public void addEdges() {
        /* We traverse over the topological order list
        * Of course skip the start node
        * then for every node...
        *
        * We should maybe first clear the incoming and outgoing edges when we first visit a node
        * That way we can use this code to visualize multiple partial variant graphs.
         */
        /*
         * The clearing out we do later on...
         */

        // we find a vertex
        // the vertex contains tokens for one or more witnesses
        // now each of these witnesses need to have a valid path
        // from the previous vertex of that witness to the current vertex.
        // so we create a map that contains the last seen vertex for eahc witness
        // at th start that is of course the start vertex.
        Map<String, VariantGraph.Vertex> witnessToLastVertexMap = new HashMap<>();
        //TODO: hardcoded for now
        List<String> witnesses = Arrays.asList("w1", "w2", "w3");
        // here we need to know the complete witness set..
        // oh wait that is no problem we can get that info from the start vertex of the graph
        // What kind of token is on there?
        // oh there are no tokens on there; then it won't work
        for (String witnessId : witnesses) {
            witnessToLastVertexMap.put(witnessId, variantGraph.getStart());
        }


//        Iterator<VariantGraph.Vertex> nodeIterator = verticesListInTopologicalOrder.iterator();
        for (VariantGraph.Vertex v : verticesListInTopologicalOrder) {
            // skip the start vertex
            if (v == variantGraph.getStart()) {
                continue;
            }
            // NOTE: oh the end vertex has no tokens
            if (v == variantGraph.getEnd()) {
                for (Map.Entry<String,VariantGraph.Vertex> witnessIdToPreviousVertexEntry : witnessToLastVertexMap.entrySet()) {
                    VariantGraph.Vertex pre = witnessIdToPreviousVertexEntry.getValue();
                    Witness witness = pre.tokens().stream().findFirst().get().getWitness();
                    variantGraph.connect(pre, v, Collections.singleton(witness));
                }
            }

            // nu moet ik alle tokens van een vertex af gaan om te kijken welke witnesses er allemaal
            // op stana.
            for (Token t: v.tokens()) {
                VariantGraph.Vertex previous = witnessToLastVertexMap.get(t.getWitness().getSigil());
                // NOTE: does connect work if it is called for the same vertex multiple times?
                // I guess so?
                variantGraph.connect(previous, v, Collections.singleton(t.getWitness()));
                // nu moet ik natuurlijk die map bijwerken
                witnessToLastVertexMap.put(t.getWitness().getSigil(), v);
            }
        }
    }




    @Override
    public String toString() {
        return verticesListInTopologicalOrder.toString();
    }
}





/*
 * We need a node comparator
 * that after checking the witness identifier overlap ( de smallest set has to be present in the fuller set)
 * delegates to a token comparator based on position to do the rest. Simple Token Comparator has that.
 *
 * We could create a node witness view.
 */


/*
 * Version 2 of the variant graph builder based on skipgrams
 * The idea here is to first create a list of tokens in what will be the topological sort of the nodes
 * of the variant graph.
 *
 * After the list of tokens is created in the right order nodes can be created from the list
 * bij deduplicating the tokens.. then the edges can be created... Sounds like a plan.
 *
 * We use a single navigableMap (either a treemap or skiplist) to accomplish this goal
 *
 *
 *
 *
 *

 */



//    /*
//    * We need a a key object that is comparable that is a composite of a witness id and a comparable token
//    * so we create an object for it..
//    * Not a key any more just a list..
//    */
//
//    public static class TokenComparatorThatAcceptsTokensFromMultipleWitnesses implements Comparator<SimpleToken> {
//        // first we check whether the tokens are of the same witness
//        // if not, we return -1.
//        // If there are from the same witness we call the compare function on the SimpleToken class itself.
//        // pretty simple concept.
//        @Override
//        public int compare(SimpleToken o1, SimpleToken o2) {
//            int result = o1.getWitness().getSigil().compareTo(o2.getWitness().getSigil());
//            if (result != 0) return result;
//            return o1.compareTo(o2);
//        }
//    }