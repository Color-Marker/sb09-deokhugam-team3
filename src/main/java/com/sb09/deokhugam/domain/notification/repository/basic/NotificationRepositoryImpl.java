package com.sb09.deokhugam.domain.notification.repository.basic;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.deokhugam.domain.notification.dto.request.NotificationListRequest;
import com.sb09.deokhugam.domain.notification.entity.Notification;
import com.sb09.deokhugam.domain.notification.entity.QNotification;
import com.sb09.deokhugam.domain.notification.repository.NotificationRepositoryCustom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QNotification notification = QNotification.notification;

  @Override
  public Slice<Notification> searchNotification(NotificationListRequest request) {
    boolean isDesc = request.getDirection() == Sort.Direction.DESC;

    List<Notification> results = queryFactory
        .selectFrom(notification)
        .where(
            notification.user.id.eq(request.getUserId()),
            cursorCondition(request)
        )
        .orderBy(
            isDesc ? notification.createdAt.desc() : notification.createdAt.asc(),
            isDesc ? notification.id.desc() : notification.id.asc()
        )
        .limit(request.getLimit() + 1)
        .fetch();

    boolean hasNext = results.size() > request.getLimit();
    if(hasNext){
      results.remove(request.getLimit());
    }

    return new SliceImpl<> (
        results,
        PageRequest.of(0, request.getLimit()),
        hasNext
    );
  }

  @Override
  public Long countNotification(NotificationListRequest request) {
    return queryFactory
        .select(notification.count())
        .from(notification)
        .where(notification.user.id.eq(request.getUserId()))
        .fetchOne();
  }

  @Override
  public long deleteOldNotification(LocalDateTime duration) {
    return queryFactory
        .delete(notification)
        .where(
            notification.confirmStatus.isTrue(),
            notification.createdAt.lt(duration)
        )
        .execute();
  }

  private BooleanExpression cursorCondition(NotificationListRequest request){
    if(request.getCursor() == null || request.getAfter() == null){
      // 첫 페이지임.
      return null;
    }

    UUID cursor = UUID.fromString(request.getCursor().toString());
    LocalDateTime after = request.getAfter();

    if(request.getDirection() == Direction.DESC){
      return notification.createdAt.lt(after)
          .or(notification.createdAt.eq(after).and(notification.id.lt(cursor)));
    }
    else{
      return notification.createdAt.gt(after)
          .or(notification.createdAt.eq(after).and(notification.id.gt(cursor)));
    }
  }
}
