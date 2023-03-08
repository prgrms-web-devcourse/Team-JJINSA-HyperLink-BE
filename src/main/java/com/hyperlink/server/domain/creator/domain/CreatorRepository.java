package com.hyperlink.server.domain.creator.domain;

import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.dto.CreatorAndSubscriptionCountMapper;
import com.hyperlink.server.domain.creator.dto.SubscribeFlagMapper;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreatorRepository extends JpaRepository<Creator, Long> {
  Optional<Creator> findByName(String name);

  @Query(value = "select c from Creator c join fetch c.category",
  countQuery = "select count(c) from Creator c")
  Page<Creator> findCreators(Pageable pageable);

  @Query(value = "select c.id as creatorId, c.name as name, count(sub.creator.id) as subscriberAmount, c.description as description, c.profileImgUrl as profileImgUrl from Creator c "
      + "left join Subscription sub "
      + "on c.id = sub.creator.id "
      + "group by c.id ")
  Slice<CreatorAndSubscriptionCountMapper> findAllCreators(Pageable pageable);

  @Query("select c.id as creatorId, c.name as name, count(sub.creator.id) as subscriberAmount, c.description as description, c.profileImgUrl as profileImgUrl from Creator c "
      + "left join Subscription sub "
      + "on c.id = sub.creator.id "
      + "where c.category.id = :categoryId "
      + "group by c.id ")
  Slice<CreatorAndSubscriptionCountMapper> findAllCreatorsByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

  @Query(
      "select c.id as creatorId, case when sub.member.id = :memberId then TRUE else FALSE end as isSubscribed from Creator c "
          + "left join Subscription sub "
          + "on c.id = sub.creator.id and sub.member.id = :memberId")
  Slice<SubscribeFlagMapper> findCreatorIdAndSubscribeFlagByMemberId(
      @Param("memberId") Long memberId, Pageable pageable);

  @Query(
      "select c.id as creatorId, case when sub.member.id = :memberId then TRUE else FALSE end as isSubscribed from Creator c "
          + "left join Subscription sub "
          + "on c.id = sub.creator.id and sub.member.id = :memberId "
          + "where c.category.id = :categoryId")
  Slice<SubscribeFlagMapper> findCreatorIdAndSubscribeFlagByMemberIdAndCategoryId(
      @Param("memberId") Long memberId, @Param("categoryId") Long categoryId, Pageable pageable);
}
