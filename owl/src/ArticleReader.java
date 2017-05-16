/**
 * Created by chenql on 2017/4/20.
 */
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理百度百科和英文维基百科的Article文件
 */
public class ArticleReader {
    private static String baiduArticle = "baidu.article";
    private static String wikipediaArticle = "enwiki.article";
    public static ArrayList<String> InvalidUris;       // 不合法的URI
    public static Map<String, String> ArticleIdMap;    // Article的ID和对于title的map
    public static int id ;

    /**
     * 处理百度百科和英文维基百科的Article文件
     * @param model
     * @throws IOException
     */
    public static void process(OntModel model) throws IOException {
        InvalidUris = new ArrayList<String>();
        ArticleIdMap = new HashMap<String, String>();
        id = 0;
        myArticleReader(CreateOntology.dir + baiduArticle, model, 600000);
        myArticleReader(CreateOntology.dir + wikipediaArticle, model, 1200000);

        writeArticleIdMap();
        writeInvalidUris();
   }

    /**
     * 用BufferedReader打开文件，按行读入和处理
     * @param path 文件路径
     * @param model 本体模型
     * @param num instance数量阈值
     * @throws IOException
     */
    public static void myArticleReader(String path, OntModel model, int num) throws IOException {
        File file=new File(path);
        if(!file.exists() || file.isDirectory())
            throw new FileNotFoundException();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        line = br.readLine();

        while(line != null){
            if(ArticleIdMap.size() > num) break;
            if(!line.equals("")&&line.charAt(0) == 'I'){
                instance2individual(line, br, model);
            }
            line = br.readLine();
        }
    }

    /**
     * 根据每行开头的字符分类处理
     * @param line 文件中的一行
     * @param br BufferedReader
     * @param model 本体模型
     */
    public static void instance2individual(String line, BufferedReader br, OntModel model) {
        String Id = null, title = null, abstractDesc = null, relatedIndividual[] = null, linkedIndividual[] = null, infoBox[] = null, belongCategory[] = null, synonym = null;
        while(line != null && !line.equals("")){
            line = XmlFilter.xmlFilter(line);
            try {
                switch (line.split(":")[0]) {
                    case "I":
                        Id = line.split(":")[1];
                        break;
                    case "T":
                        title = line.split(":")[1];
                        break;
                    case "A":
                        abstractDesc = line.substring(2);
                        break;
                    case "R":
                        relatedIndividual = line.substring(2).split("::;");
                        break;
                    case "L":
                        linkedIndividual = line.substring(2).split("::;");
                        break;
                    case "IB":
                        infoBox = line.substring(3).split("::;");
                        break;
                    case "S":
                        synonym = line.substring(2);
                        break;
                    case "C":
                        belongCategory = line.substring(2).split("::;");
                        break;
                    default:
                        break;
                }
                line = br.readLine();
            }catch (Exception e){
                System.out.println(e.getMessage());
                return ;
            }
        }
        //showInstance(id, title, abstractDesc, relatedIndividual, linkedIndividual, infoBox, belongCategory, synonym);

        OntClass leafClass = null;
        Individual individual = null;

        // 如果instance不属于任何category，则不处理
        if(belongCategory == null) return;
        for(String category : belongCategory){
            // 存在该category，则获取
            if(CategoryReader.CategoryIdMap.containsKey(category)){
                leafClass = model.getOntClass(CreateOntology.categoryNameSpace+ CategoryReader.CategoryIdMap.get(category));
            }else{
                // 不存在该category，则不处理
                continue;
            }
            // 叶子节点
            if(!leafClass.hasSubClass()){
                // create individual if not exist
                if(ArticleIdMap.containsKey(title)){
                    individual = model.getIndividual(CreateOntology.articleNameSpace+ ArticleIdMap.get(title));
                    individual.addOntClass(leafClass);
                }else{
                    id ++;
                    ArticleIdMap.put(title,  String.valueOf(id));
                    individual = leafClass.createIndividual(CreateOntology.articleNameSpace + String.valueOf(id));
                    individual.addLabel(model.createLiteral(title));
                    individual.addOntClass(leafClass);
                }
            }
        }

        // 处理link 属性
        Individual linkIndividual = null;
        if(individual != null && linkedIndividual != null){
            for(String link : linkedIndividual){
                if(ArticleIdMap.containsKey(link)){
                    linkIndividual = model.getIndividual(CreateOntology.articleNameSpace+ ArticleIdMap.get(title));
                    ObjectProperty linkTo = model.createObjectProperty(CreateOntology.objPropertyNameSpace + "linkTo");
                    individual.addProperty(linkTo, linkIndividual);
                }else{
                    continue;
                }
            }
        }

        // 处理InfoBox 属性
        if(individual != null && infoBox != null){
            for(String info : infoBox){
                try {
                    String name = info.split("::=")[0].trim();
                    String data = info.split("::=")[1].trim();

                    String uriref = CreateOntology.dataPropertyNameSpace + name;
                    uriref = XmlFilter.urlFilter(uriref);

                    // 处理下面的exception
                    // string index out of range,
                    // in org.apache.jena.rdf.model.impl.ModelCom.updateNamespace
                    int split = Util.splitNamespaceXML(uriref);
                    if(split == uriref.length()) {
                        InvalidUris.add("invalid uri: split == uriref.length() :" + uriref + "\n");
                    } else if (split > uriref.length()){
                        InvalidUris.add("invalid uri: split > uriref.length() :" + uriref + "\n");
                    }
                    else{
                        DatatypeProperty p = model.createDatatypeProperty(uriref);
                        individual.addProperty(p, data);
                    }
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }

        }
    }

    /**
     * 把不合法的URI写入文件
     * @throws IOException
     */
    public static void writeInvalidUris() throws IOException {
        BufferedWriter fUri = new BufferedWriter(new FileWriter("etc/InvalidUris.txt"));
        for(String line: InvalidUris){
            fUri.write(line);
        }
        fUri.close();
    }

    /**
     * 把Article的ID和对应名字的map写入文件
     * @throws IOException
     */
    public static void writeArticleIdMap() throws IOException {
        BufferedWriter fMap = new BufferedWriter(new FileWriter("etc/ArticleIdMap.txt"));
        for(Map.Entry<String, String> entry: ArticleIdMap.entrySet()){
            fMap.write(entry.getKey() + "\t\t" + entry.getValue() + "\n");
        }
        fMap.close();
    }

    /**
     * 打印instance
     * @throws IOException
     */
    public static void showInstance(String id, String title, String abstractDesc, String relatedIndividual[], String linkedIndividual[], String infoBox[], String belongCategory[], String synonym ){
        System.out.println("id:" + id);
        System.out.println("title:" + title);
        System.out.println("synonym:" + synonym);
        System.out.println("abstract:" + abstractDesc);
        System.out.println("===============R==================");
        if(relatedIndividual != null){
            for(String individual : relatedIndividual){
                System.out.println(individual);
            }
        }
        System.out.println("===============L==================");
        if(linkedIndividual != null){
            for(String individual : linkedIndividual){
                System.out.println(individual);
            }
        }
        System.out.println("===============IB==================");
        if(infoBox != null){
            for(String individual : infoBox){
                System.out.println(individual);
            }
        }
        System.out.println("===============C==================");
        if(belongCategory != null){
            for(String individual : belongCategory){
                System.out.println(individual);
            }
        }
        System.out.println("############");
    }
}
