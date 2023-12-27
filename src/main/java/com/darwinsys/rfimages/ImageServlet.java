package com.darwinsys.rfimages;

import java.io.*;
import java.nio.file.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
// import jakarta.servlet.*;
// import jakarta.servlet.annotation.WebServlet;
// import jakarta.servlet.http.*;

/**
 * Servlet to serve images after validating image name
 * Some code from
 * https://stackoverflow.com/questions/8623709/output-an-image-file-from-a-servlet
 */
@WebServlet(urlPatterns = "/image/*", loadOnStartup = 1)
public class ImageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	final static String[] baddies = {
		";",
		"'",
		"/",
		"\\",
	};

	@Override
	public void init() {
		System.out.println("ImageServlet initialized.");
	}

	/**
	 * This is where the work is done!
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String imageName = req.getPathInfo().substring(1); // Skip leading '/'
		System.out.printf("ImageServlet.serveImage(%s)\n", imageName);
		if (imageName == null || imageName.isEmpty()) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		// Basic sanitization
		for (String bad : baddies) {
			if (imageName.indexOf(bad) >= 0) {
				System.out.println("ImageServlet: Invalid character(s) in " + imageName);
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		}

		ServletContext cntx = req.getServletContext();
		// Get the path of the image - XXX should be from a servlet init parameter!
		String fileName = cntx.getRealPath("images" + "/" + imageName);
		// retrieve mimeType dynamically
		String mime = cntx.getMimeType(fileName);
		if (mime == null) {
			System.err.println("Failed to get MIME type for " + imageName);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		resp.setContentType(mime);
		Path path = Path.of(fileName);
		resp.setContentLength((int)Files.size(path));

		// Read the image file and write in binary as the response
		byte[] buf = Files.readAllBytes(path);
		OutputStream out = resp.getOutputStream();
		out.write(buf);

		out.close();
	}
}
