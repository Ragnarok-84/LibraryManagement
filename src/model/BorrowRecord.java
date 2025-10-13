package model;

import java.time.LocalDate;
import dao.BookDAO; // giả sử bạn có BookDAO

public class BorrowRecord {
    private final int readerId;
    private final String isbn;
    private final LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean returned;

    public BorrowRecord(int readerId, String isbn, LocalDate borrowDate, LocalDate dueDate, boolean returned) {
        this.readerId = readerId;
        this.isbn = isbn;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returned = returned;
    }

    public int getReaderId() { return readerId; }
    public String getIsbn() { return isbn; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isReturned() { return returned; }
    public void setDueDate(LocalDate dueDate){
        this.dueDate = dueDate;
    }

    public void markReturned() {
        returned = true;
    }


    public Book getBook() {
        BookDAO dao = new BookDAO();
        return dao.findByISBN(isbn);
    }


    @Override
    public String toString() {
        return readerId + " mượn " + isbn + " (" + borrowDate + " → " + dueDate + ")" + (returned ? " ✅" : " ❌");
    }
}
