package com.jamesellerbee.gitfx.Engines;

import com.jamesellerbee.gitfx.Interfaces.ICommandEngine;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GitCommandEngine implements ICommandEngine
{
    private static final Logger logger = LogManager.getLogger("com.jamesellerbee.gitfx");

    private final StringProperty commandOutputProperty;
    private Process process;
    BufferedWriter processStreamWriter;


    public GitCommandEngine()
    {
        logger.trace("Creating command engine.");

        // TODO: make this platform independent
        String shell = "cmd.exe";

        commandOutputProperty = new SimpleStringProperty();
        ProcessBuilder processBuilder = new ProcessBuilder(shell);
        try
        {
            process = processBuilder.start();
            processStreamWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        }
        catch (IOException e)
        {
            logger.error(e);
        }

        Thread lineReaderTread = new Thread(() ->
                                            {
                                                BufferedReader processStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                                String line;
                                                while (process.isAlive())
                                                {
                                                    try
                                                    {
                                                        line = processStreamReader.readLine();
                                                        if (line != null)
                                                        {
                                                            logger.debug("process output {}", line);
                                                            commandOutputProperty.setValue(line);
                                                        }
                                                    }
                                                    catch (IOException e)
                                                    {
                                                        logger.error(e);
                                                    }
                                                }

                                                try
                                                {
                                                    processStreamReader.close();
                                                }
                                                catch (IOException e)
                                                {
                                                    logger.error(e);
                                                }
                                                logger.debug("line reader thread finishing.");
                                            });
        lineReaderTread.start();
    }

    @Override
    public void send(String command)
    {
        try
        {
            processStreamWriter.write(command, 0, command.length());
            processStreamWriter.newLine();
            processStreamWriter.flush();
            logger.debug("sent '{}' to process", command);
        }
        catch (IOException e)
        {
            logger.error(e);
        }
    }

    public void shutdown()
    {
        logger.debug("Destroying the shell process.");
        process.destroy();
        try
        {
            processStreamWriter.close();
        }
        catch (IOException e)
        {
            logger.error(e);
        }
    }

    public StringProperty getCommandOutputProperty()
    {
        return commandOutputProperty;
    }
}
