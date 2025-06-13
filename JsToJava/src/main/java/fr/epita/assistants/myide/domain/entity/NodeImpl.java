package fr.epita.assistants.myide.domain.entity;

import fr.epita.assistants.myide.domain.entity.Node;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

@Value
@With
public class NodeImpl implements Node {
    final Path path;
    final Node.Type type;
    private List<Node> children = new ArrayList<>();

    public NodeImpl(Path path, Node.Type type) {
        this.path = path;
        this.type = type;

        if (isFolder()) {
            File[] files = new File(path.toString()).listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile())
                        children.add(new NodeImpl(file.toPath(), Types.FILE));
                    else
                        children.add(new NodeImpl(file.toPath(), Types.FOLDER));
                }
            }
        }
    }

    @NotNull
    public Path getPath() {
        return path;
    }

    @NotNull
    public Node.Type getType() {
        return type;
    }

    @NotNull
    public List<@NotNull Node> getChildren() { return children; }
}
