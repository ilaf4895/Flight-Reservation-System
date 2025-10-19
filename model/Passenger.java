package model;

import java.util.Objects;

public class Passenger {
    private String passengerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private int age;

    public Passenger(String passengerId, String firstName, String lastName,
                     String email, String phoneNumber, int age) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }

        this.passengerId = passengerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.age = age;
    }

    public String getPassengerId() { return passengerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public int getAge() { return age; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isAdult() {
        return age >= 18;
    }

    public boolean isChild() {
        return age >= 2 && age < 18;
    }

    public boolean isInfant() {
        return age < 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        return Objects.equals(passengerId, passenger.passengerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passengerId);
    }
}