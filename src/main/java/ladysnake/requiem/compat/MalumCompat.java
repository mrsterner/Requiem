package ladysnake.requiem.compat;

import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;

public class MalumCompat implements RequiemPlugin {

    @CalledThroughReflection
    public static void init() {
        /*TODO malum also has a soulless, allow them too
         PossessionStartCallback.EVENT.register(Requiem.id("soulless"), (target, possessor, simulate) -> {
            if (SoulHolderComponent.isSoulless(target)) {
                return PossessionStartCallback.Result.ALLOW;
            }
            return PossessionStartCallback.Result.PASS;
        });
         */
    }
}
