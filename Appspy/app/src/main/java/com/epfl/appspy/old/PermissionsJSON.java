//package com.epfl.appspy.com.epfl.appspy.database;
//
//import android.content.res.XmlResourceParser;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.xmlpull.v1.XmlPullParser;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Jonathan Duss on 27.02.15.
// */
//public class PermissionsJSON {
//
//    JSONObject permissions;
//    final String PERMISSION_ARRAY_KEY = "permissions";
//
//    public PermissionsJSON(JSONObject json){
//        this.permissions = json;
//    }
//
//    public PermissionsJSON(String permissions){
//        try {
//            this.permissions = new JSONObject(permissions);
//        }
//        catch(JSONException e){
//            e.printStackTrace();
//            //TODO HANDLE ERROR
//        }
//    }
//
//    public PermissionsJSON(List<String> permissions){
//        JSONObject permissionsGlobalJSON = new JSONObject();
//        JSONArray permissionJSONArray = new JSONArray();
//        for(String permission : permissions){
//            permissionJSONArray.put(permission);
//        }
//        try {
//            permissionsGlobalJSON.put(PERMISSION_ARRAY_KEY, permissionJSONArray);
//        }
//        catch(JSONException e){
//            e.printStackTrace();
//            // TODO HANDLE ERROR
//        }
//        this.permissions = permissionsGlobalJSON;
//    }
//
//    public String toString(){
//        return permissions.toString();
//    }
//
//
//    public List<String> toList(){
//        try {
//            JSONArray jsonArray = permissions.getJSONArray(PERMISSION_ARRAY_KEY);
//            ArrayList<String> list = new ArrayList<>();
//
//            for(int i = 0; i < jsonArray.length(); i ++){
//                list.add(jsonArray.getString(i));
//            }
//            return list;
//        }
//        catch(JSONException e){
//            e.printStackTrace();
//            //TODO HANDLE ERROR
//            return null;
//        }
//    }
//
//
//}
