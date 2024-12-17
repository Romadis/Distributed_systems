// Блокирующий режим
import mpi.*;
import java.util.ArrayList;
import java.util.List;

public class Blocking {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (size < 2) {
            System.out.println("Код должен быть запущен с >= 2 процессами");
            MPI.Finalize();
            return;
        }

        long startTime, endTime;
        startTime = System.currentTimeMillis();

        if (rank == 0) {
            GraphData graphData = generateGraphData();

            GraphData[] dataToSend = new GraphData[]{graphData};
            for (int i = 1; i < size; i++) {
                MPI.COMM_WORLD.Send(dataToSend, 0, 1, MPI.OBJECT, i, 0);
            }
        } else {
            GraphData[] dataReceived = new GraphData[1];
            MPI.COMM_WORLD.Recv(dataReceived, 0, 1, MPI.OBJECT, 0, 0);
            GraphData graphDataRecv = dataReceived[0];

            boolean isHypercube = checkIsomorphism(graphDataRecv);

            byte[] sendBuffer = new byte[]{(byte) (isHypercube ? 1 : 0)};
            MPI.COMM_WORLD.Send(sendBuffer, 0, 1, MPI.BYTE, 0, 0);
        }

        if (rank == 0) {
            byte[] recvResults = new byte[size - 1];
            for (int i = 1; i < size; i++) {
                MPI.COMM_WORLD.Recv(recvResults, i - 1, 1, MPI.BYTE, i, 0);
            }

            boolean isHypercubeOverall = true;
            for (byte result : recvResults) {
                isHypercubeOverall &= (result != 0);
            }

            endTime = System.currentTimeMillis();

            if (isHypercubeOverall) {
                System.out.println("Да! Граф является гиперкубом");
            } else {
                System.out.println("Увы! Граф не является гиперкубом");
            }
            System.out.println("Программа завершена за " + (endTime - startTime) + " мс. с кол-ом процессов = " + size);
        }
        MPI.Finalize();
    }

    private static GraphData generateGraphData() {
        int numVertices = 10;
        return generateHypercube(numVertices);
    }

    private static boolean checkIsomorphism(GraphData data) {
        int numVertices = data.getNumVertices();
        List<Edge> edges = data.getEdges();

        int n = 0;
        while (Math.pow(2, n) < numVertices) {
            n++;
        }

        if (Math.pow(2, n) != numVertices) {
            return false;
        }

        for (Edge edge : edges) {
            int source = edge.getSource();
            int destination = edge.getDestination();

            int xor = source ^ destination;
            if (Integer.bitCount(xor) != 1) {
                return false;
            }
        }
        return true;
    }

    private static GraphData generateHypercube(int n) {
        int numVertices = (int) Math.pow(2, n);
        List<Edge> edges = new ArrayList<>();

        for (int i = 0; i < numVertices; i++) {
            for (int j = i + 1; j < numVertices; j++) {
                if (Integer.bitCount(i ^ j) == 1) {
                    edges.add(new Edge(i, j));
                }
            }
        }
        return new GraphData(numVertices, edges);
    }
}
