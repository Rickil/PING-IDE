package fr.epita.assistants.myide.domain.service;

import com.google.common.primitives.Bytes;
import fr.epita.assistants.myide.domain.entity.Node;
import fr.epita.assistants.myide.domain.entity.Project;
import fr.epita.assistants.myide.domain.entity.NodeImpl;
import fr.epita.assistants.myide.domain.service.NodeService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeServiceImpl implements NodeService {
    final Node root;
    public NodeServiceImpl(Project project){
        this.root = project.getRootNode();
    }

    public Node update(final Node node,
                final int from,
                final int to,
                final byte[] insertedContent){
        if (!node.isFile())
            throw new RuntimeException();

        try {
            File file = new File(node.getPath().toString());
            byte[] content = Files.readAllBytes(file.toPath());

            int start = from;
            if (start < 0)
                start = 0;

            List<Byte> newContent = new ArrayList<>();
            Bytes.asList(Arrays.copyOfRange(content, 0, from)).forEach(b -> newContent.add(b));

            for (int i = 0; i < to - start; i++){
                if (i >= insertedContent.length)
                    break;
                newContent.add(insertedContent[i]);
            }

            if (to < content.length)
                Bytes.asList(Arrays.copyOfRange(content, to, content.length)).forEach(b -> newContent.add(b));

            byte[] finalContent = Bytes.toArray(newContent);
            Files.write(file.toPath(), finalContent);
        }
        catch (Exception e) {
            throw new RuntimeException();
        }

        return node;
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


    public boolean delete(final Node node){

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

    public Node create(final Node folder,
                final String name,
                final Node.Type type){
        Path finalPath = folder.getPath().resolve(name);
        File file = new File(finalPath.toString());

        try
        {
            if (type == Node.Types.FOLDER){
                file.mkdir();
            }
            else{
                file.createNewFile();
            }
        }
        catch (IOException e){
            throw new RuntimeException();
        }

        Node newNode = new NodeImpl(finalPath, type);
        folder.getChildren().add(newNode);

        return newNode;
    }

    private String extractNameFromPath(final String path){
        String[] pathSplitted = path.split("/");

        if (pathSplitted[pathSplitted.length - 1] == "")
            return pathSplitted[pathSplitted.length - 2];

        return pathSplitted[pathSplitted.length - 1];
    }

    public Node move(final Node nodeToMove,
              final Node destinationFolder){
        String path = nodeToMove.getPath().toString();
        Path finalPath = destinationFolder.getPath().resolve(nodeToMove.getPath().getFileName());

        if (path.equals(finalPath.toString()))
            return nodeToMove;

        Node movedNode = create(destinationFolder, extractNameFromPath(path), nodeToMove.getType());

        try{
            if (nodeToMove.isFolder()) {
                List<Node> children = nodeToMove.getChildren();
                for (Node child : children) {
                    move(child, movedNode);
                }
            }
            else{
                Files.move(nodeToMove.getPath(), finalPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }catch (Exception e){
            throw new RuntimeException();
        }

        delete(nodeToMove);
        destinationFolder.getChildren().add(movedNode);

        return movedNode;
    }
}
