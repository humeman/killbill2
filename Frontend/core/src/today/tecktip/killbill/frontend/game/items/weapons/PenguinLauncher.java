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
import today.tecktip.killbill.frontend.game.objects.explosives.Projectile;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicProjectileCommand.BasicProjectileCommandData;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicProjectileCommand.CreateProjectileContext;
import today.tecktip.killbill.frontend.gameserver.game.basic.commands.BasicProjectileCommand.ProjectileType;
import today.tecktip.killbill.frontend.screens.GameScreen;

/**
 * A Kill Bill 1 classic.
 * @author cs
 */
public class PenguinLauncher extends Item {

    /**
     * Creates a new penguin launcher.
     */
    public PenguinLauncher() {
        super(
            ItemType.PENGUIN_LAUNCHER,
            "Penguin Launcher",
            KillBillGame.get().getTextureLoader().get("items_penguin_launcher"),
            KillBillGame.get().getTextureLoader().get("items_penguin_launcher_held"),
            "items_penguin_launcher_held",
            1,
            5f
        );
    }

    @Override
    public boolean use(final Entity user) {
        if (user != KillBillGame.get().getPlayer()) return false;

        // Make a new projectile
        final CreateProjectileContext context = new CreateProjectileContext(
            new Coordinates(
                user.getRectangle().getX() / GlobalGameConfig.GRID_SIZE,
                user.getRectangle().getY() / GlobalGameConfig.GRID_SIZE
            ),
            ProjectileType.PENGUIN,
            user.getRotation());

        // Send it out over UDP
        try {
            KillBillGame.get().getUdpClient().getCommandLoader().invokeMethodFor(GameType.BASIC, MessageDataType.COMMAND_CREATE_PROJECTILE)
                .run(KillBillGame.get().getUdpClient(), context);
        } catch (final MessageFailure e) {
            Gdx.app.error(getClass().getSimpleName(), "Failed to send projectile launch over UDP.", e);
            return true;
        }

        // Turn it into a Projectile object
        ((GameScreen) KillBillGame.get().getCurrentScreen()).getGameRenderer().addObject(
            new Projectile(
                new BasicProjectileCommandData(
                    MessageDataType.COMMAND_CREATE_PROJECTILE,
                    context.getType(),
                    context.getDirection(),
                    context.getOrigin()
                ),
                true
            )
        );

        return true;
    }
}
