package es.upm.dit.gsi.episteme.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import es.upm.dit.gsi.episteme.json.JSONTreatment;
import es.upm.dit.gsi.episteme.matching.SemanticSemMF;
import es.upm.dit.gsi.episteme.matching.ServiceSemantic;
import es.upm.dit.gsi.episteme.rdfs.RdfConstructor;

/**
 * Servlet implementation class CompanyMatcher
 */
public class CompanyMatcher extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CompanyMatcher() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			doProcess(request, response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			doProcess(request, response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @throws JSONException 
	 * 
	 */
	protected void doProcess(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, JSONException {
		// Extract the parameters of the queryç
		String oferta= request.getParameter("offer").replace(" ", "_");
		
		// write rdf advertising/offer and enterprises
//        File fileOff = new File("/home/adri/Descargas/offers.rdf");
////        File fileOff = new File(getServletContext().getRealPath("/temp") + "/prove" + Long.toString(System.nanoTime()) + ".rdf");
//        String pathFileOffer = RdfConstructor.rdfOffer(fileOff);
//        
//        File fileEnt = new File("/home/adri/Descargas/enterprises.rdf");
////        File fileEnt = new File(getServletContext().getRealPath("/temp") + "/ent" + Long.toString(System.nanoTime()) + ".rdf");
//		String pathFileEnt = RdfConstructor.rdfEnterprises(fileEnt);
		
        //execute semantic matching (using semmf)
		String baseUrl = getServletContext().getRealPath("/");
		String pathFileOffer = baseUrl + "doc/offers.rdf";
		String pathFileEnt = baseUrl + "doc/enterprises.rdf";
        JSONObject semanticResult = SemanticSemMF.calMatching(baseUrl, pathFileEnt, pathFileOffer, oferta);
        
		// introduce semantic matching 
		JSONObject responseJson = ServiceSemantic.introduceSemantic(JSONTreatment.treatment(), semanticResult, oferta);
				
		// return output
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin","*");
		PrintWriter pw = new PrintWriter(response.getOutputStream());
		pw.println(responseJson);
		pw.close();
	}
}