package fr.epita.assistants.myide.domain;

import com.google.common.io.Files;
import fr.epita.assistants.myide.domain.entity.*;
import fr.epita.assistants.myide.domain.service.NodeService;
import fr.epita.assistants.myide.domain.service.NodeServiceImpl;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        Project project = new ProjectImpl(Path.of("D:\\Bureau\\testPing\\container"));
        /*FeatureImpl myfeature = new FeatureImpl(Mandatory.Features.Any.DIST);
        myfeature.execute(project, "-DoutputFile=results.txt");*/
        Node node = new NodeImpl(Path.of("D:\\Bureau\\ping\\FileToPush"), Node.Types.FILE);
        NodeServiceImpl nodeservice = new NodeServiceImpl(project);
        Node changed = nodeservice.update(node, 1, 4, "hey".getBytes());
        File file = new File(changed.getPath().toString());
        System.out.println(Files.readLines(file, StandardCharsets.UTF_8));
        //Node testNode
    }

}
