package com.rosstail.karma.datas;

import com.rosstail.karma.Karma;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.Cause;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FileResourcesUtils {

    public static void main(String folder, Karma plugin) throws IOException {

        FileResourcesUtils app = new FileResourcesUtils();

        // Sample 3 - read all files from a resources folder (JAR version)
        try {
            File pluginFolder = new File(plugin.getDataFolder() + "/" + folder);
            if (!pluginFolder.exists()) {
                pluginFolder.mkdir();
                AdaptMessage.print("Creating " + pluginFolder.getName() + " folder.", Cause.OUT);
            } else {
                return;
            }
            // get paths from src/main/resources/json
            List<Path> result = app.getPathsFromResourceJAR(folder);
            for (Path path : result) {
                //System.out.println("Path : " + path);

                String filePathInJAR = path.toString();
                // Windows will returns /json/file1.json, cut the first /
                // the correct path should be json/file1.json
                if (filePathInJAR.startsWith("/")) {
                    filePathInJAR = filePathInJAR.substring(1, filePathInJAR.length());
                }

                File file = new File(filePathInJAR);
                if (!file.exists()) {
                    plugin.saveResource(filePathInJAR, false);
                    AdaptMessage.print(" > Creating " + file + " config.", Cause.OUT);
                }


                //System.out.println("filePathInJAR : " + filePathInJAR);

                // read a file from resource folder
                //InputStream is = app.getFileFromResourceAsStream(filePathInJAR);
                //printInputStream(is);
            }

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

    }

    // get a file from the resources folder
    // works everywhere, IDEA, unit test and JAR file.
    private InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }

    // Get all paths from a folder that inside the JAR file
    private List<Path> getPathsFromResourceJAR(String folder)
            throws URISyntaxException, IOException {

        List<Path> result;

        // get path of the current running JAR
        String jarPath = getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();
        //System.out.println("JAR Path :" + jarPath);

        // file walks JAR
        URI uri = new URI("jar", "file:" + jarPath, null);
        try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            result = Files.walk(fs.getPath(folder))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }

        return result;

    }

    // print input stream
    private static void printInputStream(InputStream is) {

        try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                AdaptMessage.print(line, Cause.WARNING);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}