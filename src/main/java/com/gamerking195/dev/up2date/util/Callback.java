package com.gamerking195.dev.up2date.util;

import com.gamerking195.dev.up2date.update.PluginInfo;

import java.util.ArrayList;

/**
 * @author Caden Kriese (flogic)
 *
 * Created on 11/3/18
 */
public interface Callback {
    void call(ArrayList<PluginInfo> result);
}

