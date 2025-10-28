package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import static util.DBConnection.getConnection;

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

    public void updateReader(Reader r) {
        String sql = "UPDATE readers SET name=?, email=?, phone=?, address=?, is_active=? WHERE reader_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getName());
            ps.setString(2, r.getEmail());
            ps.setString(3, r.getPhone());
            ps.setString(4, r.getAddress());
            ps.setBoolean(5, r.isActive());
            ps.setInt(6, r.getReaderID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    @Override
    public String toString() {
        return readerID + " - " + name + (active ? " (Hoạt động)" : " (Ngưng)");
    }
}
