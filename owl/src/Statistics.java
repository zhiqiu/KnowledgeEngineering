/**
 * Created by chenql on 2017/4/20.
 */

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * 统计每个根分支下的概念数、实例数、对象性属性数、数据类型属性数等
 */
public class Statistics {
    private static ArrayList<Node> rootConcept;
    public static Map<String, Integer> InstanceVisited;
    public static Map<String, Integer> CategoryVisited;
    public static Queue<String> queue;

    // 根分支节点
    static class Node {
        public String label;
        public String uri;
        public int sumConcpet;
        public int sumInstance;
        public int sumDataProperty;
        public int sumObjProperty;


        public Node(String label, String uri, int sumConcpet, int sumInstance, int sumDataProperty, int sumObjProperty) {
            this.label = label;
            this.uri = uri;
            this.sumConcpet = sumConcpet;
            this.sumInstance = sumInstance;
            this.sumDataProperty = sumDataProperty;
            this.sumObjProperty = sumObjProperty;
        }
    }

    /**
     * 统计操作，首先获取所有根节点，然后分别统计
     *
     * @param model 本体
     * @throws IOException
     */
    static void stat(OntModel model) throws IOException {
        rootConcept = new ArrayList<Node>();
        queue = new LinkedList<String>();
        InstanceVisited = new HashMap<String, Integer>();
        CategoryVisited = new HashMap<String, Integer>();

        getRootConcept(model);

        //System.out.println(rootConcept.size());
        statConcept(model);
        writeStat();
    }

    /**
     * 获取所有根节点
     *
     * @param model
     */
    static void getRootConcept(OntModel model) {
        Iterator<OntClass> iter = model.listHierarchyRootClasses();
        while (iter.hasNext()) {
            OntClass ontClass = iter.next();
            Node node = new Node(ontClass.getLabel(null), ontClass.getURI(), 0, 0, 0, 0);
            rootConcept.add(node);
        }
    }

    /**
     * 统计每一个根节点，BFS遍历每一个根节点
     *
     * @param model 本体
     */
    static void statConcept(OntModel model) {
        Iterator iter = rootConcept.iterator();
        while (iter.hasNext()) {
            InstanceVisited.clear();
            Node node = (Node) iter.next();
            bfs(model, node);
        }
    }

    /**
     * BFS遍历每一个根节点
     *
     * @param model 本体
     * @param node  根节点
     * @return
     */
    public static void bfs(OntModel model, Node node) {
        queue.clear();
        queue.add(node.uri);
        CategoryVisited.clear();
        CategoryVisited.put(node.uri, 1);
        while (!queue.isEmpty()) {
            String head = queue.poll();
            OntClass ontClass = model.getOntClass(head);
            // ---- list subclass------
            // 把所有sub class 加入队列
            Iterator<OntClass> iter1 = ontClass.listSubClasses();
            node.sumConcpet++;
            //System.out.println(ontClass);
            while (iter1.hasNext()) {
                OntClass c = iter1.next();
                if (!CategoryVisited.containsKey(c.getURI())) {
                    CategoryVisited.put(c.getURI(), 1);
                    queue.add(c.getURI());
                }
            }
            // ---- list instance------
            // 如果是叶子节点，统计该节点的instance
            if (!ontClass.hasSubClass()) {
                ExtendedIterator<? extends OntResource> individuals = ontClass.listInstances();
                while (individuals.hasNext()) {
                    Individual individual = (Individual) individuals.next();
                    // 没有被访问过
                    if (!InstanceVisited.containsKey(individual.getLabel(null))) {
                        node.sumInstance++;
                        InstanceVisited.put(individual.getLabel(null), 1);
                        statInstance(individual, node);
                    }
                }
            }

        }
    }

    /**
     * 统计一个instance的data属性数和object属性数
     *
     * @param ind  individual实例
     * @param node individual所属根分支
     */
    static void statInstance(Individual ind, Node node) {

        for (StmtIterator j = ind.listProperties(); j.hasNext(); ) {
            Statement s = j.next();
            if (s.getObject().isLiteral()) {
                if (!s.getPredicate().getLocalName().equals("label")) {
                    //System.out.println("subject: "+ind.getLabel(null)+"\tproperty: "+ s.getPredicate().getLocalName() + "\tobject: "+ s.getLiteral().getLexicalForm());
                    node.sumDataProperty++;
                }
            } else {
                if (s.getPredicate().getLocalName().equals("linkTo")) {
                    //System.out.println("subject: "+ind.getLabel(null)+"\tproperty: " +s.getPredicate().getLocalName() + "\tobject: " +s.getObject().as(Individual.class ).getLabel(null));
                    node.sumObjProperty++;
                }
            }
        }

    }

    /**
     * 将结果写入文件
     * @throws IOException
     */
    public static void writeStat() throws IOException {
        BufferedWriter fStat = new BufferedWriter(new FileWriter("etc/Statistics.txt"));
        for (Node node : rootConcept) {
            fStat.write("URI: " + node.uri + "\tlabel: " + node.label + "\tsumConcpet: " + node.sumConcpet + "\tsumInstance: " + node.sumInstance
                    + "\tsumObjectProperty: " + node.sumObjProperty + "\tsumDataProperty: " + node.sumDataProperty + "\n");
        }
        fStat.close();
    }

}
