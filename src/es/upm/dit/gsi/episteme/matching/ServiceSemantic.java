package es.upm.dit.gsi.episteme.matching;

import java.util.NoSuchElementException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServiceSemantic {
	
	/**
	 * @param json
	 * @param weights
	 * @return
	 */
	public static JSONObject introduceSemantic(JSONObject json, JSONObject semanticResult, String oferta){
		JSONArray array;
		try {
			array = new JSONObject(json.getString("results").toString()).getJSONArray("bindings");
			for (int i = 0; i < array.length(); i++) {
				String enterprise = array.getJSONObject(i).getJSONObject("id").get("value").toString();
				json.getJSONObject("results").getJSONArray("bindings").getJSONObject(i).put(oferta, semanticResult.getJSONObject(enterprise));
			}
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
}