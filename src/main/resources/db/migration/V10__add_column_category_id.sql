DROP TABLE IF EXISTS content_category;
DROP TABLE IF EXISTS creator_category;

alter table `content`
    add column `category_id` BIGINT not null after `link`;
alter table `creator`
    add column `category_id` BIGINT not null after `description`;
