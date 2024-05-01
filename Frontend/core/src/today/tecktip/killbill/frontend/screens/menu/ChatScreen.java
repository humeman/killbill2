package today.tecktip.killbill.frontend.screens.menu;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.http.requests.DmRequests;
import today.tecktip.killbill.frontend.http.requests.DmRequests.SendDmRequestBody;
import today.tecktip.killbill.frontend.http.requests.data.Dm;
import today.tecktip.killbill.frontend.http.requests.data.User;
import today.tecktip.killbill.frontend.screens.MenuScreen;
import today.tecktip.killbill.frontend.screens.Screens;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Location.ScaledLocation;
import today.tecktip.killbill.frontend.ui.Size.ScaledSize;
import today.tecktip.killbill.frontend.ui.Size.YScaledSize;
import today.tecktip.killbill.frontend.ui.elements.Button;
import today.tecktip.killbill.frontend.ui.elements.Image;
import today.tecktip.killbill.frontend.ui.elements.Label;
import today.tecktip.killbill.frontend.ui.elements.TextInput;

/**
 * mini demo 3 chat screen
 * @author cz
 */
public class ChatScreen extends MenuScreen {

    /**
     * Status label
     */
    private Label label;

    /**
     * Message to be sent
     */
    private TextInput input;

    /**
     * button to send the Chat
     */
    private Button submitButton;

    /**
     * currently logged in user
     */
    private User meUser;

    /**
     * user to communicate with
     */
    private User themUser;

    /**
     * box for last message sent by other user to this user
     */
    private Image theirChatSquare;

    /**
     * box for last message this user sent to other user
     */
    private Image myChatSquare;

    /**
     * last message sent by other user to this user
     */
    private Label theirChat;

    /**
     * last message this user sent to other user
     */
    private Label myChat;

    private List<Dm> dms;

    private boolean needsReload;

    private Label errorLabel;

    /**
     * Constructs the test screen.
     */
    public ChatScreen() {
        super(new Color(0, 0, 0, 1));
        needsReload = false;
        dms = new ArrayList<>();
    }

    public void init(final User meUser, final User themUser){
        this.meUser = meUser;
        this.themUser = themUser;
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

        label = Label.newBuilder()
            .setLocation(new ScaledLocation(0.5f, 0.9f))
            .setCentered(true)
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(new Color(1, 1, 1, 1))
                    .setBorder(new Color(0, 0, 0, 1), 2)
                    .setScaledSize(48)
            )
            .setText("")
            .build();

        uiRenderer.add(label);

        theirChatSquare = Image.newBuilder()
            .setTexture("ui_square")
            .setLocation(new ScaledLocation(0f, .6f))
            .setSize(new ScaledSize(1f, .45f))
            .build();

        uiRenderer.add(theirChatSquare);

        myChatSquare = Image.newBuilder()
            .setTexture("ui_square")
            .setLocation(new ScaledLocation(0f, .1f))
            .setSize(new ScaledSize(1f, .45f))
            .build();

        uiRenderer.add(myChatSquare);

        theirChat = Label.newBuilder()
                .setLocation(new ScaledLocation(0.05f, .95f))
                .setCentered(false)
                .setText("")
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                        .setColor(new Color(1, 1, 1, 1))
                        .setBorder(new Color(0, 0, 0, 1), 2)
                        .setScaledSize(36)
                )
                .build();
            
        uiRenderer.add(theirChat);

        myChat = Label.newBuilder()
                .setLocation(new ScaledLocation(0.05f, .5f))
                .setText("")
                .setCentered(false)
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                        .setColor(new Color(1, 1, 1, 1))
                        .setBorder(new Color(0, 0, 0, 1), 2)
                        .setScaledSize(36)
                )
                .build();
            
        uiRenderer.add(myChat);

        input = TextInput.newBuilder()
            .setLocation(new ScaledLocation(.08f, 0f))
            .setSize(new ScaledSize(1f - 0.08f - 0.15f, 0.08f))
            .setAllowLineBreaks(false)
            .setPlaceholder("your message")
            .setTexture("ui_square")
            .setFontBuilder(
                KillBillGame.get().getFontLoader().newBuilder("main")
                    .setColor(new Color(1, 1, 1, 1))
                    .setBorder(new Color(0, 0, 0, 1), 2)
                    .setScaledSize(48)
            )
            .build();

        uiRenderer.add(input);

        submitButton = Button.newBuilder()
                .setLocation(new ScaledLocation(0.85f, 0f))
                .setSize(new ScaledSize(.15f, 0.08f))
                .setTexture("ui_mini_button")
                .setText("send")
                .setOnPress(() -> {
                    DmRequests.sendDm(
                        new SendDmRequestBody(themUser.id(), input.getText()), 
                        response -> {
                            getDms();
                        }, 
                        e -> {
                            Gdx.app.error(ChatScreen.class.getSimpleName(), "Failed to send chat message", e);
                            errorLabel.setText("error: failed to send message");
                        });
                })
                .setFontBuilder(
                    KillBillGame.get().getFontLoader().newBuilder("main")
                        .setColor(new Color(1, 1, 1, 1))
                        .setBorder(new Color(0, 0, 0, 1), 2)
                        .setScaledSize(48)
                )
                .build();
                
        uiRenderer.add(submitButton);

        input.setSubmit(submitButton);

        uiRenderer.add(
            Button.newBuilder()
                .setLocation(new ScaledLocation(0f, 0f))
                .setSize(new YScaledSize(1f, 0.08f))
                .setTexture("ui_mini_button_back")
                .setOnPress(() -> {
                    KillBillGame.get().changeScreen(Screens.FRIEND_SCREEN);
                })
                .build()
        );

        errorLabel = Label.newBuilder()
            .setLocation(new ScaledLocation(0.5f, 0.9f))
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
    public void onSwitch() {
        super.onSwitch();
        errorLabel.setText("");
        input.setText("");
        getDms();
    }

    @Override
    public void drawFirst(final float delta) {
        super.drawFirst(delta);
        if (needsReload) {
            //number of found dms from this user to output
            int myFoundNumber = 0;

            //number of found dms from other user to output
            int themFoundNumber = 0;

            //this users chats to show
            String sendMyChats = "";

            //other users chats to show
            String sendThemChats = "";

            quickSort(dms, 0, dms.size() - 1);

            //set the text boxes with the dms

            for (int i = 0; i < dms.size(); i++) {
                if (dms.get(i).fromId().equals(meUser.id()) && myFoundNumber < 5) {
                    sendMyChats += dms.get(i).message() + "\n";
                    myFoundNumber++;
                }
                if (dms.get(i).fromId().equals(themUser.id()) && themFoundNumber < 5) {
                    sendThemChats += dms.get(i).message() + "\n";
                    themFoundNumber++;
                }
            }
            
            myChat.setText(sendMyChats);
            theirChat.setText(sendThemChats);

            needsReload = false;
        }
    }

    public void getDms() {
        DmRequests.listDms(
            new DmRequests.ListDmsByChannelRequestBody(meUser.id(), themUser.id()),
            response -> {
                dms.clear();
                for (final Dm dm : response.dms())
                    dms.add(dm);
                needsReload = true;
            },
            e -> {
                Gdx.app.error("Chat Screen", e.toString(), e);
            });
    }

    public void setMessage(String msg) {
        label.setText(msg);
    }

    public void quickSort(List<Dm> dms, int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(dms, begin, end);
    
            quickSort(dms, begin, partitionIndex-1);
            quickSort(dms, partitionIndex+1, end);
        }
    }

    private int partition(List<Dm> arr, int begin, int end) {
        Dm pivot = arr.get(end);
        int i = (begin-1);
    
        for (int j = begin; j < end; j++) {
            if (arr.get(j).created().compareTo(pivot.created()) == -1 || arr.get(j).created().compareTo(pivot.created()) == 0) {
                i++;
    
                Dm swapTemp = arr.get(i);
                arr.set(i, arr.get(j));
                arr.set(j, swapTemp);
            }
        }
    
        Dm swapTemp = arr.get(i+1);
        arr.set(i + 1, arr.get(end));
        arr.set(end, swapTemp);
    
        return i+1;
    }
}
