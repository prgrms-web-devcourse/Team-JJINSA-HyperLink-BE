CREATE TABLE `member` (
    `member_id`	BIGINT	NOT NULL,
    `company_id`	BIGINT	NULL,
    `email`	varchar(255)	NOT NULL  UNIQUE	COMMENT '- member email - unique value',
    `nickname`	varchar(30)	NOT NULL,
    `career`	varchar(30)	NOT NULL,
    `career_year`	varchar(30)	NOT NULL,
    `profile_img`	varchar(255)	NOT NULL,
    `birth_year`	int	NULL,
    `created_at`	datetime	NOT NULL,
    `updated_at`	datetime	NOT NULL
);

CREATE TABLE `category` (
    `category_id`	BIGINT	NOT NULL,
    `name`	varchar(30)	NOT NULL,
    `created_at`	datetime	NOT NULL,
    `updated_at`	datetime	NOT NULL
);

CREATE TABLE `attention_category` (
    `attention_category_id`	BIGINT	NOT NULL,
    `member_id`	BIGINT	NOT NULL,
    `category_id`	BIGINT	NOT NULL,
    `created_at`	datetime	NOT NULL,
    `updated_at`	datetime	NOT NULL
);

CREATE TABLE `company` (
    `company_id`	BIGINT	NOT NULL,
    `email_address`	varchar(50)	NOT NULL	COMMENT 'ex) woori.com  ex) kakao.com',
    `logo_image_url`	varchar(255)	NOT NULL,
    `name`	varchar(30)	NOT NULL,
    `created_at`	datetime	NOT NULL,
    `updated_at`	datetime	NOT NULL
);

CREATE TABLE `creator` (
    `creator_id`	BIGINT	NOT NULL,
    `name`	varchar(255)	NOT NULL,
    `profile_img`	varchar(255)	NOT NULL,
    `description`	varchar(255)	NOT NULL,
    `created_at`	datetime	NOT NULL,
    `updated_at`	datetime	NOT NULL
);

CREATE TABLE `subscription` (
    `subscription_id`	BIGINT	NOT NULL,
    `member_id`	BIGINT	NOT NULL,
    `creator_id`	BIGINT	NOT NULL,
    `created_at`	datetime	NOT NULL,
    `updated_at`	datetime	NOT NULL
);

CREATE TABLE `creator_category` (
    `creator_category_id`	BIGINT	NOT NULL,
    `creator_id`	BIGINT	NOT NULL,
    `category_id`	BIGINT	NOT NULL,
    `created_at`	datetime	NOT NULL,
    `updated_at`	datetime	NOT NULL
);

CREATE TABLE `content` (
    `content_id`	BIGINT	NOT NULL,
    `title`	varchar(255)	NOT NULL,
    `content_img`	varchar(255)	NOT NULL,
    `link`	varchar(255)	NOT NULL,
    `created_at`	datetime	NOT NULL,
    `updated_at`	datetime	NOT NULL
);

CREATE TABLE `content_category` (
    `content_category_id`	BIGINT	NOT NULL,
    `content_id`	BIGINT	NOT NULL,
    `category_id`	BIGINT	NOT NULL,
    `created_at`	datetime	NOT NULL,
    `updated_at`	datetime	NOT NULL
);

CREATE TABLE `member_content` (
    `member_content_id`	BIGINT	NOT NULL,
    `member_id`	BIGINT	NOT NULL,
    `content_id`	BIGINT	NOT NULL,
    `type`	int	NOT NULL	COMMENT '- 좋아요는 1번 - 북마크는 2번',
    `created_at`	datetime	NOT NULL,
    `updated_at`	datetime	NOT NULL
);

ALTER TABLE `member` ADD CONSTRAINT `PK_MEMBER` PRIMARY KEY (`member_id`);

ALTER TABLE `category` ADD CONSTRAINT `PK_CATEGORY` PRIMARY KEY (`category_id`);

ALTER TABLE `attention_category` ADD CONSTRAINT `PK_ATTENTION_CATEGORY` PRIMARY KEY (
    `attention_category_id`
);

ALTER TABLE `company` ADD CONSTRAINT `PK_COMPANY` PRIMARY KEY (`company_id`);

ALTER TABLE `creator` ADD CONSTRAINT `PK_CREATOR` PRIMARY KEY (`creator_id`);

ALTER TABLE `subscription` ADD CONSTRAINT `PK_SUBSCRIPTION` PRIMARY KEY (
    `subscription_id`
);

ALTER TABLE `creator_category` ADD CONSTRAINT `PK_CREATOR_CATEGORY` PRIMARY KEY (
    `creator_category_id`
);

ALTER TABLE `content` ADD CONSTRAINT `PK_CONTENT` PRIMARY KEY (
    `content_id`
);

ALTER TABLE `content_category` ADD CONSTRAINT `PK_CONTENT_CATEGORY` PRIMARY KEY (
    `content_category_id`
);

ALTER TABLE `member_content` ADD CONSTRAINT `PK_MEMBER_CONTENT` PRIMARY KEY (
    `member_content_id`
);

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

