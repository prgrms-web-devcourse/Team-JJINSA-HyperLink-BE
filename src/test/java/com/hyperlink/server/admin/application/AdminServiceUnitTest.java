package com.hyperlink.server.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hyperlink.server.domain.admin.application.AdminService;
import com.hyperlink.server.domain.admin.dto.CategoryViewResponses;
import com.hyperlink.server.domain.admin.dto.CountingViewByCategoryDto;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService Mock 테스트")
public class AdminServiceUnitTest {

  @Nested
  @DisplayName("[어드민] 7일간의 날짜별, 카테고리별 조회수 조회 기능은")
  class CountViewCountsByCategoryAndDate {

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    AdminService adminService;

    @AllArgsConstructor
    class CountingViewByCategoryDtoImpl implements CountingViewByCategoryDto {

      Long categoryId;
      String categoryName;
      int viewCount;

      @Override
      public Long getCategoryId() {
        return categoryId;
      }

      @Override
      public String getCategoryName() {
        return categoryName;
      }

      @Override
      public int getViewCount() {
        return viewCount;
      }
    }


    @Nested
    @DisplayName("[성공]")
    class Success {

      @Test
      @DisplayName("기준일을 미포함하여 이전 7일간의 카테고리별 조회수를 조회한다.")
      public void countPastSevenDaysViewCountsByCategory() throws Exception {
        CountingViewByCategoryDtoImpl develop = new CountingViewByCategoryDtoImpl(1L, "develop",
            133);
        CountingViewByCategoryDtoImpl beauty = new CountingViewByCategoryDtoImpl(2L, "beauty",
            152);
        when(categoryRepository.countViewsByCategoryAndDate(any())).thenReturn(
            List.of(develop, beauty));

        CategoryViewResponses categoryViewResponses = adminService.countViewCountByCategory();
        assertThat(categoryViewResponses.weeklyViewCounts()).hasSize(7);
        assertThat(categoryViewResponses.createdDate()).isInstanceOf(String.class);
        assertThat(categoryViewResponses.weeklyViewCounts().get(0).results()).hasSize(2);
        assertThat(categoryViewResponses.weeklyViewCounts().get(0).results().get(0)
            .getCategoryName()).isEqualTo("develop");
        assertThat(categoryViewResponses.weeklyViewCounts().get(0).results().get(0)
            .getViewCount()).isEqualTo(133);
        assertThat(categoryViewResponses.weeklyViewCounts().get(0).results().get(1)
            .getCategoryName()).isEqualTo("beauty");
        assertThat(categoryViewResponses.weeklyViewCounts().get(0).results().get(1)
            .getViewCount()).isEqualTo(152);
      }
    }
  }

}
