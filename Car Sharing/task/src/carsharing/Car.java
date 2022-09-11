package carsharing;

public class Car {
    long id;
    String name;
    long companyId;

    public Car(long id, String name, long companyId) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getCompanyId() {
        return companyId;
    }

    @Override
    public String toString() {
        return id + ". " + name;
    }
}
