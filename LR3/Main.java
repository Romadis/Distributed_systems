import java.util.Arrays;
import java.util.Random;
import mpi.*;

public class Main {
    public static void main(String[] args)
    {
        MPI.Init(args);
        Random random = new Random();
        Request req;

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // Массивы, в которые будут записаны отсортированные данные от различных процессов
        int[] sortedArr1;
        int[] sortedArr2;

        // Массив для отправки и получения одного числа
        int[] number = new int[1];

        // Массив для хранения окончательного отсортированного результата после объединения данных
        int[] resultArr = new int[size-2];


        if (size < 5) {
            System.out.println("Ошибка: ранг < 5)");
            MPI.Finalize();
            return;
        }


        if (size % 2 == 0)
        {
            sortedArr1 = new int[((size+1)-3)/2];
            sortedArr2 = new int[((size+1)-3)/2];
        }
        else
        {
            sortedArr1 = new int[((size+1)-3)/2];
            sortedArr2 = new int[(((size+1)-3)/2)+1];
        }


        if (rank >= 1 && rank < size/2)
        {
            int randomIntBounded = random.nextInt(100);
            number[0] = randomIntBounded;
            MPI.COMM_WORLD.Isend(number, 0, 1, MPI.INT, size / 2, 0);
        }


        if (rank == size / 2)
        {
            for (int i = 0; i < sortedArr1.length; i++)
            {
                req = MPI.COMM_WORLD.Irecv(number, 0, 1, MPI.INT, i + 1, 0);
                req.Wait();
                // Сбор данных в массив sortedArr1, его сортировка и отправка процессу 0
                sortedArr1[i] = number[0];
            }
            Arrays.sort(sortedArr1);
            MPI.COMM_WORLD.Isend(sortedArr1, 0, sortedArr1.length, MPI.INT, 0, 0);
        }


        if (rank >= (size/2 + 1) && rank < size)
        {
            int randomIntBounded = random.nextInt(100);
            number[0] = randomIntBounded;
            MPI.COMM_WORLD.Isend(number, 0, 1, MPI.INT, size - 1, 0);
        }


        if (rank == size - 1)
        {
            for (int i = 0; i < sortedArr2.length; i++)
            {
                req = MPI.COMM_WORLD.Irecv(number, 0, 1, MPI.INT, size / 2 + 1 + i, 0);
                req.Wait();
                sortedArr2[i] = number[0];
            }
            Arrays.sort(sortedArr2);
            MPI.COMM_WORLD.Isend(sortedArr2, 0, sortedArr2.length, MPI.INT, 0, 0);
        }


        if (rank == 0)
        {
            Request[] requests = new Request[2];
            requests[0] = MPI.COMM_WORLD.Irecv(sortedArr1, 0, sortedArr1.length, MPI.INT, size / 2, 0);
            requests[1] = MPI.COMM_WORLD.Irecv(sortedArr2, 0, sortedArr2.length, MPI.INT, size - 1, 0);
            Request.Waitall(requests);

            for (int i: sortedArr1)
            {
                System.out.print("(" + i + ")");
            }

            for (int i: sortedArr2)
            {
                System.out.print("[" + i + "]");
            }

            System.arraycopy(sortedArr1, 0, resultArr, 0, sortedArr1.length);
            System.arraycopy(sortedArr2, 0, resultArr, sortedArr1.length, sortedArr2.length);

            Arrays.sort(resultArr);
            System.out.println(" ");
            for (int i : resultArr)
            {
                System.out.print(i + " ");
            }
        }
        MPI.Finalize();
    }
}