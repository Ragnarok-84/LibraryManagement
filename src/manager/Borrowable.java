package manager;

import model.BorrowRecord;
import model.Book;
import model.Reader;

import java.util.List;

public interface Borrowable {

    BorrowRecord borrowItem(Reader reader, Book book, java.time.LocalDate dueDate) throws Exception;
    BorrowRecord returnItem(int recordId) throws Exception;
    List<BorrowRecord> findOverdueRecords();
}
