package today.tecktip.killbill.frontend.game.objects.entities;

import today.tecktip.killbill.frontend.game.objects.DummyEntity;
import today.tecktip.killbill.frontend.game.objects.renderers.MovementBasedRenderer;
import today.tecktip.killbill.frontend.gameserver.game.basic.BasicLocalEntityState;

public class Employee extends DummyEntity {

    public Employee(BasicLocalEntityState entityState) {
        super(
            1, 
            1, 
            null, 
            entityState
        );
        setRenderer(new MovementBasedRenderer(this, entityState.getTexturePrefix()));
    }

    @Override
    protected void onStateChange(int newState) {
    }
    
}
