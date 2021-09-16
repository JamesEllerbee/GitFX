package com.jamesellerbee.gitfx.Controllers;

import com.google.inject.Inject;
import com.jamesellerbee.gitfx.GitFxApplication;
import com.jamesellerbee.gitfx.Interfaces.IAppPropertyProvider;
import com.jamesellerbee.gitfx.Interfaces.ICommandEngine;
import com.jamesellerbee.gitfx.Utilities.PropertyConstants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.File;

public class GitFxController
{
    private static final Logger logger = LogManager.getLogger("com.jamesellerbee.gitfx");
    private static final String INVALID_GIT_REPOSITORY = "Selected directory not a valid git repository. (git directory does not exist in selected location)";
    private static final String FILE_SEP = System.getProperty("file.separator");

    // region Private Fields

    @Inject
    private ICommandEngine gitCommandEngine;

    @Inject
    private IAppPropertyProvider appPropertyProvider;

    private Stage gitFxStage;

    // endregion

    // region FXML Fields

    @FXML
    private Button openRepositoryButton;
    @FXML
    private Button openRecentRepositoryButton;
    @FXML
    private Label openRecentRepositoryLabel;

    // endregion

    public GitFxController()
    {
    }

    // region Accessors and Mutators

    public void setGitFxStage(Stage gitFxStage)
    {
        this.gitFxStage = gitFxStage;
    }

    public void setOpenRecentRepositoryButtonText(String recentRepository)
    {
        if (!recentRepository.isEmpty())
        {
            openRecentRepositoryButton.setText(recentRepository);
        }
        else
        {
            openRecentRepositoryLabel.setVisible(false);
            openRecentRepositoryButton.setVisible(false);
            openRecentRepositoryButton.setDisable(true);
        }
    }

    // endregion

    @FXML
    public void exitApplication(ActionEvent event)
    {
        logger.trace("In exit application.");
        Platform.exit();
    }

    public void onAction_DebugCommandOutput(ActionEvent actionEvent)
    {
        logger.trace("debug command output clicked");

        // create new window
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/debugCommandOutputView.fxml"));
            Parent root = fxmlLoader.load();

            DebugCommandOutputController debugCommandOutputController = fxmlLoader.getController();
            GitFxApplication.getInjector().injectMembers(debugCommandOutputController);
            debugCommandOutputController.startListeningForCommandOutput();

            Scene scene = new Scene(root, 500, 500);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

            Stage stage = new Stage();
            stage.setTitle("GitFx command output (debug)");
            stage.setScene(scene);
            stage.show();

            stage.setOnHiding((value) -> debugCommandOutputController.stopListeningForCommandOutput());
        }
        catch (Exception e)
        {
            logger.error("There was an error creating the debug command output window.");
            logger.debug(e);
        }
    }

    public void onAction_OpenRepository(ActionEvent actionEvent)
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Repository");
        File selectedDirectory = directoryChooser.showDialog(gitFxStage);
        if (selectedDirectory != null)
        {
            if (isValidGitRepository(selectedDirectory))
            {
                appPropertyProvider.store(PropertyConstants.RECENT_REPOSITORY_KEY, selectedDirectory.getAbsolutePath());
                gitCommandEngine.send(String.format("cd %s", selectedDirectory.getAbsolutePath()));
                logger.info("switched pwd to {}", selectedDirectory.getAbsolutePath());
                openRepositoryView(true);
            }
            else
            {
                logger.error(INVALID_GIT_REPOSITORY);
                Alert repositoryError = new Alert(
                        Alert.AlertType.ERROR, INVALID_GIT_REPOSITORY, ButtonType.OK);
                repositoryError.show();
            }
        }
    }

    public void onAction_OpenRecent(ActionEvent actionEvent)
    {
        File selectedDirectory = new File(openRecentRepositoryButton.getText());
        gitCommandEngine.send(String.format("cd %s", selectedDirectory.getAbsolutePath()));
        logger.info("switched pwd to {}", selectedDirectory.getAbsolutePath());
        openRepositoryView(false);
    }

    private void openRepositoryView(boolean updateStoredProperty)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/repositoryView.fxml"));
            Parent root = fxmlLoader.load();

            RepositoryController repositoryController = fxmlLoader.getController();
            GitFxApplication.getInjector().injectMembers(repositoryController);
            repositoryController.startListeningToCommandOutput();
            repositoryController.updateInfoTextAreas();

            Scene repositoryScene = new Scene(root, 600, 500);
            repositoryScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

            Stage repositoryStage = new Stage();
            repositoryStage.setTitle("GitFx: Repository view");
            repositoryStage.setScene(repositoryScene);
            repositoryStage.show();
            repositoryStage.setOnHiding((value) ->
                                        {
                                            if (updateStoredProperty)
                                            {
                                                setOpenRecentRepositoryButtonText(
                                                        appPropertyProvider.get(PropertyConstants.RECENT_REPOSITORY_KEY, ""));
                                            }

                                            gitFxStage.show();
                                        });

            repositoryController.setStage(repositoryStage);

            gitFxStage.hide();
        }
        catch (Exception e)
        {
            logger.error("There was an error opening the repository view.");
            logger.debug(e);
        }
    }

    private boolean isValidGitRepository(File selectedDirectory)
    {
        String path = selectedDirectory.getAbsolutePath() + FILE_SEP + ".git";
        logger.debug("Checking for .git directory in {}", path);
        File gitDirectory = new File(path);
        return gitDirectory.exists();
    }
}