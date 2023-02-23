# 기존 FK 해제
alter table member_content
    drop foreign key FK_content_TO_member_content_1;

alter table member_content
    drop foreign key FK_member_TO_member_content_1;

alter table member
    drop foreign key FK_company_TO_member_1;

alter table attention_category
    drop foreign key FK_member_TO_attention_category_1;

alter table attention_category
    drop foreign key FK_category_TO_attention_category_1;

alter table subscription
    drop foreign key FK_member_TO_subscription_1;

alter table subscription
    drop foreign key FK_creator_TO_subscription_1;

alter table creator_category
    drop foreign key FK_category_TO_creator_category_1;

alter table creator_category
    drop foreign key FK_creator_TO_creator_category_1;

alter table content_category
    drop foreign key FK_category_TO_content_category_1;

alter table content_category
    drop foreign key FK_content_TO_content_category_1;

# auto increment 설정
alter table category
    modify category_id bigint auto_increment;

alter table category
    auto_increment = 1;

alter table attention_category
    modify attention_category_id bigint auto_increment;

alter table attention_category
    auto_increment = 1;

alter table company
    modify company_id bigint auto_increment;

alter table company
    auto_increment = 1;

alter table content
    modify content_id bigint auto_increment;

alter table content
    auto_increment = 1;

alter table content_category
    modify content_category_id bigint auto_increment;

alter table content_category
    auto_increment = 1;

alter table creator
    modify creator_id bigint auto_increment;

alter table creator
    auto_increment = 1;

alter table creator_category
    modify creator_category_id bigint auto_increment;

alter table creator_category
    auto_increment = 1;

alter table member
    modify member_id bigint auto_increment;

alter table member
    auto_increment = 1;

alter table member_content
    modify member_content_id bigint auto_increment;

alter table member_content
    auto_increment = 1;

alter table subscription
    modify subscription_id bigint auto_increment;

alter table subscription
    auto_increment = 1;

# FK 재설정
ALTER TABLE `member` ADD CONSTRAINT `FK_company_TO_member_1` FOREIGN KEY (`company_id`)
    REFERENCES `company` (
                          `company_id`
        );

ALTER TABLE `attention_category` ADD CONSTRAINT `FK_member_TO_attention_category_1` FOREIGN KEY (
                                                                                                 `member_id`
    )
    REFERENCES `member` (
                         `member_id`
        );

ALTER TABLE `attention_category` ADD CONSTRAINT `FK_category_TO_attention_category_1` FOREIGN KEY (
                                                                                                   `category_id`
    )
    REFERENCES `category` (
                           `category_id`
        );

ALTER TABLE `subscription` ADD CONSTRAINT `FK_member_TO_subscription_1` FOREIGN KEY (
                                                                                     `member_id`
    )
    REFERENCES `member` (
                         `member_id`
        );

ALTER TABLE `subscription` ADD CONSTRAINT `FK_creator_TO_subscription_1` FOREIGN KEY (
                                                                                      `creator_id`
    )
    REFERENCES `creator` (
                          `creator_id`
        );

ALTER TABLE `creator_category` ADD CONSTRAINT `FK_creator_TO_creator_category_1` FOREIGN KEY (
                                                                                              `creator_id`
    )
    REFERENCES `creator` (
                          `creator_id`
        );

ALTER TABLE `creator_category` ADD CONSTRAINT `FK_category_TO_creator_category_1` FOREIGN KEY (
                                                                                               `category_id`
    )
    REFERENCES `category` (
                           `category_id`
        );

ALTER TABLE `content_category` ADD CONSTRAINT `FK_content_TO_content_category_1` FOREIGN KEY (
                                                                                              `content_id`
    )
    REFERENCES `content` (
                          `content_id`
        );

ALTER TABLE `content_category` ADD CONSTRAINT `FK_category_TO_content_category_1` FOREIGN KEY (
                                                                                               `category_id`
    )
    REFERENCES `category` (
                           `category_id`
        );

ALTER TABLE `member_content` ADD CONSTRAINT `FK_member_TO_member_content_1` FOREIGN KEY (
                                                                                         `member_id`
    )
    REFERENCES `member` (
                         `member_id`
        );

ALTER TABLE `member_content` ADD CONSTRAINT `FK_content_TO_member_content_1` FOREIGN KEY (
                                                                                          `content_id`
    )
    REFERENCES `content` (
                          `content_id`
        );

