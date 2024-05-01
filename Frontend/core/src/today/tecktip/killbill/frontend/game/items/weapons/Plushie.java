package today.tecktip.killbill.frontend.game.items.weapons;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.items.Item;
import today.tecktip.killbill.frontend.game.objects.Entity;

import com.badlogic.gdx.Gdx;

import today.tecktip.killbill.common.exceptions.MessageFailure;
import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.gameserver.games.GameType;
import today.tecktip.killbill.common.gameserver.messages.MessageDataType;
import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.frontend.game.objects.explosives.Bomb;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicBombCommand.BasicBombCommandData;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicBombCommand.BombType;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicBombCommand.CreateBombContext;
import today.tecktip.killbill.frontend.screens.GameScreen;

/**
 * A Kill Bill 1 classic.
 * @author cs
 */
public class Plushie extends Item {

    /**
     * Creates a new plushie.
     */
    public Plushie() {
        super(
            ItemType.PLUSHIE,
            "Plushie",
            KillBillGame.get().getTextureLoader().get("objects_explosives_plushie"),
            KillBillGame.get().getTextureLoader().get("items_plushie"),
            "items_plushie",
            1,
            0.1f
        );
    }

    @Override
    public boolean use(final Entity user) {
        if (user != KillBillGame.get().getPlayer()) return false;

        // Make a new bomb
        final CreateBombContext context = new CreateBombContext(
            new Coordinates(
                user.getRectangle().getX() / GlobalGameConfig.GRID_SIZE,
                user.getRectangle().getY() / GlobalGameConfig.GRID_SIZE
            ),
            BombType.PLUSHIE);

        // Send it out over UDP
        try {
            KillBillGame.get().getUdpClient().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_CREATE_BOMB)
                .run(KillBillGame.get().getUdpClient(), context);
        } catch (final MessageFailure e) {
            Gdx.app.error(getClass().getSimpleName(), "Failed to send bomb launch over UDP.", e);
            return true;
        }

        // Turn it into a Bomb object
        ((GameScreen) KillBillGame.get().getCurrentScreen()).getGameRenderer().addObject(
            new Bomb(
                new BasicBombCommandData(
                    MessageDataType.COMMAND_CREATE_BOMB,
                    context.getType(),
                    context.getOrigin()
                ),
                true
            )
        );

        // Remove self from the player's inventory
        KillBillGame.get().getPlayer().removeItem(KillBillGame.get().getPlayer().getIndexOfItem(this));

        return true;
    }
}
