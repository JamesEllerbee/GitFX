package com.jamesellerbee.gitfx.Controllers;

import com.google.inject.Inject;
import com.jamesellerbee.gitfx.Interfaces.ICommandEngine;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugCommandOutputController
{
    private static final Logger logger = LogManager.getLogger("com.jamesellerbee.gitfx");

    @Inject
    private ICommandEngine gitCommandEngine;

    private final ChangeListener<String> changeListener = this::commandOutputChangeListener;

    @FXML
    private TextArea commandOutput;

    @FXML
    private TextField commandInput;


    public DebugCommandOutputController()
    {
    }

    public void onKeyPress_InCommandInput(KeyEvent keyEvent)
    {
        if (keyEvent.getCode().equals(KeyCode.ENTER))
        {
            logger.trace("Enter key press detected on control commandInput.");
            gitCommandEngine.send(commandInput.getText());

            // clear command input text
            commandInput.setText("");
        }
    }

    public void startListeningForCommandOutput()
    {
        logger.debug("starting command output listening");
        gitCommandEngine.getCommandOutputProperty().addListener(changeListener);
    }

    public void stopListeningForCommandOutput()
    {
        logger.debug("stopping command output listening");
        gitCommandEngine.getCommandOutputProperty().removeListener(changeListener);
    }

    private <T> void commandOutputChangeListener(ObservableValue<? extends T> observable, T oldValue, T newValue)
    {
        if(newValue == null)
        {
            return;
        }

        commandOutput.appendText(String.format("%s\n", newValue));
    }
}
