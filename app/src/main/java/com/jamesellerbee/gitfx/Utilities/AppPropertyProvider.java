package com.jamesellerbee.gitfx.Utilities;

import com.jamesellerbee.gitfx.Interfaces.IAppPropertyProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

public class AppPropertyProvider implements IAppPropertyProvider
{
    private static final Logger logger = LogManager.getLogger("com.jamesellerbee.gitfx");

    private final String propertiesPath;
    private final Properties properties;

    private Thread lastSaveTask;

    /**
     * Initialize a new instance of {@link AppPropertyProvider}.
     *
     * @param path The path to the property file.
     */
    public AppPropertyProvider(String path)
    {
        this.propertiesPath = path;
        properties = new Properties();

        File propertiesFile = new File(propertiesPath);
        try (InputStream fileInputStream = new FileInputStream(propertiesFile))
        {
            properties.load(fileInputStream);
        }
        catch (IOException e)
        {
            logger.error("Error reading properties.");
            logger.debug(e);
        }
    }

    @Override
    public <T> T get(String key, T defaultValue)
    {
        String originalValue = properties.getProperty(key);
        return (T) castTo(originalValue, defaultValue);
    }

    @Override
    public void store(String key, Object value)
    {
        properties.setProperty(key, value.toString());

        save();
    }

    private void save()
    {
        if (lastSaveTask != null && lastSaveTask.isAlive())
        {
            logger.warn("Starting new save task while last one is still running.");

            // todo: begin timer to clean up the last save tasks
        }

        lastSaveTask = new Thread(this::saveAsync);
        lastSaveTask.start();
    }

    private void saveAsync()
    {
        File propertiesFile = new File(propertiesPath);
        try (OutputStream fileOutputStream = new FileOutputStream(propertiesFile))
        {
            properties.store(fileOutputStream, "");
        }
        catch (IOException e)
        {
            logger.error("Error storing properties");
            logger.debug(e);
        }
    }

    private <T> T castTo(String originalValue, T defaultValue)
    {
        Object newValue = defaultValue;

        try
        {
            if (defaultValue instanceof String)
            {
                newValue = originalValue;
            }

            if (defaultValue instanceof Integer)
            {
                newValue = Integer.parseInt(originalValue);
            }
        }
        catch (Exception e)
        {
            logger.error("Could not cast property value to requested type.");
            logger.debug(e);
        }

        return (T) newValue;
    }
}
