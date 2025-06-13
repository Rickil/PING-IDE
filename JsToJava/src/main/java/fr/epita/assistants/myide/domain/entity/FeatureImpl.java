package fr.epita.assistants.myide.domain.entity;

import com.google.common.base.Charsets;
import fr.epita.assistants.myide.domain.rest.response.CmdResult;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.SystemUtils;
import org.checkerframework.checker.units.qual.C;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Value
@With
public class FeatureImpl implements Feature {
    final Feature.Type type;

    public FeatureImpl(Feature.Type type){
        this.type = type;
    }

    private void __delete__(final Node node){
        List<Node> children = node.getChildren();
        for (Node child : children){
            if (child.isFile()) {
                File file = new File(child.getPath().toString());
                file.delete();
            }else if (child.isFolder()){
                __delete__(child);
            }
        }
        File file = new File(node.getPath().toString());
        file.delete();
    }

    private Node findParent(Node actual, Node node) {
        List<Node> children = actual.getChildren();
        for (Node child : children) {
            if (node.getPath().equals(child.getPath())) {
                return actual;
            }
            else if (child.isFolder()){
                Node parent = findParent(child, node);
                if (parent != null)
                    return parent;
            }
        }
        return null;
    }

    public boolean delete(final Project project, final Node node){
        Node root = project.getRootNode();
        if (root.equals(node)) {
            __delete__(root);
            return true;
        }

        Node parent = findParent(root, node);
        if (parent == null){
            return false;
        }

        __delete__(node);
        parent.getChildren().remove(node);

        return true;
    }

    private List<Node> matchedNode = new ArrayList<>();
    private void findWithPath(Node node, String path){
        Pattern pattern = Pattern.compile(path.replaceAll("(?=[]\\[+&|!(){}^\"~*?:\\\\-])", "\\\\")
        );
        Matcher matcher = pattern.matcher(node.getPath().toString());
        if (matcher.find())
            matchedNode.add(node);

        List<Node> children = node.getChildren();
        for (Node child : children){
            matcher = pattern.matcher(child.getPath().toString());
            if (matcher.find())
                matchedNode.add(child);
            else if (child.isFolder()) {
                findWithPath(child, path);
            }
        }
    }


    private CmdResult cleanup(final Project project){
        System.out.println("passing by execute cleanup and path is " + project.getRootNode().getPath().toString());
        Path file = Paths.get(project.getRootNode().getPath() + "/.myideignore");
        try {
            List<String> lines = Files.readAllLines(file);
            for (String filePath : lines){
                findWithPath(project.getRootNode(), filePath);

                for (Node node : matchedNode){
                    delete(project, node);
                }
            }
        } catch (IOException fileError) {
            return new CmdResult("error during cleanup");
        }
        return new CmdResult("Cleanup finished");
    }

    //get all file of the project
    private void getAllFiles(Node node, List filesList) throws IOException {

        if (node.isFile()) {
            File file = new File(node.getPath().toString());
            filesList.add(file);

        }else {
            List<Node> childs = node.getChildren();
            for (Node child : childs) {
                getAllFiles(child, filesList);
            }
        }
    }

    //WIP
    private CmdResult dist(final Project project){
        cleanup(project);

        //zip the project
        try {
            File rootFile = new File(project.getRootNode().getPath().toString() + ".zip");

            //get all files of the project
            ArrayList<File> filesList = new ArrayList<File>();
            getAllFiles(project.getRootNode(), filesList);

            //create ZipOutputStream to write to the zip file
            FileOutputStream fos = new FileOutputStream(rootFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File file : filesList) {
                //add a new Zip Entry to the ZipOutputStream
                ZipEntry zentry = new ZipEntry(project.getRootNode().getPath().getFileName().toString() + "/" + file.getPath().replace(project.getRootNode().getPath().toString(), "").substring(1));
                zos.putNextEntry(zentry);

                //read the file and write to ZipOutputStream
                try {

                    byte[] text = Files.readAllBytes(file.toPath());
                    zos.write(text);

                } catch (IOException fileError) {
                    zos.close();
                    fos.close();
                    return new CmdResult("error while zipping project");
                }

                //Close the zip entry to write to zip file
                zos.closeEntry();
            }
            zos.close();
            fos.close();

        } catch (IOException e) {
            return new CmdResult("error while reading file");
        }

        return new CmdResult("Zip finished");
    }

    private CmdResult search(final Node node, final String text){
        return new CmdResult("");
    }

    private CmdResult pull(final Project project){
        try{Repository Repo = new FileRepositoryBuilder()
                .setGitDir(new File(project.getRootNode().getPath().toString() + "/.git"))
                .build();

        Git git = new Git(Repo);
        git.pull().call();}
        catch (Exception e) {return new CmdResult("error for the pull");}
        return new CmdResult("pull finished");
    }

    private CmdResult add(final Project project, final String files){
        try{Repository Repo = new FileRepositoryBuilder()
                .setGitDir(new File(project.getRootNode().getPath().toString() + "/.git"))
                .build();

        Git git = new Git(Repo);
        AddCommand add = git.add();
        String[] filePaths = files.split(" ");
        for (String filePath : filePaths){
            add.addFilepattern(filePath);
        }
        add.call();}
        catch (Exception e) {return new CmdResult("error while adding file");}
        return new CmdResult("files added");
    }

    private CmdResult commit(final Project project, final String message){
        try{Repository Repo = new FileRepositoryBuilder()
                .setGitDir(new File(project.getRootNode().getPath().toString() + "/.git"))
                .build();

        Git git = new Git(Repo);
        git.commit().setMessage(message).call();}
        catch (Exception e) {return new CmdResult("error during commit");}
        return new CmdResult("commit finished");
    }

    private CmdResult push(final Project project){
        try{Repository Repo = new FileRepositoryBuilder()
                .setGitDir(new File(project.getRootNode().getPath().toString() + "/.git"))
                .build();

        Git git = new Git(Repo);
        git.push().call();}
        catch (Exception e) {return new CmdResult("error with the push");}
        return new CmdResult("push finished");
    }

    private CmdResult compile(final Project project){
        String output = "";
        try{
            Process p;
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream(true);


            if (SystemUtils.IS_OS_WINDOWS)
                pb.command("cmd.exe", "/c", "mvn compile");
            else
                pb.command("sh", "-c", "mvn compile");
            pb.directory(new File(project.getRootNode().getPath().toString()));
            p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                output += line + "\n" ;
            }

            synchronized (p) {
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    System.out.println("Error p wait catched");
                    return new CmdResult("Error p wait catched");
                }
            }
        }
        catch (Exception e) {
            System.out.println("Error pb start catched");
           return new CmdResult("Error pb start catched");
        }

        return new CmdResult(output);
    }

    private CmdResult clean(final Project project){
        String output = "";
        try{
            Process p;
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", "mvn clean");
            pb.redirectErrorStream(true);
            pb.directory(new File(project.getRootNode().getPath().toString()));
            p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                output += line + "\n" ;
            }

            synchronized (p) {
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    return new CmdResult("error with the clean");
                }
            }
        }
        catch (Exception e) {
            return new CmdResult("error with the clean");
        }

        return new CmdResult(output);
    }

    private CmdResult test(final Project project){
        String output = "";
        try{
            Process p;
            //sh -c
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", "mvn test");
            pb.redirectErrorStream(true);
            pb.directory(new File(project.getRootNode().getPath().toString()));
            p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                output += line + "\n";
            }

            synchronized (p) {
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    return new CmdResult("error with the test");
                }
            }
        }
        catch (Exception e) {
            return new CmdResult("error with the test");
        }

        return new CmdResult(output);
    }

    private CmdResult pack(final Project project){
        String output = "";
        try{
            Process p;
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", "mvn package");
            pb.redirectErrorStream(true);
            pb.directory(new File(project.getRootNode().getPath().toString()));
            p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                output += line + "\n";
            }

            synchronized (p) {
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    return new CmdResult("Error with the package command");
                }
            }
        }
        catch (Exception e) {
            return new CmdResult("Error with the package command");
        }

        return new CmdResult(output);
    }

    private CmdResult install(final Project project){
        String output = "";
        try{
            Process p;
            ProcessBuilder pb;
            if (SystemUtils.IS_OS_WINDOWS)
                pb = new ProcessBuilder("cmd.exe", "/c", "mvn install");
            else
                pb = new ProcessBuilder("sh", "-c", "mvn install");
            pb.redirectErrorStream(true);
            pb.directory(new File(project.getRootNode().getPath().toString()));
            p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                output += line + "\n";
            }

            synchronized (p) {
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    return new CmdResult("error with the installation");
                }
            }
        }
        catch (Exception e) {
            return new CmdResult("Error with the installation");
        }

        return new CmdResult(output);
    }

    private CmdResult exec(final Project project, final String command){
        String output = "";
        try{
            Process p;
            ProcessBuilder pb;
            if (SystemUtils.IS_OS_WINDOWS)
                pb = new ProcessBuilder("cmd.exe", "/c", "mvn exec:java " + command);
            else
                pb = new ProcessBuilder("sh", "-c", "mvn exec:java " + command);
            pb.redirectErrorStream(true);
            pb.directory(new File(project.getRootNode().getPath().toString()));
            p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                output += line + "\n";
            }

            synchronized (p) {
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    return new CmdResult("error with the execution");
                }
            }
        }
        catch (Exception e) {
            return new CmdResult("Error with the execution");
        }

        return new CmdResult(output);
    }

    private CmdResult tree(final Project project, final String params){
        String output = "";
        try{
            Process p;
            List<String> commands = new ArrayList<String>();
            if (SystemUtils.IS_OS_WINDOWS) {
                commands.add("cmd.exe");
                commands.add("/c");
            }else{
                commands.add("sh");
                commands.add("-c");
            }
            commands.add("mvn dependency:tree " + params);
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            pb.directory(new File(project.getRootNode().getPath().toString()));
            p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                output += line + "\n";
            }

            synchronized (p) {
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    return new CmdResult("error with tree");
                }
            }
        }
        catch (Exception e) {
            return new CmdResult("Error with tree");
        }

        return new CmdResult(output);
    }

    private static File searchMakeFile(File folder){
        File[] children = folder.listFiles();
        for (File file : children) {
            if (file.isFile() && file.getName().equals("Makefile")){
                return file;
            }else if (file.isDirectory()){
                File newFile = searchMakeFile(file);
                if (newFile != null)
                    return newFile;
            }
        }
        return null;
    }

    static public CmdResult CppCompileSingleFile(File file, final String params){
        String output = "";
        try{
            Process p;
            List<String> commands = new ArrayList<String>();
            if (SystemUtils.IS_OS_WINDOWS){
                commands.add("cmd.exe");
                commands.add("/c");
            }else{
                commands.add("sh");
                commands.add("-c");
            }

            commands.add("g++ " + file.getName() + " -o " + "tempResult " + params);
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            pb.directory(file.getParentFile());
            p = pb.start();

            synchronized (p) {
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    return new CmdResult("error with file compile");
                }
            }
        }
        catch (Exception e) {
            return new CmdResult("Error with file compile");
        }
        File[] children = file.getParentFile().listFiles();
        for (File child : children){
            if (child.getName().matches(".*tempResult.*")){
                try{
                    Process p;
                    List<String> commands = new ArrayList<String>();
                    if (SystemUtils.IS_OS_WINDOWS){
                        commands.add("cmd.exe");
                        commands.add("/c");
                    }else {
                        commands.add("sh");
                        commands.add("-c");
                    }
                    commands.add(child.getName());

                    ProcessBuilder pb = new ProcessBuilder(commands);
                    pb.redirectErrorStream(true);
                    pb.directory(file.getParentFile());
                    p = pb.start();

                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        output += line + "\n";
                    }

                    synchronized (p) {
                        try {
                            p.waitFor();
                        } catch (InterruptedException e) {
                            return new CmdResult("error with file compile");
                        }
                    }
                }
                catch (Exception e) {
                    return new CmdResult("Error with file compile");
                }
                break;
            }
        }

        return new CmdResult(output);
    }

    private static CmdResult CompileWithMakefile(File makefile, final String params){
        String output = "";
        try{
            Process p;
            List<String> commands = new ArrayList<String>();
            if (SystemUtils.IS_OS_WINDOWS){
                commands.add("cmd.exe");
                commands.add("/c");
            }else {
                commands.add("sh");
                commands.add("-c");
            }
            commands.add("make " + params);
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            pb.directory(makefile.getParentFile());
            p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                output += line + "\n";
            }

            synchronized (p) {
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    return new CmdResult("error with makefile");
                }
            }
        }
        catch (Exception e) {
            return new CmdResult("Error with makefile");
        }

        return new CmdResult(output);
    }

    public static CmdResult CppCompile(final Project project, File file, final String params){
        if (project == null){
            return CppCompileSingleFile(file, params);
        }

        File makefile = searchMakeFile(project.getRootNode().getPath().toFile());

        if (makefile == null) {
            return CppCompileSingleFile(file, params);
        }

        return CompileWithMakefile(makefile, params);
    }

    @NotNull public CmdResult execute(final Project project, final String params) {
        CmdResult cmdResult = new CmdResult("");

        // Execute command?
        if (type == Mandatory.Features.Any.CLEANUP)
        {
            cmdResult = cleanup(project);
        }
        else if (type == Mandatory.Features.Any.DIST){
            cmdResult = dist(project);
        }
        else if (type == Mandatory.Features.Any.SEARCH){
            cmdResult = search(project.getRootNode(), params);
        }
        else if (type == Mandatory.Features.Git.PULL){
            cmdResult = pull(project);
        }
        else if (type == Mandatory.Features.Git.ADD){
            cmdResult = add(project, params);
        }
        else if (type == Mandatory.Features.Git.COMMIT){
            cmdResult = commit(project, params);
        }
        else if (type == Mandatory.Features.Git.PUSH){
            cmdResult = push(project);
        }
        else if (type == Mandatory.Features.Maven.COMPILE){
            cmdResult = compile(project);
        }
        else if (type == Mandatory.Features.Maven.CLEAN){
            cmdResult = clean(project);
        }
        else if (type == Mandatory.Features.Maven.EXEC){
            cmdResult = exec(project, params);
        }
        else if (type == Mandatory.Features.Maven.INSTALL){
            cmdResult = install(project);
        }
        else if (type == Mandatory.Features.Maven.TEST){
            cmdResult = test(project);
        }
        else if (type == Mandatory.Features.Maven.PACKAGE){
            cmdResult = pack(project);
        }
        else if (type == Mandatory.Features.Maven.TREE){
            cmdResult = tree(project, params);
        }

        return cmdResult;
    }

    @NotNull public Type type(){
        return type;
    }

    public static class ExecutionReportImpl implements Feature.ExecutionReport {
        private boolean success = false;

        public boolean isSuccess(){
            return success;
        }
    }
}
