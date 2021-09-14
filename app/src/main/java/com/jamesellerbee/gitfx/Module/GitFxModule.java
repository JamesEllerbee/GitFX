package com.jamesellerbee.gitfx.Module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.jamesellerbee.gitfx.Engines.CommandEngine;
import com.jamesellerbee.gitfx.Interfaces.IAppPropertyProvider;
import com.jamesellerbee.gitfx.Interfaces.ICommandEngine;
import com.jamesellerbee.gitfx.Utilities.AppPropertyProvider;

/**
 * Defines a module for dependency injection using {@link com.google.inject.Guice}
 */
public class GitFxModule extends AbstractModule
{
    private String propertyPath;

    public GitFxModule()
    {
        this("gitfx.properties");
    }

    public GitFxModule(String customPropertiesPath)
    {
        propertyPath = customPropertiesPath;
    }

    @Override
    protected void configure()
    {
        super.configure();
        bind(ICommandEngine.class)
                .to(CommandEngine.class)
                .in(Scopes.SINGLETON);

        bind(IAppPropertyProvider.class)
                .toInstance(new AppPropertyProvider(propertyPath));
    }
}
