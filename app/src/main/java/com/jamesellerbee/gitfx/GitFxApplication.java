package com.jamesellerbee.gitfx;

import com.google.inject.Injector;
import com.jamesellerbee.gitfx.Controllers.GitFxController;
import com.jamesellerbee.gitfx.Interfaces.ICommandEngine;
import com.jamesellerbee.gitfx.Module.GitFxModule;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.bootstrapfx.BootstrapFX;
import com.google.inject.Guice;

import java.io.IOException;

public class GitFxApplication extends javafx.application.Application
{
    private static final Logger logger = LogManager.getLogger("com.jamesellerbee.gitfx");

    public static final Injector injector = Guice.createInjector(new GitFxModule());

    @Override
    public void start(Stage stage) throws IOException
    {
        logger.info("Application start.");
        FXMLLoader fxmlLoader = new FXMLLoader(GitFxApplication.class.getResource("/applicationView.fxml"));
        Parent root = fxmlLoader.load();

        // FUTURE: remember screen size on stop and load
        Scene scene = new Scene(root, 500, 500);

        GitFxController gitFxController = fxmlLoader.getController();
        gitFxController.setGitFxStage(stage);
        injector.injectMembers(gitFxController);

        // add bootstrap stylesheet
        logger.trace("Adding bootstrap fx stylesheet.");
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        logger.trace("Finalizing stage.");
        stage.setTitle("GitFx");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception
    {
        super.stop();

        injector.getInstance(ICommandEngine.class).shutdown();

        logger.info("Application stop.");
    }

    public static void main(String[] args)
    {
        launch();
    }
}