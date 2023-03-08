package com.hyperlink.server.domain.member.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AwsS3Service {

  private static final int FILE_NAME_INDEX = 3;
  private static final String DIRECTORY = "members-profile-image/";
  private String bucket;

//  private final AmazonS3 amazonS3;
//
//  private final MemberRepository memberRepository;
//
//  public AwsS3Service(@Value("${cloud.aws.s3.bucket}") String bucket, AmazonS3 amazonS3,
//      MemberRepository memberRepository) {
//    this.bucket = bucket;
//    this.amazonS3 = amazonS3;
//    this.memberRepository = memberRepository;
//  }
//
//  public ProfileImgResponse changeProfileImg(Long memberId, MultipartFile multipartFile) {
//    Member foundMember = memberRepository.findById(memberId)
//        .orElseThrow(MemberNotFoundException::new);
//    String savedProfileUrl = uploadFile(multipartFile);
//    String priorProfileImgName = parsingFileName(foundMember.getProfileImgUrl());
//    deleteFile(priorProfileImgName);
//    foundMember.changeProfileImgUrl(savedProfileUrl);
//
//    return ProfileImgResponse.from(savedProfileUrl);
//  }
//
//  private String uploadFile(MultipartFile multipartFile) {
//
//    String fileName = DIRECTORY + UUID.randomUUID();
//    ObjectMetadata objectMetadata = new ObjectMetadata();
//    objectMetadata.setContentLength(multipartFile.getSize());
//    objectMetadata.setContentType(multipartFile.getContentType());
//
//    try (InputStream inputStream = multipartFile.getInputStream()) {
//
//      amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
//          .withCannedAcl(CannedAccessControlList.PublicRead));
//
//      return amazonS3.getUrl(bucket, fileName).toString();
//    } catch (IOException e) {
//      throw new ProfileImgUploadFailException();
//    }
//  }
//
//  private void deleteFile(String fileName) {
//    amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
//  }
//
//  private String parsingFileName(String profileUrl) {
//    return profileUrl.split("/")[FILE_NAME_INDEX];
//  }
}
