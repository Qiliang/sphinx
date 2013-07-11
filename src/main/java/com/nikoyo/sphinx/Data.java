package com.nikoyo.sphinx;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.chemistry.opencmis.client.util.FileUtils;

public class Data {

	static final String template = "{ '_id' : '%s', 'createdAt' : { '$date' : 1373519882315 }, 'modifiedAt' : { '$date' : 1373519882315 }, 'secondaryTypeIds' : [], 'modifiedBy' : 'unknown', 'createdBy' : 'unknown', 'name' : '%s', 'typeId' : 'ComplexType', 'properties' : [ { 'id' : 'StringProp', 'localName' : null, 'queryName' : null, 'displayName' : null, 'values' : [ 'My Doc StringProperty 20' ], 'className' : 'string' }, { 'id' : 'PickListProp', 'localName' : null, 'queryName' : null, 'displayName' : null, 'values' : [ 'blue' ], 'className' : 'string' } ], 'repositoryId' : 'A1', 'parentIds' : [ '%s' ], 'aclId' : 0, 'cmis:contentStreamId' : '466331e0-4fe7-4e52-a0bf-27ed1159b536', 'cmis:contentStreamFileName' : 'data.txt', 'cmis:contentStreamLength' : -1, 'cmis:contentStreamMimeType' : 'text/plain', 'className' : 'Document' }";
	static final String folder = "{ '_id' : '%s', 'createdAt' : { '$date' : 1373519880740 }, 'modifiedAt' : { '$date' : 1373519880740 }, 'secondaryTypeIds' : [], 'name' : '%s', 'modifiedBy' : 'unknown', 'createdBy' : 'unknown', 'typeId' : 'cmis:folder', 'properties' : [], 'repositoryId' : 'A1', 'parentIds' : [ '100' ], 'aclId' : 0, 'className' : 'Folder' }";

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		int id = 0;
		FileWriter writer = new FileWriter("c:/data.json");

		for (int i = 0; i < 30; i++) {
			String folderId = "f" + i;
			writer.append(String.format(folder, folderId, i)).append("\r\n");
			for (int j = 0; j < 100000; j++) {
				writer.append(String.format(template, "object" + id, id++, folderId)).append("\r\n");
			}
			System.out.println(i);
		}

		writer.close();
		System.out.println("ok");
	}
}
