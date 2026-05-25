import net.minecraft.world.item.ItemStack;
import java.lang.reflect.Method;
public class Dump {
    public static void main(String[] args) {
        for(Method m : ItemStack.class.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + java.util.Arrays.toString(m.getParameterTypes()));
        }
    }
}
