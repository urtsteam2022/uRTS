package org.urts.configAware;

import org.urts.Config;
import org.urts.log.Log;
import org.urts.util.FileUtil;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ConfigLoader {
    private static final Map<String, String > sCurExercisedConfigMap = new HashMap<String, String>();
    private static final Map<String, String > sOldExercisedConfigMap = new HashMap<String, String>();
    private static final Map<String, String> sChangedConfigMap = new HashMap<String, String>();
    // private static final Map<String, String> sDefaultConfigMap = new HashMap<String, String>();
    private static final Map<String, String> sProdConfigMap = new HashMap<String, String>();
    private static final String configFileSeparator = ",";
    private static final String configSeparator = "@CONFIGAWARE@";
    private static final String configValueSeparator = "=CONFIGAWARE=";

    public static Map<String, String> getChangedConfigMap() {
        if (sChangedConfigMap.isEmpty()) {
            Map<String, String> newMap = getCurTestGeneratedConfigMap();
            Map<String, String> oldMap = getOldTestGeneratedConfigMap();
            if (oldMap.isEmpty()) {
                sChangedConfigMap.putAll(newMap);
                return sChangedConfigMap;
            }
            for (Map.Entry<String, String> configPair : newMap.entrySet()) {
                String configName = configPair.getKey();
                String curConfigValue = configPair.getValue();
                if (oldMap.containsKey(configName)) {
                    if (!Objects.equals(oldMap.get(configName), curConfigValue)) {
                        sChangedConfigMap.put(configName, curConfigValue);
                    }
                } else {
                    sChangedConfigMap.put(configName, curConfigValue);
                }
            }
        }
        return sChangedConfigMap;
    }

    public static Map<String, String> getCurTestGeneratedConfigMap() {
        if (sCurExercisedConfigMap.isEmpty() || sCurExercisedConfigMap == null){
            loadCurTestExercisedFromFile();
        }
        return sCurExercisedConfigMap;
    }

    public static Map<String, String> getOldTestGeneratedConfigMap() {
        if (sOldExercisedConfigMap.isEmpty() || sOldExercisedConfigMap == null){
            loadOldTestExercisedFromFile();
        }
        return sOldExercisedConfigMap;
    }

//    public static Map<String, String> getDefaultConfigMap() {
//        if (sDefaultConfigMap.isEmpty() || sDefaultConfigMap == null) {
//            loadDefaultFromFile();
//        }
//        return sDefaultConfigMap;
//    }

    public static Map<String, String> getProdConfigMap() {
        if (sProdConfigMap.isEmpty() || sProdConfigMap == null) {
            loadProdFromFile();
        }
        return sProdConfigMap;
    }

    public static Boolean hasConfigFile(String configFileName) {
        //String configFileName = Config.CONFIG_FILE_PATH_V;
        if (configFileName.isEmpty() || configFileName == null) {
            return false;
        }
        File configFile = new File(configFileName);
        return configFile.exists();
    }

    /**
     *  Load default configuration file
     *  TODO: consider different type of configuration file (e.g. xml)
     */
//    public static void loadDefaultFromFile() {
//        String defaultFileName = Config.DEFAULT_CONFIG_FILE_PATH_V;
//        load0(defaultFileName, sDefaultConfigMap);
//    }

    /**
     *  Load test generated configuration file
     */
    public static void loadCurTestExercisedFromFile() {
        String curConfigFile = Paths.get(Paths.get(Config.CONFIG_FILE_DIR_PATH_V).toString(), ".ConfigValue").toString();
        loadCur(curConfigFile);
    }

    public static void loadOldTestExercisedFromFile() {
        String oldConfigFile = Paths.get(Paths.get(Config.CONFIG_FILE_DIR_PATH_V).toString(), ".OldConfigValue").toString();
        if (new File(oldConfigFile).exists()) {
            loadOld(oldConfigFile);
        }
    }

    /**
     *  Load prod configuration file
     */
    public static void loadProdFromFile() {
        String prodFileName = Config.CONFIG_PROD_FILE_PATH_V;
        load0(prodFileName, sProdConfigMap);
    }

    // Load current configuration from the file that generated by TestGetAllConfigValueForConfigAware
    private static void loadCur(String filename) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                String oneLineStr = reader.readLine();
                String line = oneLineStr;
                while( line != null) {
                    oneLineStr += line;
                    line = reader.readLine();
                }
                String pairs [] = oneLineStr.replace("\n", "").split(configSeparator);
                for (String configPair : pairs) {
                    configPair = configPair.trim();
                    String configName = configPair.split(configValueSeparator)[0];
                    String configValue = configPair.split(configValueSeparator)[1];
                    if (!Objects.equals(configName, "")) {
                        sCurExercisedConfigMap.put(replaceBlank(configName), replaceBlank(configValue));
                    }
                }
                reader.close();
            } else {
                Log.e("Loading configuration is not successful because no file exists, please check config.file.path");
                //Log.d2f("[ERROR] Loading configuration is not successful because no file exists, please check config.file.path");
                throw new IOException("Loading configuration is not successful because no file exists, please check config.file.path");
            }
        } catch (Exception e) {
            Log.e("Loading configuration is not successful", e);
            Log.d2f("[ERROR] Loading configuration is not successful" + e);
            sCurExercisedConfigMap.clear();
        }
    }


    // Load old configuration from the file that generated by TestGetAllConfigValueForConfigAware
    private static void loadOld(String filename) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                String oneLineStr = reader.readLine();
                String line = oneLineStr;
                while( line != null) {
                    oneLineStr += line;
                    line = reader.readLine();
                }
                String pairs [] = oneLineStr.replace("\n", "").split(configSeparator);
                for (String configPair : pairs) {
                    configPair = configPair.trim();
                    String configName = configPair.split(configValueSeparator)[0];
                    String configValue = configPair.split(configValueSeparator)[1];
                    if (!Objects.equals(configName, "")) {
                        sOldExercisedConfigMap.put(replaceBlank(configName), replaceBlank(configValue));
                    }
                }
                reader.close();
            } else {
                Log.e("Loading configuration is not successful because no file exists, please check config.file.path");
                Log.d2f("[ERROR] Loading configuration is not successful because no file exists, please check config.file.path");
                throw new IOException("Loading configuration is not successful because no file exists, please check config.file.path");
            }
        } catch (Exception e) {
            Log.e("Loading configuration is not successful", e);
            Log.d2f("[ERROR] Loading configuration is not successful" + e);
            sOldExercisedConfigMap.clear();
        }
    }

    // Load default Configuration from Configuration file
    private static void load0(String filenameList, Map<String, String> map) {
        InputStream is = null;

        //Handle several configuration files, separated by ","
        String files [] = filenameList.split(configFileSeparator);
        for (String filename : files) {
            filename = filename.trim();
            if (filename == null || filename.isEmpty()) {
                //Log.d2f("[INFO] Load configuration: Continue next file, current filename: " + filename);
                continue;
            }
            if (!hasConfigFile(filename)) {
                //Log.d2f("[ERROR] Can't find user's configuration file: " + filename);
                return;
            }
            try {
                is = new FileInputStream(filename);
                parseConfigurationFile(filename, is, map);
                is.close();
            } catch (IOException e) {
                Log.e("Loading configuration is not successful", e);
                //Log.d2f("[ERROR] Loading configuration is not successful" + e);
                map.clear();
            } finally {
                FileUtil.closeAndIgnoreExceptions(is);
            }
        }
    }

    private static String getFileSuffix(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    private static void parseConfigurationFile(String filename, InputStream is, Map<String, String> map) throws IOException {
        String fileSuffix = getFileSuffix(filename).toLowerCase();
        switch (fileSuffix) {
            case "xml":
                loadFromXML(is, map);
                break;
            case "properties":
            case "cfg":
                loadFromPropertiesAndCFG(is, map);
                break;
            default:
                Log.e("Can't load configuration from ." + fileSuffix + " file");
                throw new IOException();
        }
    }

    private static void loadFromXML(InputStream is, Map<String, String> map) {
        parseXML(is, map, "property", "name", "value");
    }

    private static void loadFromPropertiesAndCFG(InputStream is, Map<String, String> map) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            while(reader.ready()) {
                String line = reader.readLine();
                String configPairs [] = line.split("=");
                if (!line.contains("=")) {
                    continue;
                }else if (configPairs.length == 1) {
                    String configName = configPairs[0];
                    String configValue = "null";
                    if (!Objects.equals(configName, "")) {
                        map.put(replaceBlank(configName), replaceBlank(configValue));
                    }
                } else if (configPairs.length == 2) {
                    String configName = configPairs[0];
                    String configValue = configPairs[1];
                    if (!Objects.equals(configName, "")) {
                        map.put(replaceBlank(configName), replaceBlank(configValue));
                    }
                } else {
                    //Log.d2f("[ERROR] Incorrectly parse configuration from properties file: configPairs length is too long " + Arrays.toString(configPairs));
                }
            }
        } catch (IOException e) {
            //Log.d2f("[ERROR] Loading configuration is not successful: " + e.getStackTrace());
            Log.e("Loading configuration is not successful", e);
            map.clear();
        }
    }

    private static void loadFromCFG() {

    }


    public static void parseXML(InputStream is, Map<String, String> map, String tagName, String tagConfigName, String tagConfigValue) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            NodeList nl = doc.getElementsByTagName(tagName);
            for (int i = 0; i < nl.getLength(); i++) {
                NodeList nl2 = nl.item(i).getChildNodes();
                String configName = "";
                String configValue = "";
                for (int j = 0; j < nl2.getLength(); j++) {
                    Node n = nl2.item(j);
                    if (n.getNodeName().equals(tagConfigName)) configName = n.getTextContent();
                    if (n.getNodeName().equals(tagConfigValue)) configValue = n.getTextContent();
                }

                // Multiple configuration files may have duplicated settings. We choose the last one as the final value (Overwrite)
                // This is the same idea as some real-world software like Hadoop.
                if (!Objects.equals(configName, "")) {
                    map.put(replaceBlank(configName), replaceBlank(configValue));
                }
                //System.out.println(configName + " , " + configValue);
            }
        } catch (Exception e) {
            //Log.d2f("[ERROR] Loading configuration is not successful: " + e.getStackTrace());
            Log.e("Loading configuration is not successful", e);
            map.clear();
        }
    }

    // internal method

    /**
     * Remove blank space, \r, \n, \t in a given string
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    // For simply test
    public static void main(String args[]) {
        // Config.DEFAULT_CONFIG_FILE_PATH_V = "/Users/alenwang/Documents/xlab/hadoop/hadoop-common-project/hadoop-common/src/main/resources/core-default.xml";
        Config.CONFIG_PROD_FILE_PATH_V = "/Users/alenwang/Desktop/generatedProperties.properties";
        // getDefaultConfigMap();
        getProdConfigMap();
//        int count = 0;
//        for(Map.Entry<String, String> entry : sDefaultConfigMap.entrySet()) {
//            count += 1;
//            System.out.println(entry.getKey() + " , " + entry.getValue());
//        }
//        System.out.println(count);
        int count2 = 0;
        for(Map.Entry<String, String> entry : sProdConfigMap.entrySet()) {
            count2 += 1;
            System.out.println(entry.getKey() + " , " + entry.getValue());
        }
        System.out.println(count2);
    }
}
