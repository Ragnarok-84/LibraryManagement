package model;

import java.time.LocalDate;

public class Reader {
    private int readerID;          // Khóa chính, INT
    private String fullName;       // Tên đầy đủ
    private String email;
    private String phone;
    private String address;
    private LocalDate joinDate;    // Ngày tham gia
    private boolean active;        // Tình trạng hoạt động (true/false)

    public Reader() {}

    public Reader(int readerID, String fullName, String email, String phone, String address, LocalDate joinDate, boolean active) {
        this.readerID = readerID;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.joinDate = joinDate;
        this.active = active;
    }

    // Constructor rút gọn (thêm mới)
    public Reader(String fullName, String email, String phone, String address) {
        this.fullName = fullName;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    // ===== UI-friendly output =====
    @Override
    public String toString() {
        return readerID + " - " + fullName + (active ? " (Hoạt động)" : " (Ngưng)");
    }
}
