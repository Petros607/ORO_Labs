package model;

import java.io.Serializable;
import java.sql.Date;

public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private int customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Date registrationDate;

    public Customer() {}

    public Customer(int customerId, String firstName, String lastName, String email,
                    String phone, Date registrationDate) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.registrationDate = registrationDate;
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Date getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }

    @Override
    public String toString() {
        return String.format("Customer{id=%d, name='%s %s', email='%s'}",
                customerId, firstName, lastName, email);
    }
}
