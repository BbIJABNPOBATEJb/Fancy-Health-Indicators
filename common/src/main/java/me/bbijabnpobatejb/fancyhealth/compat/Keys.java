package me.bbijabnpobatejb.fancyhealth.compat;

import lombok.experimental.UtilityClass;
//? if >=1.17 {
import com.mojang.blaze3d.platform.InputConstants;
//?}

@UtilityClass
public class Keys {

    //? if >=1.17 {
    public final int ESCAPE = InputConstants.KEY_ESCAPE;
    public final int BACKSPACE = InputConstants.KEY_BACKSPACE;
    public final int RETURN = InputConstants.KEY_RETURN;
    public final int NUMPAD_ENTER = InputConstants.KEY_NUMPADENTER;
    public final int TAB = InputConstants.KEY_TAB;
    public final int LSHIFT = InputConstants.KEY_LSHIFT;
    public final int RSHIFT = InputConstants.KEY_RSHIFT;
    public final int F = InputConstants.KEY_F;
    public final int H = InputConstants.KEY_H;
    public final int V = InputConstants.KEY_V;
    public final int A = InputConstants.KEY_A;
    public final int C = InputConstants.KEY_C;
    public final int X = InputConstants.KEY_X;
    public final int Y = InputConstants.KEY_Y;
    public final int Z = InputConstants.KEY_Z;
    public final int LEFT = InputConstants.KEY_LEFT;
    public final int RIGHT = InputConstants.KEY_RIGHT;
    public final int HOME = InputConstants.KEY_HOME;
    public final int END = InputConstants.KEY_END;
    public final int DELETE = InputConstants.KEY_DELETE;
    //?} else {
    /*public final int ESCAPE = 256;
    public final int BACKSPACE = 259;
    public final int RETURN = 257;
    public final int NUMPAD_ENTER = 335;
    public final int TAB = 258;
    public final int LSHIFT = 340;
    public final int RSHIFT = 344;
    public final int F = 70;
    public final int H = 72;
    public final int V = 86;
    public final int A = 65;
    public final int C = 67;
    public final int X = 88;
    public final int Y = 89;
    public final int Z = 90;
    public final int LEFT = 263;
    public final int RIGHT = 262;
    public final int HOME = 268;
    public final int END = 269;
    public final int DELETE = 261;
    *///?}
}
