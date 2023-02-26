alter table `content`
    add column `category_id` BIGINT not null default 0 after `link`;
