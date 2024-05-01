package today.tecktip.killbill.frontend.game.objects.entities;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.game.objects.DummyEntity;
import today.tecktip.killbill.frontend.game.objects.renderers.StaticSpriteObjectRenderer;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalEntityState;

public class ClaymoreRoomba extends DummyEntity {

    public ClaymoreRoomba(BasicLocalEntityState entityState) {
        super(
            1, 
            1, 
            new StaticSpriteObjectRenderer(KillBillGame.get().getTextureLoader().get("enemies_claymore_roomba")), 
            entityState
        );
    }

    @Override
    protected void onStateChange(int newState) {
    }
    
}
