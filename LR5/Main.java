// Неблокирующий режим
import mpi.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static boolean checkIsHypercube(GraphData graphData) {
        int numVertices = graphData.getNumVertices();
        List<Edge> edges = graphData.getEdges();

        int n = 0;
        while (Math.pow(2, n) < numVertices) {
            n++;
        }
        if (Math.pow(2, n) != numVertices) {
            return false;
        }

        int[] degree = new int[numVertices];
        for (Edge edge : edges) {
            degree[edge.getSource()]++;
            degree[edge.getDestination()]++;
        }

        for (int d : degree) {
            if (d != n) {
                return false;
            }
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

    public static GraphData generateHypercube(int n) {
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

    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (size < 2) {
            System.out.println("Код должен быть запущен с >= 2-мя процессами");
            MPI.Finalize();
            return;
        }

        long startTime, endTime;
        startTime = System.currentTimeMillis();

        if (rank == 0) {
            // Генерация куба с 8 вершинами
            GraphData hypercube = generateHypercube(3);

            Status[] sendStatus = new Status[size - 1];
            Request[] sendRequests = new Request[size - 1];

            // Отправка данных всем остальным процессам
            GraphData[] dataToSend = new GraphData[]{hypercube};
            for (int i = 1; i < size; i++) {
                sendRequests[i - 1] = MPI.COMM_WORLD.Isend(dataToSend, 0, 1, MPI.OBJECT, i, 0);
            }

            // Ожидание завершения всех операций отправки
            for (int i = 0; i < size - 1; i++) {
                sendStatus[i] = sendRequests[i].Wait();
            }
        } else {
            // Процессы с рангами >= 1 принимают данные
            GraphData[] dataReceived = new GraphData[1];
            MPI.COMM_WORLD.Irecv(dataReceived, 0, 1, MPI.OBJECT, 0, 0).Wait();
            GraphData graphDataRecv = dataReceived[0];

            boolean isHypercube = checkIsHypercube(graphDataRecv);

            // Отправляем результат обратно главному процессу
            byte[] sendBuffer = new byte[]{(byte) (isHypercube ? 1 : 0)};
            MPI.COMM_WORLD.Isend(sendBuffer, 0, 1, MPI.BYTE, 0, 0);
        }

        if (rank == 0) {
            // Нулевой процесс собирает результаты проверки от остальных процессов
            byte[] recvResults = new byte[size - 1];
            Status[] recvStatus = new Status[size - 1];
            Request[] recvRequests = new Request[size - 1];

            for (int i = 1; i < size; i++) {
                recvRequests[i - 1] = MPI.COMM_WORLD.Irecv(recvResults, i - 1, 1, MPI.BYTE, i, 0);
            }

            // Ожидание завершения всех операций приёма результатов
            for (int i = 0; i < size - 1; i++) {
                recvStatus[i] = recvRequests[i].Wait();
            }

            boolean isHypercubeOverall = true;
            for (byte result : recvResults) {
                isHypercubeOverall &= (result != 0);
            }

            endTime = System.currentTimeMillis();

//            if (isHypercubeOverall) {
//                System.out.println("Да! Граф является гиперкубом");
//            } else {
//                System.out.println("Увы! Граф не является гиперкубом");
//            }

            List<Edge> edges = new ArrayList<>();
            edges.add(new Edge(0, 1));
            edges.add(new Edge(1, 2));
            edges.add(new Edge(2, 3));
            edges.add(new Edge(3, 0));
//            edges.add(new Edge(0, 1)); // 00 - 01
//            edges.add(new Edge(0, 2)); // 00 - 10
//            edges.add(new Edge(1, 3)); // 01 - 11
//            edges.add(new Edge(2, 3)); // 10 - 11
            GraphData graph = new GraphData(4, edges);
            
            boolean isHypercube = checkIsHypercube(graph);
            if (isHypercube) {
                System.out.println("Да! Граф является гиперкубом");
            } else {
                System.out.println("Увы! Граф не является гиперкубом");
            }
            System.out.println("Программа завершена за " + (endTime - startTime) + " мс. с кол-ом процессов = " + size);
        }
        MPI.Finalize();
    }
}