import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// Interface for pricing rules
interface PricingRule {

    double calculateFee(LocalDateTime entry, LocalDateTime exit);
}

// Hourly pricing rule
class HourlyPricingRule implements PricingRule {

    private final double ratePerHour;

    HourlyPricingRule(double rate) {
        ratePerHour = rate;
    }

    @Override
    public double calculateFee(LocalDateTime entry, LocalDateTime exit) {

        long minutes = Duration.between(entry, exit).toMinutes();

        if (minutes < 0) {
            throw new IllegalArgumentException("Invalid parking duration");
        }

        if (minutes == 0) {
            return 0;
        }

        long hours = (minutes + 59) / 60;

        return hours * ratePerHour;
    }
}

// Flat fee pricing rule
class FlatFeePricingRule implements PricingRule {

    private final double flatFee;

    FlatFeePricingRule(double flatFee) {
        this.flatFee = flatFee;
    }

    @Override
    public double calculateFee(LocalDateTime entry, LocalDateTime exit) {

        long minutes = Duration.between(entry, exit).toMinutes();

        if (minutes < 0) {
            throw new IllegalArgumentException("Invalid parking duration");
        }

        if (minutes == 0) {
            return 0;
        }

        return flatFee;
    }
}

// Delivery van time-based pricing rule
class DeliveryVanPricingRule implements PricingRule {

    private final double carRate;

    DeliveryVanPricingRule(double carRate) {
        this.carRate = carRate;
    }

    @Override
    public double calculateFee(LocalDateTime entry, LocalDateTime exit) {

        long minutes = Duration.between(entry, exit).toMinutes();

        if (minutes < 0) {
            throw new IllegalArgumentException("Invalid parking duration");
        }

        if (minutes == 0) {
            return 0;
        }

        long hours = (minutes + 59) / 60;

        double fee = 0;

        for (long i = 0; i < hours; i++) {

            LocalTime hourStart = entry.plusHours(i).toLocalTime();

            if (!hourStart.isBefore(LocalTime.of(9, 0))
                    && hourStart.isBefore(LocalTime.of(17, 0))) {

                fee += carRate * 2;
            } else {

                fee += carRate;
            }
        }

        return fee;
    }
}

// Abstract vehicle class
abstract class Vehicle {

    private final String regNo;
    private final PricingRule pricingRule;

    protected Vehicle(String regNo, PricingRule pricingRule) {

        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid registration number");
        }

        if (pricingRule == null) {
            throw new IllegalArgumentException("Pricing rule is required");
        }

        this.regNo = regNo;
        this.pricingRule = pricingRule;
    }

    public String getRegNo() {
        return regNo;
    }

    public double calculateParkingFee(LocalDateTime entry,
                                      LocalDateTime exit) {

        return pricingRule.calculateFee(entry, exit);
    }
}

// Two-wheeler
class TwoWheeler extends Vehicle {

    TwoWheeler(String regNo) {
        super(regNo, new HourlyPricingRule(10));
    }
}

// Car
class Car extends Vehicle {

    Car(String regNo) {
        super(regNo, new HourlyPricingRule(30));
    }
}

// Interface for EV charging
interface Chargeable {

    double chargingFee(int kWh);
}

// Electric car
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

// New vehicle category: E-bike
class EBike extends Vehicle {

    EBike(String regNo) {
        super(regNo, new FlatFeePricingRule(20));
    }
}

// New vehicle category: Delivery van
class DeliveryVan extends Vehicle {

    DeliveryVan(String regNo) {
        super(regNo, new DeliveryVanPricingRule(30));
    }
}

// Interface for parking passes
interface Pass {

    boolean isValidFor(LocalDate entryDate, LocalDate exitDate);
}

// Monthly pass for students and staff
class MonthlyPass implements Pass {

    private final LocalDate validFrom;
    private final LocalDate validTo;

    MonthlyPass(LocalDate validFrom, LocalDate validTo) {

        if (validFrom == null || validTo == null
                || validTo.isBefore(validFrom)) {

            throw new IllegalArgumentException("Invalid monthly pass dates");
        }

        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    @Override
    public boolean isValidFor(LocalDate entryDate, LocalDate exitDate) {

        return !entryDate.isBefore(validFrom)
                && !exitDate.isAfter(validTo);
    }
}

// One-day pass for visitors
class OneDayPass implements Pass {

    private final LocalDate passDate;

    OneDayPass(LocalDate passDate) {

        if (passDate == null) {
            throw new IllegalArgumentException("Invalid pass date");
        }

        this.passDate = passDate;
    }

    @Override
    public boolean isValidFor(LocalDate entryDate, LocalDate exitDate) {

        return entryDate.equals(passDate)
                && exitDate.equals(passDate);
    }
}

// Parking ticket
class ParkingTicket {

    private static int counter = 1000;

    private final String ticketNumber;
    private final Vehicle vehicle;
    private final int floor;
    private final int slot;
    private final LocalDateTime entryTime;
    private final Pass pass;

    ParkingTicket(Vehicle vehicle, int floor, int slot,
                  LocalDateTime entryTime, Pass pass) {

        ticketNumber = "P" + (++counter);

        this.vehicle = vehicle;
        this.floor = floor;
        this.slot = slot;
        this.entryTime = entryTime;
        this.pass = pass;
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

    public Pass getPass() {
        return pass;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }
}

// Parking facility
class ParkingFacility {

    private final Vehicle[][] slots;

    private final Map<String, ParkingTicket> activeTickets =
            new HashMap<>();

    private double totalCollection = 0;

    ParkingFacility(int[] slotsPerFloor) {

        if (slotsPerFloor == null || slotsPerFloor.length == 0) {
            throw new IllegalArgumentException("Invalid floor configuration");
        }

        slots = new Vehicle[slotsPerFloor.length][];

        for (int f = 0; f < slotsPerFloor.length; f++) {

            if (slotsPerFloor[f] < 0) {
                throw new IllegalArgumentException("Invalid slot count");
            }

            slots[f] = new Vehicle[slotsPerFloor[f]];
        }
    }

    // Display floor capacities
    public void displayCapacity() {

        int[] capacities = new int[slots.length];

        for (int f = 0; f < slots.length; f++) {
            capacities[f] = slots[f].length;
        }

        System.out.println("Number of floors: " + slots.length);

        System.out.println(
                "Slots per floor: " + Arrays.toString(capacities)
        );
    }

    // Create vehicle objects
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

            case "E_BIKE":
                return new EBike(regNo);

            case "DELIVERY_VAN":
                return new DeliveryVan(regNo);

            default:
                return null;
        }
    }

    // Allocate parking slot and generate ticket
    public String parkVehicle(String type, String regNo,
                              LocalDateTime entryTime, Pass pass) {

        if (regNo == null || regNo.trim().isEmpty()) {
            return "Invalid registration number";
        }

        if (entryTime == null) {
            return "Invalid entry time";
        }

        if (activeTickets.containsKey(regNo)) {
            return "Duplicate vehicle registration number";
        }

        Vehicle vehicle = createVehicle(type, regNo);

        if (vehicle == null) {
            return "Unknown vehicle type";
        }

        // Search for available slot
        for (int f = 0; f < slots.length; f++) {

            for (int s = 0; s < slots[f].length; s++) {

                if (slots[f][s] == null) {

                    slots[f][s] = vehicle;

                    ParkingTicket ticket = new ParkingTicket(
                            vehicle, f, s, entryTime, pass
                    );

                    activeTickets.put(regNo, ticket);

                    return "Ticket " + ticket.getTicketNumber()
                            + " issued for " + regNo
                            + " at Floor " + f
                            + ", Slot " + s;
                }
            }
        }

        return "Parking facility is full";
    }

    // Calculate fee and release parking slot
    public String exitVehicle(String regNo,
                              LocalDateTime exitTime,
                              int chargingKWh) {

        ParkingTicket ticket = activeTickets.get(regNo);

        if (ticket == null) {
            return "No active parking ticket found";
        }

        if (exitTime == null) {
            return "Invalid exit time";
        }

        if (chargingKWh < 0) {
            return "Invalid charging units";
        }

        LocalDateTime entryTime = ticket.getEntryTime();

        if (exitTime.isBefore(entryTime)) {
            return "Invalid exit time";
        }

        Vehicle vehicle = ticket.getVehicle();

        if (!(vehicle instanceof Chargeable) && chargingKWh != 0) {
            return "Charging is available only for electric cars";
        }

        double parkingFee = 0;

        Pass pass = ticket.getPass();

        boolean passValid = pass != null
                && pass.isValidFor(
                        entryTime.toLocalDate(),
                        exitTime.toLocalDate()
                );

        if (!passValid) {

            parkingFee = vehicle.calculateParkingFee(
                    entryTime, exitTime
            );
        }

        double chargingFee = 0;

        if (vehicle instanceof Chargeable) {

            chargingFee =
                    ((Chargeable) vehicle).chargingFee(chargingKWh);
        }

        double totalFee = parkingFee + chargingFee;

        // Release the slot
        slots[ticket.getFloor()][ticket.getSlot()] = null;

        // Remove active ticket
        activeTickets.remove(regNo);

        totalCollection += totalFee;

        return regNo
                + " -> Parking: Rs. " + parkingFee
                + ", Charging: Rs. " + chargingFee
                + ", Total: Rs. " + totalFee;
    }

    public double getTotalCollection() {
        return totalCollection;
    }
}

// Main class
public class Main {

    public static void main(String[] args) {

        // Five floors with different slot capacities
        int[] floorSlots = {20, 25, 30, 35, 40};

        ParkingFacility parking =
                new ParkingFacility(floorSlots);

        LocalDate date = LocalDate.of(2026, 11, 11);

        System.out.println("CAMPUS SMART-PARKING SYSTEM");

        parking.displayCapacity();

        System.out.println("\nVEHICLE ENTRY");

        // E-bike
        System.out.println(
                parking.parkVehicle(
                        "E_BIKE",
                        "TN01EB1111",
                        date.atTime(8, 0),
                        null
                )
        );

        // Delivery van
        System.out.println(
                parking.parkVehicle(
                        "DELIVERY_VAN",
                        "TN02DV2222",
                        date.atTime(9, 0),
                        null
                )
        );

        // Visitor purchases a one-day pass
        Pass visitorPass = new OneDayPass(date);

        System.out.println(
                parking.parkVehicle(
                        "CAR",
                        "TN03CA3333",
                        date.atTime(11, 0),
                        visitorPass
                )
        );

        // Electric car
        System.out.println(
                parking.parkVehicle(
                        "ELECTRIC_CAR",
                        "TN04EV4444",
                        date.atTime(14, 0),
                        null
                )
        );

        // Existing two-wheeler category
        System.out.println(
                parking.parkVehicle(
                        "TWO_WHEELER",
                        "TN05TW5555",
                        date.atTime(7, 0),
                        null
                )
        );

        System.out.println("\nVEHICLE EXIT");

        // E-bike: flat parking fee
        System.out.println(
                parking.exitVehicle(
                        "TN01EB1111",
                        date.atTime(10, 0),
                        0
                )
        );

        // Delivery van: double car rate during 9 AM to 5 PM
        System.out.println(
                parking.exitVehicle(
                        "TN02DV2222",
                        date.atTime(10, 30),
                        0
                )
        );

        // Visitor: valid one-day pass
        System.out.println(
                parking.exitVehicle(
                        "TN03CA3333",
                        date.atTime(13, 0),
                        0
                )
        );

        // Electric car: parking plus charging fee
        System.out.println(
                parking.exitVehicle(
                        "TN04EV4444",
                        date.atTime(15, 0),
                        2
                )
        );

        // Two-wheeler
        System.out.println(
                parking.exitVehicle(
                        "TN05TW5555",
                        date.atTime(8, 0),
                        0
                )
        );

        System.out.println(
                "\nTotal collection: Rs. "
                + parking.getTotalCollection()
        );
    }
}
