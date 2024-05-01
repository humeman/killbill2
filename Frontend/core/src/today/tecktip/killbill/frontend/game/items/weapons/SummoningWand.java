package today.tecktip.killbill.frontend.game.items.weapons;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.items.Item;
import today.tecktip.killbill.frontend.game.objects.Entity;

import java.util.Random;

import com.badlogic.gdx.Gdx;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.games.BasicGameConfig.BasicPlayerType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.common.maploader.directives.EntityDirective.EntityType;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicChangeEntityStateCommand.SummonEntityInvokeContext;

/**
 * Lets bill summon Employees and ClaymoreRoombas.
 * @author cs
 */
public class SummoningWand extends Item {

    private static final Random RANDOM = new Random();

    /**
     * Creates a new penguin launcher.
     */
    public SummoningWand() {
        super(
            ItemType.SUMMONING_WAND,
            "Summoning Wand",
            KillBillGame.get().getTextureLoader().get("items_summoning_wand"),
            KillBillGame.get().getTextureLoader().get("items_summoning_wand_held"),
            "items_summoning_wand_held",
            1,
            20f
        );
    }

    @Override
    public boolean use(final Entity user) {
        if (user != KillBillGame.get().getPlayer()) return false;
        if (!KillBillGame.get().getPlayer().getUserState().getPlayerType().equals(BasicPlayerType.BILL)) return false;

        // Choose an entity: 75% employee, 25% roomba
        EntityType type = RANDOM.nextInt(3) == 0 ? EntityType.CLAYMORE_ROOMBA : EntityType.EMPLOYEE;

        // Send it out over UDP
        try {
            KillBillGame.get().getUdpClient().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_SUMMON_ENTITY)
                .run(
                    KillBillGame.get().getUdpClient(),
                    new SummonEntityInvokeContext(
                        new Coordinates(
                            user.getRectangle().getLocation().getX() / GlobalGameConfig.GRID_SIZE,
                            user.getRectangle().getLocation().getY() / GlobalGameConfig.GRID_SIZE
                        ),
                        type
                    )
                );
        } catch (final MessageFailure e) {
            Gdx.app.error(getClass().getSimpleName(), "Failed to send projectile launch over UDP.", e);
            return true;
        }

        return true;
    }
}
