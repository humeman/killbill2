package today.tecktip.killbill.common.maploader;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import today.tecktip.killbill.common.maploader.directives.ChestDirective;
import today.tecktip.killbill.common.maploader.directives.EntityDirective;
import today.tecktip.killbill.common.maploader.directives.ObjectDirective;
import today.tecktip.killbill.common.maploader.directives.EntityDirective.EntityType;
import today.tecktip.killbill.common.maploader.directives.RoomDirective;
import today.tecktip.killbill.common.maploader.directives.TileDirective;

/**
 * Tests for the map loader.
 * @author cs
 */
public class MapLoaderTest {
    
    @Test
    public void testMapLoad() {
        final KillBillMap map = MapLoader.load(List.of(getClass().getResourceAsStream("/test.kbmap")));

        assertEquals("Test Map", map.getDisplayName());

        // Iterate through items, as we expect them, one by one
        final List<MapDirective> directives = map.getDirectives();

        // Room
        assertInstanceOf(RoomDirective.class, directives.get(0));
        RoomDirective room = (RoomDirective) directives.get(0);
        assertAll(
            () -> { assertEquals(0, room.getLocation().x()); },
            () -> { assertEquals(1, room.getLocation().y()); },
            () -> { assertEquals(10, room.getSize().x()); },
            () -> { assertEquals(11, room.getSize().y()); },
            () -> { assertEquals("walls_white", room.getWallTexture()); },
            () -> { assertEquals("floors_tile", room.getFloorTexture()); },
            () -> { assertEquals(2, room.getWallExclusions().size()); }
        );

        // Entity
        assertInstanceOf(EntityDirective.class, directives.get(2));
        EntityDirective entity = (EntityDirective) directives.get(2);
        assertAll(
            () -> { assertEquals(12, entity.getLocation().x()); },
            () -> { assertEquals(3, entity.getLocation().y()); },
            () -> { assertEquals(EntityType.CLAYMORE_ROOMBA, entity.getEntityType()); },
            () -> { assertEquals(1, entity.getSize().x()); },
            () -> { assertEquals(1, entity.getSize().y()); }
        );

        // Tile
        assertInstanceOf(TileDirective.class, directives.get(3));
        TileDirective tile = (TileDirective) directives.get(3);
        assertAll(
            () -> { assertEquals(3, tile.getLocations().size()); },
            () -> { assertEquals(7, tile.getLocations().get(0).x()); },
            () -> { assertEquals(7, tile.getLocations().get(0).y()); },
            () -> { assertEquals(8, tile.getLocations().get(1).x()); },
            () -> { assertEquals(8, tile.getLocations().get(1).y()); },
            () -> { assertEquals(9, tile.getLocations().get(2).x()); },
            () -> { assertEquals(9, tile.getLocations().get(2).y()); },
            () -> { assertEquals(1, tile.getFlags().size()); },
            () -> { assertEquals(ObjectFlag.SOLID, tile.getFlags().get(0)); },
            () -> { assertEquals(1, tile.getSize().x()); },
            () -> { assertEquals(2, tile.getSize().y()); },
            () -> { assertEquals("objects_desk_1_2", tile.getTexture()); }
        );

        // Object
        assertInstanceOf(ObjectDirective.class, directives.get(4));
        ObjectDirective o = (ObjectDirective) directives.get(4);
        assertAll(
            () -> { assertEquals(1, o.getLocations().size()); },
            () -> { assertEquals(7 + 1d/3, o.getLocations().get(0).x()); },
            () -> { assertEquals(8 - 1d/4, o.getLocations().get(0).y()); },
            () -> { assertEquals(1d/2, o.getSize().x()); },
            () -> { assertEquals(1d/2, o.getSize().y()); },
            () -> { assertEquals("objects_mug", o.getTexture()); }
        );

        // Chest
        assertInstanceOf(ChestDirective.class, directives.get(5));
        ChestDirective chest = (ChestDirective) directives.get(5);
        assertAll(
            () -> { assertEquals(1, chest.getLocation().x()); },
            () -> { assertEquals(1, chest.getLocation().y()); },
            () -> { assertEquals(1, chest.getSize().x()); },
            () -> { assertEquals(1, chest.getSize().y()); },
            () -> { assertEquals("objects_briefcase", chest.getTexture()); },
            () -> { assertEquals("objects_briefcase_open", chest.getOpenTexture()); },
            () -> { assertEquals(4, chest.getLootTable().size()); },
            () -> { assertEquals(50, chest.getLootTable().get(0).chance()); },
            () -> { assertEquals(ItemType.SWORD, chest.getLootTable().get(0).type()); },
            () -> { assertEquals(25, chest.getLootTable().get(1).chance()); },
            () -> { assertEquals(ItemType.SPEAR, chest.getLootTable().get(1).type()); },
            () -> { assertEquals(20, chest.getLootTable().get(2).chance()); },
            () -> { assertEquals(ItemType.HEALTH_POTION, chest.getLootTable().get(2).type()); },
            () -> { assertEquals(5, chest.getLootTable().get(3).chance()); },
            () -> { assertEquals(ItemType.AXE, chest.getLootTable().get(3).type()); }
        );

        // One-liner
        assertInstanceOf(EntityDirective.class, directives.get(7));
        EntityDirective entity1 = (EntityDirective) directives.get(7);
        assertAll(
            () -> { assertEquals(1, entity1.getLocation().x()); },
            () -> { assertEquals(1, entity1.getLocation().y()); },
            () -> { assertEquals(EntityType.EMPLOYEE, entity1.getEntityType()); }
        );

    }

    @Test
    public void testLootTable() {
        final KillBillMap map = MapLoader.load(List.of(getClass().getResourceAsStream("/loottable.kbmap")));

        final Random random = new Random(0);

        map.forEachDirectiveOfType(
            chest -> {
                for (int i = 0; i <= 148; i++) {
                    ItemType item = chest.rollLootTable(random);
                    if (i == 0) assertEquals(ItemType.SPEAR, item);
                    if (i == 1) assertEquals(ItemType.SWORD, item);
                    if (i == 4) assertEquals(ItemType.HEALTH_POTION, item);
                    if (i == 148) assertEquals(ItemType.AXE, item);
                }
            },
            ChestDirective.class);
    }
}
