import mpi.*;
import mpi.MPI;

public class Main
{
    public static void main(String[] args)
    {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int TAG = 0;
        int s = 0;
        int buf = rank;

        int nextRank = (rank + 1) % size; // Ранг следующего процесса по кольцу (если он последний,то станет первым)
        int prevRank = (rank - 1 + size) % size; // Ранг предыдущего процесса по кольцу (если он  первый, то станет последним)
        if (rank == 0) {
            // Массив для получения данных от соседнего процесса (блокирующий режим)
            int[] output = new int[]{0};

            // Одновременная отправка и прием данных
            MPI.COMM_WORLD.Sendrecv(new int[]{buf}, 0, 1, MPI.INT, nextRank, TAG,
                    output, 0, 1, MPI.INT, prevRank, TAG);

            System.out.println("Общая сумма: " + output[0]);
        } else {
            // Массив для получения данных от предыдущего процеса (неблокирующий режим)
            int[] output = new int[]{0};

            MPI.COMM_WORLD.Recv(output, 0, 1, MPI.INT, prevRank, TAG);

            // Суммирование полученных данных с данными текущего процессора
            s += output[0] + rank;
            System.out.println("Сумма: " + s);
            MPI.COMM_WORLD.Send(new int[]{s}, 0, 1, MPI.INT, nextRank, TAG);
        }

        MPI.Finalize();
    }

}
