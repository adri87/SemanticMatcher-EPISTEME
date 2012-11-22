package es.upm.dit.gsi.episteme.rdfs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import es.upm.dit.gsi.episteme.json.JSONTreatment;

public class RdfConstructor {
	
	/**
	 * @param enterprises
	 * @return
	 */
	public static String rdfEnterprises(File enterprises){
		String pathFileEnt = "";
		JSONArray skills = new JSONArray();
		String id ="", idComp = "";
        try {
        	skills = JSONTreatment.getJSONSkills();
    		enterprises.createNewFile();
    		FileWriter out = new FileWriter(enterprises);
    		out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            out.write("<rdf:RDF\n");
            out.write("xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
            out.write("xmlns:ecos=\"http://kmm.lboro.ac.uk/ecos/1.0#\"\n");
            out.write("xmlns:v=\"http://www.w3.org/2006/vcard/ns#\">\n");
            for (int i = 0; i < skills.length(); i++) {
            	try {
					id = skills.getJSONObject(i).getJSONObject("id").getString("value").replace(" ", "_").replace(",", "");
					if (i==0) out.write("<ecos:Enterprise rdf:about=\""+id+"\">\n");
					else if (id.compareTo(idComp)!=0) {
			            out.write("</ecos:Enterprise>\n");
						out.write("<ecos:Enterprise rdf:about=\""+id+"\">\n");
					}
	                out.write("<ecos:Skill>\n");
	                out.write("<ecos:"+transform(skills.getJSONObject(i).getJSONObject("skill").getString("value"))+">\n");
	                out.write("<ecos:competenceLevel rdf:resource=\"http://kmm.lboro.ac.uk/ecos/1.0#"+skills.getJSONObject(i).getJSONObject("skilllevel").getString("value").replace(" ", "_").replace(",", "")+"\"/>\n");
	                out.write("</ecos:"+transform(skills.getJSONObject(i).getJSONObject("skill").getString("value"))+">\n");
	                out.write("</ecos:Skill>\n");
	                idComp = id;      
				} catch (JSONException e) {
					e.printStackTrace();
			 	}
			}
            out.write("</ecos:Enterprise>\n");
            out.write("</rdf:RDF>");
            out.close();       
            pathFileEnt = enterprises.getAbsolutePath();
		} catch (JSONException e1) {
			e1.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }
		return pathFileEnt;
	}
	
	/**
	 * @param offer
	 * @return
	 */
	public static String rdfOffer(File offer){
		String pathFileOffer = "";
		JSONArray oportunities = new JSONArray();
		String namComp = "";
        try {
    		oportunities = JSONTreatment.getOportunities();
    		offer.createNewFile();
    		FileWriter out = new FileWriter(offer);
    		out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            out.write("<rdf:RDF\n");
            out.write("xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
            out.write("xmlns:cr=\"http://example.org/CategoriesRequired.rdfs#\"\n");
            out.write("xmlns:skill=\"http://kmm.lboro.ac.uk/ecos/1.0#\"\n");
            out.write("xml:base=\"http://example.org/CategoriesRequired.rdfs#\">\n\r");
            for (int i=0; i<oportunities.length(); i++){
            	String nameOffer = oportunities.getJSONObject(i).getJSONObject("name").getString("value").replace(" ", "_").replace(",", "");
                if (nameOffer.compareTo(namComp) != 0){
                	if (i !=0) {
                        out.write("</cr:CategorieDetails>\n\r");
                        out.write("</cr:hasCategorieDetails>\n\r");
                        out.write("</cr:CategoriesRequired>\n");
                	}
                	out.write("<cr:CategoriesRequired rdf:ID=\""+nameOffer+"\">\n\r");
                	out.write("<cr:hasDetails>\n");
                    out.write("<cr:Details>\n");
                    out.write("<cr:endDate>2012-08-31</cr:endDate>\n");
                    out.write("<cr:startDate>2012-07-01</cr:startDate>\n");
                    out.write("</cr:Details>\n");		
                    out.write("</cr:hasDetails>\n\r");
                    out.write("<cr:hasCategorieDetails>\n");
                    out.write("<cr:CategorieDetails>\n\r");
                } 
                out.write("<cr:requiredCompetence>\n");
                out.write("<skill:"+transform(oportunities.getJSONObject(i).getJSONObject("field").getString("value"))+">\n");
                out.write("<skill:competenceLevel rdf:resource=\"http://kmm.lboro.ac.uk/ecos/1.0#"+oportunities.getJSONObject(i).getJSONObject("weight").getString("value").replace(" ", "_").replace(",", "")+"\"/>\n");
                out.write("</skill:"+transform(oportunities.getJSONObject(i).getJSONObject("field").getString("value"))+">\n");
                out.write("</cr:requiredCompetence>\n\r"); 
                namComp = nameOffer;
                if (i == oportunities.length()-1) {
                	 out.write("</cr:CategorieDetails>\n\r");
                     out.write("</cr:hasCategorieDetails>\n\r");
                     out.write("</cr:CategoriesRequired>\n");        	
                }

            }
            out.write("</rdf:RDF>");
            out.close();        
            pathFileOffer = offer.getAbsolutePath();
		} catch (JSONException e1) {
			e1.printStackTrace();            
        } catch (IOException e) {
        	e.printStackTrace();
        }
                
		return pathFileOffer;		
	}
	

	/**
	 * @param s
	 * @return
	 */
	public static String transform (String s){
		return s.replace(" ", "_").replace(",", "").replace("(", "").replace(")","").replace("/", "_").replace("&", "_").replace("®", "").replace(":", "").replace("-*", "").replace("…", "");
	}
}
