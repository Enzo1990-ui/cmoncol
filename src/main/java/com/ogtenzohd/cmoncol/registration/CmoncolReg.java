package com.ogtenzohd.cmoncol.registration;

import com.ogtenzohd.cmoncol.CobblemonColonies;
import com.ogtenzohd.cmoncol.blocks.custom.pasture.PastureBlock;
import com.ogtenzohd.cmoncol.blocks.custom.pasture.PastureBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.daycare.DaycareBlock;
import com.ogtenzohd.cmoncol.blocks.custom.daycare.DaycareBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.traineracadamy.TrainerAcadamyBlock;
import com.ogtenzohd.cmoncol.blocks.custom.traineracadamy.TrainerAcadamyBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.harvester.HarvesterBlock;
import com.ogtenzohd.cmoncol.blocks.custom.harvester.HarvesterBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.pokeballworkshop.PokeballWorkshopBlock;
import com.ogtenzohd.cmoncol.blocks.custom.pokeballworkshop.PokeballWorkshopBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.pokecenter.PokeCenterBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.pokecenter.PokeCenterBlock;
import com.ogtenzohd.cmoncol.blocks.custom.sciencelab.ScienceLabBlock;
import com.ogtenzohd.cmoncol.blocks.custom.sciencelab.ScienceLabBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.pokemonguard.PokemonGuardBuildingBlock;
import com.ogtenzohd.cmoncol.blocks.custom.pokemonguard.PokemonGuardBuildingBlockEntity;
import com.ogtenzohd.cmoncol.blocks.custom.gym.GymBlock;
import com.ogtenzohd.cmoncol.blocks.custom.gym.GymBlockEntity;
import com.ogtenzohd.cmoncol.items.ItemPokemonEgg;
import com.ogtenzohd.cmoncol.entity.GhostReceptionistEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CmoncolReg {

    public static final String MODID = CobblemonColonies.MODID;

    //REGISTRIES
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID); // NEW REGISTRY

    //BLOCKS
    public static final DeferredHolder<Block, PastureBlock> PASTURE_BLOCK = BLOCKS.register("colony_pasture", () -> new PastureBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(2.5f)));
    public static final DeferredHolder<Block, DaycareBlock> DAYCARE_BLOCK = BLOCKS.register("colony_daycare", () -> new DaycareBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f)));
    public static final DeferredHolder<Block, TrainerAcadamyBlock> TRAINER_ACADAMY_BLOCK = BLOCKS.register("colony_traineracadamy", () -> new TrainerAcadamyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f)));
    public static final DeferredHolder<Block, HarvesterBlock> HARVESTER_BLOCK = BLOCKS.register("colony_harvester", () -> new HarvesterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f)));
    public static final DeferredHolder<Block, PokeballWorkshopBlock> POKEBALLWORKSHOP_BLOCK = BLOCKS.register("colony_pokeballworkshop", () -> new PokeballWorkshopBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f)));
	public static final DeferredHolder<Block, PokeCenterBlock> POKECENTER_BLOCK = BLOCKS.register("colony_pokecenter", () -> new PokeCenterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f)));
    public static final DeferredHolder<Block, ScienceLabBlock> SCIENCELAB_BLOCK = BLOCKS.register("colony_science_lab", () -> new ScienceLabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.5f)));
	public static final DeferredHolder<Block, PokemonGuardBuildingBlock> POKEMON_GUARD_BLOCK = BLOCKS.register("colony_pokemon_guard", () -> new PokemonGuardBuildingBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f)));
    public static final DeferredHolder<Block, GymBlock> GYM_BLOCK = BLOCKS.register("colony_gym", () -> new GymBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f)));

    //ITEMS
    public static final DeferredHolder<Item, BlockItem> PASTURE_ITEM = ITEMS.register("colony_pasture", () -> new BlockItem(PASTURE_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> DAYCARE_ITEM = ITEMS.register("colony_daycare", () -> new BlockItem(DAYCARE_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> TRAINER_ACADAMY_ITEM = ITEMS.register("colony_traineracadamy", () -> new BlockItem(TRAINER_ACADAMY_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> HARVESTER_ITEM = ITEMS.register("colony_harvester", () -> new BlockItem(HARVESTER_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> POKEBALLWORKSHOP_ITEM = ITEMS.register("colony_pokeballworkshop", () -> new BlockItem(POKEBALLWORKSHOP_BLOCK.get(), new Item.Properties()));
	public static final DeferredHolder<Item, BlockItem> POKECENTER_ITEM = ITEMS.register("colony_pokecenter", () -> new BlockItem(POKECENTER_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> SCIENCELAB_ITEM = ITEMS.register("colony_science_lab", () -> new BlockItem(SCIENCELAB_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> POKEMON_GUARD_ITEM = ITEMS.register("colony_pokemon_guard", () -> new BlockItem(POKEMON_GUARD_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> GYM_ITEM = ITEMS.register("colony_gym", () -> new BlockItem(GYM_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, ItemPokemonEgg> POKEMON_EGG = ITEMS.register("pokemon_egg", () -> new ItemPokemonEgg(new Item.Properties()));

    //BLOCK ENTITIES
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PastureBlockEntity>> PASTURE_BE = BLOCK_ENTITIES.register("colony_pasture", () -> BlockEntityType.Builder.of(PastureBlockEntity::new, PASTURE_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DaycareBlockEntity>> DAYCARE_BE = BLOCK_ENTITIES.register("colony_daycare", () -> BlockEntityType.Builder.of(DaycareBlockEntity::new, DAYCARE_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrainerAcadamyBlockEntity>> TRAINER_ACADAMY_BE = BLOCK_ENTITIES.register("colony_traineracadamy", () -> BlockEntityType.Builder.of(TrainerAcadamyBlockEntity::new, TRAINER_ACADAMY_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HarvesterBlockEntity>> HARVESTER_BE = BLOCK_ENTITIES.register("colony_harvester", () -> BlockEntityType.Builder.of(HarvesterBlockEntity::new, HARVESTER_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PokeballWorkshopBlockEntity>> POKEBALLWORKSHOP_BE = BLOCK_ENTITIES.register("colony_pokeballworkshop", () -> BlockEntityType.Builder.of(PokeballWorkshopBlockEntity::new, POKEBALLWORKSHOP_BLOCK.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PokeCenterBlockEntity>> POKECENTER_BE = BLOCK_ENTITIES.register("colony_pokecenter", () -> BlockEntityType.Builder.of(PokeCenterBlockEntity::new, POKECENTER_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ScienceLabBlockEntity>> SCIENCELAB_BE = BLOCK_ENTITIES.register("colony_science_lab", () -> BlockEntityType.Builder.of(ScienceLabBlockEntity::new, SCIENCELAB_BLOCK.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PokemonGuardBuildingBlockEntity>> POKEMON_GUARD_BE = BLOCK_ENTITIES.register("colony_pokemon_guard", () -> BlockEntityType.Builder.of(PokemonGuardBuildingBlockEntity::new, POKEMON_GUARD_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GymBlockEntity>> GYM_BE = BLOCK_ENTITIES.register("colony_gym", () -> BlockEntityType.Builder.of(GymBlockEntity::new, GYM_BLOCK.get()).build(null));

    //ENTITIES
    public static final DeferredHolder<EntityType<?>, EntityType<GhostReceptionistEntity>> GHOST_RECEPTIONIST = 
        ENTITY_TYPES.register("ghost_receptionist", () -> 
            EntityType.Builder.of(GhostReceptionistEntity::new, MobCategory.MISC)
                .sized(1.2F, 2.2F)
                .build("ghost_receptionist"));

    //CREATIVE TAB
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CMONCOL_TAB = CREATIVE_TABS.register("cmoncol_tab", () -> CreativeModeTab.builder()
            .title(Component.literal("Cobblemon Colonies"))
            .icon(() -> PASTURE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(PASTURE_ITEM.get());
                output.accept(DAYCARE_ITEM.get());
                output.accept(TRAINER_ACADAMY_ITEM.get());
                output.accept(HARVESTER_ITEM.get());
                output.accept(POKEBALLWORKSHOP_ITEM.get());
                output.accept(SCIENCELAB_ITEM.get());
				output.accept(POKEMON_GUARD_ITEM.get());
                output.accept(GYM_ITEM.get());
                output.accept(POKEMON_EGG.get());
            }).build());

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENU_TYPES.register(eventBus);
        CREATIVE_TABS.register(eventBus);
        ENTITY_TYPES.register(eventBus);
    }
}