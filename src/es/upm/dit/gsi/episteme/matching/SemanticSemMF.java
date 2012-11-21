package es.upm.dit.gsi.episteme.matching;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import de.fuberlin.wiwiss.semmf.engine.MatchingEngine;
import de.fuberlin.wiwiss.semmf.result.ClusterMatchingResult;
import de.fuberlin.wiwiss.semmf.result.GraphMatchingResult;
import de.fuberlin.wiwiss.semmf.result.MatchingResult;
import de.fuberlin.wiwiss.semmf.result.NodeMatchingResult;
import de.fuberlin.wiwiss.semmf.result.PropertyMatchingResult;
import de.fuberlin.wiwiss.semmf.vocabulary.MD;
import es.upm.dit.gsi.episteme.json.JSONTreatment;

/**
 * 
 * @author Adriano Martin
 * @version 1.0
 */
public class SemanticSemMF {

	/**
	 * @param baseURL
	 * @param pathFileEnt2 
	 * @param localURL
	 * @return
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws JSONException 
	 */
	public static JSONObject calMatching(String baseURL, String pathFileEnt,String pathFileOffer, String oferta) throws MalformedURLException, IOException, JSONException {
		String filePathServiceMD = baseURL + "doc/serviceMD.n3";
		Model serviceMD = createServiceMD(baseURL, pathFileEnt, pathFileOffer,oferta);
		writeServiceMDtoFile (serviceMD, filePathServiceMD, "N3");	     
			
		MatchingEngine me = new MatchingEngine("file:" + baseURL + "config/assemblerMappings.rdf", 
				"file:" + baseURL + "doc/serviceMD.n3", "N3");
//		File archivo = new File(pathFileEnt);
//		FileReader fr = new FileReader(archivo);
//		BufferedReader  br = new BufferedReader(fr);
//	    String linea;
//	    while((linea=br.readLine())!=null)
//	   	System.out.println(linea);
		MatchingResult mr = me.exec();
		
		File outputFile = new File(filePathServiceMD);
		if (outputFile.exists()) {
        	outputFile.delete();        	 
		}
		
		printMatchingResult(mr);
		return getSemanticSemMF(mr, oferta);
	}
	

	/**
	 * see tutorial: how to create a matching description (included in SemMF distribution)
	 */ 
	public static Model createServiceMD (String baseURL, String pathFileEnt, String pathFileOffer, String oferta) {
		
		Model m = ModelFactory.createDefaultModel();
		
		Resource gmd = m.createResource();
		gmd.addProperty(RDF.type, MD.GraphMatchingDescription);
		gmd.addProperty(MD.queryModelURL, "file:" + pathFileOffer);
		gmd.addProperty(MD.resModelURL, "file:" + pathFileEnt);
		gmd.addProperty(MD.queryGraphURI, "http://example.org/CategoriesRequired.rdfs#"+oferta);
		gmd.addProperty(MD.resGraphURIpath, "(?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://kmm.lboro.ac.uk/ecos/1.0#Enterprise>)");
						
		Bag cmds = m.createBag();
		gmd.addProperty(MD.hasClusterMatchingDescriptions, cmds);
		
		Resource cmd_skills = m.createResource();
		cmd_skills.addProperty(RDF.type, MD.ClusterMatchingDescription);		
		cmd_skills.addProperty(MD.label, "skills");
		cmd_skills.addProperty(MD.weight, "1");						
		cmds.add(cmd_skills);
			
		Bag nmds_cskills = m.createBag();
		cmd_skills.addProperty(MD.hasNodeMatchingDescriptions, nmds_cskills);
		
		Resource cskills_nmd = m.createResource();
		nmds_cskills.add(cskills_nmd);
		cskills_nmd.addProperty(RDF.type, MD.NodeMatchingDescription);
		cskills_nmd.addProperty(MD.label, "skill node");
		cskills_nmd.addProperty(MD.weight, "1");
		cskills_nmd.addProperty(MD.queryNodePath, "(<http://example.org/CategoriesRequired.rdfs#"+oferta+"> <http://example.org/CategoriesRequired.rdfs#hasCategorieDetails> ?categorieDetails) (?categorieDetails <http://example.org/CategoriesRequired.rdfs#requiredCompetence> ?x)");
		cskills_nmd.addProperty(MD.resNodePath, "(<#graphEntryURI#> <http://kmm.lboro.ac.uk/ecos/1.0#Skill> ?x)");
		cskills_nmd.addProperty(MD.reverseMatching, "false");
		
		Bag pmds = m.createBag();
		cskills_nmd.addProperty(MD.hasPropertyMatchingDescriptions, pmds);
							
		Resource pmd_skill = m.createResource();
		pmds.add(pmd_skill);
		pmd_skill.addProperty(RDF.type, MD.PropertyMatchingDescription);
		pmd_skill.addProperty(MD.label, "skill");
		pmd_skill.addProperty(MD.weight, "0.8");
		pmd_skill.addProperty(MD.queryPropURI, RDF.type);
		pmd_skill.addProperty(MD.resPropURI, RDF.type);
		pmd_skill.addProperty(MD.reverseMatching, "false");
		
		Resource tm_skill = m.createResource();
		tm_skill.addProperty(RDF.type, MD.TaxonomicMatcher);
		pmd_skill.addProperty(MD.useMatcher, tm_skill);
		tm_skill.addProperty(MD.simInheritance, "true");
					
		Resource taxon_skills = m.createResource();
		taxon_skills.addProperty(RDF.type, MD.Taxonomy);
		taxon_skills.addProperty(MD.taxonomyURL, "file:" + baseURL + "doc/it-cat.rdfs");
		taxon_skills.addProperty(MD.rootConceptURI, "http://kmm.lboro.ac.uk/ecos/1.0#IT_Cats");
		tm_skill.addProperty(MD.taxonomy, taxon_skills);
					
		Resource emc = m.createResource();
		emc.addProperty(RDF.type, MD.ExpMilestCalc);
		emc.addProperty(MD.k_factor, "2");
		tm_skill.addProperty(MD.useMilestoneCalc, emc);
		
		Resource pmd_skilllevel = m.createResource();
		pmds.add(pmd_skilllevel);
		pmd_skilllevel.addProperty(RDF.type, MD.PropertyMatchingDescription);
		pmd_skilllevel.addProperty(MD.label, "skill level");
		pmd_skilllevel.addProperty(MD.weight, "0.2");
		pmd_skilllevel.addProperty(MD.queryPropURI, "http://kmm.lboro.ac.uk/ecos/1.0#competenceLevel");
		pmd_skilllevel.addProperty(MD.resPropURI, "http://kmm.lboro.ac.uk/ecos/1.0#competenceLevel");
		pmd_skilllevel.addProperty(MD.reverseMatching, "false");
		
		Resource tm_skilllevel = m.createResource();
		tm_skilllevel.addProperty(RDF.type, MD.TaxonomicMatcher);
		pmd_skilllevel.addProperty(MD.useMatcher, tm_skilllevel);
		tm_skilllevel.addProperty(MD.simInheritance, "true");
		
		Resource taxon_skillLevel = m.createResource();
		taxon_skillLevel.addProperty(RDF.type, MD.Taxonomy);
		taxon_skillLevel.addProperty(MD.taxonomyURL, "file:" + baseURL + "doc/it-cat.rdfs");
		taxon_skillLevel.addProperty(MD.rootConceptURI, "http://kmm.lboro.ac.uk/ecos/1.0#Skill_level");
		tm_skilllevel.addProperty(MD.taxonomy, taxon_skillLevel);
		
		Resource lmc = m.createResource();
		lmc.addProperty(RDF.type, MD.LinMilestCalc);
		tm_skilllevel.addProperty(MD.useMilestoneCalc, lmc);
		
		return m;		
	}
	
	@SuppressWarnings("rawtypes")
	public static void printMatchingResult (MatchingResult mr) {
		
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("Query Graph: " + mr.getFirst().getQueryGraphEntryNode().getURI());
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\r");
		
		while (mr.hasNext()) {
			
			GraphMatchingResult gmr = mr.next();
						
			System.out.println("\r===========================================================");
			System.out.println("Res Graph : " + gmr.getResGraphEntryNode().getURI());
			System.out.println("Sim       : " + gmr.getSimilarity());
			System.out.println("===========================================================\r");
					
			List clusterList = gmr.getClusterMatchingResultList();
			for (Iterator itC = clusterList.iterator(); itC.hasNext();) {
				
				ClusterMatchingResult cmr = (ClusterMatchingResult) itC.next();
				
				System.out.println("-------------------------------------------");
				System.out.println("Cluster label : " + cmr.getLabel());
				System.out.println("Cluster sim   : " + cmr.getSimilarity());
				System.out.println("Cluster weight: " + cmr.getWeight());
				
				List nodeList = cmr.getNodeMatchingResultList();
				
				for (Iterator itN = nodeList.iterator(); itN.hasNext();) {
					
					NodeMatchingResult nmr = (NodeMatchingResult) itN.next();
					
					List propertyList = nmr.getPropertyMatchingResultList();
										
					System.out.println("    - - - - - - - - - - - - - - - - - - --");
					System.out.println("    Node label : " + nmr.getLabel());					
					if (propertyList.isEmpty()) {
						System.out.println("    query node : " + nmr.getQueryNode().toString());
						System.out.println("    res node   : " + nmr.getResNode().toString());									
					}
					System.out.println("    Node sim   : " + nmr.getSimilarity());
					System.out.println("    Node weight: " + nmr.getWeight());

					
					for (Iterator itP = propertyList.iterator(); itP.hasNext();) {
						
						PropertyMatchingResult pmr = (PropertyMatchingResult) itP.next();
						
						System.out.println("        . . . . . . . . . . . . . . . . . ");
						System.out.println("        prop label : " + pmr.getLabel());
						System.out.println("        query prop : " + pmr.getQueryPropVal().toString());
						System.out.println("        res prop   : " + pmr.getResPropVal().toString());			
						System.out.println("        prop sim   : " + pmr.getSimilarity());
						System.out.println("        prop weight: " + pmr.getWeight());
					
					}
									
				}				
			}
		}		
		mr.setToFirst();
	}
	
	@SuppressWarnings("rawtypes")
	public static JSONObject getSemanticSemMF (MatchingResult mr, String oferta) throws JSONException {
//		// Con empresas de VULKA
//		JSONObject semanticResult = new JSONObject();
//		while (mr.hasNext()) {
//			GraphMatchingResult gmr = mr.next();
//			List clusterList = gmr.getClusterMatchingResultList();
//			gmr.getResGraphEntryNode().getURI(); // name of enterprise
//			gmr.getSimilarity(); // simililarity global
//			for (Iterator itC = clusterList.iterator(); itC.hasNext();) {
//				JSONObject skills = new JSONObject();
//				ClusterMatchingResult cmr = (ClusterMatchingResult) itC.next();
//				List nodeList = cmr.getNodeMatchingResultList();
//				for (Iterator itN = nodeList.iterator(); itN.hasNext();) {
//					NodeMatchingResult nmr = (NodeMatchingResult) itN.next();
//					List propertyList = nmr.getPropertyMatchingResultList();
//					for (Iterator itP = propertyList.iterator(); itP.hasNext();) {
//						PropertyMatchingResult pmr = (PropertyMatchingResult) itP.next();
//						JSONObject skillEnt = new JSONObject();
//						skillEnt.put(pmr.getResPropVal().toString().substring(32), pmr.getSimilarity());
//						skills.put(pmr.getQueryPropVal().toString().substring(32), skillEnt);
//					}
//				}
//				skills.put("global", gmr.getSimilarity());
//				semanticResult.put(gmr.getResGraphEntryNode().getURI(), skills);
//			}
//		}		
//		mr.setToFirst();
		
		// Con INES
		JSONArray offerStructure = JSONTreatment.getOportunities(oferta);
		HashMap<String, String> relationStructureOffer = new HashMap<String, String>();
		for (int i = 0; i < offerStructure.length(); i++) {
			String req = offerStructure.getJSONObject(i).getJSONObject("req").getString("value");
			String field = transformString(offerStructure.getJSONObject(i).getJSONObject("field").getString("value"));
			relationStructureOffer.put(field, req);
		}
		
		JSONObject semanticResult = new JSONObject();
		while (mr.hasNext()) {
			GraphMatchingResult gmr = mr.next();
			List clusterList = gmr.getClusterMatchingResultList();
			gmr.getResGraphEntryNode().getURI(); // name of enterprise
			gmr.getSimilarity(); // simililarity global
			for (Iterator itC = clusterList.iterator(); itC.hasNext();) {
				HashMap<String, Float> store = new HashMap<String, Float>();
				JSONObject skills = new JSONObject();
				ClusterMatchingResult cmr = (ClusterMatchingResult) itC.next();
				List nodeList = cmr.getNodeMatchingResultList();
				for (Iterator itN = nodeList.iterator(); itN.hasNext();) {
					NodeMatchingResult nmr = (NodeMatchingResult) itN.next();
					List propertyList = nmr.getPropertyMatchingResultList();
					for (Iterator itP = propertyList.iterator(); itP.hasNext();) {
						PropertyMatchingResult pmr = (PropertyMatchingResult) itP.next();
						String req = relationStructureOffer.get(pmr.getQueryPropVal().toString().substring(32));
						if (req	!= null){
							if (store.get(req) != null)
								store.put(req, (store.get(req)+nmr.getSimilarity())/3);
							else
								store.put(req, nmr.getSimilarity());
							skills.put(req, store.get(req));
						}
					}
				}
				semanticResult.put(gmr.getResGraphEntryNode().getURI(), skills);
			}
		}		
		mr.setToFirst();
		
		return semanticResult;
	}
	
	/**
	 * @param m
	 * @param filePath
	 * @param lang
	 */
	private static void writeServiceMDtoFile(Model m, String filePath, String lang) {
		
		m.setNsPrefix("semmf", MD.NS);
		m.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		m.setNsPrefix("ja", "http://jena.hpl.hp.com/2005/11/Assembler#");
		
		try {
			File outputFile = new File(filePath);
			if (!outputFile.exists()) {
	        	outputFile.createNewFile();        	 
	        }
			FileOutputStream out = new FileOutputStream(outputFile);
			m.write(out, lang);
			out.close();
		}
		catch (IOException e) { System.out.println(e.toString()); }
	}
	
	/**
	 * @param skill
	 * @return
	 */
	public static String transformString (String skill){
		return skill.replace(" ", "_").replace(",", "").replace("(", "").replace(")","").replace("/", "_").replace("&", "_").replace("®", "").replace(":", "").replace("-*", "").replace("…", "");
	}
	
}