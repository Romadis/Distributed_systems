import mpi.*;
public class Main {
    public static void main(String[] args) throws Exception{
        MPI.Init(args);
        int myRank, size;
        int[] buf = new int[1];
        int[] s = new int[1];
        int tag = 0;
        myRank = MPI.COMM_WORLD.Rank();
        size = MPI.COMM_WORLD.Size();
        buf[0] = myRank;

        // Блокирующий режим
//        if (size > 1) {
//            if (myRank == 0) {
//                System.out.println("Процесс №" + myRank + " отправил сообщение \"" + buf[0] + "\" процессу №" + (myRank + 1));
//                MPI.COMM_WORLD.Sendrecv(buf, 0, 1, MPI.INT, myRank+1, tag, s, 0, 1, MPI.INT, size-1, tag);
//                // MPI.COMM_WORLD.Send(buf, 0, 1, MPI.INT, myRank + 1, tag);
//                // MPI.COMM_WORLD.Recv(buf, 0, 1, MPI.INT, (size-1), tag);
//                System.out.println("Процесс №" + myRank + " получил сообщение \"" + s[0] + "\" от процесса №" + (size-1));
//                System.out.println("Полная сумма = " + s[0]);
//            } else {
//                int source_process = (myRank - 1 + size) % size;
//                int destination_process = (myRank + 1) % size;
//                s[0] = buf[0];
//                MPI.COMM_WORLD.Recv(buf, 0, 1, MPI.INT, source_process, tag);
//                System.out.println("Процесс №" + myRank + " получил сообщение \"" + buf[0] + "\" от процесса №" + source_process);
//                s[0] += buf[0];
//                System.out.println("Процесс №" + myRank + " отправил сообщение \"" + s[0] + "\" процессу №" + destination_process);
//                MPI.COMM_WORLD.Send(s, 0, 1, MPI.INT, destination_process, tag);
//            }
//        } else {
//            System.out.println("Недостаточное количество процессов");
//        }

        // Неблокирующий режим
        Request request;
        if (size > 1) {
            if (myRank == 0) {
                System.out.println("Процесс №" + myRank + " отправил сообщение \"" + buf[0] + "\" процессу №" + (myRank + 1));
                request = MPI.COMM_WORLD.Isend(buf, 0, 1, MPI.INT, myRank+1, tag);
                request = MPI.COMM_WORLD.Irecv(s, 0, 1, MPI.INT, size-1, tag);

                request.Wait();
                System.out.println("Процесс №" + myRank + " получил сообщение \"" + s[0] + "\" от процесса №" + (size-1));
                System.out.println("Полная сумма = " + s[0]);
            } else {
                int source_process = (myRank - 1 + size) % size;
                int destination_process = (myRank + 1) % size;
                s[0] = buf[0];
                request = MPI.COMM_WORLD.Irecv(buf, 0, 1, MPI.INT, source_process, tag);
                request.Wait();
                System.out.println("Процесс №" + myRank + " получил сообщение \"" + buf[0] + "\" от процесса №" + source_process);
                s[0] += buf[0];
                request = MPI.COMM_WORLD.Isend(s, 0, 1, MPI.INT, destination_process, tag);
                System.out.println("Процесс №" + myRank + " отправил сообщение \"" + s[0] + "\" процессу №" + destination_process);
            }
        } else {
            System.out.println("Недостаточное количество процессов");
        }
        MPI.Finalize();
    }
}