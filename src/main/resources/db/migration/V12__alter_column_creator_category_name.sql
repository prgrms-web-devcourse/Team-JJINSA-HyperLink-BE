ALTER TABLE `category`
    ADD CONSTRAINT UQ_category_name UNIQUE(name);

ALTER TABLE `creator`
    ADD CONSTRAINT UQ_category_name UNIQUE(name);