package com.sb09.deokhugam.domain.user.repository;

import com.sb09.deokhugam.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// 임시로 만들어둔 레포지토리입니다.
public interface UserRepository extends JpaRepository<Users, UUID> {

}