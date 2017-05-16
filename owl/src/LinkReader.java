/**
 * Created by chenql on 2017/4/20.
 */

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理百度百科和英文维基百科的link文件
 */
public class LinkReader {
    private static String langlinkArticle = "langlink.article";
    private static String langlinkCategory = "langlink.category";
    public static Map<String, String> linkMap;

    /**
     * 处理百度百科和英文维基百科的link文件
     *
     * @param model 本体
     * @throws IOException
     */
    public static void process(OntModel model) throws IOException {
        linkMap = new HashMap<String, String>();
        myCategoryLinkReader(CreateOntology.dir + langlinkArticle, model, "article");
        myCategoryLinkReader(CreateOntology.dir + langlinkCategory, model, "category");
    }

    /**
     * 用BufferedReader打开文件，按行读入和处理百度百科和英文维基百科的link文件
     * @param path link文件路径
     * @param model 本体
     * @param type 是category还是article的link
     * @throws IOException
     */
    public static void myCategoryLinkReader(String path, OntModel model, String type) throws IOException {
        File file = new File(path);
        if (!file.exists() || file.isDirectory())
            throw new FileNotFoundException();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        line = br.readLine();
        OntClass EnglishClass = null, ChineseClass = null;
        Individual EnglishIndividual = null, ChineseIndividual = null;
        while (line != null) {
            line = XmlFilter.xmlFilter(line);
            try {
                String eng = line.split("\\t\\t")[0];   //"\\t"匹配制表符
                String chn = line.split("\\t\\t")[1];   //"\\t"匹配制表符
                switch (type) {
                    case "article":
                        if (ArticleReader.ArticleIdMap.containsKey(eng) && ArticleReader.ArticleIdMap.containsKey(chn)) {
                            EnglishIndividual = model.getIndividual(CreateOntology.articleNameSpace + ArticleReader.ArticleIdMap.get(eng));
                            ChineseIndividual = model.getIndividual(CreateOntology.articleNameSpace + ArticleReader.ArticleIdMap.get(chn));
                            EnglishIndividual.addSameAs(ChineseIndividual);
                        }
                        break;
                    case "category":
                        if (CategoryReader.CategoryIdMap.containsKey(eng) && CategoryReader.CategoryIdMap.containsKey(chn)) {
                            EnglishClass = model.getOntClass(CreateOntology.categoryNameSpace + CategoryReader.CategoryIdMap.get(eng));
                            ChineseClass = model.getOntClass(CreateOntology.categoryNameSpace + CategoryReader.CategoryIdMap.get(chn));
                            ChineseClass.addSameAs(EnglishClass);
                        }
                        break;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                continue;
            }
            line = br.readLine();
        }
    }
}
