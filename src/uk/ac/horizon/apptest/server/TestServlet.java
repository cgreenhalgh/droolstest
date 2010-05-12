/**
 * 
 */
package uk.ac.horizon.apptest.server;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author cmg
 *
 */
public class TestServlet extends HttpServlet {
	static Logger logger = Logger.getLogger(TestServlet.class.getName());
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	//@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			uk.ac.horizon.apptest.desktop.DroolsTest.main(null);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		Writer w = resp.getWriter();
		w.write("Hello!\n");
		w.close();
	}

}
