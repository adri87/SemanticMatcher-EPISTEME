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
     * @return
     */
    public String getPathFile(){
    	String a =getServletContext().getRealPath("/");
    	return a;
    }
    
    /**
     * @return 
     */
    public void refresh(){
    	JSONTreatment jt = new JSONTreatment();
    	RdfConstructor rc = new RdfConstructor();
    	String base = getPathFile();
    	File f = new File (base + "doc/enterprises.rdf");
    	rc.rdfEnterprises(f, jt);
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
		
		// Declaring variables
		String baseUrl = getServletContext().getRealPath("/");
		RdfConstructor rdc = new RdfConstructor();
		ServiceSemantic sm = new ServiceSemantic();
		SemanticSemMF sSem = new SemanticSemMF();
		JSONTreatment jt = new JSONTreatment();
		
		// write rdf advertising/offer and enterprises
		File fileOff = new File(baseUrl + "doc/offers.rdf");
	    rdc.rdfOffer(fileOff, jt);
		String pathFileEnt = baseUrl + "doc/enterprises.rdf";
				
        //execute semantic matching (using semmf)
        JSONObject semanticResult = sSem.calMatching(baseUrl, pathFileEnt, fileOff.getAbsolutePath(), oferta, jt, rdc);
        
		// introduce semantic matching 
		JSONObject responseJson = sm.introduceSemantic(jt.treatment(), semanticResult, oferta);
				
		// return output
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin","*");
		PrintWriter pw = new PrintWriter(response.getOutputStream());
		pw.println(responseJson);
		pw.close();
	}
	
}