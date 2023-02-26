alter table content
    add likeCount int unsigned not null default 0 after inquiry;

alter table content
rename column inquiry to viewCount;