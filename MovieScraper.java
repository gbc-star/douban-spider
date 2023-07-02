package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.json.JSONObject;
import org.json.JSONArray;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MovieScraper {
    public static void main(String[] args) {
        int startId = 1296001; // 起始id
        int endId = 1297000; // 结束id

        try {
            // 创建JSON数组用于存储电影信息
            JSONArray moviesArray = new JSONArray();

            for (int id = startId; id <= endId; id++) {
                String[] userAgents = {
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.129 Safari/537.36",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36"
                };

                String[] referers = {
                        "https://www.google.com/",
                        "https://www.bing.com/",
                        "https://www.yahoo.com/",
                        "https://www.baidu.com/",
                        "https://www.amazon.com/",
                        "https://www.netflix.com/"
                };

                // 随机选择 User-Agent 字符串
                String randomUserAgent = userAgents[new Random().nextInt(userAgents.length)];
                // 随机选择 Referer 字符串
                String randomReferer = referers[new Random().nextInt(referers.length)];

                String url = "https://movie.douban.com/subject/" + id + "/"; // 构建URL
                Random random = new Random();
                int delay = random.nextInt(1000) + 5000; // 生成5000ms到6000ms之间的随机数作为延时时间
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    // 发起HTTP请求并获取网页内容，设置超时时间为5秒
                    Document doc = Jsoup.connect(url)
                            .timeout(5000)
                            .header("User-Agent", randomUserAgent)
                            .header("Referer", randomReferer)
                            .get();

                    // 在网页内容中查找电影信息
                    Elements movieElements = doc.select(".subject"); // 替换成合适的选择器，选择包含电影信息的HTML元素

                    for (Element movieElement : movieElements) {
                        JSONObject movieObject = new JSONObject();

                        //提取标题
                        Element titleElement = doc.selectFirst("span[property=v:itemreviewed]");
                        if (titleElement != null) {
                            String title = titleElement.text();
                            movieObject.put("title", title);
                        } else {
                            movieObject.put("title", "");
                        }

                        //加入url
                        movieObject.put("url", url);

                        //提取封面图片地址
                        Element coverElement = doc.selectFirst("img[rel=v:image]");
                        if (coverElement != null) {
                            String cover = coverElement.attr("src"); // 使用attr("src")方法获取src属性值
                            movieObject.put("cover", cover);
                        } else {
                            movieObject.put("cover", "");
                        }

                        //提取年份
                        Element yearElement = doc.selectFirst("span[class=year]");
                        if (yearElement != null) {
                            String yearWithBrackets = yearElement.text();
                            String year = yearWithBrackets.replaceAll("[\\(\\)]", ""); // 使用正则表达式去除括号
                            movieObject.put("year", year);
                        } else {
                            movieObject.put("year", "");
                        }

                        //提取导演信息
                        Element directorElement = movieElement.selectFirst("a[rel=v:directedBy]");
                        if (directorElement != null) {
                            String director = directorElement.text();
                            movieObject.put("director", director);
                        } else {
                            movieObject.put("director", "");
                        }

                        //提取编剧信息
                        Elements attrsElements = doc.select(".attrs"); // 获取所有符合条件的.attrs元素

                        if (attrsElements.size() >= 2) {
                            Element attrsElement = attrsElements.get(1);
                            Elements nameElements = attrsElement.select("a[href^='/celebrity/']"); // 使用CSS选择器选择包含名字的a标签
                            if (!nameElements.isEmpty()) {
                                StringBuilder names = new StringBuilder(); // 使用StringBuilder来拼接名字
                                for (Element nameElement : nameElements) {
                                    String name = nameElement.text(); // 获取每个名字的文本内容
                                    names.append(name).append(" "); // 拼接名字，用空格隔开
                                }
                                movieObject.put("scriptwriter", names);
                            } else {
                                movieObject.put("scriptwriter", "");
                            }
                        } else {
                            System.out.println("未找到第二个.attrs元素");
                            movieObject.put("scriptwriter", "");
                        }

                        // 提取演员信息
                        Elements actorElements = movieElement.select("a[rel=v:starring]");
                        if (actorElements != null) {
                            String actors = actorElements.text();
                            movieObject.put("actors", actors);
                        } else {
                            movieObject.put("actors", "");
                        }

                        //提取电影类别
                        Elements cateElements = movieElements.select("span[property=v:genre]");
                        if (cateElements != null) {
                            String cate = cateElements.text();
                            movieObject.put("cate", cate);
                        } else {
                            movieObject.put("cate", "");
                        }

                        //提取电影评分
                        Elements ratingElements = doc.select("strong[property=v:average]");
                        if (!ratingElements.isEmpty()) { // 使用isEmpty()方法判断是否为空
                            String rating = ratingElements.text();
                            movieObject.put("rating", rating);
                        } else {
                            movieObject.put("rating", "");
                        }

                        //提取电影简介
                        Elements summaryElements = doc.select("span[property=v:summary]");
                        if (summaryElements != null) {
                            String summary = summaryElements.text().trim();
                            movieObject.put("summary", summary);
                        } else {
                            movieObject.put("summary", "");
                        }

                        //提取国家
                        Element countryElement = doc.select("span:containsOwn(制片国家/地区:)").first();
                        if (countryElement != null) {
                            String country = countryElement.nextSibling().toString().trim();
                            movieObject.put("country", country);
                        }else {
                            movieObject.put("country", "");
                        }

                        // 将电影信息对象添加到JSON数组中
                        moviesArray.put(movieObject);
                        System.out.println("第" + id + "个提取成功");
                    }
                } catch (IOException e) {
                    // 异常处理
                    int retryCount = 0;
                    int maxRetry = 2; // 最大重试次数
                    int delayMillis = 4000; // 每次请求的延时时间，单位毫秒

                    while (retryCount < maxRetry) {
                        retryCount++;
                        System.out.println("正在进行第 " + retryCount + " 次请求...");
                        try {
                            // 发起HTTP请求并获取网页内容，设置超时时间为10秒
                            Document doc = Jsoup.connect(url)
                                    .timeout(5000)
                                    .header("User-Agent", randomUserAgent)
                                    .header("Referer", randomReferer)
                                    .get();

                            Elements movieElements = doc.select(".subject"); // 替换成合适的选择器，选择包含电影信息的HTML元素
                            for (Element movieElement : movieElements) {
                                JSONObject movieObject = new JSONObject();

                                //提取标题
                                Element titleElement = doc.selectFirst("span[property=v:itemreviewed]");
                                if (titleElement != null) {
                                    String title = titleElement.text();
                                    movieObject.put("title", title);
                                } else {
                                    movieObject.put("title", "");
                                }

                                //加入url
                                movieObject.put("url", url);

                                //提取封面图片地址
                                Element coverElement = doc.selectFirst("img[rel=v:image]");
                                if (coverElement != null) {
                                    String cover = coverElement.attr("src"); // 使用attr("src")方法获取src属性值
                                    movieObject.put("cover", cover);
                                } else {
                                    movieObject.put("cover", "");
                                }

                                //提取年份
                                Element yearElement = doc.selectFirst("span[class=year]");
                                if (yearElement != null) {
                                    String yearWithBrackets = yearElement.text();
                                    String year = yearWithBrackets.replaceAll("[\\(\\)]", ""); // 使用正则表达式去除括号
                                    movieObject.put("year", year);
                                } else {
                                    movieObject.put("year", "");
                                }

                                //提取导演信息
                                Element directorElement = movieElement.selectFirst("a[rel=v:directedBy]");
                                if (directorElement != null) {
                                    String director = directorElement.text();
                                    movieObject.put("director", director);
                                } else {
                                    movieObject.put("director", "");
                                }

                                //提取编剧信息
                                Elements attrsElements = doc.select(".attrs"); // 获取所有符合条件的.attrs元素

                                if (attrsElements.size() >= 2) {
                                    Element attrsElement = attrsElements.get(1);
                                    Elements nameElements = attrsElement.select("a[href^='/celebrity/']"); // 使用CSS选择器选择包含名字的a标签
                                    if (!nameElements.isEmpty()) {
                                        StringBuilder names = new StringBuilder(); // 使用StringBuilder来拼接名字
                                        for (Element nameElement : nameElements) {
                                            String name = nameElement.text(); // 获取每个名字的文本内容
                                            names.append(name).append(" "); // 拼接名字，用空格隔开
                                        }
                                        movieObject.put("scriptwriter", names);
                                    } else {
                                        movieObject.put("scriptwriter", "");
                                    }
                                } else {
                                    System.out.println("未找到第二个.attrs元素");
                                    movieObject.put("scriptwriter", "");
                                }

                                // 提取演员信息
                                Elements actorElements = movieElement.select("a[rel=v:starring]");
                                if (actorElements != null) {
                                    String actors = actorElements.text();
                                    movieObject.put("actors", actors);
                                } else {
                                    movieObject.put("actors", "");
                                }

                                //提取电影类别
                                Elements cateElements = movieElements.select("span[property=v:genre]");
                                if (cateElements != null) {
                                    String cate = cateElements.text();
                                    movieObject.put("cate", cate);
                                } else {
                                    movieObject.put("cate", "");
                                }

                                //提取电影评分
                                Elements ratingElements = doc.select("strong[property=v:average]");
                                if (!ratingElements.isEmpty()) { // 使用isEmpty()方法判断是否为空
                                    String rating = ratingElements.text();
                                    movieObject.put("rating", rating);
                                } else {
                                    movieObject.put("rating", "");
                                }

                                //提取电影简介
                                Elements summaryElements = doc.select("span[property=v:summary]");
                                if (summaryElements != null) {
                                    String summary = summaryElements.text().trim();
                                    movieObject.put("summary", summary);
                                } else {
                                    movieObject.put("summary", "");
                                }

                                //提取国家
                                Element countryElement = doc.select("span:containsOwn(制片国家/地区:)").first();
                                if (countryElement != null) {
                                    String country = countryElement.nextSibling().toString().trim();
                                    movieObject.put("country", country);
                                }else {
                                    movieObject.put("country", "");
                                }

                                // 将电影信息对象添加到JSON数组中
                                moviesArray.put(movieObject);
                                System.out.println("第" + id + "个提取成功");
                            }
                            System.out.println("第 " + retryCount + " 次请求成功！");
                            Thread.sleep(delayMillis); // 每次请求后延时一定时间
                            break; // 成功获取电影信息后跳出重试循环
                        } catch (IOException | InterruptedException ex) {
                            // 记录错误信息
                            System.err.println("第 " + retryCount + " 次请求失败，异常信息：" + ex.getMessage());
                            Thread.sleep(delayMillis); // 请求失败后延时一定时间
                        }
                    }
                    if (retryCount == maxRetry) {
                        System.err.println("重试次数已达上限，放弃获取电影信息：" + url);
                    }

                }
            }

            // 将JSON对象写入到文件
            try (FileWriter fileWriter = new FileWriter("movie.json")) {
                fileWriter.write(moviesArray.toString());
                System.out.println("电影信息已成功写入到movie.json文件中。");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
