package com.hyperlink.server.domain.memberContent.domain;

import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.memberContent.domain.entity.MemberContent;
import com.hyperlink.server.domain.memberContent.dto.ContentViewerAgeAndGenderRecommendDto;
import com.hyperlink.server.domain.memberContent.dto.ContentViewerCompanyRecommendDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberContentRepository extends JpaRepository<MemberContent, Long> {

  Optional<MemberContent> findMemberContentByMemberIdAndContentAndType(Long memberId,
      Content content, int type);

  @Override
  void delete(MemberContent entity);

  @Query("select mc from MemberContent mc join fetch mc.content where mc.memberId = :memberId")
  Slice<MemberContent> findMemberContentForSlice(@Param("memberId") Long memberId,
      Pageable pageable);

  boolean existsMemberContentByMemberIdAndContentIdAndType(Long memberId, Long contentId, int type);

  @Query(value = "select c.name as companyName, c.logo_img_url as logoImgUrl from member_content mc "
      + "inner join member m "
      + "on mc.member_id = m.member_id "
      + "inner join company c "
      + "on c.company_id = m.company_id "
      + "where mc.content_id = :contentId and mc.type = 1 and c.is_using_recommend = 1 "
      + "group by c.name "
      + "having count(c.name) >= :standardRecommendCount", nativeQuery = true)
  List<ContentViewerCompanyRecommendDto> recommendCompanies(@Param("contentId") Long contentId,
      @Param("standardRecommendCount") int standardRecommendCount);

  @Query(value = "select case "
      + "when YEAR(:now) - m.birth_year between 0 and 19 then 10 "
      + "when YEAR(:now) - m.birth_year between 20 and 29 then 20 "
      + "when YEAR(:now) - m.birth_year between 30 and 39 then 30 "
      + "when YEAR(:now) - m.birth_year >= 40 then 40 "
      + "end as age, gender "
      + "from member_content mc "
      + "inner join member m "
      + "on mc.member_id = m.member_id "
      + "where mc.content_id = :contentId and mc.type = 1 and m.birth_year is not null and m.gender is not null "
      + "group by age, gender "
      + "having count(age) >= :standardRecommendCount", nativeQuery = true)
  List<ContentViewerAgeAndGenderRecommendDto> recommendAgeAndGender(
      @Param("now") LocalDate now,
      @Param("contentId") Long contentId,
      @Param("standardRecommendCount") int standardRecommendCount);
}
