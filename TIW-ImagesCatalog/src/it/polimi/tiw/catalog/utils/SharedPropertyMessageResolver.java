package it.polimi.tiw.catalog.utils;


import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;

import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.ServletContextTemplateResource;


public class SharedPropertyMessageResolver extends StandardMessageResolver {

	private ServletContext context;
	private String directory;
	private String fileName;

	public SharedPropertyMessageResolver(ServletContext context, String path, String fileName) {
		super();
		this.context = context;
		this.directory = path;
		this.fileName = fileName;
	}

	@Override
	protected Map<String, String> resolveMessagesForTemplate(String template, ITemplateResource templateResource,
			Locale locale) {

		String finalpath = "/WEB-INF/" + directory + "/" + fileName + ".html";
		//System.out.println(finalpath);
		templateResource = new ServletContextTemplateResource(context, finalpath, null);
		return super.resolveMessagesForTemplate(finalpath, templateResource, locale);
	}

}
