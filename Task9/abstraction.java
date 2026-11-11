abstract class Vehicle {

    private final String regNo;

    Vehicle(String regNo) {
        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    abstract double ratePerHour();

    public double calculateFee(int minutes) {

        if (minutes <= 0) {
            return 0;
        }

        int hours = (minutes + 59) / 60;

        return hours * ratePerHour();
    }
}

interface Chargeable {

    double chargingFee(int kWh);

    default void chargingInfo() {
        System.out.println("Electric vehicle charging is available");
    }

    static boolean validateUnits(int kWh) {
        return kWh >= 0;
    }
}

interface Trackable {

    void updateLocation(String location);
}

class TwoWheeler extends Vehicle {

    TwoWheeler(String regNo) {
        super(regNo);
    }

    @Override
    double ratePerHour() {
        return 10;
    }
}

class Car extends Vehicle {

    Car(String regNo) {
        super(regNo);
    }

    @Override
    double ratePerHour() {
        return 30;
    }
}

class ElectricCar extends Car implements Chargeable, Trackable {

    ElectricCar(String regNo) {
        super(regNo);
    }

    @Override
    public double chargingFee(int kWh) {

        if (!Chargeable.validateUnits(kWh)) {
            return 0;
        }

        return kWh * 12;
    }

    @Override
    public void updateLocation(String location) {
        System.out.println("Location: " + location);
    }
}

public class Main {

    public static void main(String[] args) {

        TwoWheeler t = new TwoWheeler("TN01A1");

        Car c = new Car("TN02B2");

        ElectricCar e = new ElectricCar("TN03C3");

        System.out.println("TwoWheeler Fee: Rs. " 
                + t.calculateFee(130));

        System.out.println("Car Fee: Rs. " 
                + c.calculateFee(130));

        System.out.println("ElectricCar Parking Fee: Rs. " 
                + e.calculateFee(130));

        System.out.println("Charging Fee: Rs. " 
                + e.chargingFee(10));

        e.chargingInfo();

        e.updateLocation("Floor 2");
    }
}
