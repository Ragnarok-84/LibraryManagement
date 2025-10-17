import model.Book;
import model.BorrowRecord;
import model.Reader;

import java.time.LocalDate;
import java.util.*;

public class Library {
    private final List<Book> books;
    private final List<Reader> readers;
    private final List<BorrowRecord> records;

    public Library() {
        books = new ArrayList<>();
        readers = new ArrayList<>();
        records = new ArrayList<>();
    }

    public void addBook(Book book) { books.add(book); }
    public void addReader(Reader reader) { readers.add(reader); }

    public List<Book> searchByTitle(String keyword) {
        List<Book> result = new ArrayList<>();
        for (Book b : books)
            if (b.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                result.add(b);
        return result;
    }

    public void borrowBook(Reader reader, Book book) {
        if (book.getAvailable() > 0) {
            book.borrow();
            BorrowRecord record = new BorrowRecord();
            record.setReader(reader);
            record.setBook(book);
            record.setBorrowDate(LocalDate.now());
            records.add(record);
            System.out.println(reader.getName() + " borrowed " + book.getTitle());
        } else {
            System.out.println("No copies available for " + book.getTitle());
        }
    }

    public void returnBook(Reader reader, Book book) {
        for (BorrowRecord r : records) {
            if (r.getReader() != null && r.getBook() != null && r.getReader().getReaderID() == reader.getReaderID() && r.getBook().getIsbn().equals(book.getIsbn()) && r.getReturnDate() == null) {
                r.setReturnDate(LocalDate.now());
                book.returnBook();
                System.out.println(reader.getName() + " returned " + book.getTitle());
                break;
            }
        }
    }

    public void showTopBorrowedBooks() {
        Map<String, Integer> borrowCount = new HashMap<>();
        for (BorrowRecord r : records) {
            borrowCount.put(r.getBook().getTitle(), borrowCount.getOrDefault(r.getBook().getTitle(), 0) + 1);
        }
        borrowCount.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(3)
                .forEach(e -> System.out.println(e.getKey() + " - " + e.getValue() + " times"));
    }

    public Reader findReaderById(int id) {
        for (Reader r : readers)
            if (r.getReaderID() == id)
                return r;
        return null;
    }

    public void showAllReaders() {
        for (Reader r : readers) {
            System.out.println(r);
        }
    }

}
