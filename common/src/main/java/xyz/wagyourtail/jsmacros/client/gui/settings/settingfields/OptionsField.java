package xyz.wagyourtail.jsmacros.client.gui.settings.settingfields;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import xyz.wagyourtail.jsmacros.client.gui.settings.SettingsOverlay;
import xyz.wagyourtail.jsmacros.client.gui.settings.settingcontainer.AbstractSettingContainer;
import xyz.wagyourtail.wagyourgui.BaseScreen;
import xyz.wagyourtail.wagyourgui.elements.Button;
import xyz.wagyourtail.wagyourgui.overlays.SelectorDropdownOverlay;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

public class OptionsField extends AbstractSettingField<Object> {

    public OptionsField(int x, int y, int width, FontRenderer textRenderer, AbstractSettingContainer parent, SettingsOverlay.SettingField<Object> field) {
        super(x, y, width, textRenderer.fontHeight + 2, textRenderer, parent, field);
    }

    @Override
    public void init() {
        super.init();
        try {
            List<Object> values = setting.getOptions();
            List<IChatComponent> textvalues = values.stream().map(e -> new ChatComponentText(e.toString())).collect(Collectors.toList());
            this.addButton(new Button(x + width / 2, y, width / 2, height, textRenderer, 0, 0xFF000000, 0x7FFFFFFF, 0xFFFFFF, new ChatComponentText(setting.get().toString()), (btn) -> {
                getFirstOverlayParent().openOverlay(new SelectorDropdownOverlay(x + width / 2, y, width / 2, values.size() * (textRenderer.fontHeight + 1) + 4, textvalues, textRenderer, getFirstOverlayParent(), (choice) -> {
                    btn.setMessage(textvalues.get(choice));
                    try {
                        setting.set(values.get(choice));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }));
            }));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPos(int x, int y, int width, int height) {
        super.setPos(x, y, width, height);
        for (GuiButton btn : buttons) {
            btn.y = y;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        textRenderer.draw(BaseScreen.trimmed(textRenderer, settingName.asFormattedString(), width / 2), x, y + 1, 0xFFFFFF);
    }

}
