package xyz.wagyourtail.jsmacros.macros;

import java.util.HashMap;

import xyz.wagyourtail.jsmacros.config.RawMacro;
import xyz.wagyourtail.jsmacros.runscript.RunScript;

public abstract class BaseMacro {
    private final RawMacro macro;
    
    public BaseMacro(RawMacro macro) {
        this.macro = macro;
    }
    
    public RawMacro getRawMacro() {
        return macro;
    }
    
    public Thread runMacro(String type, HashMap<String, Object> args) {
        if (macro.enabled) {
            try {
                return RunScript.exec(macro, type, args);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } 
        return null;
    }
    
    public abstract Thread trigger(String type, HashMap<String, Object> args);
}