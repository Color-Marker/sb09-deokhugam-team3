package com.sb09.deokhugam.domain.comment.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentsRepository extends JpaRepository {

  void hardDeleteById(UUID id);
}
