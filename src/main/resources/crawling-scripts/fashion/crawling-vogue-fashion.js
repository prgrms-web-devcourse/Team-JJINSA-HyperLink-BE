const axios = require('axios');
const cheerio = require('cheerio');

async function main() {
    resultList = []
    for(let pageNum = 1; pageNum <= 10; pageNum++) {
        const response = await axios.get(
            'https://www.vogue.co.kr/category/fashion/page/' + pageNum
        );
    
        const $ = cheerio.load(response.data);
        const elements = $('.fusion-posts-container.fusion-blog-layout-grid.fusion-blog-layout-grid-3.isotope.fusion-blog-layout-masonry.fusion-posts-container-infinite').children(".fusion-post-masonry.fusion-post-grid.fusion-element-portrait.post.fusion-clearfix.type-post");
    
        elements.each((idx, elem) => {
            // if(Date.parse($(elem).find("span.post-meta").find("span.post-date").text()) < Date.parse("2022-01-01")) return false;
            // console.log($(elem).find("span.post-thumb").attr("style")== undefined);
            // console.log(new Date(Date.parse($(elem).find("span.post-meta").find("span.post-date").text())).toDateString());
            resultList.push({
                title: $(elem).find(".fusion-post-content-wrapper").find(".entry-title a").text(),
                link: $(elem).find(".fusion-post-content-wrapper").find(".entry-title a").attr("href"),
                contentImgLink: 
                    $(elem).find(".fusion-masonry-element-container.fusion-image-wrapper a img").attr("src") != undefined  ? 
                    $(elem).find(".fusion-masonry-element-container.fusion-image-wrapper a img").attr("src") : 
                    "https://hyperlink-data.s3.ap-northeast-2.amazonaws.com/content-default-image/logo_vogue.png",
                category: "fashion",
                creator: "VOGUE 패션 매거진"
            })
        });
    }
    
    return resultList;
}
main()
.then(response => console.log(response));
