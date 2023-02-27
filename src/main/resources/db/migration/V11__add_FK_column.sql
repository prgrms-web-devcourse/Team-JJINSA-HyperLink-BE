ALTER TABLE `content` ADD CONSTRAINT `FK_content_category` FOREIGN KEY (
                                                                                               `category_id`
    )
    REFERENCES `category` (
                           `category_id`
        );

ALTER TABLE `content` ADD CONSTRAINT `FK_content_creator` FOREIGN KEY (
                                                                        `creator_id`
    )
    REFERENCES `creator` (
                           `creator_id`
        );

ALTER TABLE `creator` ADD CONSTRAINT `FK_creator_category` FOREIGN KEY (
                                                                       `category_id`
    )
    REFERENCES `category` (
                          `category_id`
        );