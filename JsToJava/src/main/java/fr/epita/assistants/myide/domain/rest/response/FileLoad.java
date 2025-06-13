package fr.epita.assistants.myide.domain.rest.response;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@With
@AllArgsConstructor
public class FileLoad {
    public String Path;
    public String FileContent;
}
