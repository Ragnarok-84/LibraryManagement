package model;

import java.time.LocalDate;

public class Reader {
    private int readerID;
    private String name;
    private String email;
    private String phone;
    private String address;
    private LocalDate joinDate;
    private boolean active;

    public Reader() {}

    public Reader(int readerID, String name, String email, String phone, String address, LocalDate joinDate, boolean active) {
        this.readerID = readerID;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.joinDate = joinDate;
        this.active = active;
    }


    public Reader(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.joinDate = LocalDate.now();
        this.active = true;
    }

    // ===== Getter / Setter =====
    public int getReaderID() {
        return readerID;
    }

    public void setReaderID(int readerID) {
        this.readerID = readerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    @Override
    public String toString() {
        return readerID + " - " + name + (active ? " (Hoạt động)" : " (Ngưng)");
    }
}
