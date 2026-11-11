import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class LookupBenchmark {

    private static volatile int sink = 0;

    // Search in Array
    private static ParkingTicket findInArray(
            ParkingTicket[] tickets, String regNo) {

        for (int i = 0; i < tickets.length; i++) {

            if (tickets[i] != null &&
                tickets[i].getVehicle().getRegNo().equals(regNo)) {

                return tickets[i];
            }
        }

        return null;
    }

    // Search in ArrayList
    private static ParkingTicket findInList(
            ArrayList<ParkingTicket> tickets, String regNo) {

        for (ParkingTicket ticket : tickets) {

            if (ticket.getVehicle().getRegNo().equals(regNo)) {
                return ticket;
            }
        }

        return null;
    }

    public static void main(String[] args) {

        int n = 5000;
        int repetitions = 500;

        ParkingTicket[] array = new ParkingTicket[n];

        ArrayList<ParkingTicket> list = new ArrayList<>();

        HashMap<String, ParkingTicket> map = new HashMap<>();

        LocalDateTime entry =
                LocalDateTime.of(2026, 11, 11, 9, 0);

        // Insert tickets into all three structures
        for (int i = 0; i < n; i++) {

            String regNo = String.format("TN%06d", i);

            ParkingTicket ticket = new ParkingTicket(
                    new Car(regNo),
                    0,
                    i,
                    entry,
                    new ParkingPass(false)
            );

            array[i] = ticket;
            list.add(ticket);
            map.put(regNo, ticket);
        }

        String target = String.format("TN%06d", n - 1);

        // Warm-up
        for (int i = 0; i < 100; i++) {

            sink += findInArray(array, target)
                    .getTicketNumber().hashCode();

            sink += findInList(list, target)
                    .getTicketNumber().hashCode();

            sink += map.get(target)
                    .getTicketNumber().hashCode();
        }

        // Measure Array
        long start = System.nanoTime();

        for (int i = 0; i < repetitions; i++) {
            sink += findInArray(array, target)
                    .getTicketNumber().hashCode();
        }

        long arrayTime = System.nanoTime() - start;

        // Measure ArrayList
        start = System.nanoTime();

        for (int i = 0; i < repetitions; i++) {
            sink += findInList(list, target)
                    .getTicketNumber().hashCode();
        }

        long listTime = System.nanoTime() - start;

        // Measure HashMap
        start = System.nanoTime();

        for (int i = 0; i < repetitions; i++) {
            sink += map.get(target)
                    .getTicketNumber().hashCode();
        }

        long mapTime = System.nanoTime() - start;

        System.out.println("Tickets: " + n);
        System.out.println("Lookup repetitions: " + repetitions);

        System.out.println("Array: " + arrayTime + " ns");

        System.out.println("ArrayList: " + listTime + " ns");

        System.out.println("HashMap: " + mapTime + " ns");
    }
}
