const axios = require('axios');
const cheerio = require('cheerio');

async function getHTML(year, month) {
    if(month < 10) {
        month = '0' + month;
    }
    try {
        console.log("https://medium.com/daangn/archive/" + year + "/" + month);
        let config = {
            headers: {
                Accept: "text/html"
            }
        }
        return await axios.get("https://medium.com/daangn/archive/" + year + "/" + month, config);
    } catch(err) {
        console.log(err);
    }
}

async function main() {
    resultList = []
    for(let year = 2022; year <= 2023; year++) {
        for(let month = 1; month <= 12; month++) {
            if(year == 2023 && month >= 3) break;
            const response = await getHTML(year, month);
            const $ = cheerio.load(response.data);
            // response ==> html
            const elements = $("div.js-postStream").children("div.streamItem");
            elements.each((idx, elem) => {
                resultList.push({
                    title: $(elem).find("h3.graf--title").text(),
                    link: $(elem).find("div.postArticle").find("div:nth-child(3) a").attr("href"),
                    contentImgLink: 
                    $(elem).find(".postArticle-content.js-postField").find(".section-content").find(".section-inner.sectionLayout--insetColumn").find("#previewImage").find(".graf-image").attr("src") != undefined ? 
                    $(elem).find(".postArticle-content.js-postField").find(".section-content").find(".section-inner.sectionLayout--insetColumn").find("#previewImage").find(".graf-image").attr("src") : 
                        "https://hyperlink-data.s3.ap-northeast-2.amazonaws.com/content-default-image/logo_daangn.png",
                    category: "develop",
                    creator: "당근마켓 기술 블로그"
                })
            });
        }
    }
    
    return resultList;
}
main()
.then(response => console.log(response));
