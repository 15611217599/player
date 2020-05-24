--网站
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('0', '网站地址', '', 'https://wolongzy.net;https://www.wlzy.tv','');


INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('-1', '推荐', '0', '-1','0');

--电影
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('1', '电影', '0', '/type/1.html','1');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('5', '动作片', '1', '/type/5.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('6', '喜剧片', '1', '/type/6.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('7', '爱情片', '1', '/type/7.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('8', '科幻片', '1', '/type/8.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('9', '恐怖片', '1', '/type/9.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('10', '剧情片', '1', '/type/10.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('11', '战争片', '1', '/type/11.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('19', '纪录片', '1', '/type/19.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('20', '纪录片', '1', '/type/20.html','');
--电视剧
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('2', '电视剧', '0', '/type/2.html','2');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('12', '国产剧', '2', '/type/12.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('13', '香港剧', '2', '/type/13.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('14', '韩国剧', '2', '/type/14.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('15', '欧美剧', '2', '/type/15.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('16', '台湾剧', '2', '/type/16.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('17', '日本剧', '2', '/type/17.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('18', '海外剧', '2', '/type/18.html','');
--综艺
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('3', '综艺节目', '0', '/type/3.html','3');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('23', '内地综艺', '3', '/type/23.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('24', '港台综艺', '3', '/type/24.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('25', '内地综艺', '3', '/type/25.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('26', '内地综艺', '3', '/type/26.html','');
--动漫
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('4', '动漫剧', '0', '/type/4.html','4');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('27', '国产动漫', '4', '/type/27.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('28', '日韩动漫', '4', '/type/28.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('29', '欧美动漫', '4', '/type/29.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('30', '港台动漫', '4', '/type/30.html','');
INSERT INTO player.video_category (id, name, pid, url,sort_id) VALUES ('31', '海外动漫', '4', '/type/31.html','');

INSERT INTO player.music (id, show_name)VALUES ('0', '排行榜');

INSERT INTO player.music (id, detail_url, request_head, search_url, show_name) VALUES ('1', 'http://www.kugou.com/yy/index.php?r=play/getdata&hash=',
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


INSERT INTO player.music (id, detail_url, request_head, search_url, show_name) VALUES ('2', 'https://api.imjad.cn/cloudmusic/?type=lyric&br=320000&id=%s',
null, 'https://music.163.com/weapi/cloudsearch/get/web?csrf_token=', '网易');
