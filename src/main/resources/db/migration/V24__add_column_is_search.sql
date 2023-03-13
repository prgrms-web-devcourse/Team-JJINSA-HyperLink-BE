alter table `member_history`
    add column `is_search` TINYINT(1) not null after `content_id`;
