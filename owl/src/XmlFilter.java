/**
 * Created by chenql on 2017/4/20.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * 简单的字符过滤类
 */
public class XmlFilter {
    // RFC定义的XML可用字符集
    private static final String validChar = "[\\u0009|\\u000A|\\u000D|[\u0020-\uD7FF]|[\uE000-\uFFFD]|[\u10000-\u10FFFF]]+";

    /**
     * 根据XML的规则过滤字符串，将非法字符替换为下划线_
     * @param s 待过滤的字符串
     * @return 过滤后的字符串
     */
    public static String xmlFilter(String s){
        for(int i = 0; i < s.length(); i ++){
            String sub = s.substring(i, i+1);
            if(!sub.matches(validChar)){
//                System.out.println("filter");
                s = s.replace(s.charAt(i), '_');
            }
        }
        return s;
    }

    /**
     * 根据Jena URI/IRI的规则过滤字符串，将非法字符替换为下划线_
     * @param s 待过滤的字符串
     * @return 过滤后的字符串
     */
    public static String urlFilter(String s){
//      利用正则表达式去除URI中8个避用字符{}|\^[]`,还有易出错的字符&#%，以及空格
//      []需要两个\转义，\需要用四个\转义
        s = s.replaceAll("[&#%{}\\[\\]^|`\\\\ ]","_");
        return s;
    }

    /**
     * 一个简单的IRI地址有效性验证，通过向http://sparql.org/iri-validator.html发get请求验证IRI是否正确
     * @param iri 待验证的IRI地址
     * @return 是否有效
     * @throws Exception
     */
    public static boolean IriValidator(String iri) throws Exception {
        String url = "http://sparql.org/validate/iri";
        String param = "iri=" + URLEncoder.encode(iri, "UTF-8");
        String res = get(url, param);

        // 判断是否包含非法信息
        if(res.indexOf("Code: ")==-1){
            return false;
        } else{
            return true;
        }
    }

    /**
     *
     * @param url get请求地址
     * @param param get请求参数
     * @return get请求返回内容
     */
    public static String get(String url, String param) {
        try {
            URL realUrl = new URL(url + "?" + param);
            URLConnection urlConnection = realUrl.openConnection(); // 打开连接
            System.out.println(urlConnection.getURL().toString());

            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8")); // 获取输入流
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            br.close();
            System.out.println(sb.toString());
            String res = sb.toString();
            return res;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
