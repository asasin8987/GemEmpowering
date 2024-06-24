import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.faris.first.block.self.screen.GemEmpoweringStationSidedMenu;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU = DeferredRegister.create(ForgeRegistries.MENU_TYPES, FirstMod.MODID);

    public static final RegistryObject<MenuType<GemEmpoweringStationMenu>> GEM_EMPOWERING_STATION_MENU =
                        registerMenuTypes(GemEmpoweringStationMenu::new, "gem_empowering_station_menu");
    public static final RegistryObject<MenuType<GemEmpoweringStationSidedMenu>> GEM_EMPOWERING_STATION_SIDED_MENU =
            registerMenuTypes(GemEmpoweringStationSidedMenu::new, "gem_empowering_station_sided_menu");
    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuTypes (IContainerFactory<T> factory, String name)
    {
        return MENU.register(name, ()-> IForgeMenuType.create(factory));
    }
    public static void register(IEventBus bus)
    {
        MENU.register(bus);
    }
}
