package com.rosstail.karma;

import com.rosstail.karma.lang.AdaptMessage;
import org.apache.commons.io.IOUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FileResourcesUtils {

    private static final FileResourcesUtils fileResourcesUtils = new FileResourcesUtils();
    private static YamlConfiguration defaultFileConfiguration; //en_EN.yml
    public static void generateYamlFile(String folder, Karma plugin) throws IOException {
        boolean doGenerate = false;
        String pluginFolderPath = (plugin.getDataFolder() + "/" + folder).replaceAll(" ", "%20");

        // Sample 3 - read all files from a resources folder (JAR version)
        try {
            File pluginFolder = new File(pluginFolderPath);
            if (!pluginFolder.exists()) {
                pluginFolder.mkdir();
                AdaptMessage.print("Creating " + pluginFolder.getName() + " folder.", AdaptMessage.prints.OUT);
                doGenerate = true;
            }
            // get paths from src/main/resources/json
            List<Path> result = fileResourcesUtils.getPathsFromResourceJAR(folder);
            defaultFileConfiguration = YamlConfiguration.loadConfiguration(fileResourcesUtils.getReaderFromStream(plugin.getResource("lang/en_EN.yml")));

            for (Path path : result) {

                String filePathInJAR = path.toString();
                // Windows will returns /json/file1.json, cut the first /
                // the correct path should be json/file1.json
                if (filePathInJAR.startsWith("/")) {
                    filePathInJAR = filePathInJAR.substring(1);
                }

                File file = new File(filePathInJAR);

                if (!file.exists() && doGenerate) {
                    plugin.saveResource(filePathInJAR, false);
                    AdaptMessage.print(" > Creating " + file + " config.", AdaptMessage.prints.OUT);
                }
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

    public Reader getReaderFromStream(InputStream initialStream)
            throws IOException {

        byte[] buffer = IOUtils.toByteArray(initialStream);
        return new StringReader(new String(buffer));
    }

    // Get all paths from a folder that inside the JAR file
    private List<Path> getPathsFromResourceJAR(String folder)
            throws URISyntaxException, IOException {
        String folderPath = folder.replaceAll(" ", "%20");
        List<Path> result;

        // get path of the current running JAR
        String jarPath = getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath().replaceAll(" ", "%20");

        // file walks JAR
        URI uri = new URI("jar", "file:" + jarPath, null);
        try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {

            result = Files.walk(fs.getPath(URLEncoder.encode(folderPath, "UTF-8")))
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
                AdaptMessage.print(line, AdaptMessage.prints.WARNING);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static YamlConfiguration getDefaultFileConfiguration() {
        return defaultFileConfiguration;
    }
}