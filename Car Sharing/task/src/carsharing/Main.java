package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:./src/carsharing/db/";

    //  Database credentials
    static Connection conn = null;
    static String name = "randomdb";

    public static void main(String[] args) {

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-databaseFileName")) {
                name = args[i + 1];
                break;
            }
        }
        System.out.println(name);
        // write your code here

        Statement stmt = null;
        try {
            // STEP 1: Register JDBC driver
            createConnection();
            // sendSQL("DROP TABLE IF EXISTS CUSTOMER");
            //sendSQL("DROP TABLE IF EXISTS CAR");
            //sendSQL("DROP TABLE IF EXISTS COMPANY");

            stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS `COMPANY` (\n" + "  `ID` INT(20) NOT NULL AUTO_INCREMENT,\n" + "  `NAME` VARCHAR(20) NOT NULL UNIQUE,\n" + "  PRIMARY KEY (`ID`)\n" + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                stmt.close();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        try {
            //System.out.println("Trying to create car table");
            stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS CAR (\n" + "  ID int AUTO_INCREMENT,\n" + "  NAME VARCHAR(20) UNIQUE NOT NULL,\n" + "  `COMPANY_ID` int NOT NULL,\n" + "  PRIMARY KEY (ID),\n" + "  FOREIGN KEY (`COMPANY_ID`) REFERENCES COMPANY(ID)\n" + ");\n";
            stmt.executeUpdate(sql);
            stmt.close();
            sendSQL("CREATE TABLE IF NOT EXISTS CUSTOMER (\n" + "  ID int AUTO_INCREMENT,\n" + "  NAME varchar UNIQUE NOT NULL,\n" + "  RENTED_CAR_ID int,\n" + "  PRIMARY KEY (ID),\n" + "  FOREIGN KEY (`RENTED_CAR_ID`) REFERENCES CAR(ID)\n" + ");");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                stmt.close();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        //clearTables();
        start();
        closeConnection();
    }

    static void clearTables() {
        try {
            sendSQL("DELETE FROM COMPANY");
            sendSQL("DELETE FROM CAR");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void start() {
        System.out.println("1. Log in as a manager\n" + "2. Log in as a customer\n" + "3. Create a customer\n" + "0. Exit");


        int input = input();
        switch (input) {
            case 0:
                exit();
                break;
            case 1:
                logIn();
                break;
            case 2:
                customerLogin();
                break;
            case 3:
                createCustomer();
                break;
            default:
                start();
                break;
        }
    }

    static void createCustomer() {
        System.out.println("Enter the customer name:");
        String input = readString();
        try {
            sendSQL("INSERT INTO CUSTOMER(NAME) VALUES ('" + input + "');");
            System.out.println("The customer was added!");
            System.out.println();
            start();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    static void customerLogin() {
        Statement selectStmt = null;
        try {
            selectStmt = conn.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ResultSet rs;
        try {
            rs = selectStmt.executeQuery("SELECT * FROM CUSTOMER;");
            List<Customer> customerList = new ArrayList<>();

            while (rs.next()) {
                try {
                    customerList.add(new Customer(rs.getLong(1), rs.getString(2), rs.getLong(3)));
                } catch (Exception e) {
                    customerList.add(new Customer(rs.getLong(1), rs.getString(2)));
                }
                //Fourth Column
            }
            if (customerList.size() == 0) {
                System.out.println("The customer list is empty!");
                System.out.println();
                start();
            } else {
                System.out.println("Customer list:");
                AtomicInteger count = new AtomicInteger(1);
                customerList.forEach(c -> {
                    System.out.println(count.getAndIncrement() + ". " + c.getName());
                });
//                int input = input();
//                for (Company c : carList) {
//                    if (c.id == input) {
//                        selectCompany(c);
//                        break;
//                    }
//                }
                System.out.println("0. Back");

                int input = input();
                if (input == 0) {
                    start();
                    return;
                }
                Customer customer = customerList.get(input - 1);
                selectCustomer(customer);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void selectCustomer(Customer customer) {
        System.out.println("1. Rent a car\n" + "2. Return a rented car\n" + "3. My rented car\n" + "0. Back");
        switch (input()) {
            case 0:
                customerLogin();
                break;
            case 1:
                rentCar(customer);
                break;
            case 2:
                returnCar(customer);
                break;
            case 3:
                viewCar(customer);
                break;
            default:
                customerLogin();
        }
    }

    static void viewCar(Customer customer) {
        if (customer.getCarId() == 0) {
            System.out.println("You didn't rent a car!");
            System.out.println();
            selectCustomer(customer);
            return;
        }
        Statement selectStmt = null;
        try {
            selectStmt = conn.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ResultSet rs;
        try {
            rs = selectStmt.executeQuery("SELECT * FROM CAR WHERE ID = " + customer.getCarId() + ";");
            List<Car> carList = new ArrayList<>();

            while (rs.next()) {
                carList.add(new Car(rs.getLong(1), rs.getString(2), rs.getLong(3)));
            }
            if (carList.size() == 0) {
                System.out.println("You didn't rent a car!");
                System.out.println();
                selectCustomer(customer);
                return;
            }
            Car car = carList.get(0);
            System.out.println("Your rented car:");
            System.out.println(car.getName());
            selectStmt = conn.createStatement();
            rs = selectStmt.executeQuery("SELECT * FROM COMPANY WHERE ID = " + car.getCompanyId() + ";");
            rs.next();
            System.out.println("Company: ");
            System.out.println(rs.getString(2));
            System.out.println();
            selectCustomer(customer);
        } catch (Exception e) {

        }
    }

    static void returnCar(Customer customer) {
        if (customer.getCarId() == 0) {
            System.out.println("You didn't rent a car!");
            selectCustomer(customer);
            return;
        }
        try {
            sendSQL("UPDATE CUSTOMER SET RENTED_CAR_ID = NULL WHERE ID = " + customer.getId() + ";");
            customer.carId = 0;
            System.out.println();
            System.out.println("You've returned a rented car!");
            System.out.println();
            selectCustomer(customer);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    static void rentCar(Customer customer) {
        if (customer.getCarId() != 0) {
            System.out.println("You've already rented a car!");
            System.out.println();
            selectCustomer(customer);
            return;
        }
        Statement selectStmt = null;
        try {
            selectStmt = conn.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ResultSet rs;
        try {
            rs = selectStmt.executeQuery("SELECT * FROM COMPANY;");
            List<Company> companyList = new ArrayList<>();

            while (rs.next()) {
                companyList.add(new Company(rs.getLong(1), rs.getString(2)));
            }
            if (companyList.size() == 0) {
                System.out.println("No companies available!");
                System.out.println();
                selectCustomer(customer);
                return;
            }
            System.out.println("Choose a company:");
            AtomicInteger count = new AtomicInteger(1);
            companyList.forEach(c -> {
                System.out.println(count.getAndIncrement() + ". " + c.getName());
            });
            System.out.println("0. Back");
            int input = input();
            if (input == 0) {
                selectCustomer(customer);
                return;
            }
            rentCar(customer, companyList.get(input - 1));
        } catch (Exception e) {

        }
    }

    static void rentCar(Customer customer, Company company) {
        Statement selectStmt = null;
        try {
            selectStmt = conn.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ResultSet rs;
        try {
            rs = selectStmt.executeQuery("SELECT * FROM CAR WHERE COMPANY_ID = " + company.getId() + ";");
            List<Car> carList = new ArrayList<>();

            while (rs.next()) {
                carList.add(new Car(rs.getLong(1), rs.getString(2), rs.getLong(3)));
                //Fourth Column
            }
            if (carList.size() == 0) {
                System.out.println("The car list is empty!");
                System.out.println();
                rentCar(customer);
            } else {
                System.out.println("Choose a car:");
                AtomicInteger count = new AtomicInteger(1);
                carList.forEach(c -> {
                    System.out.println(count.getAndIncrement() + ". " + c.getName());
                });
                System.out.println("0. Back");
                int input = input();
                if (input == 0) {
                    rentCar(customer);
                    return;
                }
                System.out.println();
                Car car = carList.get(input - 1);
                sendSQL("UPDATE CUSTOMER SET RENTED_CAR_ID = " + car.getId() + " WHERE ID = " + customer.getId() + ";");
                customer.carId = car.getId();
                System.out.println("You rented '" + car.getName() + "'");
                System.out.println();
                selectCustomer(customer);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void closeConnection() {
        try {
            conn.close();
            conn = null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void createConnection() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);

        //STEP 2: Open a connection
        System.out.println("Connecting to database...");
        conn = DriverManager.getConnection(DB_URL + name);
        conn.setAutoCommit(true);
    }

    static int input() {
        System.out.println();
        System.out.print(">");
        Scanner in = new Scanner(System.in);
        try {
            int input = in.nextInt();
            return input;
        } catch (Exception e) {
            return input();
        }

    }

    static String readString() {
        System.out.println();
        System.out.print(">");
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        return input;
    }

    static void sendSQL(String query) throws SQLException {
        Statement stmt;
        stmt = conn.createStatement();

        stmt.executeUpdate(query);
        stmt.close();
    }

    static void logIn() {
        System.out.println("1. Company list\n" + "2. Create a company\n" + "0. Back");
        switch (input()) {
            case 0:
                start();
                break;
            case 1:
                companyList();
                break;
            case 2:
                createCompany();
                break;
        }
    }

    static void companyList() {
        Statement selectStmt = null;
        try {
            selectStmt = conn.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ResultSet rs = null;
        try {
            rs = selectStmt.executeQuery("SELECT * FROM COMPANY;");
            List<Company> companyList = new ArrayList<>();

            while (rs.next()) {
                companyList.add(new Company(rs.getLong(1), rs.getString(2)));
                //Fourth Column
            }
            if (companyList.size() == 0) {
                System.out.println("The company list is empty!");
                System.out.println();
                logIn();
            } else {
                System.out.println("Choose a company::");
                AtomicInteger count = new AtomicInteger(1);
                companyList.forEach(c -> {
                    System.out.println(count.getAndIncrement() + ". " + c.getName());
                });
                System.out.println("0. Back");
                int input = input();
                if (input == 0) {
                    logIn();
                    return;
                }
                selectCompany(companyList.get(input - 1));

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void carList(Company company) {
        Statement selectStmt = null;
        try {
            selectStmt = conn.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ResultSet rs;
        try {
            rs = selectStmt.executeQuery("SELECT * FROM CAR WHERE COMPANY_ID = " + company.getId() + ";");
            List<Car> carList = new ArrayList<>();

            while (rs.next()) {
                carList.add(new Car(rs.getLong(1), rs.getString(2), rs.getLong(3)));
                //Fourth Column
            }
            if (carList.size() == 0) {
                System.out.println("The car list is empty!");
                System.out.println();
                selectCompany(company);
            } else {
                System.out.println("'" + company.getName() + "' cars:");
                AtomicInteger count = new AtomicInteger(1);
                carList.forEach(c -> {
                    System.out.println(count.getAndIncrement() + ". " + c.getName());
                });
//                int input = input();
//                for (Company c : carList) {
//                    if (c.id == input) {
//                        selectCompany(c);
//                        break;
//                    }
//                }
                System.out.println();
                System.out.println();
                selectCompany(company);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void selectCompany(Company company) {
        System.out.println("'" + company.name + "' company:");
        System.out.println("1. Car list\n" + "2. Create a car\n" + "0. Back");
        int input = input();
        switch (input) {
            case 0:
                logIn();
                break;
            case 1:
                carList(company);
                break;
            case 2:
                createCar(company);
                break;
            default:
                selectCompany(company);
                break;
        }
    }

    static void createCar(Company company) {
        System.out.println("Enter the car name:");
        String carName = readString();
        try {
            sendSQL("INSERT INTO CAR (NAME, COMPANY_ID) VALUES ('" + carName + "', " + company.getId() + ");");
            System.out.println("The car was added!");
            selectCompany(company);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    static void createCompany() {
        System.out.println("Enter the company name:");
        String companyName = readString();
        createSQLCompany(companyName);

    }

    static void createSQLCompany(String name) {
        try {
            sendSQL("INSERT INTO COMPANY (NAME) VALUES ('" + name + "');");
            System.out.println("The company was created!");
            logIn();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void exit() {
        System.out.println("Goodbye!");
        System.exit(0);
    }

}