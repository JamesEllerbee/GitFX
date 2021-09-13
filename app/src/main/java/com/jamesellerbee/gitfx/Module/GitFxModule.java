package com.jamesellerbee.gitfx.Module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.jamesellerbee.gitfx.Engines.CommandEngine;
import com.jamesellerbee.gitfx.Interfaces.ICommandEngine;

/**
 * Defines a module for dependency injection using {@link com.google.inject.Guice}
 */
public class GitFxModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        super.configure();
        bind(ICommandEngine.class)
                .to(CommandEngine.class)
                .in(Scopes.SINGLETON);
    }
}
