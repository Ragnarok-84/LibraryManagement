package manager;

import java.util.List;
import java.util.Optional;


public interface Searchable<T> {

    List<T> search(String query);
    Optional<T> searchById(int id);
}
