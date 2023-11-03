package com.podcast.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.podcast.Progress.WebSocketServerDownload;
import com.podcast.pojo.Download;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Aria2c {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("Aria2c");

    /**
     * 重命名
     */
    private String rename;
    /**
     * 保存路径
     */
    private String paths;

    /**
     * 下载URL链接
     */
    private String url;
    /**
     * 解析Json
     */
    private Gson gson = new Gson();

    /**
     * 构造器
     */
    public Aria2c() {
    }


    /**
     *
     * @param url 下载链接
     * @param rename 重命名
     * @param paths 保存路径
     */
    public Aria2c(String url, String rename, String paths) {
        this.rename = rename+".format";
        this.paths = paths;
        this.url = url;
    }

    /**
     * 获取拼接后的命令
     * @return
     */
    public String getDownloadCmd(){
        return null;
    }
    /**
     *
     * @return 是否正确下载,错误返回false，否则返回true
     * @throws IOException
     */
    public  boolean startDownload() throws IOException {
        Aria2Client aria2Client = null;
        boolean status = false;
        //读取Properties文件中的配置信息
        // aria2_RPO_URL
        // aira2_RPO_secret
        Properties mainProperties = new Properties();
        try {
            // 使用ClassLoader加载Properties文件
            InputStream inputStream = Aria2c.class.getClassLoader().getResourceAsStream("conf.properties");
            mainProperties.load(inputStream);
            inputStream.close();

            //相关配置
            String aria2_RPO_URL = mainProperties.getProperty("aria2_RPO_URL");
            Object aira2_rpo_secret = mainProperties.get("aira2_RPO_secret");

            //下载选择
            Map options = new HashMap<>();
            options.put("out",rename);
            options.put("dir",paths);
            aria2Client = new Aria2Client(aria2_RPO_URL);
            String response = aria2Client.addUri(aira2_rpo_secret, Arrays.asList(url),options);
            System.out.println(response);

            //封装下载信息
            Download download = new Download();
            download.setDownloaderName("Aria2");

            String[] keys = {"status","totalLength","completedLength","downloadSpeed","dir"};
            String tellStatusStr;
            while ((tellStatusStr=aria2Client.tellStatus(keys))!=null){
                JsonObject jsonObject = gson.fromJson(tellStatusStr, JsonObject.class);

                int totalLength = jsonObject.get("result").getAsJsonObject().get("totalLength").getAsInt();
                int completedLength = jsonObject.get("result").getAsJsonObject().get("completedLength").getAsInt();
                int downloadSpeed = jsonObject.get("result").getAsJsonObject().get("downloadSpeed").getAsInt();
                String downloadStatus = jsonObject.get("result").getAsJsonObject().get("status").getAsString();
                String dir = jsonObject.get("result").getAsJsonObject().get("dir").getAsString();
                String id = jsonObject.get("id").getAsString();


                if (!downloadStatus.contains("error") && !id.contains(" ")){

                    if (downloadStatus.contains("complete")){
                        //通过WS推送到前端
                        if (WebSocketServerDownload._session!=null && WebSocketServerDownload._session.isOpen()){
                            download.setPercentage(100.0);
                            WebSocketServerDownload._session.getBasicRemote().sendText(gson.toJson(download));
                        }
                        return true;
                    }

                    if (totalLength!=0 && completedLength!=0) {
                        //downloadSpeed转换成MB/s
                        BigDecimal B = BigDecimal.valueOf(downloadSpeed);
                        BigDecimal M = BigDecimal.valueOf(1000000);
                        BigDecimal MB = B.divide(M, 2, RoundingMode.HALF_UP);

                        //获取下载进度 totalLength-completedLength
                        BigDecimal totalLengthBigDecimal = BigDecimal.valueOf(totalLength);
                        BigDecimal completedLengthBigDecimal = BigDecimal.valueOf(completedLength);
                        BigDecimal hundred = BigDecimal.valueOf(100);
                        BigDecimal divide = completedLengthBigDecimal.divide(totalLengthBigDecimal, 2, RoundingMode.HALF_UP);
                        BigDecimal percentage = divide.multiply(hundred);
                        
                        //求剩余时间
                        //(totalLength-completedLength)/downloadSpeed
                        BigDecimal surplusLength = totalLengthBigDecimal.subtract(completedLengthBigDecimal);
                        BigDecimal surplusSecond = surplusLength.divide(B, 0, RoundingMode.HALF_UP);
                        //转换成00:00:00格式
                        String duration = TimeFormat.duration(Integer.parseInt(surplusSecond.toString()));

                        //总大小转换成MB
                        BigDecimal totalLengthMB = totalLengthBigDecimal.divide(M,2,RoundingMode.UP);

                        //封装信息
                        download.setId(id);
                        download.setDescription(dir);
                        download.setTotalSize(totalLengthMB+"MB");
                        download.setPercentage(Double.parseDouble(percentage.toString()));
                        download.setCurrentSpeed(MB+"MB/s");
                        download.setETA(duration);

                        //通过WS推送到前端
                        if (WebSocketServerDownload._session!=null && WebSocketServerDownload._session.isOpen()){
                            WebSocketServerDownload._session.getBasicRemote().sendText(gson.toJson(download));
                            //延时1秒,避免推送过快导致前端页面无法渲染
                            Thread.sleep(1000);
                        }
                    }


                }else {
                    return false;
                }
            }

        } catch (Exception e) {
            LOGGER.error("使用startDownload时出错！"+"详细:"+e);
        }
       return status;
    }

    /**
     * 测试用
     * @throws Exception
     */
    @Test
    public void t1() throws Exception {
        String url = "https://video.31dm.com/sda1/pikpak%20x/补1-7v2/网络胜利组/网络胜利组%2011.mp4?sign=6fb575879ada5a0f3245bf7b89e16a97&t=1696348112";
        String name = "t200";
        String path = "D:/Download/Aria2/t1";
        Aria2c aria2c = new Aria2c(url,name,"/opt/");
        aria2c.startDownload();
    }

}
