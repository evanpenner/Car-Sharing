package carsharing;

public class Company {
    long id;
    String name;

    public Company(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Company) {
            return ((Company) obj).id == this.id;
        }
        if (obj instanceof Long) {
            return (Long) obj == this.id;
        }
        return false;
    }

    @Override
    public String toString() {
        return id + ". " + name;
    }
}
