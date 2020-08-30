--网站
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('0', '网站地址', '', 'http://www.zuidazy5.net','');


INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('-1', '推荐', '0', '-1','0');

--电影
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('1', '电影', '0', '/?m=vod-type-id-1.html','1');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('5', '动作片', '1', '/?m=vod-type-id-5.html','');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('6', '喜剧片', '1', '/?m=vod-type-id-6.html','');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('7', '爱情片', '1', '/?m=vod-type-id-7.html','');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('8', '科幻片', '1', '/?m=vod-type-id-8.html','');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('9', '恐怖片', '1', '/?m=vod-type-id-9.html','');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('10', '剧情片', '1', '/?m=vod-type-id-10.html','');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('11', '战争片', '1', '/?m=vod-type-id-11.html','');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('22', '纪录片', '1', '/?m=vod-type-id-22.html','');
--电视剧
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('2', '电视剧', '0', '/?m=vod-type-id-2.html','2');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('12', '国产剧', '2', '/?m=vod-type-id-12.html','');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('13', '香港剧', '2', '/?m=vod-type-id-13.html','');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('14', '韩国剧', '2', '/?m=vod-type-id-14.html','');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('15', '欧美剧', '2', '/?m=vod-type-id-15.html','');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('19', '台湾剧', '2', '/?m=vod-type-id-19.html','');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('20', '日本剧', '2', '/?m=vod-type-id-20.html','');
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('21', '海外剧', '2', '/?m=vod-type-id-21.html','');
--综艺

INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('3', '综艺节目', '0', '/?m=vod-type-id-3.html','3');
--动漫
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('4', '动漫剧', '0', '/?m=vod-type-id-4.html','4');
/*--福利
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('16', '福利片', '0', '/?m=vod-type-id-16.html','5');
--伦理
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('17', '伦理片', '0', '/?m=vod-type-id-17.html','6');*/
--音乐
INSERT INTO video_category (id, name, pid, url,sort_id) VALUES ('18', '音乐片', '0', '/?m=vod-type-id-18.html','7');

INSERT INTO music (id, show_name)VALUES ('0', '排行榜');

INSERT INTO music (id, detail_url, request_head, search_url, show_name) VALUES ('1', 'http://www.kugou.com/yy/index.php?r=play/getdata&hash=',
'{"Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
"Accept-Encoding": "gzip, deflate",
"Accept-Language": "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7",
"Cache-Control": "no-cache",
"Connection": "keep-alive",
"Cookie": "kg_mid=7f117539d96f3e7b2830dcf676422c38",
"Host": "www.kugou.com",
"Pragma": "no-cache",
"Upgrade-Insecure-Requests": "1",
"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.53 Safari/537.36 Edg/80.0.361.33"

}', 'http://mobilecdn.kugou.com/api/v3/search/song?format=json&showtype=3&keyword=', '酷狗');


INSERT INTO music (id, detail_url, request_head, search_url, show_name) VALUES ('2', 'https://api.imjad.cn/cloudmusic/?type=lyric&br=320000&id=%s',
null, 'https://music.163.com/weapi/cloudsearch/get/web?csrf_token=', '网易');
