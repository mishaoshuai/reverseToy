package com.appadhoc.reversetoy.aar;


import brut.androlib.AndrolibException;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResTable;
import brut.common.BrutException;
import brut.directory.Directory;
import brut.directory.DirectoryException;
import brut.directory.ZipRODirectory;
import brut.util.AaptManager;
import brut.util.OS;
import com.appadhoc.reversetoy.AbstractManager;
import com.appadhoc.reversetoy.data.AarID;
import com.appadhoc.reversetoy.utils.Utils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class AarManager extends AbstractManager {
    private final static Logger LOGGER = Logger.getLogger(AarManager.class.getName());
    private String aarPackageName;
    private Map<String, LinkedHashMap> ids;
    private File toyWorkspace;
    private File tmpDir;
    private File rFiledir;
    private String hostPackageName;
    private File aarFile;

    private String getAarFileName() {
        return aarFile == null ? "" : Utils.getNameRemovedSuffix(aarFile.getName());
    }

    public String getHostPackageName() {
        return hostPackageName;
    }

    public AarManager(String aarFile) {
        try {
            LOGGER.fine("aarFile  name " + aarFile + "");
            setWorkSpace(new File(aarFile).getParentFile().getAbsolutePath());
            setAarFile(aarFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAarFile(String aarfile) throws Exception {
        aarFile = new File(aarfile);
        if (!aarFile.exists()) {
            throw new Exception("aar file not exist");
        }
    }

    private void setWorkSpace(String workSpaceDir) throws Exception {
        if (toyWorkspace == null || !toyWorkspace.exists()) {
            try {
                toyWorkspace = new File(workSpaceDir);
                toyWorkspace.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 添加初始化tmp文件夹
        File file = new File(toyWorkspace, "aar/tmp" + UUID.randomUUID().toString().substring(0, 5));
        if (!file.exists()) {
            file.mkdirs();
        } else {
            // 清空tmp文件夹
            OS.rmdir(file);
        }

        this.tmpDir = file;
    }

    private File getRFileDir() {

        if (rFiledir == null || !rFiledir.exists()) {
            rFiledir = new File(tmpDir, "rFiles");
            rFiledir.mkdirs();
        }
        return rFiledir;
    }

    public void setHostPackageName(String hostPackageName) {
        this.hostPackageName = hostPackageName;
    }

    private File getAarManifest() {
        File unzipFile = new File(tmpDir, getAarFileName());
        File manifest = new File(unzipFile, "AndroidManifest.xml");
        return manifest;
    }

    private File getAarres() {
        File unzipFile = new File(tmpDir, getAarFileName());
        File res = new File(unzipFile, "res");
        if (!res.exists()) {
            res.mkdirs();
        }
        return res;
    }

    // 替换了manestfest文件的place holder
    private void replaceAndroidManifestWithHostPackageId() throws IOException, BrutException {

        File manifest = getAarManifest();

        StringBuilder builder = Utils.FileUtils.readStringFromFile(manifest);

        String result = builder.toString().replaceAll("\\$\\{applicationId\\}", getHostPackageName());

        OS.rmfile(manifest.getAbsolutePath());

        Utils.FileUtils.writeString2File(new File(new File(tmpDir, getAarFileName()), "AndroidManifest.xml"), result);

    }


    public void addAarids2ResTable(ResTable resTable) throws Exception {
        if (!getAarres().exists()) {
            LOGGER.fine("aar file have no res file");
            return;
        }

        ResPackage mainPackage = resTable.getPackage(127);
        // 将restyp里面的都替换一下
        // 重新组成新的respect
        // type eg id integer string  attr bool
        for (String typeKey : ids.keySet()) {
            Map<String, AarID> aarIdMap = (LinkedHashMap) ids.get(typeKey);
            for (String resKey : aarIdMap.keySet()) {
                AarID source_aarID = aarIdMap.get(resKey);
                mainPackage.addAarRes(source_aarID, typeKey, resKey);
            }
        }
        // 将resTable中的数据重新加载一次。
        mainPackage.reArrange();

        // 删除valuse文件里的重复的key
        Utils.XmlUtils.removeDuplicateLineAndRemoveIdType(ids, getAarres());

    }

    public void preCombin(File hostUnzipDir) {
        try {
            unzipAarFile();
            setAarPackageId();
            replaceAndroidManifestWithHostPackageId();
            aaptAarPackageOld();
            compileRfile2class();
            // read aar ids
            File rRile = getRjavaFile();
            if (rRile.exists()) {
                ids = Utils.RFileUtils.readAarIds(rRile);
            }
            // 合并AndroidManifest文件
            combinHostManifestWithAar(hostUnzipDir);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAarPackageId() throws IOException, SAXException, ParserConfigurationException {
        File manifest = getAarManifest();
        String packageName = Utils.XmlUtils.getPackageName(manifest);
        this.aarPackageName = packageName;
    }

    private void combinHostManifestWithAar(File hostUnzipDir) {

        File hostManifest = new File(hostUnzipDir, "AndroidManifest.xml");
        // aar manifest
        File aarManifest = getAarManifest();
        try {
            Utils.XmlUtils.combin(aarManifest, hostManifest);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    private File getRjavaFile() throws Exception {
//        String rFilename = getRfileName();
        File Rfile = new File(getRFileDir(), aarPackageName.replaceAll("\\.", File.separator) + File.separator + "R.java");
//        if (!Rfile.exists()) {
//            throw new Exception("R file not exist");
//        }
        return Rfile;
    }

    private void compileRfile2class() throws Exception {

        File Rfile = getRjavaFile();
        if (Rfile.exists()) {
            compileFile(Rfile);
        }

    }

    private void compileFile(File rfile)
            throws BrutException {
        List<String> cmd = new ArrayList<String>();

        String aaptCommand = Utils.BuildPackage.getJavacFile().getAbsolutePath();
        cmd.add(aaptCommand);

        cmd.add(rfile.getAbsolutePath());
        Utils.OSCMD.runCMD(cmd);

    }

    private void unzipAarFile()
            throws AndrolibException {
        LOGGER.fine("Copying assets and libs...");
        if (toyWorkspace == null) {
            LOGGER.fine("please set workspace");
            return;
        }
        try {
            Directory in = new ZipRODirectory(aarFile);
            File unzipFile = new File(tmpDir, getAarFileName());
            if (unzipFile.exists()) {
                LOGGER.info("remove exist " + getAarFileName());
                OS.rmdir(unzipFile);
            }
            // 创建文件路径
            unzipFile.mkdirs();

            if (in.containsDir("assets")) {
                in.copyToDir(unzipFile, "assets");
            }

            if (in.containsDir("jni")) {
                in.copyToDir(unzipFile, "jni");
            }
            if (in.containsDir("res")) {
                in.copyToDir(unzipFile, "res");
            }
            if (in.containsDir("libs")) {
                in.copyToDir(unzipFile, "libs");
            }
            for (String file : in.getFiles()) {
                if (file.endsWith(".jar")) {
                    in.copyToDir(unzipFile, file);
                }
            }
            // ----以上cp .jar 文件到libs

            if (in.containsFile("AndroidManifest.xml")) {
                in.copyToDir(unzipFile, "AndroidManifest.xml");
            }
            if (in.containsFile("R.txt")) {
                in.copyToDir(unzipFile, "R.txt");
            }
            // cp .jar 文件到libs
            File aarlibs = new File(unzipFile, "libs");
            if (!aarlibs.exists()) {
                aarlibs.mkdirs();
            }
            for (File file : Objects.requireNonNull(unzipFile.listFiles())) {
                if (file.getName().endsWith(".jar")) {
                    File newName = new File(unzipFile, System.currentTimeMillis() + ".jar");
                    Utils.FileUtils.reNameFile(file.getAbsolutePath(), newName.getAbsolutePath());
                    OS.cpfile2src(newName, aarlibs);
                }
            }

        } catch (DirectoryException ex) {
            throw new AndrolibException(ex);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getTmpApkFile() {
        return new File(tmpDir, "aar_tmp.apk");
    }

    public void aaptAarPackageOld()
            throws BrutException, IOException {
        List<String> cmd = new ArrayList<String>();
        if (!getAarres().exists()) {
            LOGGER.fine("aar file have no res file");
            return;
        }
        try {
            String aaptCommand = AaptManager.getAppt1().getAbsolutePath();
            cmd.add(aaptCommand);
        } catch (BrutException ex) {
            LOGGER.warning("aapt: " + ex.getMessage() + " (defaulting to $PATH binary)");
        }
        cmd.add("package");
        cmd.add("-f");
        cmd.add("-m");
        cmd.add("-J");
        cmd.add(getRFileDir().getAbsolutePath());
//        cmd.add("-F");
//        cmd.add(getTmpApkFile().getAbsolutePath());

        cmd.add("-S");
        cmd.add(getAarres().getAbsolutePath());
        cmd.add("-I");

        cmd.add(Utils.BuildPackage.getAndroidJar(AarManager.class).getAbsolutePath());
        cmd.add("-M");

        cmd.add(getAarManifest().getAbsolutePath());
        Utils.OSCMD.runCMD(cmd);
    }
    public void aaptAarPackageNew()
            throws BrutException, IOException {
        List<String> cmd = new ArrayList<String>();
        if (!getAarres().exists()) {
            LOGGER.fine("aar file have no res file");
            return;
        }
        try {
            String aaptCommand = AaptManager.getAppt1().getAbsolutePath();
            cmd.add(aaptCommand);
        } catch (BrutException ex) {
            LOGGER.warning("aapt: " + ex.getMessage() + " (defaulting to $PATH binary)");
        }
        cmd.add("package");
        cmd.add("-f");
        cmd.add("-m");
        cmd.add("-J");
        cmd.add(getRFileDir().getAbsolutePath());
        cmd.add("-F");
        cmd.add(getTmpApkFile().getAbsolutePath());

        cmd.add("-S");
        cmd.add(getAarres().getAbsolutePath());
        cmd.add("-I");

        cmd.add(Utils.BuildPackage.getAndroidJar(AarManager.class).getAbsolutePath());
        cmd.add("-M");

        cmd.add(getAarManifest().getAbsolutePath());
        Utils.OSCMD.runCMD(cmd);
    }

    public File smaliClassFilesAndModifyids(File hostdir) throws Exception {
        // before gen all smali file ,delete old sdk smali files
        File aarSmaliFile = smaliClass(hostdir);
        reArrangeRsmalifileIDs(aarSmaliFile);
        copyFiles(hostdir);
        return aarSmaliFile;
    }

    @Override
    public void setHostDir(File hostDir) throws Exception {

    }

    private void copyFiles(File hostdir) throws IOException, BrutException {
        File unzipFile = new File(tmpDir, getAarFileName());
        File valuesFile = new File(unzipFile, "/res/values");
        // rename values files
        if (valuesFile.exists()) {
            for (File xmlFile : valuesFile.listFiles()) {
                if (xmlFile.getName().endsWith(".xml")) {
                    String fileName = xmlFile.getName();
                    fileName = fileName.substring(0, fileName.lastIndexOf(".xml"));
                    String newName = fileName + System.currentTimeMillis() + "_sdk.xml";
                    File rename = new File(valuesFile, newName);
                    boolean success = Utils.FileUtils.reNameFile(xmlFile.getAbsolutePath(), rename.getAbsolutePath());
                    LOGGER.info("change file " + fileName + " 2 " + newName + " success " + success);
                }
            }
        } else {
            LOGGER.info("res/values/ file not exist");
        }

        // copy res
        File resHost = new File(hostdir, "res");
        File resAar = new File(unzipFile, "res");
        if (resAar.exists()) {
            OS.cpdir(resAar, resHost);
            LOGGER.info("拷贝res文件到host文件");
        }


        // copy assets
        File assetsHost = new File(hostdir, "assets");
        File assetsAar = new File(unzipFile, "assets");
        if (assetsAar.exists()) {
            OS.cpdir(assetsAar, assetsHost);
            LOGGER.info("拷贝assets文件到host文件");
        }

        // copy jni

        File jniHost = new File(hostdir, "jni");
        File jniAar = new File(unzipFile, "jni");
        if (jniAar.exists()) {
            OS.cpdir(jniAar, jniHost);
            LOGGER.info("拷贝jni文件到host文件");
        }

    }

    private void reArrangeRsmalifileIDs(File aarSmaliFilerDirs) throws Exception {


        if (aarSmaliFilerDirs == null || !aarSmaliFilerDirs.exists()) {
            throw new Exception("need all aarSmaliFile but not found");
        }

        if (aarPackageName == null || aarPackageName.equals("")) {
            throw new Exception("aar file pacagename is null");
        }

        if (ids == null) {
            LOGGER.fine("ids is empty");
            return;
        }

        String rSmaliDirs_str = aarPackageName.replaceAll("\\.", String.valueOf(File.separatorChar));

        File rSmalidir = new File(aarSmaliFilerDirs, rSmaliDirs_str);
        if (!rSmalidir.exists()) {
            throw new Exception("aar R$  smali file not exist");
        }
        for (File file : Objects.requireNonNull(rSmalidir.listFiles())) {
            if (file.getName().contains("R$")) {
                System.out.println(file.getName());
                StringBuilder stringBuilder = Utils.RFileUtils.smaliFileIdReplace(file, ids);
                Utils.FileUtils.writeString2File(new File(file.getAbsolutePath()), stringBuilder.toString());
            }
        }

    }

    private File smaliClass(File hostdir) throws Exception {
        rClass2jar();
        File libs = getAarLibDir();
        File dexFile = Utils.BuildPackage.dx2dexfiles(libs, AarManager.class);
        return Utils.BuildPackage.all2Smali(hostdir, dexFile, AarManager.class);
        // change R.smali ids
    }

    private File getAarLibDir() {
        File aardir = new File(tmpDir, getAarFileName());
        File libs = new File(aardir, "libs");
        if(!libs.exists()){
            libs.mkdirs();
        }
        return libs;
    }

    private void rClass2jar() throws Exception {
        File rfiledir = getRFileDir();
        if (!rfiledir.exists() || (rfiledir.listFiles().length == 0)) {
            LOGGER.fine("r class  not exist");
            return;
        }
        File[] files = rfiledir.listFiles();
        if (files.length == 0) {
            throw new Exception("R class file not exist");
        }
        List<String> cmd = new ArrayList<>();
        File fileJar = Utils.BuildPackage.getJarComm(AarManager.class);
        File rclassJar = new File(rfiledir, "rClasses.jar");
        if (rclassJar.exists()) {
            rclassJar.delete();
        }
        cmd.add(fileJar.getAbsolutePath());
//        cmd.add("jar");
        cmd.add("cf");
        cmd.add(rclassJar.getAbsolutePath());
        cmd.add("-C");
//        cmd.add(file.getAbsolutePath());
        cmd.add(rfiledir.getAbsolutePath());
        cmd.add(".");
        Utils.OSCMD.runCMD(cmd);
        // copy rClasses.jar 2 libs
        rclassJar.setExecutable(true);
        if (!rclassJar.exists()) {
            throw new Exception("rClasses.jar 文件生成失败");
        }
        // tood change 2 getFilename
        File libs = getAarLibDir();
        OS.cpfile2src(rclassJar, libs);

    }

    public void addIDs2HostFile(File apkOutFile) throws Exception {

        Utils.XmlUtils.addIDs2HostIds(ids, apkOutFile);
    }

    public static void main(String[] args) throws Exception {

        AarManager manager = new AarManager("/Users/dongyuangui/Desktop/aar-1/abtest-release.aar");
        File fileHostApk = new File("/Users/dongyuangui/Desktop/apk-blue/app-debug-remove-statusbutton.apk");
        File outArsc = new File("/Users/dongyuangui/Desktop/apk-blue/out--.arsc");
//       第一步，让arsc decoder 读取文件 aar arsc文件，
//
//
        // 读取了hostApk了
//        第三步，去重复aar arsc文件， // 先不去重，
//        第四部，重新写arsc文件，
//        写的时候，从aar arsc table里面拿出来数据，填进去


//        第二步，读取host arsc文件

        manager.unzipAarFile();
        manager.setHostPackageName("abc.abc.eea");
        manager.replaceAndroidManifestWithHostPackageId();
        manager.aaptAarPackageNew();

//        如何去重呢？两个arsc文件都要读取出来，aar 的arsc去重。 todo ,暂时不去重



    }

    public File getOutPutArscFile() {
        return new File(tmpDir, "output.arsc");
    }


}
