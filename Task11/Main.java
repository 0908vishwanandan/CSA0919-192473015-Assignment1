import java.time.Duration;
import java.time.LocalDateTime;

// Interface for electric vehicle charging
interface Chargeable {

    double chargingFee(int kWh);
}

// Abstract parent class
abstract class Vehicle {

    private final String regNo;

    protected Vehicle(String regNo) {

        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid registration number");
        }

        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    protected abstract double ratePerHour();

    // Calculate parking fee
    public double calculateParkingFee(long minutes) {

        if (minutes < 0) {
            throw new IllegalArgumentException("Invalid duration");
        }

        if (minutes == 0) {
            return 0;
        }

        long hours = (minutes + 59) / 60;

        return hours * ratePerHour();
    }
}

// Two-wheeler subclass
class TwoWheeler extends Vehicle {

    TwoWheeler(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 10;
    }
}

// Car subclass
class Car extends Vehicle {

    Car(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 30;
    }
}

// Electric car subclass
class ElectricCar extends Car implements Chargeable {

    ElectricCar(String regNo) {
        super(regNo);
    }

    @Override
    public double chargingFee(int kWh) {

        if (kWh < 0) {
            throw new IllegalArgumentException("Invalid charging units");
        }

        return kWh * 12;
    }
}

// Monthly parking pass
class ParkingPass {

    private final boolean valid;

    ParkingPass(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }
}

// Parking ticket
class ParkingTicket {

    private static int nextNumber = 1000;

    private final String ticketNumber;
    private final Vehicle vehicle;
    private final int floor;
    private final int slot;
    private final LocalDateTime entryTime;
    private final ParkingPass pass;

    ParkingTicket(Vehicle vehicle, int floor, int slot,
                  LocalDateTime entryTime, ParkingPass pass) {

        this.ticketNumber = "P" + (++nextNumber);
        this.vehicle = vehicle;
        this.floor = floor;
        this.slot = slot;
        this.entryTime = entryTime;
        this.pass = pass;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public int getFloor() {
        return floor;
    }

    public int getSlot() {
        return slot;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public ParkingPass getPass() {
        return pass;
    }
}

// Main parking facility
class ParkingFacility {

    private final Vehicle[][] slots;

    // Active tickets are stored in an array
    private final ParkingTicket[] activeTickets;

    private double totalCollection;

    ParkingFacility(int[] slotsPerFloor) {

        if (slotsPerFloor == null || slotsPerFloor.length == 0) {
            throw new IllegalArgumentException("At least one floor is required");
        }

        slots = new Vehicle[slotsPerFloor.length][];

        int totalSlots = 0;

        for (int f = 0; f < slotsPerFloor.length; f++) {

            if (slotsPerFloor[f] < 0) {
                throw new IllegalArgumentException("Invalid slot count");
            }

            slots[f] = new Vehicle[slotsPerFloor[f]];

            totalSlots += slotsPerFloor[f];
        }

        activeTickets = new ParkingTicket[totalSlots];
    }

    // Create vehicle according to its type
    private Vehicle createVehicle(String type, String regNo) {

        if (type == null) {
            return null;
        }

        switch (type.toUpperCase()) {

            case "TWO_WHEELER":
                return new TwoWheeler(regNo);

            case "CAR":
                return new Car(regNo);

            case "ELECTRIC_CAR":
                return new ElectricCar(regNo);

            default:
                return null;
        }
    }

    // Find active ticket using registration number
    private int findTicketIndex(String regNo) {

        for (int i = 0; i < activeTickets.length; i++) {

            if (activeTickets[i] != null &&
                activeTickets[i].getVehicle().getRegNo().equals(regNo)) {

                return i;
            }
        }

        return -1;
    }

    // Find an empty ticket position
    private int findEmptyTicketIndex() {

        for (int i = 0; i < activeTickets.length; i++) {

            if (activeTickets[i] == null) {
                return i;
            }
        }

        return -1;
    }

    // Find an available parking slot
    private int[] findAvailableSlot() {

        for (int f = 0; f < slots.length; f++) {

            for (int s = 0; s < slots[f].length; s++) {

                if (slots[f][s] == null) {
                    return new int[]{f, s};
                }
            }
        }

        return null;
    }

    // Validate floor and slot position
    public boolean isValidSlotPosition(int floor, int slot) {

        return floor >= 0 && floor < slots.length &&
               slot >= 0 && slot < slots[floor].length;
    }

    // Release occupied slot
    private boolean releaseSlot(int floor, int slot) {

        if (!isValidSlotPosition(floor, slot) ||
            slots[floor][slot] == null) {

            return false;
        }

        slots[floor][slot] = null;

        return true;
    }

    // Vehicle entry operation
    public String parkVehicle(String type, String regNo,
                              LocalDateTime entryTime,
                              boolean validMonthlyPass) {

        if (regNo == null || regNo.trim().isEmpty()) {
            return "Rejected: invalid registration number";
        }

        if (entryTime == null) {
            return "Rejected: entry time is missing";
        }

        if (findTicketIndex(regNo) != -1) {
            return "Rejected: duplicate registration number " + regNo;
        }

        Vehicle vehicle = createVehicle(type, regNo);

        if (vehicle == null) {
            return "Rejected: unknown vehicle type " + type;
        }

        int[] position = findAvailableSlot();

        if (position == null) {
            return "Parking full: no slot available for " + regNo;
        }

        int ticketIndex = findEmptyTicketIndex();

        if (ticketIndex == -1) {
            return "Parking full: no ticket space available";
        }

        slots[position[0]][position[1]] = vehicle;

        ParkingPass pass = new ParkingPass(validMonthlyPass);

        ParkingTicket ticket = new ParkingTicket(
            vehicle,
            position[0],
            position[1],
            entryTime,
            pass
        );

        activeTickets[ticketIndex] = ticket;

        return "Ticket " + ticket.getTicketNumber() +
               ": " + regNo +
               " allocated at Floor " + position[0] +
               ", Slot " + position[1];
    }

    // Vehicle exit operation
    public String exitVehicle(String regNo,
                              LocalDateTime exitTime,
                              int chargingKWh) {

        int ticketIndex = findTicketIndex(regNo);

        if (ticketIndex == -1) {
            return "Exit rejected: no active ticket for " + regNo;
        }

        if (exitTime == null) {
            return "Exit rejected: exit time is missing";
        }

        ParkingTicket ticket = activeTickets[ticketIndex];

        long minutes = Duration.between(
            ticket.getEntryTime(),
            exitTime
        ).toMinutes();

        if (minutes < 0) {
            return "Exit rejected: invalid exit time";
        }

        Vehicle vehicle = ticket.getVehicle();

        double parkingFee = ticket.getPass().isValid()
                ? 0.0 : vehicle.calculateParkingFee(minutes);

        double chargingFee = 0.0;

        if (vehicle instanceof Chargeable) {

            try {
                chargingFee =
                    ((Chargeable) vehicle).chargingFee(chargingKWh);

            } catch (IllegalArgumentException e) {
                return "Exit rejected: invalid charging units";
            }

        } else if (chargingKWh != 0) {

            return "Exit rejected: charging units are only valid for electric cars";
        }

        if (!releaseSlot(ticket.getFloor(), ticket.getSlot())) {
            return "Exit failed: parking slot could not be released";
        }

        double totalFee = parkingFee + chargingFee;

        totalCollection += totalFee;

        activeTickets[ticketIndex] = null;

        return regNo +
               " | Duration: " + minutes +
               " min | Parking: Rs. " + parkingFee +
               " | Charging: Rs. " + chargingFee +
               " | Total: Rs. " + totalFee;
    }

    // Return total collection
    public double getTotalCollection() {
        return totalCollection;
    }
}

// Main class
public class Main {

    public static void main(String[] args) {

        ParkingFacility parking =
            new ParkingFacility(new int[]{2, 1});

        LocalDateTime entry =
            LocalDateTime.of(2026, 11, 11, 9, 0);

        System.out.println("PARKING ENTRY");

        System.out.println(
            parking.parkVehicle("TWO_WHEELER",
                "TN01AA1111", entry, false)
        );

        System.out.println(
            parking.parkVehicle("CAR",
                "TN02BB2222", entry, false)
        );

        System.out.println(
            parking.parkVehicle("ELECTRIC_CAR",
                "TN03CC3333", entry, false)
        );

        // Parking full test
        System.out.println(
            parking.parkVehicle("CAR",
                "TN04DD4444", entry, false)
        );

        // Duplicate registration test
        System.out.println(
            parking.parkVehicle("TWO_WHEELER",
                "TN01AA1111", entry, false)
        );

        // Unknown vehicle type test
        System.out.println(
            parking.parkVehicle("BUS",
                "TN05EE5555", entry, false)
        );

        // Invalid slot test
        System.out.println(
            "Invalid slot position (5, 0): " +
            parking.isValidSlotPosition(5, 0)
        );

        System.out.println("\nPARKING EXIT");

        // Zero-minute parking
        System.out.println(
            parking.exitVehicle(
                "TN01AA1111", entry, 0)
        );

        // Student with valid monthly pass
        System.out.println(
            parking.parkVehicle("TWO_WHEELER",
                "TN04DD4444", entry, true)
        );

        System.out.println(
            parking.exitVehicle(
                "TN04DD4444",
                entry.plusMinutes(120), 0)
        );

        // Car exits after exactly 60 minutes
        System.out.println(
            parking.exitVehicle(
                "TN02BB2222",
                entry.plusMinutes(60), 0)
        );

        // Electric car exits after 61 minutes
        System.out.println(
            parking.exitVehicle(
                "TN03CC3333",
                entry.plusMinutes(61), 2)
        );

        System.out.printf(
            "\nTotal daily collection: Rs. %.2f%n",
            parking.getTotalCollection()
        );
    }
}
