/**
 * Created by chenql on 2017/4/20.
 */

import java.io.*;

import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * SPARQL实现了简单的本体查询功能
 */
public class SPARQL {
	private static OntModel model = null;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
		model.read(new BufferedInputStream(new FileInputStream(
				"etc/ontology-demo.owl")), "RDF/XML");

		String label = "邱勇";

		String query = "PREFIX j.0: <http://keg.cs.tsinghua.edu.cn/> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?subject ?property ?value " + "WHERE " + "{"
				+ "?subject rdfs:label \"" + label + "\" . "
				+ "?subject ?property ?value " + "}";
		simpleQuery(model, query);
	}

	/**
	 * 通过查询语句构造查询，打印结果
	 * @param model 本体
	 * @param query 查询语句
	 * @throws IOException
	 */
	public static void simpleQuery(OntModel model, String query) throws IOException {
		long startTime = System.currentTimeMillis();

		org.apache.jena.query.Query q = QueryFactory.create(query);

		QueryExecution qexec = QueryExecutionFactory.create(q, model);
		ResultSet results = qexec.execSelect();

		ResultSetFormatter.out(System.out, results);
		qexec.close();

		long endTime = System.currentTimeMillis();
		System.out.println("SPARQL Time: " + (double) (endTime - startTime) / 1000 + "s.");
	}

}
