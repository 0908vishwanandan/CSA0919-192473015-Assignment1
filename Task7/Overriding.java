abstract class Vehicle {

    private final String regNo;

    Vehicle(String regNo) {
        this.regNo = regNo;
    }

    public final String getRegNo() {
        return regNo;
    }

    // Abstract method
    protected abstract double ratePerHour();

    // Calculates parking fee
    public double calculateFee(int minutes) {

        if (minutes < 0) {
            throw new IllegalArgumentException("Invalid duration");
        }

        int hours = (minutes + 59) / 60;

        return hours * ratePerHour();
    }

    // Override toString()
    @Override
    public String toString() {
        return "Vehicle Registration Number: " + regNo;
    }

    // Override equals()
    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Vehicle) {
            Vehicle v = (Vehicle) obj;
            return regNo.equals(v.regNo);
        }

        return false;
    }

    // Override hashCode()
    @Override
    public int hashCode() {
        return regNo.hashCode();
    }
}

class TwoWheeler extends Vehicle {

    TwoWheeler(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 10;
    }
}

class Car extends Vehicle {

    Car(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 30;
    }
}

class ElectricCar extends Car {

    ElectricCar(String regNo) {
        super(regNo);
    }
}

public class Main {

    public static void main(String[] args) {

        Vehicle[] vehicles = {
            new TwoWheeler("TN01A1"),
            new Car("TN02B2"),
            new ElectricCar("TN03C3")
        };

        for (Vehicle v : vehicles) {

            double fee = v.calculateFee(130);

            System.out.println(v.getClass().getSimpleName()
                    + " " + v.getRegNo() + " -> Rs " + fee);
        }

        System.out.println(
            new Car("TN02B2").equals(
                new ElectricCar("TN02B2")
            )
        );
    }
}
