import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WaterBuckets {
    public static void main(String[] args) {
        Node start = new Node(
                new Bucket(4, 4), // Format is capacity, starting_amount
                new Bucket(7, 6),
                new Bucket(10, 0)
        );


        // The goal is to end up with 2 gallons in the first or second bucket
        Function<Node, Boolean> success_condition = b -> b.buckets.get(0).amt == 2 || b.buckets.get(1).amt == 2;
        String solution = dfs(start, success_condition);

        System.out.println(solution);
    }

    static String dfs(Node start, Function<Node, Boolean> stop_condition) {
        ArrayDeque<Node> to_explore = new ArrayDeque<>(1024);
        HashMap<Node, Integer> distances = new HashMap<Node, Integer>(1024);
        to_explore.addLast(start);

        distances.put(start, 0);

        while (!to_explore.isEmpty()) {
            Node curr_node = to_explore.poll();


            if (stop_condition.apply(curr_node)) {
                return unwindPath(distances, curr_node);
            }

            for (Node neighbor : curr_node.getAdjacentNodes()) {
                if (distances.containsKey(neighbor)) continue;

                distances.put(neighbor, 1 + distances.get(curr_node));
                to_explore.addLast(neighbor);

            }
        }

        throw new IllegalArgumentException("Cannot find a path");
    }

    private static String unwindPath(HashMap<Node, Integer> distances, Node curr_node) {

        System.out.println(curr_node);
        if (distances.get(curr_node) == 0) {
            return curr_node.toString();
        }
        for (Node adj : curr_node.getAdjacentNodes()) {
            if (distances.get(adj) == distances.get(curr_node)-1) {
                String prev_path = unwindPath(distances, adj);

                if (prev_path.length() > 0) {
                    prev_path = prev_path + " -> ";
                }
                return prev_path +  curr_node.toString();

            }
        }

        throw new IllegalArgumentException("Could not unwind the completed path");
    }

}
class Bucket {
    int cap;
    int amt;

    Bucket(int cap, int amt) {
        this.cap = cap;
        this.amt = amt;
    }

    Bucket(Bucket other) {
        this.cap = other.cap;
        this.amt = other.amt;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Bucket)) {
            return false;
        }

        Bucket b = (Bucket)o;
        return b.amt == this.amt && b.cap == this.cap;
    }

    void PourInto(Bucket other) {
        int remaining_space_in_other = other.cap - other.amt;
        int to_pour = Math.min(remaining_space_in_other, amt);
        this.amt -= to_pour;
        other.amt += to_pour;

        assert this.amt <= this.cap && this.amt >= 0;
        assert other.amt <= other.cap && other.amt >= 0;
    }
}
class Node {

    ArrayList<Bucket> buckets;

    Node(Bucket ... buckets_to_add) {
        this.buckets = new ArrayList<>(Arrays.asList(buckets_to_add));
    }

    Node(Node other) {
        this.buckets = new ArrayList<>(other.buckets.size());

        for (Bucket b : other.buckets) {
            this.buckets.add(new Bucket(b));
        }
    }

    ArrayList<Node> getAdjacentNodes() {
        ArrayList<Node> adj = new ArrayList<Node>();

        for (int i = 0; i < this.buckets.size(); i++) {
            for (int j = 0; j < this.buckets.size(); j++) {
                if (i == j) continue;

                Node node_clone = new Node(this);
                node_clone.buckets.get(i).PourInto(node_clone.buckets.get(j));

                if (!node_clone.equals(this)) {
                    adj.add(node_clone);
                }
            }
        }

        return adj;
    }


    @Override
    public int hashCode() {
        int total = 0;
        int mul = 1;
        for (Bucket b : this.buckets) {
            total += mul*b.amt;
            mul*=10;
        }
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Node)) {
            return false;
        }
        Node n = (Node) o;
        return this.buckets.equals(n.buckets);
    }

    @Override
    public String toString() {
        return this.buckets.stream()
                .map(b -> String.format("%d/%d", b.amt, b.cap))
                .collect(Collectors.joining(" "));
    }
}
