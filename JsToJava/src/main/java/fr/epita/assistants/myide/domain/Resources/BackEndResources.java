package fr.epita.assistants.myide.domain.Resources;

import com.google.common.base.Charsets;
import fr.epita.assistants.MyIde;
import fr.epita.assistants.myide.domain.entity.FeatureImpl;
import fr.epita.assistants.myide.domain.entity.Mandatory;
import fr.epita.assistants.myide.domain.entity.ProjectImpl;
import fr.epita.assistants.myide.domain.rest.request.CmdParams;
import fr.epita.assistants.myide.domain.rest.request.CppCompile;
import fr.epita.assistants.myide.domain.rest.request.ProjectInfos;
import fr.epita.assistants.myide.domain.rest.request.SaveFile;
import fr.epita.assistants.myide.domain.rest.response.CmdResult;
import fr.epita.assistants.myide.domain.rest.response.FileLoad;
import fr.epita.assistants.myide.domain.rest.response.ProjectLoad;
import fr.epita.assistants.myide.domain.service.ProjectServiceImpl;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BackEndResources {
    public static ProjectServiceImpl projectService = (ProjectServiceImpl) MyIde.init(new MyIde.Configuration(Paths.get(".."), Paths.get("..")));
    public ProjectImpl project = null;
    String language;

    public static String printDirectoryTree(File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("folder is not a Directory");
        }
        int indent = 0;
        StringBuilder sb = new StringBuilder();
        printDirectoryTree(folder, indent, sb);
        return sb.toString();
    }

    private static void printDirectoryTree(File folder, int indent,
                                           StringBuilder sb) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("folder is not a Directory");
        }
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(folder.getName());
        sb.append("/");
        sb.append("\n");
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                printDirectoryTree(file, indent + 1, sb);
            } else {
                printFile(file, indent + 1, sb);
            }
        }

    }

    private static void printFile(File file, int indent, StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(file.getName());
        sb.append("\n");
    }

    private static String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("|  ");
        }
        return sb.toString();
    }

    @GET @Path("/check")
    public Response check(){
        return Response.ok().build();
    }

    @POST @Path("/openProject")
    public Response openDirectory(ProjectInfos projectInfos) throws Exception {
        JFileChooser choose = new JFileChooser(
                FileSystemView
                        .getFileSystemView()
                        .getHomeDirectory()
        );
        choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = choose.showOpenDialog(null);
        File file = null;
        if (res == JFileChooser.APPROVE_OPTION) {
            file = choose.getSelectedFile();
        }

        if (file != null) {
            String path = file.getAbsolutePath();
            String language = "c++";

            project = new ProjectImpl(java.nio.file.Path.of(path));
            projectService.load(java.nio.file.Path.of(path));

            if (project.getAspects().contains(Mandatory.Aspects.MAVEN))
                language = "java";

            ProjectLoad projectLoad = new ProjectLoad("Project Loaded!", language, path, printDirectoryTree(file));
            return Response.ok(projectLoad).build();
        }

        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET @Path("/loadFile")
    public Response loadFile() throws Exception {
        JFileChooser choose = new JFileChooser(
                FileSystemView
                        .getFileSystemView()
                        .getHomeDirectory()
        );

        if(project.getPath() != null)
            choose.setCurrentDirectory(project.getPath().toFile());

        choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int res = choose.showOpenDialog(null);
        File file = null;
        if (res == JFileChooser.APPROVE_OPTION) {
            file = choose.getSelectedFile();
        }

        if (file != null) {
            String path = file.getAbsolutePath();

            java.nio.file.Path path1 = java.nio.file.Path.of(path);
            List<String> allLinesContent = Files.readAllLines(path1, Charsets.UTF_8);
            StringBuilder contentBuilder= new StringBuilder();

            for (String line : allLinesContent) {
                contentBuilder.append(line + "\n");
            }

            FileLoad fileLoad = new FileLoad(path, contentBuilder.toString());
            return Response.ok(fileLoad).build();
        }

        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @POST @Path("/saveFiles")
    public Response saveFiles(SaveFile f){
        java.nio.file.Path path = java.nio.file.Path.of(f.Path);
        String content = f.Content;

        try {
            Files.write(path, content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.ok().build();
    }

    @GET @Path("/any/cleanup")
    public Response cleanupMethod(){
        System.out.println("passing by cleanup function and path is " + project.getRootNode().getPath().toString());
        CmdResult cmdResult = projectService.execute(project, Mandatory.Features.Any.CLEANUP, "");
        return Response.ok(cmdResult).build();
    }

    @GET @Path("/any/dist")
    public Response distMethod(){
        CmdResult cmdResult = projectService.execute(project, Mandatory.Features.Any.DIST, "");
        return Response.ok(cmdResult).build();
    }

    @GET @Path("/mvn/compile")
    public Response mvncompileMethod(){
        CmdResult cmdResult = projectService.execute(project, Mandatory.Features.Maven.COMPILE, "");
        return Response.ok(cmdResult).build();
    }

    @GET @Path("/mvn/clean")
    public Response mvncleanMethod(){
        CmdResult cmdResult = projectService.execute(project, Mandatory.Features.Maven.CLEAN, "");
        return Response.ok(cmdResult).build();
    }

    @GET @Path("/mvn/test")
    public Response mvntestMethod(){
        CmdResult cmdResult = projectService.execute(project, Mandatory.Features.Maven.TEST, "");
        return Response.ok(cmdResult).build();
    }

    @GET @Path("/mvn/package")
    public Response mvnpackageMethod(){
        CmdResult cmdResult = projectService.execute(project, Mandatory.Features.Maven.PACKAGE, "");
        return Response.ok(cmdResult).build();
    }

    @GET @Path("/mvn/install")
    public Response mvninstallMethod(){
        CmdResult cmdResult = projectService.execute(project, Mandatory.Features.Maven.INSTALL, "");
        return Response.ok(cmdResult).build();
    }

    @POST @Path("/mvn/exec")
    public Response mvnexecMethod(CmdParams cmdParams){
        CmdResult cmdResult = projectService.execute(project, Mandatory.Features.Maven.EXEC, cmdParams.params);
        return Response.ok(cmdResult).build();
    }

    @POST @Path("/mvn/tree")
    public Response mvntreeMethod(CmdParams cmdParams){
        CmdResult cmdResult = projectService.execute(project, Mandatory.Features.Maven.TREE, cmdParams.params);
        return Response.ok(cmdResult).build();
    }

    @POST @Path("/git/add")
    public Response gitaddMethod(CmdParams cmdParams){
        CmdResult cmdResult = projectService.execute(project, Mandatory.Features.Git.ADD, cmdParams.params);
        return Response.ok(cmdResult).build();
    }

    @POST @Path("/git/commit")
    public Response gitcommitMethod(CmdParams cmdParams){
        CmdResult cmdResult = projectService.execute(project, Mandatory.Features.Git.COMMIT, cmdParams.params);
        return Response.ok(cmdResult).build();
    }

    @GET @Path("/git/push")
    public Response gitpushMethod(){
        CmdResult cmdResult = projectService.execute(project, Mandatory.Features.Git.PUSH, "");
        return Response.ok(cmdResult).build();
    }

    @GET @Path("/git/pull")
    public Response gitpullMethod(){
        CmdResult cmdResult = projectService.execute(project, Mandatory.Features.Git.PULL, "");
        return Response.ok(cmdResult).build();
    }

    @POST @Path("/cpp/compile")
    public Response cppCompile(CppCompile c){
        CmdResult cmdResult = new CmdResult("");
        if (project != null)
            cmdResult = FeatureImpl.CppCompile(project,  new File(c.file.Path), c.params);
        else {
            cmdResult = FeatureImpl.CppCompileSingleFile(new File(c.file.Path), c.params);
        }
        return Response.ok(cmdResult).build();
    }
}
