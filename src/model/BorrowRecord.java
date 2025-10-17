package model;

import java.time.LocalDate;

public class BorrowRecord {
    private long recordID;          // Khóa chính
    private int readerID;           // Khóa ngoại -> readers(readerID)
    private int bookID;             // Khóa ngoại -> books(bookID)

    private Reader reader;          // Tham chiếu đối tượng Reader (cho UI)
    private Book book;              // Tham chiếu đối tượng Book (cho UI)

    private LocalDate borrowDate;   // Ngày mượn
    private LocalDate dueDate;      // Hạn trả
    private LocalDate returnDate;   // Ngày trả thực tế
    private String status;          // Trạng thái: borrowed / returned / overdue

    public BorrowRecord() {}

    public BorrowRecord(int readerID, int bookID, LocalDate borrowDate, LocalDate dueDate) {
        this.readerID = readerID;
        this.bookID = bookID;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = "borrowed";
    }

    public BorrowRecord(long recordID, int readerID, int bookID, LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate, String status) {
        this.recordID = recordID;
        this.readerID = readerID;
        this.bookID = bookID;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // ===== Getter / Setter =====
    public long getRecordID() {
        return recordID;
    }

    public void setRecordID(long recordID) {
        this.recordID = recordID;
    }

    public int getReaderID() {
        return readerID;
    }

    public void setReaderID(int readerID) {
        this.readerID = readerID;
    }

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getStatus() {
        if (status != null) return status;

        // Nếu chưa có status từ DB, xác định động:
        if (returnDate != null) return "returned";
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) return "overdue";
        return "borrowed";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ===== Hỗ trợ hiển thị UI =====
    @Override
    public String toString() {
        String bookTitle = (book != null) ? book.getTitle() : ("Book#" + bookID);
        String readerName = (reader != null) ? reader.getFullName() : ("Reader#" + readerID);
        return "Record #" + recordID + " | " + readerName + " → " + bookTitle + " | " + getStatus();
    }
}
