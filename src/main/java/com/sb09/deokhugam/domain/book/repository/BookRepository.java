package com.sb09.deokhugam.domain.book.repository;

import com.sb09.deokhugam.domain.book.entity.Book;
import com.sb09.deokhugam.domain.book.repository.custom.CustomBookRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, UUID>, CustomBookRepository {

  boolean existsByIsbnAndDeletedAtIsNull(String isbn);

  Optional<Book> findByIsbn(String isbn);

  @Modifying
  @Query("DELETE FROM Book b WHERE b.id = :id")
  void hardDeleteById(UUID id);

}
