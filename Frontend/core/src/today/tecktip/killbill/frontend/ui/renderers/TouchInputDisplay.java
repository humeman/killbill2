package today.tecktip.killbill.frontend.ui.renderers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.KillBillGame.InputType;
import today.tecktip.killbill.frontend.config.Keybinds.KeyType;
import today.tecktip.killbill.frontend.screens.game.UdpGameScreen;
import today.tecktip.killbill.frontend.ui.Location;
import today.tecktip.killbill.frontend.ui.Size;
import today.tecktip.killbill.frontend.ui.elements.Button;

/**
 * An extension to the UiRenderer (a package of elements that can be
 *  added to or removed from a UiRenderer).
 * @author andres lopzpopezeloplesz
 */
public class TouchInputDisplay extends RendererExtension {


    /**
     * Creates a new debug display.
     */
    public TouchInputDisplay() {
        super(true);

        Button upBtn = Button.newBuilder()
            .setOnPress(
                () -> {
                        KillBillGame.get().getPlayer().setMovementType(KeyType.UP);
                }
            )
            .setOnUnpress(
                () -> {
                        KillBillGame.get().getPlayer().removeMovementType(KeyType.UP);
                        KillBillGame.get().getPlayer().removeMovementType(KeyType.SPRINT);
                }
            )
                .setTexture("ui_hud_up")
                .setSize(new Size.XScaledSize(.1f,1))
                .setLocation(new Location.ScaledLocation(.15f,.28f))
            .build();

        Button rightBtn = Button.newBuilder()
                .setOnPress(
                        () -> {
                            KillBillGame.get().getPlayer().setMovementType(KeyType.RIGHT);
                        }
                )
                .setOnUnpress(
                        () -> {
                            KillBillGame.get().getPlayer().removeMovementType(KeyType.RIGHT);
                            KillBillGame.get().getPlayer().removeMovementType(KeyType.SPRINT);
                        }
                )
                .setTexture("ui_hud_right")
                .setSize(new Size.XScaledSize(.1f,1))
                .setLocation(new Location.ScaledLocation(.27f,.16f))
                .build();

        Button leftBtn = Button.newBuilder()
                .setOnPress(
                () -> {
                    KillBillGame.get().getPlayer().setMovementType(KeyType.LEFT);
                }
        )
                .setOnUnpress(
                        () -> {
                            KillBillGame.get().getPlayer().removeMovementType(KeyType.LEFT);
                            KillBillGame.get().getPlayer().removeMovementType(KeyType.SPRINT);
                        }
                )
                .setTexture("ui_hud_left")
                .setSize(new Size.XScaledSize(.1f,1))
                .setLocation(new Location.ScaledLocation(.03f,.16f))
                .build();

        Button downBtn = Button.newBuilder()
                .setOnPress(
                        () -> {
                            KillBillGame.get().getPlayer().setMovementType(KeyType.DOWN);
                        }
                )
                .setOnUnpress(
                        () -> {
                            KillBillGame.get().getPlayer().removeMovementType(KeyType.DOWN);
                            KillBillGame.get().getPlayer().removeMovementType(KeyType.SPRINT);
                        }
                )
                .setTexture("ui_hud_down")
                .setSize(new Size.XScaledSize(.1f,1))
                .setLocation(new Location.ScaledLocation(.15f,.04f))
                .build();

        Button sprintBtn = Button.newBuilder()
                .setOnPress(
                        () -> {
                            KillBillGame.get().getPlayer().setMovementType(KeyType.SPRINT);
                        }
                )
                .setTexture("ui_hud_sprint")
                .setSize(new Size.XScaledSize(.1f,1))
                .setLocation(new Location.ScaledLocation(.88f,.02f))
                .build();

        Button chatBtn = Button.newBuilder()
                .setOnPress(
                        () -> {
                            ((UdpGameScreen) KillBillGame.get().getCurrentScreen()).enableChat();
                        }
                )
                .setTexture("ui_hud_chat")
                .setSize(new Size.XScaledSize(.05f,1))
                .setLocation(new Location.ScaledLocation(.94f,.85f))
                .build();

        Button pauseBtn = Button.newBuilder()
                .setOnPress(
                        () -> {
                                UdpGameScreen gScreen = (UdpGameScreen) KillBillGame.get().getCurrentScreen();
                                if (gScreen.isPaused()) gScreen.unpause();
                                else gScreen.pause();
                        }
                )
                .setTexture("ui_hud_pause")
                .setSize(new Size.XScaledSize(.05f,1))
                .setLocation(new Location.ScaledLocation(.87f,.85f))
                .build();

        Button interactBtn = Button.newBuilder()
                .setOnPress(
                        () -> {
                            KillBillGame.get().getPlayer().interact();
                        }
                )
                .setTexture("ui_hud_interact")
                .setSize(new Size.XScaledSize(.1f,1))
                .setLocation(new Location.ScaledLocation(.89f,.28f))
                .build();

        Button useBtn = Button.newBuilder()
                .setOnPress(
                        () -> {
                            KillBillGame.get().getPlayer().use();
                        }
                )
                .setTexture("ui_hud_use")
                .setSize(new Size.XScaledSize(.1f,1))
                .setLocation(new Location.ScaledLocation(.75f,.08f))
                .build();

        elements.add(upBtn);
        elements.add(rightBtn);
        elements.add(leftBtn);
        elements.add(downBtn);
        elements.add(sprintBtn);
        elements.add(chatBtn);
        elements.add(pauseBtn);
        elements.add(interactBtn);
        elements.add(useBtn);

    }

    @Override
    public void render(final SpriteBatch batch, final float delta) {
        if (KillBillGame.get().getInputType().equals(InputType.KEYBOARD_MOUSE)) setEnabled(false);
        else setEnabled(true);

        super.render(batch, delta);
    }
}
