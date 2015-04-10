//package com.epfl.appspy.com.epfl.appspy.database;
//
//import android.content.Context;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.util.Log;
//
//import com.epfl.appspy.ApplicationsInformation;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedInputStream;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.Hashtable;
//import java.util.Iterator;
//
///**
// * Created by Jonathan Duss on 26.02.15.
// */
//public class PermissionsDatabase {
//
//    private String CURRENT_APP_PERMISSIONS_FILE = "current_apps_permissions_filename";
//    private String MAXIMUM_APP_PERMISSIONS_FILE = "maximum_apps_permissions_filename";
//
//    private String KEY_PACKAGE_NAME = "package_name";
//    private String KEY_APP_NAME = "app_name";
//    private String KEY_PERMISSIONS = "permissions";
//
//    private Context context;
//
//
//    //Singleton
//    public PermissionsDatabase(Context context) {
//        this.context = context;
//    }
//
////    private static PermissionsDatabase INSTANCE = new PermissionsDatabase();
////
////    public static PermissionsDatabase getDatabase(){
////        return INSTANCE;
////    }
//
//
//    public void addPermissions(Hashtable<PackageInfo, String[]> permissions) {
//
//        try {
//
//            //Format of the JSON:
////            {
////               pkgName: { "name" : "app name", "pkg" : "pkgName", "permissions": [...]}
////            }
//
//            //First step: create the json object that contains all
//            JSONObject json = new JSONObject();
//
//            //add a json object in the previous one for each app
//            for (PackageInfo packageInfo : permissions.keySet()) {
//                JSONObject infoForOneApp = new JSONObject();
//
//                ApplicationsInformation ai = new ApplicationsInformation(context);
//                String permissionsForApp[] = permissions.get(packageInfo);
//
//                infoForOneApp.put(KEY_PACKAGE_NAME, packageInfo.packageName);
//                infoForOneApp.put(KEY_APP_NAME, ai.getAppName(packageInfo));
//
//                JSONArray jsonArray = new JSONArray();
//                for (String permission : permissionsForApp) {
//                    jsonArray.put(permission);
//                }
//                infoForOneApp.put(KEY_PERMISSIONS, jsonArray);
//
//                json.put(packageInfo.packageName, infoForOneApp);
//
//            }
//            // write in the file
//            FileOutputStream fos = context.openFileOutput(CURRENT_APP_PERMISSIONS_FILE, Context.MODE_PRIVATE);
//            fos.write(json.toString().getBytes());
//            fos.close();
//        } catch (JSONException | IOException e2) {
//            // TODO
//        }
//
//    }
//
//    private void storeMaximumPermissions(Hashtable<PackageInfo, String[]> permissions){
//
//    }
//
//
//    public void getCurrentPermissions(boolean systemIncluded) {
//        try {
//
//            FileInputStream fis = context.openFileInput(CURRENT_APP_PERMISSIONS_FILE);
//
//            byte buffer[] = new byte[1024];
//            String s = "";
//
//
//            while (fis.available() > 0) {
//                fis.read(buffer);
//                s = s + new String(buffer);
//            }
//
//
//            Log.d("Appspy", "JSON READ FROM MEMORY: " + s);
//
//        } catch (IOException e2) {
//            // TODO
//
//        }
//
//    }
//
//
////    private Hashtable<PackageInfo, String[]> jsonToHashTable(JSONObject json){
////        Iterator<String> iterator = json.keys();
////        PackageManager pm = context.getPackageManager();
////
////        while(iterator.hasNext()){
////            try {
////                String pkgName = iterator.next();
////                PackageInfo packageInfo = pm.getPackageInfo(pkgName, PackageManager.GET_META_DATA);
////            } catch (PackageManager.NameNotFoundException e) {
////                e.printStackTrace();
////                // TODO say app was uninstalled
////            }
////        }
////
////    }
//
//
//}
