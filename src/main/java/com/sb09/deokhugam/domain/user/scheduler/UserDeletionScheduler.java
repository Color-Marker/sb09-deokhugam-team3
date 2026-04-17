package com.sb09.deokhugam.domain.user.scheduler;

import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletionScheduler {

  private final UserRepository userRepository;

  /**
   * 프로토타입 요구사항: 5분마다 논리 삭제된 지 5분이 지난 유저를 찾아 물리 삭제
   * 실제 운영 시에는 minusDays(1)로 변경
   */
  @Scheduled(fixedDelay = 300000)
  @Transactional
  public void processScheduledDeletion() {
    LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
    List<Users> targets = userRepository.findAllByDeletedAtBefore(threshold);

    if (!targets.isEmpty()) {
      userRepository.deleteAll(targets);
      log.info("스케줄러 동작: {}명의 유저가 완전히 삭제되었습니다.", targets.size());
    }
  }
}
