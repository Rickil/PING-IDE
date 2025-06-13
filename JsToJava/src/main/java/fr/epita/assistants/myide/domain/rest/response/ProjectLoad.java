package fr.epita.assistants.myide.domain.rest.response;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@With
@AllArgsConstructor
public class ProjectLoad {
    public String Output;
    public String Language;
    public String Path;
    public String Tree;
}
