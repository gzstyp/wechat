package com.fwtai.tool;

import com.alibaba.fastjson.JSONObject;
import com.fwtai.entity.AccessToken;
import okhttp3.Response;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信工具类
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2020-11-29 16:34
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
public final class ToolWechat{

    //用于存储token
    private static AccessToken at;

    private static final String TOKEN = "wwwfwtaicom";
    private static final String APPID = "wx587a3e21b333da19";
    private static final String APPSECRET = "1f28967090e792a91ac2e458a4e8d1e8";
    private static final String URL_GET_TOKEN ="https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+APPID+"&secret="+APPSECRET;

    public static boolean check(final String timestamp,final String nonce,final String signature) {
        //1）将token、timestamp、nonce三个参数进行字典序排序
        final String[] strs = new String[] {TOKEN,timestamp,nonce};
        Arrays.sort(strs);
        //2）将三个参数字符串拼接成一个字符串进行sha1加密
        final String src = strs[0]+strs[1]+strs[2];
        //3）开发者获得加密后的字符串可与signature对比，标识该请求来源于微信
        return sha1(src).equalsIgnoreCase(signature);
    }

    /**
     * 获取token
    */
    private static void getToken(){
        try {
            final Response response = ToolOkHttp.ajaxGet(URL_GET_TOKEN);
            final String json = ToolOkHttp.parseResponse(response);
            final JSONObject jsonObject = JSONObject.parseObject(json);
            final String token = jsonObject.getString("access_token");
            final String expireIn = jsonObject.getString("expires_in");
            //创建token对象,并存起来。
            at = new AccessToken(token,expireIn);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取带参数二维码的ticket
    */
    public static String getQrCodeTicket() {
        final String at = getAccessToken();
        final String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token="+at;
        final JSONObject params = new JSONObject();
        final JSONObject scene = new JSONObject();
        scene.put("scene_str",TOKEN);
        final JSONObject action_info = new JSONObject();
        action_info.put("scene",scene);
        params.put("action_info",action_info);
        params.put("action_name","QR_STR_SCENE");
        params.put("expire_seconds",600);// 60 * 10 = 10分钟
        try {
            final String result = getQrCodeTicket(url,params.toString());
            final JSONObject object = JSONObject.parseObject(result);
            return object.getString("ticket");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAccessToken() {
        if(at==null||at.isExpired()) {
            getToken();
        }
        return at.getAccessToken();
    }

    //网页显示临时的二维码
    public static String getQrCodeTicket(final String url,final String data) {
        try {
            final URL urlObj = new URL(url);
            final URLConnection connection = urlObj.openConnection();
            // 要发送数据出去，必须要设置为可发送数据状态
            connection.setDoOutput(true);
            // 获取输出流
            final OutputStream os = connection.getOutputStream();
            // 写出数据
            os.write(data.getBytes());
            os.close();
            // 获取输入流
            final InputStream is = connection.getInputStream();
            final byte[] b = new byte[1024];
            int len;
            final StringBuilder sb = new StringBuilder();
            while ((len = is.read(b)) != -1) {
                sb.append(new String(b,0,len));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 进行sha1加密
     * @param src
    */
    private static String sha1(final String src) {
        try {
            //获取一个加密对象
            final MessageDigest md = MessageDigest.getInstance("sha1");
            //加密
            final byte[] digest = md.digest(src.getBytes());
            final char[] chars= {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
            final StringBuilder sb = new StringBuilder();
            //处理加密结果
            for(byte b:digest) {
                sb.append(chars[(b>>4)&15]);
                sb.append(chars[b&15]);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析xml数据包
     * @param is
     */
    public static Map<String,String> parseRequest(final InputStream is) {
        final Map<String,String> map = new HashMap<>();
        final SAXReader reader = new SAXReader();
        try {
            //读取输入流，获取文档对象
            final Document document = reader.read(is);
            //根据文档对象获取根节点
            final Element root = document.getRootElement();
            //获取根节点的所有的子节点
            final List<Element> elements = root.elements();
            for(Element e:elements) {
                map.put(e.getName(),e.getStringValue());
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return map;
    }
}