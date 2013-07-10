package org.apache.chemistry.opencmis.inmemory.storedobj.impl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoMap {

	private final DBCollection dbCollection;
	private final ObjectStore objStore;
	
	public MongoMap(DBCollection dbCollection,ObjectStore objStore) {
		this.dbCollection = dbCollection;
		this.objStore = objStore;
	}

	public void remove(String id) {
		dbCollection.remove(new BasicDBObject("_id", id));
	}

	public void save(DBObject jo) {
		dbCollection.save(jo);
	}

	public DBObject findOne(String id) {
		return dbCollection.findOne(new BasicDBObject("_id", id));
	}

	public DBObject findOne(DBObject o) {
		return dbCollection.findOne(o);
	}

	public StoredObject get(String id) {
		try {
			DBObject dbObject = dbCollection.findOne(new BasicDBObject("_id", id));
			StoredObject so =  newInstance((String) dbObject.get("className"));
			if(so==null){
				System.out.println(so);
			}
			so.putAll(dbObject);
			return so;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Set<String> keySet() {

		BasicDBObject keys = new BasicDBObject();
		keys.put("_id", 1);
		DBCursor dbCursor = dbCollection.find(new BasicDBObject(), keys);
		HashSet<String> ids = new LinkedHashSet<String>();
		for (DBObject dbObject : dbCursor) {
			ids.add(dbObject.get("_id").toString());
		}
		dbCursor.close();

		return ids;
	}

	public Set<StoredObject> values() {
		HashSet<StoredObject> objects = new LinkedHashSet<StoredObject>();
		BasicDBObject keys = new BasicDBObject();
//		keys.put("_id", 1);
//		keys.put("_id", 1);
		DBCursor dbCursor = dbCollection.find(new BasicDBObject(), keys);
		try {
			for (DBObject dbObject : dbCursor) {
				StoredObject so = newInstance((String) dbObject.get("className"));
				so.putAll(dbObject);
				objects.add(so);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return objects;
		} finally {
			dbCursor.close();
		}

		return objects;

	}

	public void clear() {
		dbCollection.drop();
	}

	public long size() {
		return dbCollection.getCount();
	}

	private StoredObject newInstance(String className) {
		if (RelationshipImpl.class.getName().equals(className)) {
			return new RelationshipImpl(objStore);
		} else if (PolicyImpl.class.getName().equals(className)) {
			return new PolicyImpl(objStore);
		}else if (DocumentVersionImpl.class.getName().equals(className)) {
			return null;
			//return new DocumentVersionImpl(objStore);
		}else if (DocumentImpl.class.getName().equals(className)) {
			return new DocumentImpl(objStore);
		}else if (ItemImpl.class.getName().equals(className)) {
			return new ItemImpl(objStore);
		}else if (VersionedDocumentImpl.class.getName().equals(className)) {
			return new VersionedDocumentImpl(objStore);
		}else if (FolderImpl.class.getName().equals(className)) {
			return new FolderImpl(objStore);
		}
		return null;
	}

}
