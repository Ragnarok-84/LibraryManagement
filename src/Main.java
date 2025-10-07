import model.Book;
import model.Reader;

public class Main {
    public static void main(String[] args) {
        Library lib = new Library();

        Book b1 = new Book("001", "Clean Code", "Robert Martin", 2008, 2);
        Book b2 = new Book("002", "Design Patterns", "GoF", 1994, 1);
        Reader r1 = new Reader(1, "Nguyen Kien", "kien@gmail.com");

        lib.addBook(b1);
        lib.addBook(b2);
        lib.addReader(r1);

        lib.borrowBook(r1, b1);
        lib.borrowBook(r1, b2);

        lib.returnBook(r1, b1);
        System.out.println("Top books:");
        lib.showTopBorrowedBooks();
    }
}
