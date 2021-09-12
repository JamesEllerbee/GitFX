package com.jamesellerbee.gitfx.Controllers;

import com.google.inject.Inject;
import com.jamesellerbee.gitfx.Engines.GitCommandEngine;
import com.jamesellerbee.gitfx.Interfaces.ICommandEngine;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GitFxController
{
    private static final Logger logger = LogManager.getLogger("com.jamesellerbee.gitfx");

    private boolean isListeningToCommandEngine;

    @Inject
    private ICommandEngine gitCommandEngine;

    @FXML
    private TextField commandInput;
    @FXML
    private TextArea commandOutput;

    public GitFxController()
    {
        isListeningToCommandEngine = false;
    }

    public void addStyle()
    {
        // hellButton style
//        helloButton.getStyleClass().addAll("btn", "btn-primary");

        // welcomeLabel style
//        welcomeLabel.getStyleClass().addAll("strong");

        // etc...
    }

    public void onKeyPress_InCommandInput(KeyEvent keyEvent)
    {
        startListeningToCommandEngine();

        if(keyEvent.getCode().equals(KeyCode.ENTER))
        {
            logger.debug("enter key press detected on control commandinput");
            gitCommandEngine.send(commandInput.getText());

            // clear command input text
            commandInput.setText("");
        }
    }

    @FXML
    public void exitApplication(ActionEvent event)
    {
        logger.trace("In exit application.");
        Platform.exit();
    }

    private void startListeningToCommandEngine()
    {
        if(!isListeningToCommandEngine)
        {
            gitCommandEngine.getCommandOutputProperty().addListener(
                    (observable, oldValue, newValue) ->
                    {
                        try
                        {
                            logger.debug("command output property changed: {} -> {}", oldValue, newValue);
                            commandOutput.appendText(newValue + "\n");
                        }

                        // catch errors so the observer doesn't collapse
                        catch (Exception e)
                        {
                            logger.error(e);
                        }
                    });

            isListeningToCommandEngine = true;
        }
    }
}