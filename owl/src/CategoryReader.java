/**
 * Created by chenql on 2017/4/20.
 */

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 处理百度百科和英文维基百科的Category文件
 */
public class CategoryReader {
    private static String baiduCategory = "baidu.category";
    private static String wikipediaCategory = "enwiki.category";

    public static int id;
    public static Map<String, String> CategoryIdMap;  // Categpory的ID和对于名字的map
    public static ArrayList<String> CategoryCycles;  // category中的环中的反向边

    /**
     * 处理百度百科和英文维基百科的Category文件
     *
     * @param model 本体模型
     * @throws IOException
     */
    public static void process(OntModel model) throws IOException {
        CategoryCycles = new ArrayList<String>();
        CategoryIdMap = new HashMap<String, String>();
        id = 0;
        myCategoryReader(CreateOntology.dir + baiduCategory, model);

        // 由于百度百科语料中有一个Root是无意义的concept，故去除
        if (CategoryIdMap.containsKey("Root")) {
            OntClass root = model.getOntClass(CreateOntology.categoryNameSpace + CategoryIdMap.get("Root"));
            root.remove();
            CategoryIdMap.remove("Root");
        }
        myCategoryReader(CreateOntology.dir + wikipediaCategory, model);
        // 去重concept里面的环路
        removeCycle(model);
        writeCategoryIdMap();
        writeCategoryCycles();
    }

    /**
     * 用BufferedReader打开文件，按行读入和处理
     *
     * @param path  文件路径
     * @param model 本体模型
     * @throws IOException
     */
    public static void myCategoryReader(String path, OntModel model) throws IOException {
        File file = new File(path);
        if (!file.exists() || file.isDirectory())
            throw new FileNotFoundException();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        line = br.readLine();
        while (line != null) {
            line2class(line, CreateOntology.categoryNameSpace, model);
            line = br.readLine();
        }
    }

    /**
     * 处理每一行的subCategoryOf关系
     *
     * @param line      文件一行
     * @param nameSpace 命名空间
     * @param model     本体
     */
    public static void line2class(String line, String nameSpace, OntModel model) {

        line = XmlFilter.xmlFilter(line);
        OntClass superClass = null, subClass = null;
        String superStr = line.split("\\t")[0];   //"\\t"匹配制表符
        String subStr[] = (line.split("\\t")[1]).split(";");

        // 如果已经存在，就不重复创建
        if (CategoryIdMap.containsKey(superStr)) {
            superClass = model.getOntClass(nameSpace + CategoryIdMap.get(superStr));
        } else {
            id++;
            CategoryIdMap.put(superStr, String.valueOf(id));
            superClass = model.createClass(nameSpace + String.valueOf(id));
            Literal label = model.createLiteral(superStr);
            superClass.addLabel(label);
        }

        for (String sub : subStr) {
            // 如果已经存在，就不重复创建
            if (CategoryIdMap.containsKey(sub)) {
                subClass = model.getOntClass(nameSpace + CategoryIdMap.get(sub));
            } else {
                id++;
                CategoryIdMap.put(sub, String.valueOf(id));
                subClass = model.createClass(nameSpace + String.valueOf(id));
                Literal label = model.createLiteral(sub);
                subClass.addLabel(label);
            }
            superClass.addSubClass(subClass);
            subClass.addSuperClass(superClass);
        }
    }

    /**
     * DFS 判断有向图是否有环，及简单去环
     * 正在处理的节点标0
     * 未被访问标-1，由于是map，所以在map中没有则为-1，要设置时，直接设置。
     * 处理完的节点标1，该节点的所有后代都被访问过。
     * 如果第一次访问(u,v)时v标记为0，则(u,v)为反向边。则存在环，删除该边。
     *
     * @param node    当前访问的节点
     * @param visited 访问过的节点数组
     */
    public static void dfs(OntClass node, Map<String, Integer> visited) {
        visited.put(node.getURI(), 0);
        Iterator<OntClass> subs = node.listSubClasses();
        while (subs.hasNext()) {
            OntClass sub = subs.next();
            // 不存在，即为-1
            if (!visited.containsKey(sub.getURI())) {
                dfs(sub, visited);
            }
            // 有换，删除该节点
            else if (visited.get(sub.getURI()) == 0) {
                CategoryCycles.add("cycle, with reverse edge: " + node.getLabel(null) + "====>" + sub.getLabel(null) + "\n");
                subs.remove();
                node.removeSubClass(sub);
            }
        }
        visited.put(node.getURI(), 1);
    }

    /**
     * cetogory 去环,获取所有顶级cetegory，对每个顶级category进行dfs实现
     * @param model 本体
     */
    public static void removeCycle(OntModel model) {
        Iterator<OntClass> iter = model.listHierarchyRootClasses();
        Map<String, Integer> visited = new HashMap<String, Integer>();
        while (iter.hasNext()) {
            OntClass ontClass = iter.next();
            if (!visited.containsKey(ontClass.getURI())) {
                dfs(ontClass, visited);
            } else {
                System.out.println("impossible" + ontClass.getLabel(null));
            }
        }
    }

    /**
     * 把category的环写入文件
     * @throws IOException
     */
    public static void writeCategoryCycles() throws IOException {
        BufferedWriter fCycle = new BufferedWriter(new FileWriter("etc/CategoryCycle.txt"));
        for (String line : CategoryCycles) {
            fCycle.write(line);
        }
        fCycle.close();
    }

    /**
     * 把category的ID及对应name的map写入文件
     * @throws IOException
     */
    public static void writeCategoryIdMap() throws IOException {
        BufferedWriter fMap = new BufferedWriter(new FileWriter("etc/CategoryIdMap.txt"));
        for (Map.Entry<String, String> entry : CategoryIdMap.entrySet()) {
            fMap.write(entry.getKey() + "\t\t" + entry.getValue() + "\n");
        }
        fMap.close();
    }
}
