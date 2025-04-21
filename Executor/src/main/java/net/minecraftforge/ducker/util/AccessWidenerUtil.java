package net.minecraftforge.ducker.util;

import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for handling access widener files.
 */
public class AccessWidenerUtil {

    private static final Logger LOGGER = LogManager.getLogger(AccessWidenerUtil.class);

    /**
     * Creates and configures an AccessWidener instance from a list of access widener files.
     *
     * @param accessWidenerFiles List of paths to access widener files
     * @return Configured AccessWidener instance
     */
    public static AccessWidener createAccessWidener(List<String> accessWidenerFiles) {
        AccessWidener accessWidener = new AccessWidener();
        AccessWidenerReader reader = new AccessWidenerReader(accessWidener);

        if (accessWidenerFiles == null || accessWidenerFiles.isEmpty()) {
            LOGGER.info("No access widener files specified");
            return accessWidener;
        }

        LOGGER.info("Processing {} access widener file(s)", accessWidenerFiles.size());

        for (String filePath : accessWidenerFiles) {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                LOGGER.warn("Access widener file does not exist or is not a file: {}", filePath);
                continue;
            }

            try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                LOGGER.info("Reading access widener file: {}", filePath);
                reader.read(fileReader);
                LOGGER.info("Successfully processed access widener file: {}", filePath);
            } catch (IOException e) {
                LOGGER.error("Failed to read access widener file: {}", filePath, e);
            }
        }

        LOGGER.info("Access widener targets: {}", accessWidener.getTargets());

        return accessWidener;
    }
}
