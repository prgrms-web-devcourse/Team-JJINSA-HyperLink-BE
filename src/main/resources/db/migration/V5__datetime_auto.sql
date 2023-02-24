alter table category
    modify created_at datetime default CURRENT_TIMESTAMP;
alter table category
    modify updated_at datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP;

alter table attention_category
    modify created_at datetime default CURRENT_TIMESTAMP;
alter table attention_category
    modify updated_at datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP;

alter table company
    modify created_at datetime default CURRENT_TIMESTAMP;
alter table company
    modify updated_at datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP;

alter table content
    modify created_at datetime default CURRENT_TIMESTAMP;
alter table content
    modify updated_at datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP;

alter table content_category
    modify created_at datetime default CURRENT_TIMESTAMP;
alter table content_category
    modify updated_at datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP;

alter table creator
    modify created_at datetime default CURRENT_TIMESTAMP;
alter table creator
    modify updated_at datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP;

alter table creator_category
    modify created_at datetime default CURRENT_TIMESTAMP;
alter table creator_category
    modify updated_at datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP;

alter table member
    modify created_at datetime default CURRENT_TIMESTAMP;
alter table member
    modify updated_at datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP;

alter table member_content
    modify created_at datetime default CURRENT_TIMESTAMP;
alter table member_content
    modify updated_at datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP;

alter table subscription
    modify created_at datetime default CURRENT_TIMESTAMP;
alter table subscription
    modify updated_at datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP;