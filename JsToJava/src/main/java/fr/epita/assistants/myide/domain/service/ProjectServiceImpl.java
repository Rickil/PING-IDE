package fr.epita.assistants.myide.domain.service;

import fr.epita.assistants.MyIde;
import fr.epita.assistants.myide.domain.entity.Feature;
import fr.epita.assistants.myide.domain.entity.Project;
import fr.epita.assistants.myide.domain.entity.ProjectImpl;
import fr.epita.assistants.myide.domain.rest.response.CmdResult;
import fr.epita.assistants.myide.domain.service.NodeService;
import fr.epita.assistants.myide.domain.service.NodeServiceImpl;
import fr.epita.assistants.myide.domain.service.ProjectService;

import javax.validation.Configuration;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;


public class ProjectServiceImpl implements ProjectService {
    private Project project = null;
    private MyIde.Configuration configuration;

    public ProjectServiceImpl(MyIde.Configuration configuration) {
        this.configuration = configuration;
    }

    @NotNull public Project load(@NotNull final Path root){
        Project newProject = new ProjectImpl(root);
        this.project = newProject;
        return newProject;
    }

    @NotNull public CmdResult execute(@NotNull final Project project,
                                      @NotNull final Feature.Type featureType,
                                      final String params){
        return project.getFeature(featureType).get().execute(project, params);
    }

    @NotNull public NodeService getNodeService(){
        return new NodeServiceImpl(this.project);
    }
}
