ALTER TABLE `member`
    ADD COLUMN gender varchar(10);

ALTER TABLE `member`
    ADD COLUMN isAdmin TINYINT(1) DEFAULT 0;