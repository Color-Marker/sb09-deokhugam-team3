package com.sb09.deokhugam.domain.book.repository.custom;

import com.sb09.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.sb09.deokhugam.domain.book.entity.Book;
import org.springframework.data.domain.Slice;

public interface CustomBookRepository {

  Slice<Book> searchBooks(BookSearchCondition condition);

}
