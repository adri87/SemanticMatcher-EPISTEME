package es.upm.dit.gsi.episteme.json;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONTreatment {
	public static String queryPrefix = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
		+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
		+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
		+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
		+ "PREFIX dc: <http://purl.org/dc/elements/1.1/>"
		+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
		+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
		+ "PREFIX gsi: <http://www.gsi.dit.upm.es/>"
		+ "PREFIX ecos: <http://kmm.lboro.ac.uk/ecos/1.0#>";
	
	/**
	 * @return
	 * @throws JSONException 
	 */
	public JSONArray getJSONSkills() throws JSONException{
		String querySkills = queryPrefix + "SELECT ?id ?skill ?skilllevel WHERE {" +
				"?id <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://kmm.lboro.ac.uk/ecos/1.0#Enterprise> ." +
				"?id <http://www.w3.org/2006/vcard/ns#VCard> ?vcard ." +
				"?id <http://kmm.lboro.ac.uk/ecos/1.0#Specific> ?specific ." +
				"?specific <http://kmm.lboro.ac.uk/ecos/1.0#Skill> ?nodoskill ." +
				"?nodoskill rdf:Bag ?nodobag ." +
				"?nodobag ?p ?skillcontent ." +
				"?skillcontent <http://kmm.lboro.ac.uk/ecos/1.0#name> ?skill ." +
				"?skillcontent <http://kmm.lboro.ac.uk/ecos/1.0#level> ?skilllevel" +
				"}";

		try {
			querySkills = URLEncoder.encode(querySkills, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		querySkills = "http://minsky.gsi.dit.upm.es/episteme/tomcat/LMF/sparql/select?query="+querySkills+"&output=json";
		JSONObject skills = getJson(querySkills);
		
		return skills.getJSONObject("results").getJSONArray("bindings");
	}
		
	/**
	 * @param offer
	 * @return
	 * @throws JSONException
	 */
	public JSONObject getOportunitie(String offer) throws JSONException {

		String queryOportunitie = "http://minsky.gsi.dit.upm.es/episteme/tomcat/LMF/config/data/episteme.search." + offer;	
		JSONObject jo = getJson(queryOportunitie);
		JSONArray oportunitie = new JSONArray(jo.getJSONArray("episteme.search." + offer).toString().replace("[\"", "[").replace("\"]", "]").replace("\\\"", "\"").replace("\",\"", ",").replace("\\", "\""));
		
		return oportunitie.getJSONObject(0);
	}
	
	/**
	 * @return 
	 * @throws JSONException 
	 * 
	 */
	public JSONArray treatment() throws JSONException{		
		String queryEnterprise = queryPrefix + "SELECT ?id ?name ?logo ?postalcode ?province ?address ?type ?description WHERE {" +
				"?id <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://kmm.lboro.ac.uk/ecos/1.0#Enterprise> ;" +
				"   <http://www.w3.org/2006/vcard/ns#VCard> ?vcard ." +
				"OPTIONAL{" +
				"?vcard <http://www.w3.org/2006/vcard/ns#logo> ?logo ." +
				"}" +
				"?vcard <http://www.w3.org/2006/vcard/ns#fn> ?name ;" +
				"       <http://www.w3.org/2006/vcard/ns#adr> ?direccionnodo ." +
				"?direccionnodo <http://www.w3.org/2006/vcard/ns#postal-code> ?postalcode ;" +
				"               <http://www.w3.org/2006/vcard/ns#locality> ?province ;" +
				"               <http://www.w3.org/2006/vcard/ns#street-address> ?address ." +
				"?vcard <http://www.w3.org/2006/vcard/ns#org> ?org ." +
				"?org <http://www.w3.org/2006/vcard/ns#organisation-unit> ?type ." +
				"?id <http://kmm.lboro.ac.uk/ecos/1.0#Specific> ?specific ." +
				"?specific <http://kmm.lboro.ac.uk/ecos/1.0#Plan> ?plan ." +
				"?plan <http://kmm.lboro.ac.uk/ecos/1.0#detail> ?description" +
		"}";

		
		try {
			queryEnterprise = URLEncoder.encode(queryEnterprise, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		queryEnterprise = "http://minsky.gsi.dit.upm.es/episteme/tomcat/LMF/sparql/select?query="+queryEnterprise+"&output=json";
		JSONObject result = getJson(queryEnterprise);
		
		JSONArray enterprises = new JSONArray();
		JSONArray aux = result.getJSONObject("results").getJSONArray("bindings");
		for (int i = 0; i < aux.length(); i++) {
			JSONObject enterprise = new JSONObject();
			JSONObject auxObj = aux.getJSONObject(i);
			enterprise.put("id", auxObj.getJSONObject("id").get("value").toString().substring(28));
//			if (auxObj.getJSONObject("logo") != null)
//				enterprise.put("logo", auxObj.getJSONObject("logo").get("value"));
			enterprise.put("type", auxObj.getJSONObject("type").get("value"));
			enterprise.put("name", auxObj.getJSONObject("name").get("value"));
			enterprise.put("description", auxObj.getJSONObject("description").get("value"));
			enterprise.put("postalcode", auxObj.getJSONObject("postalcode").get("value"));
			enterprise.put("address", auxObj.getJSONObject("address").get("value"));
			enterprise.put("province", auxObj.getJSONObject("province").get("value"));
			enterprises.put(enterprise);
		}
		
		return enterprises;		
	}
	
	/**
	 * @param query
	 * @param categorieRequired
	 * @return
	 */
	public JSONObject getJson(String query){
		URL url;
	    HttpURLConnection conn;
	    BufferedReader rd;
	    JSONObject json = new JSONObject();
	    String line;
	    String result = "";
	    try {
	    	url = new URL(query);
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        while ((line = rd.readLine()) != null) {
	        	result += line;
	        }
	        rd.close();
	        json = new JSONObject(result);
	    } catch (Exception e) {
	    	e.printStackTrace();
		}
		return json;
	}
}
