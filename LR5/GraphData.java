import java.io.Serializable;
import java.util.List;

public class GraphData implements Serializable {
    private int numVertices;
    private List<Edge> edges;

    public GraphData(int numVertices, List<Edge> edges) {
        this.numVertices = numVertices;
        this.edges = edges;
    }

    public int getNumVertices() {
        return numVertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }
}