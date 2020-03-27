package com.appadhoc.reversetoy.inject;

import brut.common.BrutException;
import brut.util.OS;
import com.appadhoc.reversetoy.utils.Resource;
import com.appadhoc.reversetoy.utils.Utils;
import luyao.parser.xml.XmlParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectionOper {
    private final static Logger LOGGER = Logger.getLogger(ReflectionOper.class.getName());
    private HashMap<String, Object> options;
    String stubDir = "com.reverse.stub";
    private String SDK_DIR = "com.analysys";//代码路径
    private String exclue = "track";//路径路径下面有子项目不能删除
    private static final String appNameStub = "com.reverse.stub.ReverseApp";
//    private File jsonFile;

    private void addOrModifyApplicationSmali(File hostDir, List<File> newSmaliFolder, XmlParser hostAndmanifestData) throws Exception {
        if (newSmaliFolder == null || newSmaliFolder.size() == 0) {
            throw new Exception("SDK smali 文件夹不存在");
        }
//        deleteOldSdkSmaliFile(SDK_DIR, hostDir, newSmaliFolder, exclue);
//        String appName = Utils.XmlUtils.setApplicationName(hostDir, appNameStub_Eguan);
        String appHostFullName = Utils.XmlUtils.setBinaryManifestApplicationName(hostAndmanifestData, appNameStub);

        File lastFolder = newSmaliFolder.get(newSmaliFolder.size() - 1);

        if (appHostFullName.equals(appNameStub)) {

            copyStubSmali2HostDir(stubDir, lastFolder);

        } else {
            modifyExistAppSmali(hostDir, appHostFullName, lastFolder,hostAndmanifestData.getManifestPackageName());
        }
        copyJSON2HostAssets(hostDir);
        copyUtilsSmaliFile(stubDir, lastFolder);
    }

    private void copyJSON2HostAssets(File hostDir) throws IOException {

        String code = null;
        String jsonName = "reverse_code_json_default.txt";
        File assets = new File(hostDir, "assets");
        if (!assets.exists()) {
            assets.mkdirs();
        }
        if (options != null && options.containsKey("json")) {
            File jsonFile = new File((String) options.get("json"));
            code = Utils.FileUtils.readStringFromFile(jsonFile).toString();
        } else {
            InputStream inputStream = null;

            try {
                inputStream = Resource.getResourceAsStream("/brut/androlib/" + jsonName, getClass());
            } catch (BrutException e) {
                e.printStackTrace();
            }
            code = Utils.FileUtils.readStringFromStream(inputStream).toString();
        }
        LOGGER.info("copy utils file to folder" + jsonName);
        File saveApplicationFile = new File(assets, jsonName);
        Utils.FileUtils.writeString2File(saveApplicationFile, code);
    }

    private void copyUtilsSmaliFile(String appName, File aarSmaliFolder) throws IOException {
        File stubDir = new File(aarSmaliFolder, appName.replaceAll("\\.", File.separator));
        if (!stubDir.exists()) {
            stubDir.mkdirs();
        }
        ArrayList<String> files = new ArrayList<>();
        files.add("Utils$1.smali");
        files.add("Utils$CodeBean$Method_$Para.smali");
        files.add("Utils$CodeBean$Method_.smali");
        files.add("Utils$CodeBean$StaticField.smali");
        files.add("Utils$CodeBean.smali");
        files.add("Utils$JSONparse.smali");
        files.add("Utils$Reflection.smali");
        files.add("Utils.smali");
        // multDex

        files.add("MultiDex$V4.smali");
        files.add("MultiDex$V14$ElementConstructor.smali");
        files.add("MultiDex$V14$ICSElementConstructor.smali");
        files.add("MultiDex$V14$JBMR2ElementConstructor.smali");
        files.add("MultiDex$V14$JBMR11ElementConstructor.smali");
        files.add("MultiDex$V14.smali");
        files.add("MultiDex$V19.smali");
        files.add("MultiDex.smali");
        files.add("MultiDexExtractor$1.smali");
        files.add("MultiDexExtractor$ExtractedDex.smali");
        files.add("MultiDexExtractor.smali");
        copy(files, stubDir);

    }

    private void copyStubSmali2HostDir(String appName, File aarSmaliFolder) throws Exception {

        // copy App.smali file 2 host smali folder
        File stubDir = new File(aarSmaliFolder, appName.replaceAll("\\.", File.separator));
        if (!stubDir.exists()) {
            stubDir.mkdirs();
        }
        InputStream stubAppSmaliFile = getAssetsAppStubSmaliFile();
        String code = Utils.FileUtils.readStringFromStream(stubAppSmaliFile).toString();

        LOGGER.info("change result is copy" + code);
        String fileName = getSmaliApplicationName();
        File saveApplicationFile = new File(stubDir, fileName);
        Utils.FileUtils.writeString2File(saveApplicationFile, code);
    }

    private void modifyExistAppSmali(File hostdir, String hostAppName, File lastFolder,String packageName) throws Exception {
        if (!hostdir.exists()) {
            throw new Exception("host dir not exist");
        }
        if (hostAppName == null || hostAppName.equals("")) {
            throw new Exception("hostAppName must be not  null");
        }

//            invoke-direct {p0}, Lcom/reverse/stub/App;->initReverseSDK()V
//        String callMethodCode = "invoke-direct {p0}, L" + hostAppName.replaceAll("\\.", "/") + ";->initReverseSDK()V";
        String callMethodCode = "invoke-static {p0}, Lcom/reverse/stub/Utils;->initReverseSDK(Landroid/content/Context;)V";
//        InputStream codePieceFileIputSream = getAssetsCodeMethodInit();

//        String methodCode = Utils.FileUtils.readStringFromStream(codePieceFileIputSream).toString();

//        String methodCodeReplaceMent = null;
//        if (methodCode != null) {
//            methodCodeReplaceMent = Matcher.quoteReplacement(methodCode);
//        }
//        LOGGER.info("change result is " + methodCodeReplaceMent);
        File needModiFile = getApplicationFile(hostdir, hostAppName,packageName);
        if (needModiFile == null) {
            throw new Exception("can not find src Application smali file ,file name path " + hostAppName);
        }
        System.out.println(needModiFile.getAbsolutePath());
        String srcStr = Utils.FileUtils.readStringFromFile(needModiFile).toString();
//        srcStr = srcStr.replaceFirst(".method\\s+public\\s+constructor\\s+<init>\\(\\)V(.*\\n)+?.end\\s+method", "$0\n\n" + methodCodeReplaceMent);

        if (!haveOncreate(srcStr)) {
            srcStr = insertOnCreateMethod(srcStr);
        }
        srcStr = srcStr.replaceFirst(".method\\s+public\\s+(final\\s+)?onCreate\\(\\)V(.*\\n)+?\\s*.locals\\s+\\d+", "$0\n\n" + callMethodCode);

//        srcStr = srcStr.replaceFirst(".method\\s+public\\s+constructor\\s+<init>\\(\\)V(.*\\n)+?\\s*.locals\\s+\\d+", "$0\n\n" + callMethodCode);
        // add copy lastFolder avoid 65536 error
        File newLocationAppFile = createNewApplicationFileinLastFolder(hostAppName, lastFolder);

        // 在新的file那里新建。
        Utils.FileUtils.writeString2File(newLocationAppFile, srcStr);
        // 删除旧的file
        OS.rmfile(needModiFile.getAbsolutePath());
        boolean replaceCallSuccess = srcStr.contains("->initReverseSDK(Landroid/content/Context;)V");
        if (!replaceCallSuccess) {
            throw new Exception("modify " + hostAppName + " smali modify failed");
        }
//        boolean replaceSuccess = srcStr.contains("method private initReverseSDK");
//        boolean replaceCallSuccess = srcStr.contains("->initReverseSDK()V");
//        if (!replaceCallSuccess || !replaceSuccess) {
//            throw new Exception("modify " + hostAppName + " smali modify failed");
//        }
    }

    private File getApplicationFile(File hostdir, String appNameRaw, String packageName) {
        if (appNameRaw.startsWith(".")) {
            appNameRaw = packageName + appNameRaw;
        }
        appNameRaw = appNameRaw.replaceAll("\\.", File.separator) + ".smali";

        File needModiFile = null;
        for (File subSmaiFolder : hostdir.listFiles()) {
            if (subSmaiFolder.isDirectory() && subSmaiFolder.getName().startsWith("smali")) {
                File file = new File(subSmaiFolder, appNameRaw);
                if (file.exists()) {
                    needModiFile = file;
                    break;
                }
            }
        }
        return needModiFile;
    }

    String onCreateMethod = ".method public onCreate()V\n" +
            "    .locals 2\n" +
            "\n" +
            "    .line 17\n" +
            "    invoke-super {p0}, Landroid/app/Application;->onCreate()V\n" +
            "\n" +
            "    .line 18\n" +
            "    const-string v0, \"reverse\"\n" +
            "\n" +
            "    const-string v1, \"onCreate\"\n" +
            "\n" +
            "    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I\n" +
            "\n" +
            "    .line 19\n" +
            "    return-void\n" +
            ".end method\n";

    private String insertOnCreateMethod(String srcStr) {
        return srcStr.replaceFirst(".method\\s+public\\s+constructor\\s+<init>\\(\\)V(.*\\n)+?.end\\s+method", "$0\n\n" + Matcher.quoteReplacement(onCreateMethod));
    }

    private String getSmaliApplicationName() {
        String fupath = appNameStub;
        String[] names = fupath.split("\\.");
        return names[names.length - 1] + ".smali";
    }

    private InputStream getAssetsAppStubSmaliFile() {
        InputStream inputStream = null;
        try {
            inputStream = Resource.getResourceAsStream("/brut/androlib/ReverseApp.smali", getClass());
        } catch (BrutException e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    private InputStream getAssetsCodeMethodInit() {
        InputStream inputStream = null;
        try {
            inputStream = Resource.getResourceAsStream("/brut/androlib/fun_init_smali_code.txt", getClass());
        } catch (BrutException e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    // 删除旧sdk 的smali文件 // upgrade sdk may be useful
    public void deleteOldSdkSmaliFile(File hostdir, List<File> aarSmaliFolder, XmlParser hostAndmanifestData) throws Exception {
        String path = SDK_DIR;
        String excludeSDKdir = exclue;
        if (options != null) {
            if (options.get("exclude") != null) {
                excludeSDKdir = (String) options.get("exclude");
            }
            if (options.get("ousc") != null) {
                path = (String) options.get("ousc");
            }
        }

        if (path == null || path.equals("")) {
            throw new Exception("旧的SDK路径不存在");
        }
        if (!hostdir.exists()) {
            throw new Exception("host dir 不存在");
        }
        path = path.replaceAll("\\.", File.separator);
        for (File file : Objects.requireNonNull(hostdir.listFiles())) {
            String fileName = file.getName();
            if (fileName.startsWith("smali") && !fileNameInList(fileName, aarSmaliFolder)) { // 新生成的sdk smali不删除
                File existOldSdkdir = new File(file, path);
                if (existOldSdkdir.exists()) {
                    for (File fileYiguan : Objects.requireNonNull(existOldSdkdir.listFiles())) {
                        if (!fileYiguan.getName().equals(excludeSDKdir)) {
                            LOGGER.info("删除旧的SDK目录" + existOldSdkdir.getAbsolutePath());
                            if (fileYiguan.isFile()) {
                                OS.rmfile(fileYiguan.getAbsolutePath());
                            } else {
                                OS.rmdir(fileYiguan);
                            }
                        }
                    }

                }
            }
        }
        if (options == null || options.get("upg") == null) {
            this.addOrModifyApplicationSmali(hostdir, aarSmaliFolder, hostAndmanifestData);
        }
    }

    private boolean fileNameInList(String fileName, List<File> files) {
        boolean inFiles = false;
        for (File file : files) {
            if (file.getName().equals(fileName)) {
                inFiles = true;
                break;
            }
        }
        return inFiles;
    }

    private void copy(ArrayList<String> filesNames, File stubDir) throws IOException {
        for (int i = 0; i < filesNames.size(); i++) {
            InputStream inputStream = null;
            String fileName_ = filesNames.get(i);
            try {
                inputStream = Resource.getResourceAsStream("/brut/androlib/" + fileName_, getClass());
            } catch (BrutException e) {
                e.printStackTrace();
            }
            String code = Utils.FileUtils.readStringFromStream(inputStream).toString();
            LOGGER.info("copy utils file to folder" + fileName_);
            File saveApplicationFile = new File(stubDir, fileName_);
            Utils.FileUtils.writeString2File(saveApplicationFile, code);

        }
    }

    public void setOptions(HashMap opt) {
        options = opt;
    }

    public static boolean haveOncreate(String s) {
        Matcher matcher = Pattern.compile(".method\\s+public\\s+(final\\s+)?onCreate\\(\\)V").matcher(s);
        return matcher.find();
    }

    private File createNewApplicationFileinLastFolder(String appName, File lastFolder) {
        String fileName = appName.substring(appName.lastIndexOf(".") + 1);
        String tmpStr = appName.substring(0, appName.lastIndexOf("."));
        tmpStr = tmpStr.replaceAll("\\.", File.separator);
        File appdir = new File(lastFolder, tmpStr);
        if (!appdir.exists()) {
            appdir.mkdirs();
        }
        File file = new File(appdir, fileName + ".smali");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static void main(String[] args) throws IOException {

        String str = "com.reverse.stub.ReverseApp";

    }
}
