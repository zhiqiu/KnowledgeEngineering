/**
 * Created by chenql on 2017/4/20.
 */
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfxml.xmloutput.impl.BaseXMLWriter;
import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.query.Dataset;

public class CreateOntology {

	public static String dir = "./data/";
	//public static String dir = "C:\\Users\\chenql\\Desktop\\knowledge engineering\\Project1\\data\\";
	public static String ontpath = "etc/myontology.owl";

	public static String categoryNameSpace = "http://my.ontmodel.com/category/";
	public static String articleNameSpace = "http://my.ontmodel.com/article/";
	public static String objPropertyNameSpace = "http://my.ontmodel.com/objProperty/";
	public static String dataPropertyNameSpace = "http://my.ontmodel.com/dataProperty/";


	private static OntModel model = null;


	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
		long endTime, startTime = System.currentTimeMillis();
		//model.read(new BufferedInputStream(new FileInputStream("etc/myontology.owl")), "RDF/XML");
		// 将baidu和wikipedia的Category处理为OWL
		CategoryReader.process(model);

		// 将baidu和wikipedia的article处理为OWL
		ArticleReader.process(model);

		// 将中英文link处理为OWL
		LinkReader.process(model);

		// OntModel写入文件	
		BufferedWriter out1 = new BufferedWriter(new FileWriter(ontpath));
		model.write(out1, "RDF/XML");	
		out1.close();
		endTime  = System.currentTimeMillis();
		System.out.println("Create Ontology Time: " + (double) (endTime - startTime) / 1000+ "s.");

		// 统计顶级concept的相关信息
		Statistics.stat(model);
		endTime  = System.currentTimeMillis();
		System.out.println("Statistic Time: " + (double) (endTime - startTime) / 1000 +"s.");

		// 简单的SPARQL查询
		String query = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in ));
		while(true) {
			try{
				System.out.println("input query:");
				query = br.readLine();
				SPARQL.simpleQuery(model, query);
				if(query.equals("end")) break;
			} catch(IOException e){
				e.printStackTrace();
			}
		}
		// 写入数据库
		//		Dataset ds = TDBFactory.createDataset("etc/tdb");
		//		ds.addNamedModel(ontpath,model);
		//		ds.commit();
		//		ds.close();
	}

	/* merge two model
	public static OntModel merge(String path1, String path2) throws FileNotFoundException, IOException{

		final OntModel model1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
		final OntModel model2 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
		model1.read(new BufferedInputStream(new FileInputStream(path1)), "RDF/XML");
		model2.read(new BufferedInputStream(new FileInputStream(path2)), "RDF/XML");

		// If you only have two models, you can use Union model.
		final Model union = ModelFactory.createUnion( model1, model2 );
		try ( final OutputStream out1 = new FileOutputStream( new File( "union.owl" )) ) {
			union.write( out1, "RDF/XML");
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
		return union;
	}
	*/

}
