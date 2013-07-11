package com.nikoyo.sphinx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.lang.StringUtils;

public class Program {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Session session = login();
		Folder root = session.getRootFolder();
		
		long begin = System.currentTimeMillis();
		double count = 100d;
		for (int i = 0; i < count; i++) {
			createDocument(root);
			if (i % 1000 == 0)
				System.out.println(i);
		}

		long end = System.currentTimeMillis();
		System.out.println(end - begin);
		System.out.println((end - begin)/count);

	}

	private static void createDocument(Folder parent) {

		String name = UUID.randomUUID().toString();

		// properties
		// (minimal set: name and object type id)
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		properties.put(PropertyIds.NAME, name);

		// content
		byte[] content = "Hello World!".getBytes();
		InputStream stream = new ByteArrayInputStream(content);
		ContentStream contentStream = new ContentStreamImpl(name, BigInteger.valueOf(content.length), "text/plain", stream);

		// create a major version
		parent.createDocument(properties, contentStream, VersioningState.NONE);
	}

	public static void createFolder(Session session) {
		Folder root = session.getRootFolder();

		// properties
		// (minimal set: name and object type id)
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		properties.put(PropertyIds.NAME, UUID.randomUUID().toString());

		// create the folder
		Folder newFolder = root.createFolder(properties);
	}


	private static Session login() {

		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		parameter.put(SessionParameter.USER, "Admin");
		parameter.put(SessionParameter.PASSWORD, "admin");

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, "http://192.168.1.138:8080/sphinx/atom11");
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.REPOSITORY_ID, "A1");

		for (Repository repository : factory.getRepositories(parameter)) {
			System.out.println(repository.getId());
		}
		// create session
		return factory.createSession(parameter);
	}

}
