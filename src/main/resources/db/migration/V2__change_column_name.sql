alter table company
rename column logo_image_url to logo_img_url;

alter table content
rename column content_img to content_img_url;

alter table creator
rename column profile_img to profile_img_url;

alter table member
rename column profile_img to profile_img_url;