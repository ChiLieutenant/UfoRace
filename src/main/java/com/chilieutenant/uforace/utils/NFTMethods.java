package com.chilieutenant.uforace.utils;

import com.chilieutenant.uforace.Main;
import com.chilieutenant.uforace.MongoUtils;
import com.chilieutenant.uforace.NFTSelection.NFTItem;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.nftworlds.wallet.objects.Network;
import lombok.SneakyThrows;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.mongodb.client.model.Filters.eq;

public class NFTMethods {


    @SneakyThrows
    public static void loadNFT(Player p){
        removePermission(p);
        JSONObject nfts = Main.getWallet().getPrimaryWallet(p).getOwnedNFTsFromContract(Network.POLYGON, "0x2953399124f0cbb46d2cbacd8a89cf0599974963");
        JSONArray ownedNFTs = nfts.getJSONArray("ownedNfts");
        for(int i = 0; i < ownedNFTs.length(); i++){
            JSONObject nft = ownedNFTs.getJSONObject(i).getJSONObject("metadata");
            String vehType = getVehType(nft);
            if(vehType.length() > 1) {
                Utils.addPermission(p, "vehicle." + vehType. replace(" ", "_"));
            }
        }

        String s = getPlayerVehicle(p);
        if(!Utils.hasPermission(p, s.replace(" ", "_"))){
            editVehicle(p, "SAD Speedster");
        }
    }

    public static String getPlayerVehicle(Player p){
        return MongoUtils.getData("UUID", p.getUniqueId().toString(), "veh", "selectedVEHS");
    }

    @SneakyThrows
    public static String getVehType(JSONObject nft){
        String type = "";

        JSONArray responsearray;
        try{
            responsearray = nft.getJSONArray("traits");

        }catch(JSONException e){
            return "";
        }

        for(int i = 0; i < responsearray.length(); i++){
            try{
                if(!responsearray.getJSONObject(i).getString("trait_type").equalsIgnoreCase("The Happy Place Vehicle"))
                    return "";
            }catch (JSONException e){
                return "";
            }
            try{
                type = responsearray.getJSONObject(i).getString("value");
            }catch (JSONException e){
                Bukkit.getLogger().info(e.toString());
            }
        }

        return type;
    }

    public static void saveDefault(Player p){
        MongoCollection<Document> collection = MongoUtils.getCollection(Main.getDndDB(), "selectedVEHS");

        if(collection.find(eq("UUID", p.getUniqueId().toString())).first() != null) return;

        Document doc = new Document();
        doc.append("UUID", p.getUniqueId().toString());
        doc.append("veh", "SAD Speedster");
        collection.insertOne(doc);
    }

    public static void editVehicle(Player p, String selected){
        MongoCollection<Document> collection = MongoUtils.getCollection(Main.getDndDB(), "selectedVEHS");

        BasicDBObject query = new BasicDBObject();
        query.append("UUID", p.getUniqueId().toString());

        BasicDBObject newDocument = new BasicDBObject();
        newDocument.put("veh", selected); // (2)

        BasicDBObject updateObject = new BasicDBObject();
        updateObject.put("$set", newDocument); // (3)

        collection.updateOne(query, updateObject);
    }

    public static NFTItem getNFTItemBySlot(int slot){
        NFTItem item = null;
        for(NFTItem items : Main.getItems()){
            if(items.getSlot() == slot){
                item = items;
                break;
            }
        }

        return item;
    }

    public static void removePermission(Player p){
        for(String string : Main.getVehs()){
            if(Utils.hasPermission(p, "vehicle." + string.replace(" ", "_"))) {
                Utils.removePermission(p, "vehicle." + string.replace(" ", "_"));
            }
        }
    }

    public static String getCar(Player p){
        return Main.getVehmobs().get(getPlayerVehicle(p));
    }

}
