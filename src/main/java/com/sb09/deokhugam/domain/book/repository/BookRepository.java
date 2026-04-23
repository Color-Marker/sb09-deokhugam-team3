package com.sb09.deokhugam.domain.book.repository;

import com.sb09.deokhugam.domain.book.entity.Book;
import com.sb09.deokhugam.domain.book.repository.custom.CustomBookRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, UUID>, CustomBookRepository {

  //  boolean existsByIsbnAndDeletedAtIsNull(String isbn);
  @Query("SELECT COUNT(b) > 0 FROM Book b WHERE b.isbn = :isbn AND b.deletedAt IS NULL")
  boolean existsByIsbnAndDeletedAtIsNull(@Param("isbn") String isbn);

  Optional<Book> findByIsbn(String isbn);

  @Modifying
  @Query("DELETE FROM Book b WHERE b.id = :id")
  void hardDeleteById(UUID id);

}
