package com.hyperlink.server.creator.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.creator.application.CreatorService;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollRequest;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollResponse;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.notRecommendCreator.domain.NotRecommendCreatorRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatorService Mock 테스트")
public class CreatorServiceMockTest {

  @Mock
  MemberRepository memberRepository;

  @Mock
  NotRecommendCreatorRepository notRecommendCreatorRepository;

  @Mock
  CategoryRepository categoryRepository;

  @Mock
  CreatorRepository creatorRepository;

  @Nested
  @DisplayName("크리에이터 생성 메서드는")
  class CreatorEnrollTest {

    @Test
    @DisplayName("성공하면 크리에이터를 생성한다.")
    public void success() throws Exception {
      Category developCategory = new Category("개발");
      CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("크리에이터 이름",
          "profileImgUrl", "크리에이터입니다.", developCategory.getName());
      Creator creator = CreatorEnrollRequest.toCreator(creatorEnrollRequest, developCategory);

      when(categoryRepository.findByName(developCategory.getName())).thenReturn(
          Optional.of(developCategory));
      when(creatorRepository.save(any()))
          .thenReturn(new Creator("크리에이터 이름",
              "profileImgUrl", "크리에이터입니다.", developCategory));
      CreatorService creatorService = new CreatorService(memberRepository, creatorRepository, categoryRepository,
          notRecommendCreatorRepository);
      CreatorEnrollResponse creatorEnrollResponse = creatorService.enrollCreator(
          creatorEnrollRequest);


      Assertions.assertNotNull(creatorEnrollResponse);
    }

    @Test
    @DisplayName("저장하려고하는 크리에이터의 카테고리가 없으면 CategoryNotFoundException이 발생한다.")
    public void fail() throws Exception {

      CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("크리에이터 이름",
          "profileImgUrl", "크리에이터입니다.", "emptyCategory");

      when(categoryRepository.findByName(any())).thenThrow(new CategoryNotFoundException());
      CreatorService creatorService = new CreatorService(memberRepository, creatorRepository, categoryRepository,
          notRecommendCreatorRepository);

      Assertions.assertThrows(CategoryNotFoundException.class, () -> {
        creatorService.enrollCreator(creatorEnrollRequest);
      });
    }
  }

  @Nested
  @DisplayName("크리에이터 삭제 메서드는")
  class CreatorDeleteTest {
    @Test
    @DisplayName("성공하면 크리에이터를 삭제한다.")
    public void success() throws Exception {
      long existCreatorId = 1L;
      CreatorService creatorService = new CreatorService(memberRepository, creatorRepository, categoryRepository,
          notRecommendCreatorRepository);

      creatorService.deleteCreator(existCreatorId);

      verify(creatorRepository, times(1)).deleteById(existCreatorId);
    }

    @Test
    @DisplayName("실패하면 CreatorNotFoundException을 발생한다.")
    public void fail() {
      long notExistCreatorId = 1L;
      CreatorService creatorService = new CreatorService(memberRepository, creatorRepository, categoryRepository,
          notRecommendCreatorRepository);

      doThrow(new CreatorNotFoundException()).when(creatorRepository).deleteById(notExistCreatorId);

      Assertions.assertThrows(CreatorNotFoundException.class,
          () -> creatorService.deleteCreator(notExistCreatorId));
    }
  }
}
