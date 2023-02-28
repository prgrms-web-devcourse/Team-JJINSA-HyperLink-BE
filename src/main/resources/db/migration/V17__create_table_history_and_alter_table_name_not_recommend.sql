create table member_history (
                                `member_history_id`	BIGINT	NOT NULL auto_increment     primary key,
                                `member_id` BIGINT	NOT NULL,
                                `content_id` BIGINT	NOT NULL,
                                `created_at`	datetime default CURRENT_TIMESTAMP	NOT NULL,
                                `updated_at`	datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP	NOT NULL
);

ALTER TABLE `member_history` ADD CONSTRAINT `FK_content_TO_history_1` FOREIGN KEY (
                                                                                          `content_id`
    )
    REFERENCES `content` (
                          `content_id`
        );

ALTER TABLE `member_history` ADD CONSTRAINT `FK_member_TO_history_1` FOREIGN KEY (
                                                                                         `member_id`
    )
    REFERENCES `member` (
                         `member_id`
        );

# rename table
rename table member_creator to not_recommend_creator;