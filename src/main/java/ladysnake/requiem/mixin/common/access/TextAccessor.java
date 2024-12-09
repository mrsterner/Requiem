package ladysnake.requiem.mixin.common.access;

import com.google.gson.JsonElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Text.Serialization.class)
public interface TextAccessor {

    @Invoker("fromJson")
    public static MutableText getFromJson(JsonElement json, RegistryWrapper.WrapperLookup registries) {
        throw new AssertionError();
    };

    @Invoker("toJson")
    public static JsonElement getToJson(Text text, RegistryWrapper.WrapperLookup registries) {
        throw new AssertionError();
    };
}
