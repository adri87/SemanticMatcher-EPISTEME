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
	 * 
	 */
	public JSONObject treatment(){		
		String queryEnterprise = queryPrefix + "SELECT ?id ?name ?logo ?postalcode ?province ?address ?type ?summary WHERE {" +
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
				"?plan <http://kmm.lboro.ac.uk/ecos/1.0#detail> ?summary" +
		"}";

		
		try {
			queryEnterprise = URLEncoder.encode(queryEnterprise, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
//		queryEnterprise = "http://shannon.gsi.dit.upm.es/episteme/lmf/sparql/select?query="+queryEnterprise+"&output=json";
		queryEnterprise = "http://minsky.gsi.dit.upm.es/episteme/tomcat/LMF/sparql/select?query="+queryEnterprise+"&output=json";
		
		JSONObject enterprises = getJson(queryEnterprise);
		
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
	
	/**
	 * @param id
	 * @param skillsCompany
	 * @param enterprises
	 */
	public void introduceSkillsToEnterprises(String id, JSONArray skillsCompany, JSONObject enterprises){
		JSONArray array; 
		String idComp = "";
		try {
			array = enterprises.getJSONObject("results").getJSONArray("bindings");
			for (int i = 0; i < array.length(); i++) {
				idComp = array.getJSONObject(i).getJSONObject("id").getString("value");
				if (idComp.compareTo(id)==0) 
					enterprises.getJSONObject("results").getJSONArray("bindings").getJSONObject(i).put("skills", skillsCompany);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param enterprises
	 * @param skills
	 * @return
	 */
	public JSONObject compact(JSONObject enterprises, JSONObject skills){
		JSONArray skillsCompany = new JSONArray();
		String idComp = "";
		try {
			JSONArray allSkills = new JSONObject(skills.getString("results").toString()).getJSONArray("bindings");
			for (int i = 0; i < allSkills.length(); i++) {
				JSONObject skill = allSkills.getJSONObject(i);
				if (i==0) idComp = skill.getJSONObject("id").getString("value");
				String id = skill.getJSONObject("id").getString("value");
				if (id.compareTo(idComp)==0){
					skillsCompany.put(skill.getJSONObject("skill").getString("value"));
				}else {
					introduceSkillsToEnterprises(idComp, skillsCompany, enterprises);
					idComp = id;
					skillsCompany = new JSONArray();
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return enterprises;
	}
	
	/**
	 * @param enterprises
	 * @param skills
	 * @return
	 */
	public JSONObject compact2(JSONObject enterprises, JSONObject skills){
		JSONArray skillsCompany = new JSONArray();
		JSONObject aux, auxVal, auxVal2;
		String idComp = "";
		try {
			JSONArray allSkills = new JSONObject(skills.getString("results").toString()).getJSONArray("bindings");
			for (int i = 0; i < allSkills.length(); i++) {
				JSONObject skill = allSkills.getJSONObject(i);
				if (i==0) idComp = skill.getJSONObject("id").getString("value");
				String id = skill.getJSONObject("id").getString("value");
				if (id.compareTo(idComp)==0){
					auxVal = new JSONObject();
					auxVal2 = new JSONObject();
					aux = new JSONObject();
					auxVal.put("value", skill.getJSONObject("skill").getString("value"));
					auxVal.put("type", "literal");
					aux.put("skill", auxVal);
					auxVal2.put("value", skill.getJSONObject("skilllevel").getString("value"));
					auxVal2.put("type", "literal");
					aux.put("skilllevel", auxVal);
					skillsCompany.put(aux);
					skillsCompany.put(aux);					
				}else {
					introduceSkillsToEnterprises(idComp, skillsCompany, enterprises);
					idComp = id;
					skillsCompany = new JSONArray();
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return enterprises;
	}
	
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

		querySkills = "http://shannon.gsi.dit.upm.es/episteme/lmf/sparql/select?query="+querySkills+"&output=json";
		JSONObject skills = getJson(querySkills);
		
		return skills.getJSONObject("results").getJSONArray("bindings");
	}
	
	/**
	 * @return
	 * @throws JSONException
	 */
	public JSONArray getOportunities() throws JSONException {
		String queryOportunities = queryPrefix + "SELECT ?name ?req ?field ?weight WHERE {" +
				"?s ecos:name ?name ." +
				"?s gsi:companyReq ?req ." +
				"?req ecos:Preference ?pref1t ." +
				"?pref1t gsi:field ?field ." +
				"?pref1t ecos:weight ?weight" +
				"}ORDER BY(?name)";
		
		try {
			queryOportunities= URLEncoder.encode(queryOportunities, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

//		queryOportunities = "http://shannon.gsi.dit.upm.es/episteme/lmf/sparql/select?query="+queryOportunities+"&output=json";
		queryOportunities = "http://minsky.gsi.dit.upm.es/episteme/tomcat/LMF/sparql/select?query="+queryOportunities+"&output=json";
		
		JSONObject oportunities = getJson(queryOportunities);
		
		return oportunities.getJSONObject("results").getJSONArray("bindings");		
	}
	
	/**
	 * @param offer
	 * @return
	 * @throws JSONException
	 */
	public JSONArray getOportunities(String offer) throws JSONException {
		String queryOportunities = queryPrefix + "SELECT ?req ?field ?weight WHERE {" +
				"?s ecos:name \""+offer+"\" ." +
				"?s gsi:companyReq ?req ." +
				"?req ecos:Preference ?pref1t ." +
				"?pref1t gsi:field ?field ." +
				"?pref1t ecos:weight ?weight" +
				"}ORDER BY(?name)";
		
		try {
			queryOportunities= URLEncoder.encode(queryOportunities, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

//		queryOportunities = "http://shannon.gsi.dit.upm.es/episteme/lmf/sparql/select?query="+queryOportunities+"&output=json";
		queryOportunities = "http://minsky.gsi.dit.upm.es/episteme/tomcat/LMF/sparql/select?query="+queryOportunities+"&output=json";
		
		JSONObject oportunities = getJson(queryOportunities);
		
		return oportunities.getJSONObject("results").getJSONArray("bindings");		
	}
}
