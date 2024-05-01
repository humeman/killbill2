package today.tecktip.killbill.frontend.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.http.requests.FriendRequests;
import today.tecktip.killbill.frontend.http.requests.FriendRequests.ListFriendsRequestBody;
import today.tecktip.killbill.frontend.http.requests.UserRequests;
import today.tecktip.killbill.frontend.http.requests.data.Friend;
import today.tecktip.killbill.frontend.http.requests.data.User;
import today.tecktip.killbill.frontend.http.requests.data.Friend.FriendState;
import today.tecktip.killbill.frontend.screens.MenuScreen;
import today.tecktip.killbill.frontend.screens.Screens;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Location.ScaledLocation;
import today.tecktip.killbill.frontend.ui.Size.ScaledSize;
import today.tecktip.killbill.frontend.ui.Size.XScaledSize;
import today.tecktip.killbill.frontend.ui.Size.YScaledSize;
import today.tecktip.killbill.frontend.ui.UiElement;
import today.tecktip.killbill.frontend.ui.elements.Button;
import today.tecktip.killbill.frontend.ui.elements.Image;
import today.tecktip.killbill.frontend.ui.elements.Label;
import today.tecktip.killbill.frontend.ui.elements.TextInput;

/**
 * The friend screen where players see their comrades.
 */
public class FriendScreen extends MenuScreen {
    /**
     * Search for a player
     */
    public TextInput search;

    /**
     * Search for a player
     */
    private Label user;

    /**
     * Errors go here
     */
    private Label errorLabel;

    public List<Friend> friends;
    private Map<UUID, User> users;

    private List<UiElement> friendElements;

    private boolean needsReload;

    public Button addFriendButton;

    /**
     * Constructs the friend screen.
     */
    public FriendScreen() {
        super(new Color(0, 0, 0, 1));
        needsReload = false;
        friendElements = new ArrayList<>();
        users = new HashMap<>();
    }

    @Override
    public void onCreate() {
        
        uiRenderer.add(
            Image.newBuilder()
                .setTexture("ui_bg_*")
                .setSize(new YScaledSize(1, 1))
                .setLocation(new FixedLocation(0, 0))
                .setTint(new Color(1, 1, 1, 0.4f))
                .build());

        search = TextInput.newBuilder()
            .setLocation(new ScaledLocation(.05f, .6f))
            .setSize(new ScaledSize(.4f, .1f))
            .setTexture("ui_square")
            .setPlaceholder("username")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(new Color(0, 0, 0, 1))
                    .setScaledSize(36)
            )
            .build();
        uiRenderer.add(search);

        addFriendButton = Button.newBuilder()
            .setLocation(new ScaledLocation(.15f, .4f))
            .setSize(new XScaledSize(.2f, .5f))
            .setTexture("ui_button")
            .setText("add")
            .setOnPress(
                () -> {
                    UserRequests.getUser(
                        new UserRequests.GetUserRequestBody(null, search.getText()),
                        response -> {
                            FriendRequests.createFriend(
                                    new FriendRequests.CreateFriendRequestBody(response.user().id()),
                                    acceptResponse -> {
                                        refreshFriends();
                                    },
                                    error -> {
                                        Gdx.app.error(FriendScreen.class.getSimpleName(), "API error", error);
                                        errorLabel.setText("error: failed to invite friend");
                                    }
                            );
                        },
                        error -> {
                            Gdx.app.error(FriendScreen.class.getSimpleName(), "API error", error);
                            errorLabel.setText("error: couldn't find user");
                        }
                    );
                }
            )
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(48)
            )
            .build();
        uiRenderer.add(addFriendButton);

        search.setSubmit(addFriendButton);

        uiRenderer.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0.2f, 0.1f))
                .setSize(new XScaledSize(0.1f, 1f))
                .setTexture("ui_button_back")
                .setOnPress(() -> {
                    KillBillGame.get().changeScreen(Screens.USER_SCREEN);
                })
                .build()
        );

        user = Label.newBuilder()
            .setLocation(new ScaledLocation(.25f, .85f))
            .setCentered(true)
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                    .setScaledSize(56)
            )
            .setText("")
            .build();
        uiRenderer.add(user);
 
        uiRenderer.add(
            Label.newBuilder()
                .setLocation(new ScaledLocation(.775f, .9f))
                .setCentered(true)
                .setText("friends")
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                        .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                        .setScaledSize(54)
                )
                .build());
        
        errorLabel = Label.newBuilder()
            .setLocation(new ScaledLocation(0.5f, 0.15f))
            .setCentered(true)
            .setText("")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                .setColor(GlobalGameConfig.PRIMARY_COLOR.cpy())
                .setScaledSize(32)
            )
            .build();

        uiRenderer.add(errorLabel);
    }

    @Override
    public void drawFirst(float delta) {
        super.drawFirst(delta);

        if (needsReload) {
            for (final UiElement element : friendElements) {
                uiRenderer.remove(element);
            }

            friendElements.clear();

            float yOffset = 0.75f;
            for (Friend friend : friends) {
                User me = KillBillGame.get().getUser();
                User from = friend.fromId().equals(me.id()) ? me : users.get(friend.fromId());
                User to = friend.toId().equals(me.id()) ? me : users.get(friend.toId());
                boolean iInitiated = from.id().equals(me.id());
                User them = iInitiated ? to : from;

                float xOffset = 0.55f;

                // Chat button?
                if (friend.state().equals(FriendState.FRIENDS)) {
                    // Yep.
                    friendElements.add(
                        Button.newBuilder()
                        .setLocation(new ScaledLocation(xOffset, yOffset))
                        .setSize(new XScaledSize(0.0725f * 0.75f, 1f))
                        .setTexture("ui_mini_mini_button_chat")
                        .setOnPress(
                            () -> {
                                Screens.CHAT_SCREEN.init(KillBillGame.get().getUser(), them);
                                KillBillGame.get().changeScreen(Screens.CHAT_SCREEN);
                            }
                        )
                        .build());
                    xOffset += 0.0725f * 0.75f + 0.02f;
                }

                // Accept button?
                if (friend.state().equals(FriendState.INVITED) && !iInitiated) {
                    // Yep.
                    friendElements.add(
                        Button.newBuilder()
                        .setLocation(new ScaledLocation(xOffset, yOffset))
                        .setSize(new XScaledSize(0.0725f * 0.75f, 1f))
                        .setTexture("ui_mini_mini_button_confirm")
                        .setOnPress(
                            () -> {
                                FriendRequests.acceptFriend(
                                    new FriendRequests.CreateFriendRequestBody(them.id()),
                                    response -> {
                                        refreshFriends();
                                    },
                                    error1 -> {
                                        Gdx.app.error(FriendScreen.class.getSimpleName(), "API error", error1);
                                        errorLabel.setText("error: failed to accept friend");
                                    }
                                );
                            }
                        )
                        .build());
                    xOffset += 0.0725f * 0.75f + 0.02f;
                }

                // Remove button?
                // Yep. Always.
                friendElements.add(
                    Button.newBuilder()
                    .setLocation(new ScaledLocation(xOffset, yOffset))
                    .setSize(new XScaledSize(0.0725f * 0.75f, 1f))
                    .setTexture("ui_mini_mini_button_remove")
                    .setOnPress(
                        () -> {
                            FriendRequests.deleteFriend(
                                new FriendRequests.DeleteFriendRequestBody(them.id()),
                                response -> {
                                    refreshFriends();
                                },
                                error1 -> {
                                    Gdx.app.error(FriendScreen.class.getSimpleName(), "API error", error1);
                                    errorLabel.setText("error: failed to remove friend");
                                }
                            );
                        }
                    )
                    .build());
                xOffset += 0.0725f * 0.75f + 0.02f;

                // Now the label
                friendElements.add(
                    Label.newBuilder()
                        .setLocation(new ScaledLocation(xOffset + 0.02f, yOffset + 0.055f))
                        .setCentered(false)
                        .setText(them.name())
                        .setFontBuilder(
                            KillBillGame.get().getFontLoader().newBuilder("main")
                                .setColor(GlobalGameConfig.TERTIARY_COLOR.cpy())
                                .setScaledSize(36)
                        )
                        .build());

                
                yOffset -= 0.15f;
                if (yOffset <= 0.05f) break;
            }

            for (final UiElement element : friendElements) {
                uiRenderer.add(element);
            }
            needsReload = false;
        }
    }

    @Override
    public void onSwitch() {
        super.onSwitch();
        search.setText("");
        refreshFriends();
    }

    public void refreshFriends() {
        user.setText(KillBillGame.get().getUser().name());
        FriendRequests.listFriends(
            new ListFriendsRequestBody(KillBillGame.get().getUser().id()),
            resp -> {
                users.clear();
                friends = resp.friends();
                if (friends.size() == 0) needsReload = true;

                for (Friend friend : friends) {
                    // get the other user
                    UUID otherUserId;
                    if (friend.fromId().equals(KillBillGame.get().getUser().id())) {
                        otherUserId = friend.toId();
                    } else {
                        otherUserId = friend.fromId();
                    }
                    
                    UserRequests.getUser(
                        new UserRequests.GetUserRequestBody(otherUserId, null),
                        userResp -> {
                            users.put(userResp.user().id(), userResp.user());
                            if (friends.size() == users.size()) needsReload = true;
                        }, 
                        e -> {
                            Gdx.app.error(FriendScreen.class.getSimpleName(), "API error", e);
                            errorLabel.setText("error: unexpected server error");
                        }
                    );
                }
            }, 
            e -> {
                Gdx.app.error(FriendScreen.class.getSimpleName(), "API error", e);
                errorLabel.setText("error: unexpected server error");
            }
        );

    }
}
