package com.hyperlink.server.domain.creator.domain;

import com.hyperlink.server.domain.creator.domain.vo.CreatorScore;
import com.hyperlink.server.domain.creator.domain.vo.RecommendCreatorPool;
import com.hyperlink.server.domain.creator.dto.CreatorAndSubscriptionCountMapper;
import com.hyperlink.server.domain.creator.dto.CreatorIdAndSubscriptionCountMapper;
import com.hyperlink.server.domain.creator.dto.GetCreatorRecommendResponse;
import com.hyperlink.server.domain.creator.dto.GetCreatorRecommendResponses;
import com.hyperlink.server.domain.creator.exception.CreatorEmptyException;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import com.hyperlink.server.domain.memberContent.dto.CreatorAndLikeCountByMember;
import com.hyperlink.server.domain.memberHistory.domain.MemberHistoryRepository;
import com.hyperlink.server.domain.notRecommendCreator.domain.NotRecommendCreatorRepository;
import com.hyperlink.server.domain.subscription.domain.SubscriptionRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreatorRecommendService {

  private static final int FIRST_INDEX = 0;
  private static final int LAST_INDEX = 29;
  private Map<Long, CreatorScore> creatorScoreMap;

  private final CreatorRepository creatorRepository;
  private final MemberHistoryRepository memberHistoryRepository;
  private final MemberContentRepository memberContentRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final NotRecommendCreatorRepository notRecommendCreatorRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  public GetCreatorRecommendResponses getRecommendCreators(Long memberId) {
    if (memberId == null) {
      List<Long> allCreatorIds = creatorRepository.findAllIds();
      List<GetCreatorRecommendResponse> randomCreators = extractRandomCreators(
          allCreatorIds, allCreatorIds.size());
      return new GetCreatorRecommendResponses(randomCreators);
    }

    Set<Object> recommendedPoolCreatorIdsSet = redisTemplate.opsForZSet()
        .reverseRange(memberId.toString(), FIRST_INDEX, LAST_INDEX);
    List<Long> recommendedPoolCreatorIds = Objects.requireNonNull(recommendedPoolCreatorIdsSet)
        .stream()
        .map(Object::toString)
        .map(Long::parseLong).toList();

    List<GetCreatorRecommendResponse> randomCreators = extractRandomCreators(
        recommendedPoolCreatorIds, LAST_INDEX);
    return new GetCreatorRecommendResponses(randomCreators);
  }

  public List<GetCreatorRecommendResponse> extractRandomCreators(
      List<Long> recommendedPoolCreatorIds, int randomIntRange) {
    List<GetCreatorRecommendResponse> getCreatorRecommendResponses = new ArrayList<>();

    Random random = new Random();
    for (int i = 0; i < 4; i++) {
      int randomIndex = random.nextInt(randomIntRange);
      Long creatorId = recommendedPoolCreatorIds.get(randomIndex);

      CreatorAndSubscriptionCountMapper creatorAndSubscriptionCount = creatorRepository.findCreatorAndSubscriptionCount(
          creatorId);
      GetCreatorRecommendResponse getCreatorRecommendResponse = new GetCreatorRecommendResponse(
          creatorId, creatorAndSubscriptionCount.getName(),
          creatorAndSubscriptionCount.getProfileImgUrl(),
          creatorAndSubscriptionCount.getDescription(),
          creatorAndSubscriptionCount.getSubscriberAmount());
      getCreatorRecommendResponses.add(getCreatorRecommendResponse);
    }

    return getCreatorRecommendResponses;
  }

  public void initCreatorScore() {
    creatorScoreMap = new HashMap<>();
  }

  public List<CreatorScore> getCreatorScoreList(Member member) {
    List<Long> subscriptionCreatorIds = subscriptionRepository.findCreatorIdByMemberId(
        member.getId());
    List<Long> notRecommendCreatorIds = notRecommendCreatorRepository.findNotRecommendCreatorIdByMemberId(
        member.getId());

    return creatorScoreMap.values().stream()
        .filter(creatorScore -> !subscriptionCreatorIds.contains(creatorScore.getCreatorId()))
        .filter(creatorScore -> !notRecommendCreatorIds.contains(creatorScore.getCreatorId()))
        .toList();
  }

  public void saveRecommendCreatorPool(RecommendCreatorPool recommendCreatorPool) {
    String memberId = String.valueOf(recommendCreatorPool.getMemberId());
    recommendCreatorPool.getCreatorScores().forEach(
        creatorScore -> {
          String value = String.valueOf(creatorScore.getCreatorId());
          redisTemplate.opsForZSet().remove(memberId, value);
          redisTemplate.opsForZSet().add(memberId, value, creatorScore.getScore());
        }
    );
  }

  public void calculateScore(Member member) {
    calculateScoreByCreatorPerformance(member);
  }

  private void calculateScoreByCreatorPerformance(Member member) {
    List<CreatorIdAndSubscriptionCountMapper> allCreatorIdAndSubscriptionCount = creatorRepository.findAllCreatorIdAndSubscriptionCountOrderBySubscriberAmountDesc();
    int creatorSize = allCreatorIdAndSubscriptionCount.size();
    if (creatorSize == 0) {
      throw new CreatorEmptyException();
    }

    for (int i = 0; i < creatorSize; i++) {
      CreatorIdAndSubscriptionCountMapper creatorIdAndSubscriptionCount = allCreatorIdAndSubscriptionCount.get(
          i);
      double ranking = (i + 1) / creatorSize;
      creatorScoreMap.put(creatorIdAndSubscriptionCount.getCreatorId(),
          new CreatorScore(creatorIdAndSubscriptionCount.getCreatorId(), ranking));
    }

    calculateScoreByHistory(member);
  }

  private void calculateScoreByHistory(Member member) {
    List<Long> creatorIdsByMemberId = memberHistoryRepository.findCreatorIdsByMemberId(
        member.getId(), PageRequest.of(0, 100));
    creatorIdsByMemberId.forEach(creatorId -> {
      CreatorScore creatorScore = creatorScoreMap.get(creatorId);
      creatorScore.addScore(0.4);
    });

    calculateScoreByLikeCount(member);
  }

  private void calculateScoreByLikeCount(Member member) {
    long totalLikeCount = memberContentRepository.countByMemberId(member.getId());
    if (totalLikeCount == 0) {
      return;
    }

    List<CreatorAndLikeCountByMember> likeCountByMemberId = memberContentRepository.findLikeCountByMemberId(
        member.getId());
    likeCountByMemberId.forEach(creatorAndLikeCountByMember -> {
      CreatorScore creatorScore = creatorScoreMap.get(creatorAndLikeCountByMember.getCreatorId());
      creatorScore.addScore((double) creatorAndLikeCountByMember.getLikeCount() / totalLikeCount);
    });
  }

}
