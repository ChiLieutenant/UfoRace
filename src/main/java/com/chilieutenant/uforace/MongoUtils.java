package com.chilieutenant.uforace;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class MongoUtils {
    public static MongoClient mongoDB;

    public static MongoDatabase loadDatabase(String databaseName) {
        MongoClientURI uri = new MongoClientURI("mongodb://admin1:4tAyw99rZjvqd5TsciHpw8zm@172.96.160.193:25580/?readPreference=primary&appname=MongoDB%20Compass&directConnection=true&ssl=false");
        mongoDB = new MongoClient(uri);

        MongoDatabase db = mongoDB.getDatabase(databaseName);
        return db;
    }

    public static MongoCollection<Document> getCollection(MongoDatabase database, String collectionName) {
        MongoCollection<Document> collection = database.getCollection(collectionName); // Gets the collection.
        return collection;
    }

    public static Object getData(String searchKey, String searchValue, String targetData, String collectionname) {
        MongoCollection<Document> collection = MongoUtils.getCollection(Main.getDndDB(), collectionname);
        Object data = null;
        if(collection.find(Filters.eq(searchKey, searchValue)).first() != null)
            data = collection.find(Filters.eq(searchKey, searchValue)).first().get(targetData);
        return data;
    }
}
