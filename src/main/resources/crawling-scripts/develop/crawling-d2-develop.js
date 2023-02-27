const axios = require('axios');
const cheerio = require('cheerio');

async function main() {
    const response = await axios.get(
       "https://d2.naver.com/api/v1/contents?categoryId=&page=0&size=30"
    );
    // response.data => json data
    const elements = response.data.content;


    resultList = []
    elements.forEach((elem) => {
        if(elem.postPublishedAt < Date.parse("2022-01-01")) return false;
        console.log((new Date(elem.postPublishedAt).toDateString()));
        resultList.push({
            title: elem.postTitle,
            link: "https://d2.naver.com" +  elem.url,
            contentImgLink: 
                elem.postImage != undefined ? 
                "https://d2.naver.com" + elem.postImage : 
                "https://hyperlink-data.s3.ap-northeast-2.amazonaws.com/content-default-image/logo_naver_d2.jpg",
            category: "develop",
            creator: "네이버 D2 기술 블로그"
        })
    });
    return resultList;
}
main()
.then(response => console.log(response));
