package com.sb09.deokhugam.domain.user.config;

import com.sb09.deokhugam.domain.user.entity.Users;
import com.sb09.deokhugam.domain.user.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserBatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;
  private final UserRepository userRepository;
  //스프링 부트에서 메트릭(숫자 데이터)을 관리하는 중앙 저장소. 여기에 데이터를 등록하면 Actuator가 이를 읽어 API로 노출함
  private final MeterRegistry meterRegistry;

  //전체적인 배치 프로세스를 정의하고 여기서는
  //deleteUserStep 하나만 실행하도록 설정됨
  @Bean
  public Job deleteUserJob(Step deleteUserStep) {
    return new JobBuilder("deleteUserJob", jobRepository)
        .start(deleteUserStep)
        .build();
  }

  //Tasklet이 "실제로 방을 청소하는 행위"라면, Step은 "오늘의 청소 1단계: 거실 청소"라는 공식적인 작업 단위로 등록하는 것이다
  //이렇게 등록(캡슐화)을 해두어야만, 나중에 "거실 청소는 끝났는데 안방 청소에서 에러가 났네?
  //그럼 거실은 완료로 기록하고 안방만 다시 하자!" 같은 지능적인 배치 관리가 가능해진다
  //나중에 삭제뿐만 아니라 '휴면 계정 전환', '통계 생성' 등 단계가 늘어날 때,
  //이 Step 구조 덕분에 훨씬 안전하게 기능을 확장할 수 있다
  @Bean
  public Step deleteUserStep(Tasklet deleteUserTasklet) {
    return new StepBuilder("deleteUserStep", jobRepository)
        .tasklet(deleteUserTasklet, platformTransactionManager)
        .build();
  }

  //실제 DB에서 유저를 삭제한다
  //하루가 지난 탈퇴 유저를 찾아 deleteAll을 수행한다
  @Bean
  public Tasklet deleteUserTasklet() {
    return (contribution, chunkContext) -> {
      LocalDateTime threshold = LocalDateTime.now().minusDays(1);
      List<Users> targets =
          userRepository.findAllByDeletedAtBefore(threshold);
      if (!targets.isEmpty()) {
        userRepository.deleteAll(targets);
        log.info("{}명의 유저가 완전히 삭제되었습니다.",
            targets.size());
      }

      //아래 코드 두줄의 목적 : API 호출 한 번으로 "지금까지 총 몇 명이 삭제됐지?"를 바로 알 수 있게 함
      // "value": 15.0 <- JSON 형식의 데이터가 응답
      //아래 메트릭이 나타날려면 배치가 최소 한 번은 실행된 이후여야함

      //배치가 한 번 돌 때 삭제된 유저 수를 누적해서 더함
      meterRegistry.counter("user.deletion.count").increment(targets.size());
      //삭제된 데이터가 있든 없든, 배치가 '실행'된 횟수 자체를 1씩 올림
      meterRegistry.counter("user.deletion.batch.runs").increment();
      return RepeatStatus.FINISHED;
    };
  }
}
