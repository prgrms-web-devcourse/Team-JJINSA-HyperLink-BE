create table member_creator (
    `member_creator_id`	BIGINT	NOT NULL auto_increment     primary key,
    `member_id` BIGINT	NOT NULL,
    `creator_id` BIGINT	NOT NULL,
    `created_at`	datetime default CURRENT_TIMESTAMP	NOT NULL,
    `updated_at`	datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP	NOT NULL
);

ALTER TABLE `member_creator` ADD CONSTRAINT `FK_creator_TO_member_creator_1` FOREIGN KEY (
                                                                                               `creator_id`
    )
    REFERENCES `creator` (
                           `creator_id`
        );

ALTER TABLE `member_creator` ADD CONSTRAINT `FK_member_TO_member_creator_1` FOREIGN KEY (
                                                                                  `member_id`
    )
    REFERENCES `member` (
                          `member_id`
        );