package xyz.wagyourtail.jsmacros.core;

import org.apache.logging.log4j.Logger;
import xyz.wagyourtail.SynchronizedWeakHashSet;
import xyz.wagyourtail.jsmacros.core.config.BaseProfile;
import xyz.wagyourtail.jsmacros.core.config.ConfigManager;
import xyz.wagyourtail.jsmacros.core.config.CoreConfigV2;
import xyz.wagyourtail.jsmacros.core.config.ScriptTrigger;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.event.BaseEventRegistry;
import xyz.wagyourtail.jsmacros.core.extensions.Extension;
import xyz.wagyourtail.jsmacros.core.extensions.ExtensionLoader;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.BaseScriptContext;
import xyz.wagyourtail.jsmacros.core.language.BaseWrappedException;
import xyz.wagyourtail.jsmacros.core.language.EventContainer;
import xyz.wagyourtail.jsmacros.core.library.LibraryRegistry;
import xyz.wagyourtail.jsmacros.core.service.ServiceManager;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Core<T extends BaseProfile, U extends BaseEventRegistry> {
    /**
     * static reference to instance created by {@link #createInstance(Function, Function, File, File, Logger)}
     */
    private static Core<?, ?> instance;

    public final LibraryRegistry libraryRegistry = new LibraryRegistry();
    public final BaseEventRegistry eventRegistry;

    public final T profile;
    public final ConfigManager config;
    private final Set<BaseScriptContext<?>> contexts = new SynchronizedWeakHashSet<>();

    public final ServiceManager services;

    private boolean deferredInit = false;

    protected Core(Function<Core<T, U>, U> eventRegistryFunction, Function<Core<T, U>, T> profileFunction, File configFolder, File macroFolder, Logger logger) {
        instance = this;
        eventRegistry = eventRegistryFunction.apply(this);
        config = new ConfigManager(configFolder, macroFolder, logger);
        profile = profileFunction.apply(this);
        this.services = new ServiceManager(this);
    }

    /**
     * static reference to instance created by {@link #createInstance(Function, Function, File, File, Logger)}
     */
    public static Core<?, ?> getInstance() {
        return instance;
    }

    public void deferredInit() {
        if (deferredInit) {
            throw new RuntimeException("deferredInit has already ran!");
        }
        instance.profile.init(instance.config.getOptions(CoreConfigV2.class).defaultProfile);
        instance.services.load();
        deferredInit = true;
    }

    /**
     * @param container
     */
    public void addContext(EventContainer<?> container) {
        contexts.add(container.getCtx());
    }

    /**
     * @return
     */
    public Set<BaseScriptContext<?>> getContexts() {
        return contexts;
    }

    /**
     * start by running this function, supplying implementations of {@link BaseEventRegistry} and {@link BaseProfile} and a {@link Supplier} for
     * creating the config manager with the folder paths it needs.
     *
     * @param eventRegistryFunction
     * @param profileFunction
     *
     * @param configFolder
     * @param macroFolder
     * @param logger
     * @return
     */
    public static <V extends BaseProfile, R extends BaseEventRegistry> Core<V, R> createInstance(Function<Core<V, R>, R> eventRegistryFunction, Function<Core<V, R>, V> profileFunction, File configFolder, File macroFolder, Logger logger) {
        if (instance != null) throw new RuntimeException("Can't declare RunScript instance more than once");

        new Core<>(eventRegistryFunction, profileFunction, configFolder, macroFolder, logger);

        return (Core<V, R>) instance;
    }

    /**
     * executes an {@link BaseEvent Event} on a ${@link ScriptTrigger}
     * @param macro
     * @param event
     *
     * @return
     */
    public EventContainer<?> exec(ScriptTrigger macro, BaseEvent event) {
        return exec(macro, event, null, null);
    }

    /**
     * Executes an {@link BaseEvent Event} on a ${@link ScriptTrigger} with callback.
     * @param macro
     * @param event
     * @param then
     * @param catcher
     *
     * @return
     */
    public EventContainer<?> exec(ScriptTrigger macro, BaseEvent event, Runnable then,
                                  Consumer<Throwable> catcher) {
        BaseLanguage<?> l = ExtensionLoader.getExtensionForFileName(macro.getScriptFile()).getLanguage(this);
        return l.trigger(macro, event, then, catcher);
    }

    /**
     * @since 1.7.0
     * @param lang
     * @param script
     * @param fakeFile
     * @param event
     * @param then
     * @param catcher
     * @return
     */
    public EventContainer<?> exec(String lang, String script, File fakeFile, BaseEvent event, Runnable then, Consumer<Throwable> catcher) {
        BaseLanguage<?> l = ExtensionLoader.getExtensionForName(lang).getLanguage(this);
        return l.trigger(script, fakeFile, event, then, catcher);
    }

    /**
     * wraps an exception for more uniform parsing between languages, also extracts useful info.
     * @param ex exception to wrap.
     *
     * @return
     */
    public BaseWrappedException<?> wrapException(Throwable ex) {
        if (ex == null) return null;
        for (Extension lang : ExtensionLoader.getAllExtensions()) {
            BaseWrappedException<?> e = lang.wrapException(ex);
            if (e != null) return e;
        }
        Iterator<StackTraceElement> elements = Arrays.stream(ex.getStackTrace()).iterator();
        String message = ex.getClass().getSimpleName();
        String intMessage = ex.getMessage();
        if (intMessage != null) {
            message += ": " + intMessage;
        }
        return new BaseWrappedException<>(ex, message, null, elements.hasNext() ? wrapHostInternal(elements.next(), elements) : null);
    }
    
    private BaseWrappedException<StackTraceElement> wrapHostInternal(StackTraceElement e, Iterator<StackTraceElement> elements) {
        return BaseWrappedException.wrapHostElement(e, elements.hasNext() ? wrapHostInternal(elements.next(), elements) : null);
    }
    
    private static class SortLanguage implements Comparator<BaseLanguage<?>> {

        @Override
        public int compare(BaseLanguage a, BaseLanguage b) {
            final String[] as = a.extension.replaceAll("\\.", " ").trim().split(" ");
            final String[] bs = b.extension.replaceAll("\\.", " ").trim().split(" ");
            final int lendif = bs.length-as.length;
            if (lendif != 0) return lendif;
            int comp = 0;
            for (int i = bs.length - 1; i >= 0; --i) {
                comp = as[i].compareTo(bs[i]);
                if (comp != 0) break;
            }
            return comp;
        }
        
    }
}
