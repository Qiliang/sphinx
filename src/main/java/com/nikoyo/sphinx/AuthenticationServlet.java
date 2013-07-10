package com.nikoyo.sphinx;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.commons.lang.StringUtils;

public class AuthenticationServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2045128023145797293L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String username = req.getParameter("username");
		String password = req.getParameter("password");
		if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.flushBuffer();
			return;
		}
		
		Session session=	login();
		ObjectType objectType=session.getTypeDefinition("system:user");
		System.out.println(objectType);
		ItemIterable<QueryResult> result =session.query(String.format("select cmis:name from system:user where cmis:name='%s' and system:password='%s'",username,password), false);
		if(!success(result)){
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		resp.setStatus(HttpServletResponse.SC_OK);
		String s= Base64.encodeBytes((username+":"+password).getBytes());
		resp.getWriter().write(s);
	}
	
	private boolean success(ItemIterable<QueryResult> result){
		for(QueryResult re:result){
			String name = re.getPropertyValueById("cmis:name");
			if(StringUtils.isNotEmpty(name)){
				return true;
			}
		}
		
		return false;
	}
	
	
	private Session login(){
		
		
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		parameter.put(SessionParameter.USER, "Admin");
		parameter.put(SessionParameter.PASSWORD, "admin");

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/sphinx/atom11");
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.REPOSITORY_ID, "A1");
		
		for(Repository repository :	factory.getRepositories(parameter)){
			System.out.println(repository.getId());
		}
		// create session
		return factory.createSession(parameter);
	}
	
	
}
