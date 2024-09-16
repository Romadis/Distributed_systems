import mpi.*;
public class Main {
    public static void main(String[] args) {
        MPI.Init(args);
        
        int myRank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int[] message = new int[1];
        message[0] = myRank;

        if (myRank % 2 == 0) {
            if (myRank + 1 < size) {
                MPI.COMM_WORLD.Send(message, 0, 1, MPI.INT, myRank + 1, 0);
                System.out.println("Процесс №" + myRank + " отправил сообщение " + message[0] + " процессу №" + (myRank + 1));
            }
        }
        else {
            MPI.COMM_WORLD.Recv(message, 0, 1, MPI.INT, myRank - 1, 0);
            System.out.println("Процесс №" + myRank + " получил сообщение " + message[0] + " от процесса №" + (myRank - 1));
        }
        MPI.Finalize();
    }
}



