package fr.epita.assistants.myide.domain.entity;

import fr.epita.assistants.myide.domain.entity.*;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.*;

@Value
@With
@AllArgsConstructor
public class ProjectImpl implements Project {
    final Path path;
    final Node root;

    public ProjectImpl(Path path) {
        this.path = path;
        this.root = new NodeImpl(path, Node.Types.FOLDER);
    }

    @NotNull public Node getRootNode(){
        return root;
    }

    @NotNull public Set<Aspect> getAspects(){
        Set<Aspect> aspects = new HashSet<>();
        List<Node> children = getRootNode().getChildren();

        for (Node node: children){
            Path path = node.getPath();

            if (path.toString().contains(".git"))
                aspects.add(new AspectImpl(Mandatory.Aspects.GIT));
            if (path.toString().contains("pom.xml"))
                aspects.add(new AspectImpl(Mandatory.Aspects.MAVEN));
        }
        aspects.add(new AspectImpl(Mandatory.Aspects.ANY));

        return aspects;
    }

    @NotNull public Optional<Feature> getFeature(@NotNull final Feature.Type featureType) {
        Set<Aspect> aspects = getAspects();

        for (Aspect aspect : aspects) {
            List<Feature> featureList = aspect.getFeatureList();
            for (Feature feature : featureList) {
                if (feature.type() == featureType) {
                    return Optional.of(feature);
                }
            }
        }

        return Optional.empty();
    }
}
