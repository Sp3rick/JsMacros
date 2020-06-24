package xyz.wagyourtail.jsmacros.macros;

import java.util.HashMap;

import net.minecraft.client.util.InputUtil;
import xyz.wagyourtail.jsmacros.config.RawMacro;

public class KeyUpMacro extends BaseMacro {
    private InputUtil.Key key;
    private boolean prevKeyState = false;
    
    public KeyUpMacro(RawMacro macro) {
        super(macro);
        key = InputUtil.fromTranslationKey(macro.eventkey);
    }
    
    public void setKey(InputUtil.Key setkey) {
        key = setkey;
    }
    
    public String getKey() {
        return key.getTranslationKey();
    }
    
    @Override
    public Thread trigger(String type, HashMap<String, Object> args) {
        if (check(args)) {
            return runMacro(type, args);
        }
        return null;
    }
    
    private boolean check(HashMap<String, Object> args) {
        boolean keyState = false;
        if ((int)args.get("action") > 0) keyState = true;
        if ((InputUtil.Key)args.get("rawkey") == key)
            if (keyState && !prevKeyState) {
                prevKeyState = true;
                return false;
            } else if (!keyState && prevKeyState) {
                prevKeyState = false;
                return true;
            }
        return false;
    }
}