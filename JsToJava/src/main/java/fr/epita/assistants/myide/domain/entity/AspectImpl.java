package fr.epita.assistants.myide.domain.entity;

import fr.epita.assistants.myide.domain.entity.*;
import lombok.Value;
import lombok.With;

import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Value
@With
public class AspectImpl implements Aspect {
    final Aspect.Type type;

    public AspectImpl(Aspect.Type type){
        this.type = type;
    }

    @NotNull public Aspect.Type getType(){
        return type;
    }

    @NotNull public List<Feature> getFeatureList(){
        Aspect.Type type = getType();
        List<Feature> featureList = new ArrayList<>();

        if (type == Mandatory.Aspects.ANY) {
            for (Mandatory.Features.Any featureEnum : Mandatory.Features.Any.values()) {
                Feature feature = new FeatureImpl(featureEnum);
                featureList.add(feature);
            }
        }

        if (type == Mandatory.Aspects.GIT){
            for (Mandatory.Features.Git featureEnum : Mandatory.Features.Git.values()){
                Feature feature = new FeatureImpl(featureEnum);
                featureList.add(feature);
            }
        }

        if (type == Mandatory.Aspects.MAVEN){
            for (Mandatory.Features.Maven featureEnum : Mandatory.Features.Maven.values()){
                Feature feature = new FeatureImpl(featureEnum);
                featureList.add(feature);
            }
        }

        return featureList;
    }
}
