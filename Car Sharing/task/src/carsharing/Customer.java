package carsharing;

public class Customer {
    long id;
    String name;
    long carId;

    public Customer(long id, String name, long carId) {
        this.id = id;
        this.name = name;
        this.carId = carId;
    }

    public Customer(long id, String name) {
        this.id = id;
        this.name = name;
        this.carId = 0L;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getCarId() {
        return carId;
    }
}
