alter table `content`
    add column `creator_id` BIGINT not null default 0 after `link`;
