package fr.epita.assistants.myide.domain.rest.request;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class SaveFile {
    public String Path;
    public String Content;
}
